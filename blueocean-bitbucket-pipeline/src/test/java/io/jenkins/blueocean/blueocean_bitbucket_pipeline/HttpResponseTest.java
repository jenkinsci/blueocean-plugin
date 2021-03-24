package io.jenkins.blueocean.blueocean_bitbucket_pipeline;

import io.jenkins.blueocean.commons.ServiceException;
import org.apache.http.HttpVersion;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicStatusLine;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.when;

public class HttpResponseTest {
    @Test
    public void testWithoutErrorDetails() {
        org.apache.http.HttpResponse rawHttpResponse = Mockito.mock(org.apache.http.HttpResponse.class);
        when(rawHttpResponse.getStatusLine()).thenReturn(new BasicStatusLine(new HttpVersion(1, 1), 400, ""));
        when(rawHttpResponse.getEntity()).thenReturn(new BasicHttpEntity() {
            @Override
            public InputStream getContent() throws IllegalStateException {
                return new ByteArrayInputStream("{\"errors\": [{\"message\": \"msg\", \"exceptionName\": \"name\"}]}".getBytes());
            }
        });

        HttpResponse httpResponse = new HttpResponse(rawHttpResponse);
        assertThrows(ServiceException.class, () -> {
            try {
                httpResponse.getContent();
            } catch (ServiceException e) {
                assertTrue(e.toJson().contains("no error details"));
                throw e;
            }
        });
    }
    @Test
    public void testWithErrorDetails() {
        org.apache.http.HttpResponse rawHttpResponse = Mockito.mock(org.apache.http.HttpResponse.class);
        when(rawHttpResponse.getStatusLine()).thenReturn(new BasicStatusLine(new HttpVersion(1, 1), 400, ""));
        when(rawHttpResponse.getEntity()).thenReturn(new BasicHttpEntity() {
            @Override
            public InputStream getContent() throws IllegalStateException {
                return new ByteArrayInputStream("{\"errors\": [{\"message\": \"msg\", \"exceptionName\": \"name\", \"details\":[\"fake-detail\"]}]}".getBytes());
            }
        });

        HttpResponse httpResponse = new HttpResponse(rawHttpResponse);
        assertThrows(ServiceException.class, () -> {
            try {
                httpResponse.getContent();
            } catch (ServiceException e) {
                assertTrue(e.toJson().contains("fake-detail"));
                throw e;
            }
        });
    }
}
