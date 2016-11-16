package io.jenkins.blueocean.rest;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import hudson.Extension;
import io.jenkins.blueocean.commons.JsonJacksonDeserializer;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.model.BluePipelineCreateRequest;
import io.jenkins.blueocean.rest.model.BluePipelineCreator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * JSON to {@link BluePipelineCreateRequest} deserializer
 *
 * @author Vivek Pandey
 */
@Extension
public class PipelineCreateDeserializer extends JsonJacksonDeserializer {
    @Override
    public String getName() {
        return PipelineCreateDeserializer.class.getName();
    }

    @Override
    public Class getType() {
        return BluePipelineCreateRequest.class;
    }

    @Override
    public JsonDeserializer getJsonDeserializer() {
        return new DeserializerImpl();
    }

    public static class DeserializerImpl extends StdDeserializer<BluePipelineCreateRequest> {

        protected DeserializerImpl() {
            super(BluePipelineCreateRequest.class);
        }

        @Override
        public BluePipelineCreateRequest deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            ObjectMapper mapper = (ObjectMapper) jp.getCodec();
            ObjectNode root = mapper.readTree(jp);
            Iterator<Map.Entry<String, JsonNode>> elementsIterator = root.fields();
            String creatorId = null;
            while (elementsIterator.hasNext()) {
                Map.Entry<String, JsonNode> element = elementsIterator.next();
                String name = element.getKey();
                JsonNode value = element.getValue();
                if (name.equals("creatorId") && value != null
                        && value.isTextual()) {
                    creatorId = value.asText();
                    for (BluePipelineCreator creator : BluePipelineCreator.all()) {
                        if (creator.getId().equals(value.asText())) {
                            return mapper.readValue(root.toString(), creator.getType());
                        }
                    }
                }
            }
            if(creatorId == null){
                throw new ServiceException.BadRequestExpception("creatorId is required element. It must be fully qualified name of the class that implements BluePipelineCreator extension point");
            }
            throw new ServiceException.BadRequestExpception("No pipeline creator found for creatorId: "+creatorId);
        }
    }
}
