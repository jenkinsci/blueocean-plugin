package io.jenkins.blueocean.service.embedded.rest;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Reader;

/**
 * Provides log text that can be appended at the end of log
 *
 * @author Vivek Pandey
 *
 */
public interface LogAppender {

    /**
     * Gives log that can be appended
     */
    @Nonnull Reader getLog();

    LogAppender DEFAULT = new LogAppender() {
        @Override
        public @Nonnull Reader getLog() {
            return new Reader(){

                @Override
                public int read(char[] cbuf, int off, int len) throws IOException {
                    return -1;
                }

                @Override
                public void close() throws IOException {
                    //noop
                }
            };
        }
    };
}
