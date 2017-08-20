package io.jenkins.blueocean.commons.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;

import java.io.IOException;
import java.util.Set;

/**
 * Ensures that every exported bean has a "_class" property
 */
public class ClassBeanSerializerModifier extends BeanSerializerModifier {
    @Override
    public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
        if (serializer instanceof BeanSerializerBase) {
            return new ExtraFieldSerializer((BeanSerializerBase) serializer);
        }
        return serializer;
    }

    class ExtraFieldSerializer extends BeanSerializerBase {

        ExtraFieldSerializer(BeanSerializerBase source) {
            super(source);
        }

        ExtraFieldSerializer(ExtraFieldSerializer source,
                             ObjectIdWriter objectIdWriter) {
            super(source, objectIdWriter);
        }

        ExtraFieldSerializer(ExtraFieldSerializer source,
                             Set<String> toIgnore) {
            super(source, toIgnore);
        }

        public ExtraFieldSerializer(ExtraFieldSerializer source, ObjectIdWriter objectIdWriter, Object filterId) {
            super(source, objectIdWriter, filterId);
        }

        public BeanSerializerBase withObjectIdWriter(
            ObjectIdWriter objectIdWriter) {
            return new ExtraFieldSerializer(this, objectIdWriter);
        }

        @Override
        protected BeanSerializerBase withIgnorals(Set<String> toIgnore) {
            return new ExtraFieldSerializer(this, toIgnore);
        }

        @Override
        protected BeanSerializerBase asArraySerializer() {
            return new ExtraFieldSerializer(this);
        }

        @Override
        public BeanSerializerBase withFilterId(Object filterId) {
            return new ExtraFieldSerializer(this, _objectIdWriter, filterId);
        }

        public void serialize(Object bean, JsonGenerator jgen,
                              SerializerProvider provider) throws IOException {
            jgen.writeStartObject();
            Class<?> aClass = bean.getClass();
            String name;
            if (aClass.getEnclosingClass() == null) {
                name = aClass.getName();
            } else {
                name = aClass.getSimpleName();
            }
            jgen.writeStringField("_class", name);
            serializeFields(bean, jgen, provider);
            jgen.writeEndObject();
        }

        private String getClassName(Class<?> aClass) {
            if (aClass.getEnclosingClass() == null) {
                return aClass.getName();
            } else {
                return aClass.getSimpleName();
            }
        }
    }
}
