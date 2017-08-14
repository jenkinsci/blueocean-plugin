package io.jenkins.blueocean.blueocean_git_pipeline;

import org.apache.sshd.common.Factory;
import org.apache.sshd.common.util.GenericUtils;
import org.apache.sshd.common.util.OsUtils;
import org.apache.sshd.common.util.ValidateUtils;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.shell.InvertedShell;
import org.apache.sshd.server.shell.InvertedShellWrapper;

import java.io.File;
import java.util.Arrays;
import java.util.List;

class SSHProcessFactory implements Factory<Command> {
    private final List<String> command;
    private final File cwd;
    private final boolean interactive;

    public SSHProcessFactory(File cwd, List<String> command) {
        this.command = ValidateUtils.checkNotNullAndNotEmpty(command, "No command");
        this.cwd = cwd;
        interactive = false;
    }

    public SSHProcessFactory(File cwd) {
        this.command = OsUtils.resolveDefaultInteractiveCommand();
        this.cwd = cwd;
        interactive = true;
    }

    private List<String> getCommand() {
        return this.command;
    }

    public Command create() {
        return new InvertedShellWrapper(this.createInvertedShell());
    }

    private InvertedShell createInvertedShell() {
        return new SSHShell(cwd, interactive, this.resolveEffectiveCommand(this.getCommand()));
    }

    private List<String> resolveEffectiveCommand(List<String> original) {
        if (!OsUtils.isWin32()) {
            return original;
        } else if (GenericUtils.size(original) <= 1) {
            return original;
        } else {
            String cmdName = original.get(0);
            return "cmd.exe".equalsIgnoreCase(cmdName) ? original : Arrays.asList("cmd.exe", "/C", GenericUtils.join(original, ' '));
        }
    }
}
