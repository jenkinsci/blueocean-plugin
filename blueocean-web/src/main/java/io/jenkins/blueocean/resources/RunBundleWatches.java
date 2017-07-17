package io.jenkins.blueocean.resources;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.SystemUtils;

import hudson.PluginWrapper;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import io.jenkins.blueocean.BlueOceanUI;
import jenkins.model.Jenkins;

public class RunBundleWatches {

    @Initializer(after = InitMilestone.EXTENSIONS_AUGMENTED)
    public static void startBundleWatches() {
        if (true) return;
        if (!(new BlueOceanUI().isDevelopmentMode())) {
            return;
        }

        System.out.println("Running in development mode, watching bundles...");

        HashSet<Object> loadedExtensions = new HashSet<>();
        List<PluginWrapper> plugins = Jenkins.getInstance().pluginManager.getPlugins();;
        for (PluginWrapper p : plugins) {
            try {
                Enumeration<URL> urls = p.classLoader.getResources("/package.json");
                while (urls.hasMoreElements()) {
                    URL u = urls.nextElement();
                    if (u != null && "file".equals(u.getProtocol())) {
                        File f = new File(u.toExternalForm().replace("file:", ""));
                        final File projectDir = f.getParentFile();
                        if (!loadedExtensions.contains(projectDir)) {
                            loadedExtensions.add(projectDir);
                            if (new File(projectDir, "pom.xml").exists()) {
                                Thread watcher = new Thread() {
                                    public void run() {
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
                                                        new File(projectDir, "node").getAbsolutePath() + ";" + env.get("Path"));
                                            } else {
                                                pb.environment().put("PATH",
                                                        new File(projectDir, "node").getAbsolutePath() + ":" + env.get("PATH"));
                                            }

                                            Process process = pb.start();
                                            InputStream in = process.getInputStream();
                                            try (BufferedReader rdr = new BufferedReader(new InputStreamReader(in, "utf-8"))) {
                                                String line;
                                                while ((line = rdr.readLine()) != null) {
                                                    System.out.println(line);
                                                    if (line.contains("Starting 'log-env'")) {
                                                        System.out.println("---- Rebuilding: " + projectDir.getAbsolutePath());
                                                    }
                                                    if (line.contains("missing script: bundle:watch")) {
                                                        System.out.println("---- Unable to find script 'bundle:watch' in: " + projectDir.getAbsolutePath());
                                                    }
                                                    if (line.contains("Finished 'bundle:watch'")) {
                                                        System.out.println("---- Rebuilt bundle in: " + projectDir.getAbsolutePath());
                                                    }
                                                }
                                            }
                                        } catch (RuntimeException e) { // Thanks findbugs
                                            e.printStackTrace();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                };
                                watcher.setDaemon(true);
                                watcher.setName("Watching: " + projectDir);
                                watcher.start();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
