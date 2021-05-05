package io.jenkins.blueocean.events.sse;


import io.jenkins.blueocean.commons.JsonConverter;

/**
 * One event from SSE
 *
 * @author Kohsuke Kawaguchi
 * @author Vivek Pandey
 */
public class SSEMessage {
    public final String event;
    public final String data;

    public SSEMessage(String event, String data) {
        this.event = event;
        this.data = data;
    }

    /**
     * Obtains the data as Java type.
     */
    public <T> T asJSON(Class<T> type) {
        return JsonConverter.toJava(data, type);
    }

    public static SSEMessage parse(String input) {
        String event = null;
        StringBuilder data = new StringBuilder();

        // see section 7 "Interpreting an event stream" in the spec
        String[] lines = input.split("\\r\\n?|\\n"); // CR, LF, CRLF are all valid
        for (String s : lines) {
            if (s.length()==0)      continue;   // end of message

            int idx = s.indexOf(":");
            if (idx==0)  continue;   // comment

            String name,value;
            if (idx>0) {
                name = s.substring(0,idx);
                value = s.substring(idx+1);
                if (value.startsWith(" "))
                    value = value.substring(1);
            } else {
                name = s;
                value = "";
            }

            switch (name) {
                case "event":
                    event = value;
                    break;
                case "data":
                    data.append(value).append('\n');
                    break;
                default:
                    // ignore other fields
            }
        }

        return new SSEMessage(event,data.toString());
    }

    @Override
    public String toString() {
        return String.format("SSEMessage[event=%s,data=%s]",event,data);
    }
}
