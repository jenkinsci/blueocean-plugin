package io.jenkins.blueocean.commons.json;

import io.jenkins.blueocean.commons.ExportTest;
import io.jenkins.blueocean.commons.stapler.Export;
import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class JSONTest {

    @Test
    public void simple() throws Exception {
        String json = JSON.toJson(new TestObject());
        Assert.assertEquals("{\"_class\":\"TestObject\",\"value\":\"Foo\"}", json);
    }

    public class TestObject {
        @Exported
        public String getValue() {
            return "Foo";
        }

        public String getOther() {
            return "other";
        }
    }

    @Test
    public void test_json() throws IOException {
        String xJson = JSON.toJson(new ExportTest.X());
        JSONObject jsonObj = JSONObject.fromObject(xJson);

        Assert.assertEquals("X", jsonObj.get("_class"));
        Assert.assertEquals("xVal", jsonObj.get("val"));
    }

    @ExportedBean
    public static class X {
        @Exported
        public String val() {
            return "xVal";
        }
    }

    @ExportedBean
    public static class X2 {
        @Exported
        public String a = "aval";
        public String b = "bval";
        @Exported public String getC() {return "cval";}
        public String getD() {return "dval";}
    }

    @Test
    public void testSimpleUsage() throws Exception {
        Assert.assertEquals("{\"_class\":\"X2\",\"a\":\"aval\",\"c\":\"cval\"}",
            JSON.toJson(new X2()));
    }

    @ExportedBean(defaultVisibility=2) public static abstract class Super {
        @Exported public String basic = "super";
        @Exported public abstract String generic();
    }
    public static class Sub extends Super {
        public String generic() {return "sub";}
        @Exported public String specific() {return "sub";}
    }
    @ExportedBean public static class Container {
        @Exported public Super polymorph = new Sub();
    }

    @Test
    public void testInheritance() throws Exception {
        Assert.assertEquals("{\"_class\":\"Container\",\"polymorph\":{\"_class\":\"Sub\",\"basic\":\"super\",\"generic\":\"sub\",\"specific\":\"sub\"}}",
            JSON.toJson(new Container()));
    }

    public static class Sub2 extends Super {
        @Exported @Override public String generic() {return "sub2";}
    }

    @Test
    public void testInheritance2() throws Exception { // JENKINS-13336
        Assert.assertEquals("{\"_class\":\"Sub2\",\"basic\":\"super\",\"generic\":\"sub2\"}",
            JSON.toJson(new Sub2()));
    }

    @Test
    public void propertiesAreMerged() throws Exception {
        Assert.assertEquals("",
            JSON.toJson(new MergedProperties()));
    }

    @ExportedBean
    class MergedProperties {
        public String getFoo() {
            return "bar";
        }

        @Exported(merge = true)
        public X getX() {
            return new X();
        }
    }

    @Test
    public void exceptionHandling() throws IOException {
        Assert.assertEquals("{\"_class\":\"Supers\",\"elements\":[{\"_class\":\"Sub\",\"basic\":\"super\",\"generic\":\"sub\",\"specific\":\"sub\"},{\"_class\":\"Sub2\",\"basic\":\"super\",\"generic\":\"sub2\"}]}",
            JSON.toJson(new Supers(new Sub(), new Broken(), new Sub2())));
    }

    public static class Broken extends Super {
        @Exported @Override public String generic() {throw new RuntimeException("oops");}
    }

    @ExportedBean public static class Supers {
        @Exported public final List<? extends Super> elements;
        public Supers(Super... elements) {
            this.elements = Arrays.asList(elements);
        }
    }
}
