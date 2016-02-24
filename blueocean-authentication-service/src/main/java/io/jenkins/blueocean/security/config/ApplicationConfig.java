package io.jenkins.blueocean.security.config;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.Stapler;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Ivan Meredith
 */
@Singleton
public class ApplicationConfig {

    private final ImmutableMap<String, String> properties;

    public ApplicationConfig() throws IOException {
        this.properties = ImmutableMap.copyOf(Maps.fromProperties(loadConfig()));
    }

    public String getApplicationPath(){
        return getApplicationPath(Stapler.getCurrentRequest());
    }

    public String getApplicationPath(HttpServletRequest request){
        return request.getContextPath();
    }

    public String getCookiePassword() {
        return getValue("cookie.password");
    }

    public String getCookieSalt() {
        return getValue("cookie.salt");
    }

    public String getValue(String key) {
        String value = System.getenv(key.replace('.', '_').toUpperCase());

        if (Strings.isNullOrEmpty(value)) {
            value = System.getProperty(key);
        }
        if (Strings.isNullOrEmpty(value)) {
            value = properties.get(key);
        }
        if (Strings.isNullOrEmpty(value)) {
            throw new IllegalStateException("Could not find property " + key);
        }
        return value;
    }

    static Properties loadConfig() throws IOException {
        Properties properties = new Properties();
        String propertiesFilePath = System.getProperty("blueocean.config.file");
        if (StringUtils.isNotEmpty(propertiesFilePath)) {
            File file = new File(propertiesFilePath);
            if (file.exists()) {
                InputStream is = null;
                try {
                    is = new FileInputStream(file);
                    properties.load(is);
                } finally {
                    IOUtils.closeQuietly(is);
                }
            }
        }
        return properties;
    }
}
