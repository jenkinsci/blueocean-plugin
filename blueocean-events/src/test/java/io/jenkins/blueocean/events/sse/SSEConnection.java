package io.jenkins.blueocean.events.sse;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.Cookie;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.pubsub.ChannelSubscriber;
import org.jenkinsci.plugins.pubsub.EventFilter;
import org.jenkinsci.plugins.pubsub.Message;
import org.jenkinsci.plugins.pubsub.SimpleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

/**
 * Represents an SSE connection to a server.
 *
 * @author Kohsuke Kawaguchi
 * @author Vivek Pandey
 * @see <a href="https://www.w3.org/TR/eventsource/">spec</a>
 */
public class SSEConnection implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(SSEConnection.class);

    private URL jenkins;
    private final String clientId;
    private ChannelSubscriber listener;

    /**
     * Represents the ongoing connection that gets SSE from the backend.
     */
    private ListenableFuture<?> connection;
    private final List<Cookie> cookies;
    private final AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
    private boolean closed;

    /**
     * Connect to the given Jenkins as the specified identity,
     * passing on received events into the given callback.
     *
     * @param clientId In Jenkins SSE gateway, client ID uniquely identifies a session of SSE
     */
    public SSEConnection(URL jenkins, String clientId, ChannelSubscriber listener) throws IOException {
        try {
            this.jenkins = jenkins;
            this.clientId = clientId;
            this.listener = listener;

            // first make the 'connect' request to establish our client ID
            Response r = ensureSuccess(asyncHttpClient.prepareGet(new URL(jenkins, "sse-gateway/connect?clientId=" + clientId).toExternalForm()).execute().get());
            cookies = r.getCookies();

            listen();
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException("Failed to subscribe to " + jenkins, e);
        }
    }

    /**
     * Make a connection to listen to events
     */
    private void listen() throws IOException {
        if (closed)
            return;

        // then make the 'listen' request which is actually long-running
        connection = addCookies(asyncHttpClient
                .prepareGet(new URL(jenkins, "sse-gateway/listen/" + clientId).toExternalForm()))
                .addHeader("Accept", "text/event-stream")
                .addHeader("Cache-Control", "no-cache")
                .execute(new AsyncSSEHandler(new SSEMessageListener()))
                .addListener(new Runnable() {
                                 @Override
                                 public void run() {
                                    try {
                                        listen();
                                    } catch (IOException e) {
                                        LOGGER.warn("Failed to reconnect SSE connection, giving up", e);
                                    }
                                 }
                             }, SyncExecutor.INSTANCE

                );
    }

    class SSEMessageListener {
        public void onMessage(SSEMessage msg) {
            LOGGER.info("Received message " + msg);
            switch (msg.event) {
                case "open":
                case "configure":
                    // TODO: do we need to do anything about these?
                    break;
                default:
                    Message event = new SimpleMessage();
                    event.putAll(msg.asJSON(Map.class));
                    listener.onMessage(event);
                    break;
            }
        }
    }

    private BoundRequestBuilder addCookies(BoundRequestBuilder builder) {
        for (Cookie cookie : cookies) {
            builder.addCookie(cookie);
        }
        return builder;
    }

    public void subscribe(String channel) throws IOException {
        // constructor is not public
        //        EventFilter f = new EventFilter();
        JSONObject o = new JSONObject();
        o.put("jenkins_channel", channel);
        EventFilter f = (EventFilter) o.toBean(EventFilter.class);
        configure(Collections.singletonList(f),Collections.<EventFilter>emptyList());
    }

    /**
     * Bulk method to change the subscription.
     */
    public void configure(Collection<EventFilter> subscribe, Collection<EventFilter> unsubscribe) throws IOException {
        try {
            JSONObject body = new JSONObject();
            body.accumulate("dispatcherId", clientId)
                    .accumulate("subscribe", toJSONArray(subscribe))
                    .accumulate("unsubscribe", toJSONArray(unsubscribe));

            ensureSuccess(addCookies(asyncHttpClient
                    .preparePost(new URL(jenkins, "sse-gateway/configure").toExternalForm()))
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Cache-Control", "no-cache")
                    .setBody(body.toString())
                    .execute().get());
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException("Failed to subscribe to " + jenkins, e);
        }
    }

    private JSONArray toJSONArray(Collection<EventFilter> filters) {
        JSONArray a = new JSONArray();
        for (EventFilter f : filters) {
            JSONObject o = new JSONObject();
            o.putAll(f);
            a.add(o);
        }
        return a;
    }

    private Response ensureSuccess(Response rsp) throws IOException {
        // TODO: am I supposed to check the error here or does it happen on its own?
        if (rsp.getStatusCode() != 200) {
            throw new IOException("Failed to connect to " + rsp.getUri() + ": " + rsp.getStatusCode() + " " + rsp.getStatusText());
        }
        return rsp;
    }

    @Override
    public void close() throws IOException {
        LOGGER.info("Closing SSEConnection to " + jenkins);
        closed = true;
        if (connection != null)
            connection.cancel(true);
        asyncHttpClient.close();
    }

    /**
     * Reads SSE HTTP connection and dispatches events as they arrive.
     *
     * @author Kohsuke Kawaguchi
     */
    private static class AsyncSSEHandler implements AsyncHandler<Void> {

        /**
         * Spools unprocessed bytes from network that we haven't dispatched yet.
         */
        private final ByteArrayOutputStream buf = new ByteArrayOutputStream();

        /**
         * Did we see the CRLF for the last time? Used to detect message boundary, which is two CRLF.
         */
        private boolean lastByteWasCRLF =false;

        private final SSEMessageListener listener;

        public AsyncSSEHandler(SSEMessageListener listener) {
            this.listener = listener;
        }

        @Override
        public STATE onBodyPartReceived(HttpResponseBodyPart content) throws Exception {
            // spec says the content is always UTF-8
            for (byte b : content.getBodyPartBytes()) {
                boolean isCRLF = b==0x0D || b==0x0A;
                buf.write(b);
                if (lastByteWasCRLF && isCRLF)
                    dispatch();
                lastByteWasCRLF = isCRLF;
            }
            return STATE.CONTINUE;
        }

        /**
         * Invoked when {@link #buf} contains exactly one message.
         */
        private void dispatch() throws IOException {
            SSEMessage msg = SSEMessage.parse(buf.toString("UTF-8"));
            buf.reset();
            listener.onMessage(msg);
        }

        @Override
        public void onThrowable(Throwable t) {
            // not sure what I'm expected to do here
            LOGGER.warn("HTTP failure", t);
        }

        @Override
        public STATE onStatusReceived(HttpResponseStatus status) throws Exception {
            if (status.getStatusCode() >= 400) {
                throw new IOException("HTTP error: "+status.getStatusCode()+" "+status.getStatusText());
            }
            return STATE.CONTINUE;
        }

        @Override
        public STATE onHeadersReceived(HttpResponseHeaders headers) throws Exception {
            // we don't care about any header
            return STATE.CONTINUE;
        }

        @Override
        public Void onCompleted() throws Exception {
            LOGGER.info(String.format("%s connection closed by the server",this));
            return null;
        }

        private static final Logger LOGGER = LoggerFactory.getLogger(AsyncSSEHandler.class.getName());
    }

    //synchronous executor
    public  static class SyncExecutor implements Executor {
        @Override
        public void execute(Runnable command) {
            command.run();
        }

        static final SyncExecutor INSTANCE = new SyncExecutor();
    }
}
