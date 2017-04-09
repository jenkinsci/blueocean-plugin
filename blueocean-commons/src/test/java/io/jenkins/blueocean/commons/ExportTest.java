package io.jenkins.blueocean.commons;

import io.jenkins.blueocean.commons.stapler.Export;
import io.jenkins.blueocean.commons.stapler.ModelObjectSerializerTest;
import io.jenkins.blueocean.commons.stapler.ModelObjectSerializerTest.X;
import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.IOException;

public class ExportTest {
    @Test
    public void test_json() throws IOException {
        String xJson = Export.toJson(new ModelObjectSerializerTest.X());
        JSONObject jsonObj = JSONObject.fromObject(xJson);

        Assert.assertEquals(ModelObjectSerializerTest.X.class.getName(), jsonObj.getString("_class"));
        Assert.assertEquals("xVal", jsonObj.getString("val"));
    }

    @ExportedBean
    public static class X {
        @Exported
        public String val() {
            return "xVal";
        }
    }
}
