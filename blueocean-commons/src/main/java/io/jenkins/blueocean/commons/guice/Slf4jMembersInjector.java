package io.jenkins.blueocean.commons.guice;

import com.google.inject.MembersInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * @author Vivek Pandey
 */
public class Slf4jMembersInjector<T> implements MembersInjector<T> {
    private final Field field;
    private final Logger logger;

    Slf4jMembersInjector(Field aField) {
        field = aField;
        logger = LoggerFactory.getLogger(field.getDeclaringClass());
        field.setAccessible(true);
    }

    @Override
    public void injectMembers(T t) {
        try {
            field.set(t, logger);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
