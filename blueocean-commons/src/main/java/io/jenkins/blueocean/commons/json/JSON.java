package io.jenkins.blueocean.commons.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class JSON {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.setAnnotationIntrospector(new StaplerAnnotationIntrospector());
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        MAPPER.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        MAPPER.registerModule(new SimpleModule() {
            @Override
            public void setupModule(SetupContext context) {
                super.setupModule(context);
                context.addBeanSerializerModifier(new ClassBeanSerializerModifier());
            }
        });
    }

    /**
     * Serialize the supplied object to JSON and return as a {@link String}.
     * @param object The object to serialize.
     * @return The JSON as a {@link String}.
     * @throws IOException Error serializing model object.
     */
    @Nonnull
    public static String toJson(@Nonnull Object object) throws IOException {
        try (StringWriter writer = new StringWriter()) {
            MAPPER.writeValue(writer, object);
            return writer.toString();
        }
    }

    public static void toJson(@Nonnull StaplerRequest req, @Nonnull StaplerResponse rsp, @Nonnull Object bean) throws IOException, ServletException {
        rsp.setHeader("X-Jenkins", Jenkins.VERSION);
        rsp.setHeader("X-Jenkins-Session", Jenkins.SESSION_HASH);
        rsp.setContentType("application/json;charset=UTF-8");
        Writer writer = rsp.getCompressedWriter(req);
        MAPPER.writeValue(writer, bean);
        writer.close();
    }

    private JSON() {}
}
