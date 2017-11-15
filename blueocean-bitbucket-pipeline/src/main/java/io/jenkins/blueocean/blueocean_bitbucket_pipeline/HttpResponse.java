package io.jenkins.blueocean.blueocean_bitbucket_pipeline;

import io.jenkins.blueocean.commons.ServiceException;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Vivek Pandey
 */
@Restricted(NoExternalUse.class)
public class HttpResponse {
    private final org.apache.http.HttpResponse response;

    public HttpResponse(org.apache.http.HttpResponse response) {
        this.response = response;
    }

    public @CheckForNull InputStream getContent() {
        try {
            HttpEntity entity = response.getEntity();
            if(getStatus() >= 300){
                EntityUtils.consume(entity);
                throw new ServiceException(getStatus(), getStatusLine());
            }
            return entity == null ? null :entity.getContent();
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException(e.getMessage(), e);
        }
    }

    public int getStatus() {
        return response.getStatusLine().getStatusCode();
    }

    public @Nonnull String getStatusLine() {
        return response.getStatusLine().getReasonPhrase();
    }

    public @CheckForNull String getHeader(String name) {
        return response.getFirstHeader(name).getValue();
    }
}
