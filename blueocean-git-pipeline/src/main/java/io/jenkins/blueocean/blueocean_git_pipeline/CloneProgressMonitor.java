/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.jenkins.blueocean.blueocean_git_pipeline;

import org.eclipse.jgit.lib.ProgressMonitor;

/**
 *
 * @author kzantow
 */
public class CloneProgressMonitor implements ProgressMonitor {
    @Override
    public void beginTask(String string, int i) {
        System.out.println("beginTask" + string + " " + i);
    }

    @Override
    public void start(int i) {
        System.out.println("start " + i);
    }

    @Override
    public void update(int i) {
        System.out.println("update " + i);
    }

    @Override
    public void endTask() {
        System.out.println("endTask ");
    }

    @Override
    public boolean isCancelled() {
        System.out.println("isCancelled ");
        return false;
    }
}