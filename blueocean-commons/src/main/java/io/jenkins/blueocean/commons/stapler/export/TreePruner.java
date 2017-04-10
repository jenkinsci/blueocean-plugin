/*
 * Copyright (c) 2004-2010, Kohsuke Kawaguchi
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of
 *       conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.jenkins.blueocean.commons.stapler.export;

/**
 * Controls the portion of the object graph to be written to {@link DataWriter}.
 *
 * @author Kohsuke Kawaguchi
 * @see Model#writeTo(Object, org.kohsuke.stapler.export.TreePruner, DataWriter)
 */
public abstract class TreePruner {
    /**
     * Called before Hudson writes a new property.
     *
     * @return
     *      null if this property shouldn't be written. Otherwise the returned {@link org.kohsuke.stapler.export.TreePruner} object
     *      will be consulted to determine properties of the child object in turn.
     */
    public abstract TreePruner accept(Object node, Property prop);

    public Range getRange() {
        return Range.ALL;
    }

    public static class ByDepth extends TreePruner {
        final int n;
        private ByDepth next;

        public ByDepth(int n) {
            this.n = n;
        }

        private ByDepth next() {
            if (next==null)
                next = new ByDepth(n+1);
            return next;
        }

        @Override
        public TreePruner accept(Object node, Property prop) {
            if (prop.visibility < n)    return null;    // not visible

            if (prop.inline || prop.merge)    return this;
            return next();
        }
    }

    /**
     * Probably the most common {@link org.kohsuke.stapler.export.TreePruner} that just visits the top object and its properties,
     * but none of the referenced objects.
     */
    public static final TreePruner DEFAULT = new ByDepth(1);
}
