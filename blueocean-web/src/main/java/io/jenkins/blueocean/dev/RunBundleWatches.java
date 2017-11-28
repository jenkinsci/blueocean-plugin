package io.jenkins.blueocean.dev;

import hudson.PluginWrapper;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import io.jenkins.blueocean.BlueOceanUI;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

@SuppressWarnings("all")
public class RunBundleWatches {
    public final static boolean isEnabled = (new BlueOceanUI().isDevelopmentMode());
    final static List<BundleBuild> builds = new CopyOnWriteArrayList<>();
    final static long DEFAULT_BACK_OFF = 10000;
    final static Pattern EXTENSIONS_TO_CAUSE_REBUILD = Pattern.compile(".*[.](js|jsx|less|css|json|yaml)");

    static class BundleBuild {
        final List<LogLine> logLines = new CopyOnWriteArrayList<>();
        String name;
        Thread thread;
        final AtomicInteger buildCount = new AtomicInteger(0);
        volatile Destructor destructor;
        volatile long lastBuildTimeMillis;
        boolean hasError = false;

        boolean isBuilding() {
            return buildCount.get() > 0;
        }
    }

    static class LogLine {
        final long time;
        final String text;

        LogLine(String text) {
            this(System.currentTimeMillis(), text);
        }

        LogLine(long time, String text) {
            this.time = time;
            this.text = text;
        }
    }

    private static interface Destructor {
        void destroy();
    }

    static final RecursivePathWatcher.PathFilter PROJECT_PATH_FILTER = new RecursivePathWatcher.PathFilter() {
        private final Pattern allowedPaths = Pattern.compile("(src|less)/?.*");
        private final Pattern disallowedPaths = Pattern.compile("(src/test).*");
        @Override
        public boolean allows(Path path) {
            String unixPath = path.toString().replaceAll("\\+", "/");
            if (disallowedPaths.matcher(unixPath).matches()) {
                return false;
            }
            if (!allowedPaths.matcher(unixPath).matches()) {
                return false;
            }
            return true;
        }
    };

