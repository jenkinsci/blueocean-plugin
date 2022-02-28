package io.jenkins.blueocean.commons.guice;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.slf4j.Logger;

import java.lang.reflect.Field;


/**
 * @author Vivek Pandey
 */
public class Slf4jTypeListener implements TypeListener{
    @Override
    public <I> void hear(TypeLiteral<I> iTypeLiteral, TypeEncounter<I> iTypeEncounter) {
        for (Field field : iTypeLiteral.getRawType().getDeclaredFields()) {
            if (field.getType() == Logger.class
                && field.isAnnotationPresent(InjectLogger.class)) {
                iTypeEncounter.register(new Slf4jMembersInjector<>(field));
            }
        }
    }
}
