package io.jenkins.blueocean.commons.stapler.export;

import org.jvnet.tiger_types.Types;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

/**
 * Setting that controls how the '_class' attribute will be produced in the output.
 *
 * <p>
 * Three basic constants are defined:
 *
 * <dl>
 *     <dt>{@link #NONE}
 *     <dd>No type information will show up in the output whatsoever
 *
 *     <dt>{@link #ALWAYS}
 *     <dd>Every object gets the type information explicitly written out
 *
 *     <dt>{@link #IF_NEEDED}
 *     <dd>Type information will be produced only when it is necessary,
 *         for example when the declared type and the actual type differ.
 * </dl>
 *
 * <p>
 * In addition, {@link #simple()} can be used to produce just the simple name of the class,
 * whereas by default {@linkplain Class#getName() full class name} will be printed.
 *
 * @author Kohsuke Kawaguchi
 * @see ExportConfig#withClassAttribute(ClassAttributeBehaviour)
 */
public abstract class ClassAttributeBehaviour {
    private final String name;

    // no subtyping outside this package
    private ClassAttributeBehaviour(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return super.toString()+'['+name+']';
    }

    /**
     * Determines if this type attribute should be written.
     *
     * @return
     *      non-null if the type should be written, null if it shouldn't.
     * @see DataWriter#type(Type, Class)
     */
    abstract @Nullable
    Class map(@Nullable Type expected, @Nullable Class actual);

    String print(Type expected, Class actual) {
        return print(map(expected,actual));
    }

    protected String print(Class t) {
        return t==null ? null : t.getName();
    }

    public static final ClassAttributeBehaviour NONE = new ClassAttributeBehaviour("NONE") {
        @Override
        Class map(Type expected, Class actual) {
            return null;
        }
    };

    public static final ClassAttributeBehaviour ALWAYS = new ClassAttributeBehaviour("ALWAYS") {
        @Override
        Class map(Type expected, Class actual) {
            return actual;
        }
    };

    public static final ClassAttributeBehaviour IF_NEEDED = new ClassAttributeBehaviour("IF_NEEDED") {
        @Override
        Class map(Type expected, Class actual) {
            if (actual==null)
                return null;    // nothing to write
            if (expected==actual)
                return null;    // fast pass when we don't need it
            if (expected==null)
                return actual;  // we need to print it
            if (Types.erasure(expected)==actual)
                return null;    // slow pass
            return actual;
        }
    };

    public ClassAttributeBehaviour simple() {
        final ClassAttributeBehaviour outer = this;
        return new ClassAttributeBehaviour(this.name+"+simple") {
            @Override
            Class map(Type expected, Class actual) {
                return outer.map(expected,actual);
            }

            @Override
            protected String print(Class t) {
                return t==null ? null : t.getSimpleName();
            }
        };
    }
}