    @Initializer(after = InitMilestone.JOB_LOADED)
    public static void startBundleWatches() {
        if (!isEnabled || Boolean.getBoolean("blueocean.features.BUNDLE_WATCH_SKIP")) {
            return;
        }

        System.out.println("Running in development mode, watching bundles...");

        int buildNumber = 0;
        List<PluginWrapper> plugins = Jenkins.getInstance().pluginManager.getPlugins();;
        for (final PluginWrapper p : plugins) {
            try {
                final File projectDir = findPluginWorkDir(new File(p.baseResourceURL.getPath()));
                if (projectDir != null) {
                    final String path = projectDir.getCanonicalPath();
                    final File packageFile = new File(projectDir, "package.json");

                    final BundleBuild build = new BundleBuild();
                    build.name = p.getShortName();

                    System.out.println("---- Watching " + build.name + " in: " + path);

                    JSONObject packageJson = JSONObject.fromObject(FileUtils.readFileToString(packageFile));
                    final String[] npmCommand = { "mvnbuild", null };

                    if (packageJson.has("scripts")) {
                        JSONObject scripts = packageJson.getJSONObject("scripts");
                        if(scripts.has("mvnbuild:fast")) {
                            npmCommand[0] = "mvnbuild:fast";
                        }
                        if(scripts.has("watch")) {
                            npmCommand[1] = "watch";
                        }
                        if(scripts.has("bundle:watch")) {
                            npmCommand[1] = "bundle:watch";
                        }
                    }

                    // Leaving this code here because we may want to enable "watch" behavior
                    if ("watch".equals(npmCommand[1]) && Boolean.getBoolean("blueocean.features.NATIVE_NPM_WATCH")) {
                        build.thread = new Thread() {
                            public void run() {
                                long backOff = DEFAULT_BACK_OFF;
                                while (true) {
                                    try {
                                        Map<String, String> env = new HashMap<>(System.getenv());
                                        String[] command;
                                        if (SystemUtils.IS_OS_WINDOWS) {
                                            command = new String[]{"cmd", "/C", "npm", "run", npmCommand[1]};
                                        } else {
                                            command = new String[]{"bash", "-c", "${0} ${1+\"$@\"}", "npm", "run", npmCommand[1]};
                                        }

                                        ProcessBuilder pb = new ProcessBuilder(Arrays.asList(command))
                                            .redirectErrorStream(true)
                                            .directory(projectDir);

                                        if (SystemUtils.IS_OS_WINDOWS) {
                                            pb.environment().put("Path",
                                                new File(projectDir, "node").getCanonicalPath() + ";" + env.get("Path"));
                                        } else {
                                            pb.environment().put("PATH",
                                                new File(projectDir, "node").getCanonicalPath() + ":" + env.get("PATH"));
                                        }

                                        final Process process = pb.start();

                                        build.destructor = new Destructor() {
                                            @Override
                                            public void destroy() {
                                                if (process.isAlive()) {
                                                    try {
                                                        process.destroy();
                                                        process.destroyForcibly();
                                                    } catch (Exception e) {
                                                        // ignore
                                                    }
                                                }
                                                build.destructor = null;
                                            }
                                        };

                                        InputStream in = process.getInputStream();
                                        try (BufferedReader rdr = new BufferedReader(new InputStreamReader(in, "utf-8"))) {
                                            String line;
                                            long startMillis = 0;
                                            while ((line = rdr.readLine()) != null) {
                                                if (line.contains("Starting 'log-env'")) {
                                                    startMillis = System.currentTimeMillis();
                                                    build.buildCount.incrementAndGet();
                                                    build.logLines.clear();
                                                    System.out.println("---- Rebuilding: " + path);
                                                }

                                                // add here because we clear when starting a new build
                                                build.logLines.add(new LogLine(System.currentTimeMillis(), line));

                                                if (line.contains("missing script: bundle:watch")) {
                                                    System.out.println("---- Unable to find script 'bundle:watch' in: " + packageFile.getCanonicalPath());
                                                    // don't retry this case
                                                    build.thread = null;
                                                    return;
                                                }
                                                //if (line.contains("Finished 'bundle:watch'") || line.contains("Finished 'bundle'")) {
                                                if (line.contains("Finished 'bundle'")) {
                                                    if (build.hasError) {
                                                        for (LogLine l : build.logLines) {
                                                            System.out.println(l.text);
                                                        }
                                                        build.hasError = false;
                                                    }
                                                    long time = System.currentTimeMillis() - startMillis;
                                                    build.lastBuildTimeMillis = time;
                                                    build.buildCount.decrementAndGet();
                                                    System.out.println("---- Rebuilt " + build.name + " in: " + time + "ms");
                                                }
                                                if (line.contains("Failed at the")) {
                                                    System.out.println("---- Failed to build: " + build.name);

                                                    build.buildCount.decrementAndGet();
                                                    for (LogLine l : build.logLines) {
                                                        System.out.println(l.text);
                                                    }
                                                }
                                                if (line.contains("error") || line.contains("Error") || line.contains("ERROR")) {
                                                    build.hasError = true;
                                                }
                                            }
                                        }
                                    } catch (RuntimeException e) { // Thanks findbugs
                                        e.printStackTrace();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    build.destructor = null;

                                    try {
                                        Thread.sleep(backOff);
                                        backOff = (long) Math.floor(backOff * 2);
                                    } catch (InterruptedException e) {
                                        // nothing to see here
                                        backOff = DEFAULT_BACK_OFF;
                                    }
                                }
                            }
                        };

                        build.thread.setDaemon(true);
                        build.thread.setName("Watching " + build.name + " for changes in: " + path);
                        build.thread.start();
                    } else {
                        // Might be better to be "/src/main"
                        // but JDL and core js currently have a different structure
                        // limiting the scope is handled with the PROJECT_PATH_FILTER and EXTENSIONS_TO_CAUSE_REBUILD
                        final File watchDir = projectDir;
                        final Path watchPath = watchDir.toPath();

                        // java nio watch, run `mvnbuild` instead
                        Thread fileWatchThread = new Thread() {
                            volatile Process buildProcess;

                            @Override
                            public void run() {
                                build.destructor = new Destructor() {
                                    @Override
                                    public void destroy() {
                                        if (buildProcess != null && buildProcess.isAlive()) {
                                            try {
                                                buildProcess.destroy();
                                                if (buildProcess != null && buildProcess.isAlive()) {
                                                    buildProcess.destroyForcibly();
                                                }
                                            } catch (Exception e) {
                                                // ignore
                                                e.printStackTrace();
                                            }
                                        }
                                        buildProcess = null;
                                        build.logLines.add(new LogLine("Process restarted."));
                                    }
                                };

                                RecursivePathWatcher watcher = new RecursivePathWatcher(watchPath, PROJECT_PATH_FILTER);
                                watcher.start(new RecursivePathWatcher.PathEventHandler() {
                                    @Override
                                    public void accept(final RecursivePathWatcher.Event event, final Path modified) {
                                        // TODO we mainly only want to rebuild if there are changes in 'src/main/js'
                                        // we might want to re-run npm install if there's a change to package.json

                                        // only rebuild for certain types of files
                                        if (!EXTENSIONS_TO_CAUSE_REBUILD.matcher(modified.toString()).matches()) {
                                            return;
                                        }

                                        // kill any currently running builds
                                        build.destructor.destroy();

                                        // run in a separate thread so we can pick up changes and kill any existing
                                        // running processes
                                        build.thread = new Thread() {
                                            @Override
                                            public void run() {
                                                build.buildCount.incrementAndGet();
                                                long startMillis = System.currentTimeMillis();

                                                try {
                                                    Map<String, String> env = new HashMap<>(System.getenv());
                                                    String[] command;
                                                    if (SystemUtils.IS_OS_WINDOWS) {
                                                        command = new String[]{"cmd", "/C", "npm", "run", npmCommand[0]};
                                                    } else {
                                                        command = new String[]{"bash", "-c", "${0} ${1+\"$@\"}", "npm", "run", npmCommand[0]};
                                                    }

                                                    System.out.println("---- Rebuilding: " + build.name + " due to change in: " + watchPath.relativize(modified));
                                                    ProcessBuilder pb = new ProcessBuilder(Arrays.asList(command))
                                                        .redirectErrorStream(true)
                                                        .directory(projectDir);

                                                    if (SystemUtils.IS_OS_WINDOWS) {
                                                        pb.environment().put("Path",
                                                            new File(projectDir, "node").getCanonicalPath() + ";" + env.get("Path"));
                                                    } else {
                                                        pb.environment().put("PATH",
                                                            new File(projectDir, "node").getCanonicalPath() + ":" + env.get("PATH"));
                                                    }

                                                    buildProcess = pb.start();
                                                    InputStream in = buildProcess.getInputStream();
                                                    try (BufferedReader rdr = new BufferedReader(new InputStreamReader(in, "utf-8"))) {
                                                        String line;
                                                        while ((line = rdr.readLine()) != null) {
                                                            build.logLines.add(new LogLine(System.currentTimeMillis(), line));
                                                            if (line.contains("missing script: mvnbuild")) {
                                                                build.hasError = true;
                                                            }
                                                            if (line.contains("Failed at the")) {
                                                                build.hasError = true;
                                                            }
                                                            if (line.contains("error") || line.contains("Error") || line.contains("ERROR")) {
                                                                build.hasError = true;
                                                            }
                                                        }
                                                    }

                                                    // If process wasn't killed or error, output the rebuild info
                                                    if (buildProcess != null && buildProcess.waitFor() == 0) {
                                                        build.lastBuildTimeMillis = System.currentTimeMillis() - startMillis;
                                                        System.out.println("---- Rebuilt " + build.name + " in: " + build.lastBuildTimeMillis + "ms");
                                                    }
                                                } catch (RuntimeException e) { // Thanks findbugs
                                                    e.printStackTrace();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                } finally {
                                                    buildProcess = null;
                                                    build.buildCount.decrementAndGet();

                                                    if (build.hasError) {
                                                        for (LogLine l : build.logLines) {
                                                            System.out.println(l.text);
                                                        }
                                                        build.hasError = false;
                                                    }

                                                    // Don't use excessive memory:
                                                    build.logLines.clear();
                                                }
                                            }
                                        };

                                        build.thread.setDaemon(true);
                                        build.thread.setName("Building: " + path);
                                        build.thread.start();
                                    }
                                });
                            }
                        };

                        fileWatchThread.setDaemon(true);
                        fileWatchThread.setName("Watching " + build.name + " for changes in: " + path);
                        fileWatchThread.start();
                    }

                    builds.add(build);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Try to find the plugin's working directory
     * @param dir to search
     * @return plugin's dir
     */
    private static File findPluginWorkDir(File dir) {
        if (dir == null) {
            return null;
        }
        if (new File(dir, "pom.xml").exists() && new File(dir, "package.json").exists()) {
            return dir;
        }
        return findPluginWorkDir(dir.getParentFile());
    }

    public static void waitForScriptBuilds() {
        if (!isEnabled) return;

        for (BundleBuild build : builds) {
            while (build.isBuilding()) {
                try {
                    // Poll since the npm bundle:watch won't complete the thread
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
