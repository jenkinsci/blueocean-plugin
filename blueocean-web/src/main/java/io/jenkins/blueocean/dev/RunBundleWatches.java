package io.jenkins.blueocean.dev;

import hudson.Extension;
import hudson.PluginWrapper;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import io.jenkins.blueocean.BlueOceanUI;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.commons.stapler.TreeResponse;
import io.jenkins.blueocean.rest.ApiRoutable;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.SystemUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.json.JsonBody;
import org.kohsuke.stapler.verb.DELETE;
import org.kohsuke.stapler.verb.GET;
import org.kohsuke.stapler.verb.POST;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("all")
public class RunBundleWatches {
    final static List<BundleBuild> builds = new CopyOnWriteArrayList<>();
    final static long DEFAULT_BACK_OFF = 10000;

    @Extension
    public static class BundleWatchEndpoint implements ApiRoutable {
        @Override
        public String getUrlName() {
            return "bundle:watch";
        }

        @GET
        @WebMethod(name = "")
        @TreeResponse
        public BundleLog[] get(@QueryParameter long since) {
            BundleLog[] logs = new BundleLog[builds.size()];
            int i = 0;
            for (BundleBuild build : builds) {
                long max = since;
                StringBuilder out = new StringBuilder();
                for (LogLine line : build.logLines) {
                    if (line.time > since) {
                        if (line.time > max) {
                            max = line.time;
                        }
                        out.append(line.text).append("\n");
                    }
                }
                logs[i] = new BundleLog(build.name, out.toString(), max, build.isBuilding, build.lastBuildTimeMillis);
                i++;
            }
            return logs;
        }

        @POST
        @WebMethod(name = "")
        public void reRun(@JsonBody JSONObject request) {
            if (!request.has("name")) throw new ServiceException.BadRequestException("Must specify name");
            for (BundleBuild build : builds) {
                if (build.name.equals(request.getString("name"))) {
                    build.isBuilding = true;
                    build.reRun();
                    return;
                }
            }
        }

        @DELETE
        @WebMethod(name = "")
        public void clearLogs(@JsonBody JSONObject request) {
            if (!request.has("name")) throw new ServiceException.BadRequestException("Must specify name");
            for (BundleBuild build : builds) {
                if (build.name.equals(request.getString("name"))) {
                    build.logLines.clear();
                    build.logLines.add(new LogLine("Log cleared...\n"));
                    return;
                }
            }
        }
    }

    @ExportedBean
    public static class BundleLog {
        private final String name;
        private final String lines;
        private final long since;
        private final boolean isBuilding;
        private final long lastBuildTime;

        public BundleLog(String name, String lines, long since, boolean isBuilding, long lastBuildTime) {
            this.name = name;
            this.lines = lines;
            this.since = since;
            this.isBuilding = isBuilding;
            this.lastBuildTime = lastBuildTime;
        }

        @Exported
        public String getName() {
            return name;
        }

        @Exported
        public String getLog() {
            return lines;
        }

        @Exported
        public long getSince() {
            return since;
        }

        @Exported
        public boolean isBuilding() {
            return isBuilding;
        }

        @Exported
        public long getLastBuildTime() {
            return lastBuildTime;
        }
    }

    static class BundleBuild {
        final List<LogLine> logLines = new CopyOnWriteArrayList<>();
        String name;
        Thread thread;
        volatile boolean isBuilding;
        volatile Process process;
        volatile long lastBuildTimeMillis;
        int lastBuildIdx = 0;
        boolean hasError = false;

        void reRun() {
            synchronized(this) {
                logLines.add(new LogLine("\nRestarting bundle process...\n"));
                if (process != null) {
                    process.destroy();
                    try {
                        process.destroyForcibly();
                    } catch(Exception e) {
                        // ignore
                    }
                } else {
                    thread.interrupt();
                }
            }
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

    @Initializer(after = InitMilestone.JOB_LOADED)
    public static void startBundleWatches() {
        if (!(new BlueOceanUI().isDevelopmentMode())) {
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

                    final BundleBuild build = new BundleBuild();
                    build.name = p.getShortName();

                    System.out.println("---- Watching " + build.name + " in: " + path);

                    build.thread = new Thread() {
                        public void run() {
                            long backOff = DEFAULT_BACK_OFF;
                            while (true) {
                                try {
                                    Map<String, String> env = new HashMap<>(System.getenv());
                                    String[] command;
                                    if (SystemUtils.IS_OS_WINDOWS) {
                                        command = new String[]{"cmd", "/C", "npm", "run", "bundle:watch"};
                                    } else {
                                        command = new String[]{"bash", "-c", "${0} ${1+\"$@\"}", "npm", "run", "bundle:watch"};
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

                                    build.process = pb.start();
                                    InputStream in = build.process.getInputStream();
                                    try (BufferedReader rdr = new BufferedReader(new InputStreamReader(in, "utf-8"))) {
                                        String line;
                                        long startMillis = 0;
                                        while ((line = rdr.readLine()) != null) {
                                            build.logLines.add(new LogLine(System.currentTimeMillis(), line));
                                            if (line.contains("Starting 'log-env'")) {
                                                startMillis = System.currentTimeMillis();
                                                build.isBuilding = true;
                                                build.lastBuildIdx = build.logLines.size() - 1;
                                                System.out.println("---- Rebuilding: " + path);
                                            }
                                            if (line.contains("missing script: bundle:watch")) {
                                                System.out.println("---- Unable to find script 'bundle:watch' in: " + new File(projectDir, "package.json").getCanonicalPath());
                                                // don't retry this case
                                                build.thread = null;
                                                return;
                                            }
                                            //if (line.contains("Finished 'bundle:watch'") || line.contains("Finished 'bundle'")) {
                                            if (line.contains("Finished 'bundle'")) {
                                                if (build.hasError) {
                                                    for (LogLine l : build.logLines.subList(build.lastBuildIdx, build.logLines.size()-1)) {
                                                        System.out.println(l.text);
                                                    }
                                                    build.hasError = false;
                                                }
                                                long time = System.currentTimeMillis() - startMillis;
                                                build.lastBuildTimeMillis = time;
                                                build.isBuilding = false;
                                                System.out.println("---- Rebuilt " + build.name + " in: " + time + "ms");
                                            }
                                            if (line.contains("Failed at the")) {
                                                System.out.println("---- Failed to build: " + build.name);

                                                build.isBuilding = false;
                                                for (LogLine l : build.logLines.subList(build.lastBuildIdx, build.logLines.size()-1)) {
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

                                build.process = null;

                                try {
                                    Thread.sleep(backOff);
                                    backOff = (long)Math.floor(backOff * 2);
                                } catch (InterruptedException e) {
                                    // nothing to see here
                                    backOff = DEFAULT_BACK_OFF;
                                }
                            }
                        }
                    };

                    build.thread.setDaemon(true);
                    build.thread.setName("bundle:watch for: " + build.name + " in: " + path);
                    build.thread.start();

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
}
