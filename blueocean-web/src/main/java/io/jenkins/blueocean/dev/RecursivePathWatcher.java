package io.jenkins.blueocean.dev;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * Recursively watches directories for file changes only notifies
 * when files are added, modified, or deleted but tracks all
 * added/removed directories.
 *
 * @author Keith Zantow &lt;kzantow@gmail.com&gt;
 */
public class RecursivePathWatcher {
    /**
     * Events to listen to from {@link #start(PathEventHandler)}
     */
    public enum Event {
        CREATE,
        DELETE,
        MODIFY,
    };

    /**
     * Used to receive events from {@link #start(PathEventHandler)}
     */
    public interface PathEventHandler {
        void accept(Event event, Path t);
    }

    /**
     * Allows filtering of specific subtrees
     */
    public interface PathFilter {
        /**
         * Determins if the path is allowed
         * @param path relative path from the root directory
         * @return false to exclude this path and all subtrees
         */
        boolean allows(Path path);
    }

    /**
     * Basic path filter that is used for entire subtree watching
     */
    private static final PathFilter ALLOW_ALL_PATHS = new PathFilter() {
        @Override
        public boolean allows(Path path) {
            return true;
        }
    };

    // To try to improve performance, at least on OSX
    // See: https://github.com/HotswapProjects/HotswapAgent/issues/41
    // ... before resorting to: http://hg.netbeans.org/main/file/7a6bb9c59185/masterfs.macosx/src/org/netbeans/modules/masterfs/watcher/macosx/OSXNotifier.java
    private static WatchEvent.Modifier get_com_sun_nio_file_SensitivityWatchEventModifier_HIGH() {
        try {
            Class<?> c = Class.forName("com.sun.nio.file.SensitivityWatchEventModifier");
            Field f = c.getField("HIGH");
            return (WatchEvent.Modifier) f.get(c);
        } catch (Exception e) {
            return null;
        }
    }

    private static WatchEvent.Modifier HIGH_SENSITIVITY = get_com_sun_nio_file_SensitivityWatchEventModifier_HIGH();
    private static WatchEvent.Kind[] EVENT_KINDS = { ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY };
    private static Logger log = Logger.getLogger(RecursivePathWatcher.class.getName());

    private final Path root;
    private final PathFilter pathFilter;
    private volatile WatchService watchService;
    private boolean running;
    private final Map<WatchKey, Path> keys = new ConcurrentHashMap<>();
    private final Map<Path, WatchKey> dirs = new ConcurrentHashMap<>();

    public RecursivePathWatcher(Path root) {
        this(root, ALLOW_ALL_PATHS);
    }

    public RecursivePathWatcher(Path root, PathFilter pathFilter) {
        this.root = root;
        this.pathFilter = pathFilter;
        initWatchService();
    }

    private void initWatchService() {
        try {
            keys.clear();
            watchService = root.getFileSystem().newWatchService();
            register(root);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void register(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) throws IOException {
                // always include root, otherwise give a relative dir to check
                if (root.equals(dir) || pathFilter.allows(root.relativize(dir))) {
                    WatchKey key = dir.register(watchService, EVENT_KINDS, HIGH_SENSITIVITY);
                    keys.put(key, dir);
                    dirs.put(dir, key);
                    return FileVisitResult.CONTINUE;
                }
                return FileVisitResult.SKIP_SUBTREE;
            }
        });
    }

    public void start(final PathEventHandler eventHandler) {
        running = true;
        while (running) {
            try {
                if (log.isLoggable(Level.FINE)) log.fine("Ready... watching: " + dirs.keySet());
                WatchKey watchKey = watchService.take();
                Path dir = keys.get(watchKey);
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    Object entry = event.context();
                    if (log.isLoggable(Level.FINE)) log.fine(event.kind() + ": " + entry);
                    if (entry instanceof Path) {
                        Path name = (Path)entry;
                        Path child = dir.resolve(name);
                        if (dirs.containsKey(child)) {
                            if (ENTRY_DELETE.equals(event.kind())) {
                                // a watched directory was deleted, we may not get notifications
                                // for deleted subdirectories, clean them all up here
                                for (Iterator<Map.Entry<Path, WatchKey>> i = dirs.entrySet().iterator(); i.hasNext(); ) {
                                    Map.Entry<Path, WatchKey> d = i.next();
                                    if (d.getKey().startsWith(child)) {
                                        i.remove();
                                        d.getValue().cancel();
                                        keys.remove(d.getValue());
                                    }
                                }
                            }
                        } else {
                            if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                                if (ENTRY_CREATE.equals(event.kind())) {
                                    register(child);
                                }
                            } else {
                                if (ENTRY_CREATE.equals(event.kind())) {
                                    eventHandler.accept(Event.CREATE, child);
                                } else if (ENTRY_DELETE.equals(event.kind())) {
                                    eventHandler.accept(Event.DELETE, child);
                                } else if (ENTRY_MODIFY.equals(event.kind())) {
                                    eventHandler.accept(Event.MODIFY, child);
                                }
                            }
                        }
                    }
                }

                // Check for this watchKey being invalid
                if(!watchKey.reset()) {
                    watchKey.cancel();
                    if (root.equals(dir)) {
                        watchService.close();
                        initWatchService();
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void terminate() {
        running = false;
        try {
            watchService.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
