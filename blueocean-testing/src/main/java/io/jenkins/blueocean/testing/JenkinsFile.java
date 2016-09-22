package io.jenkins.blueocean.testing;

public class JenkinsFile {
    private String file = "";
    public static JenkinsFile createFile() {
        return new JenkinsFile();
    }

    public JenkinsFile node() {
        file += "node {\n";
        return this;
    }

    public JenkinsFile endNode() {
        file += "}\n";
        return this;
    }

    public JenkinsFile stage(String name) {
        file += "stage '"+name+"';\n";
        return this;
    }
    public JenkinsFile sleep(int seconds) {
        file += "sleep " + seconds + ";\n";
        return this;
    }

    public JenkinsFile echo(String msg) {
        file += "echo '"+msg+"'";
        return this;
    }

    public String getFileContents() {
        return file;
    }
}
