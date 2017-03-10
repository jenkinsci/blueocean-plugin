/*
 * The MIT License
 *
 * Copyright (c) 2015 Red Hat, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.jenkins.blueocean.commons.stapler.export;

import org.junit.Assert;
import org.junit.Test;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ModelTest {
    private ExportConfig config = new ExportConfig().withFlavor(Flavor.JSON).withClassAttribute(ClassAttributeBehaviour.ALWAYS.simple());
    ModelBuilder builder = new ModelBuilder();

    @Test // JENKINS-26775
    public void sytheticMethodShouldNotBeExported() {
        Model<Impl> model = builder.get(Impl.class);
        assertEquals("Redundant properties discovered: " + model.getProperties(), 1, model.getProperties().size());
    }

    public static interface GenericInterface<T extends Number> {
        Collection<T> get();
    }

    @ExportedBean
    public static class Impl implements GenericInterface<Integer> {
        @Exported
        public List<Integer> get() {
            return Arrays.asList(42);
        }
    }

    //===========================================

    @Test
    public void merge() throws Exception {
        StringWriter sw = new StringWriter();
        builder.get(B.class).writeTo(b, Flavor.JSON.createDataWriter(b, sw, config));
        // B.x should maskc C.x, so x should be 40
        // but C.y should be printed as merged
        assertEquals("{'_class':'B','y':20,'z':30,'x':40}", sw.toString().replace('"','\''));
    }

    /**
     * y is a property from a merged object but that shouldn't be visible to {@link NamedPathPruner}.
     */
    @Test
    public void merge_pathPrune() throws Exception {
        StringWriter sw = new StringWriter();
        builder.get(B.class).writeTo(b, new NamedPathPruner("z,y"), Flavor.JSON.createDataWriter(b, sw, config));
        assertEquals("{'_class':'B','y':20,'z':30}", sw.toString().replace('"','\''));
    }

    B b = new B();
    public static class B extends A {
        @Exported
        public int x = 40;
    }

    @ExportedBean
    public static class A {
        @Exported(merge=true)
        public C c = new C();

        @Exported
        public int z = 30;
    }

    @ExportedBean
    public static class C {
        @Exported public int x = 10;
        @Exported public int y = 20;
    }

    //===========================================

    @Test
    public void skipNull() throws Exception {
        StringWriter sw = new StringWriter();
        SomeNullProperty o = new SomeNullProperty();
        builder.get(SomeNullProperty.class).writeTo(o, TreePruner.DEFAULT, Flavor.JSON.createDataWriter(o, sw, config));
        assertEquals("{'_class':'SomeNullProperty','bbb':'bbb','ccc':null,'ddd':'ddd'}", sw.toString().replace('"','\''));
    }

    @ExportedBean
    public static class SomeNullProperty {
        @Exported(skipNull=true)
        public String aaa = null;

        @Exported(skipNull=true)
        public String bbb = "bbb";

        @Exported(skipNull=false)
        public String ccc = null;

        @Exported(skipNull=false)
        public String ddd = "ddd";
    }

    /**
     * Test ExportInterceptor
     */

    public static class ExportInterceptor1 extends ExportInterceptor{

        @Override
        public Object getValue(Property property, Object model, ExportConfig config) throws IOException {
            try {
                return property.getValue(model);
            } catch (IllegalAccessException | InvocationTargetException | NotExportableException e) {
                if(property.name.equals("shouldBeSkipped")){
                    return SKIP;
                }
                throw new IOException(e);
            }
        }
    }

    public static class ExportInterceptor2 extends ExportInterceptor{

        @Override
        public Object getValue(Property property, Object model, ExportConfig config) throws IOException {
            try {
                return property.getValue(model);
            } catch (IllegalAccessException | InvocationTargetException | NotExportableException e) {
                if(!property.getType().isAssignableFrom(NotExportedBean.class)){
                    throw new IOException("Failed to write "+property.name);
                }
                return SKIP; //skip failing property
            }
        }
    }

    @Test
    public void testNotExportedBean() throws IOException {
        ExportConfig config = new ExportConfig().withFlavor(Flavor.JSON).withExportInterceptor(new ExportInterceptor1()).withSkipIfFail(true);
        StringWriter writer = new StringWriter();
        ExportableBean b = new ExportableBean();
        builder.get(ExportableBean.class).writeTo(b,Flavor.JSON.createDataWriter(b, writer, config));
        Assert.assertEquals("{\"_class\":\""+ExportableBean.class.getName()+"\",\"name\":\"property1\",\"notExportedBean\":{},\"shouldBeNull\":null}",
                writer.toString());
    }

    // should fail when serializing getShouldBeSkippedAsNull()
    @Test(expected = IOException.class)
    public void testNotExportedBeanFailing() throws IOException {
        ExportConfig config = new ExportConfig().withFlavor(Flavor.JSON).withExportInterceptor(new ExportInterceptor2()).withSkipIfFail(true);
        StringWriter writer = new StringWriter();
        ExportableBean b = new ExportableBean();
        builder.get(ExportableBean.class).writeTo(b,Flavor.JSON.createDataWriter(b, writer, config));
    }

    @ExportedBean
    public static class ExportableBean{
        @Exported
        public String getName(){
            return "property1";
        }

        // should be serialized as null
        @Exported
        public String getShouldBeNull(){
            return null;
        }

        // should be skipped
        @Exported
        public String getShouldBeSkipped(){
            throw new NullPointerException();
        }

        // null should be skipped
        @Exported(skipNull = true)
        public String getShouldBeSkippedAsNull(){
            return null;
        }
        // should not get serialized in to JSON as empty map due to skipIfNull is true
        @Exported
        public NotExportedBean getNotExportedBean(){
            return new NotExportedBean();
        }

        // should not get serialized in to JSON
        @Exported(merge = true)
        public NotExportedBean getNotExportedBeanMerged(){
            return new NotExportedBean();
        }
    }

    public static class NotExportedBean{
        public String getName(){
            return "property1";
        }
    }
}
