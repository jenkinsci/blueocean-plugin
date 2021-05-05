package io.jenkins.blueocean.blueocean_bitbucket_pipeline;

import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vivek Pandey
 */
@Restricted(NoExternalUse.class)
public class HttpResponse {
    private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);
    private final org.apache.http.HttpResponse response;

    public HttpResponse(org.apache.http.HttpResponse response) {
        this.response = response;
    }

    public @CheckForNull InputStream getContent() {
        try {
            HttpEntity entity = response.getEntity();
            if(getStatus() >= 300){
                ErrorMessage errorMessage = new ErrorMessage(getStatus(), getStatusLine());
                // WireMock returns "Bad Request" for the status message; it's also pretty nondescript
                if (StringUtils.isEmpty(errorMessage.message) || "Bad Request".equals(errorMessage.message)) {
                    String message;;
                    List<ErrorMessage.Error> errors = new ArrayList<>();
                    try {
                        JSONObject jsonResponse = JSONObject.fromObject(IOUtils.toString(entity.getContent()));
                        JSONArray arr = jsonResponse.getJSONArray("errors");
                        StringBuilder messageBuilder = new StringBuilder();
                        for (int i = 0; i < arr.size(); i++) {
                            JSONObject err = arr.getJSONObject(i);
                            if (i > 0) {
                                messageBuilder.append(", ");
                            }
                            messageBuilder.append(err.getString("message"));

                            StringBuilder details = new StringBuilder();
                            if (err.has("details")) {
                                JSONArray errorDetails = err.getJSONArray("details");
                                for (int detailIdx = 0; detailIdx < errorDetails.size(); detailIdx++) {
                                    details.append(errorDetails.getString(detailIdx)).append("\n");
                                }
                            } else {
                                details.append("no error details");
                            }
                            errors.add(new ErrorMessage.Error("", err.getString("exceptionName"), details.toString()));
                        }
                        message = messageBuilder.toString();
                    } catch(Exception e) {
                        logger.error("An error occurred getting BitBucket API error content", e);
                        message = "An unknown error was reported from the BitBucket server";
                    }
                    errorMessage = new ErrorMessage(getStatus(), message).addAll(errors);
                } else {
                    EntityUtils.consume(entity);
                }
                throw new ServiceException(getStatus(), errorMessage, null);
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
