package io.jenkins.blueocean.commons;

import io.jenkins.blueocean.commons.stapler.Export;
import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.IOException;

public class ExportTest {
    @Test
    public void test_json() throws IOException {
        String xJson = Export.toJson(new X());
        JSONObject jsonObj = JSONObject.fromObject(xJson);

        Assert.assertEquals(X.class.getName(), jsonObj.getString("_class"));
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
