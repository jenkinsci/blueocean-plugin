/*
 * The MIT License
 *
 * Copyright (c) 2017, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.jenkins.blueocean.blueocean_git_pipeline;

import org.eclipse.jgit.lib.ProgressMonitor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple progress monitor to track progress during any clone operations
 * @author kzantow
 */
class CloneProgressMonitor implements ProgressMonitor {
    private final String repositoryUrl;
    private final AtomicInteger cloneCount = new AtomicInteger(0);
    private final AtomicInteger latestProgress = new AtomicInteger(0);
    private boolean cancel = false;

    public CloneProgressMonitor(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    @Override
    public void beginTask(String task, int i) {
        CloneProgressMonitor existing = currentStatus.get(repositoryUrl);
        if (existing == null) {
            currentStatus.put(repositoryUrl, existing = this);
        }
        existing.cloneCount.incrementAndGet();
        existing.latestProgress.set(i);
    }

    @Override
    public void start(int i) {
        latestProgress.set(i);
    }

    @Override
    public void update(int i) {
        latestProgress.set(i);
    }

    @Override
    public void endTask() {
        latestProgress.set(100);
        if (0 == cloneCount.decrementAndGet()) {
            currentStatus.remove(repositoryUrl);
        }
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    /**
     * Call this for the percentage complete
     * @return a number 0-100
     */
    public int getPercentComplete() {
        return latestProgress.get();
    }

    /**
     * Call this to cancel the clone
     */
    public void cancel() {
        this.cancel = true;
    }

    private static final Map<String, CloneProgressMonitor> currentStatus = new ConcurrentHashMap<>();

    /**
     * Get the latest progress for a clone operation on the given repository
     * @param repositoryUrl url to find information for
     * @return the progress monitor
     */
    public static CloneProgressMonitor get(String repositoryUrl) {
        return currentStatus.get(repositoryUrl);
    }
}
