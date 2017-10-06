package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud;

import io.jenkins.blueocean.commons.ServiceException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
@Restricted(NoExternalUse.class)
public class CanonicalHttpUriRequest {
    private final String method;
    private final String path;

    private final Map<String, List<String>> params = new HashMap<>();

    public CanonicalHttpUriRequest(String method, String baseUrl, String urlStr) {
        this.method = StringUtils.upperCase(method);
        try {
            URL url = new URL(urlStr);
            this.path = StringUtils.removeEnd(url.getPath(), "/");
            createQueryMap(url);
        } catch (UnsupportedEncodingException | MalformedURLException e) {
            throw new ServiceException.UnexpectedErrorException(e.getMessage());
        }
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, List<String>> getParams() {
        return params;
    }

    private void createQueryMap(URL url) throws UnsupportedEncodingException {
        String q = url.getQuery();
        if(StringUtils.isBlank(q)){
            return;
        }
        final String[] pairs = q.split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
            if (!params.containsKey(key)) {
                params.put(key, new ArrayList<String>());
            }
            final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
            params.get(key).add(value);
        }
    }

    public String canonicalizedHash() {
        try {
            String value =  String.format("%s%s%s%s%s", method, '&', path, ',', canonicalizeQueryParams());
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashInputBytes = value.getBytes("UTF-8");
            digest.update(hashInputBytes, 0, hashInputBytes.length);
            return new String(Hex.encodeHex(digest.digest()));
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new ServiceException.UnexpectedErrorException(e.getMessage());
        }
    }

    private String canonicalizeQueryParams() throws IOException {
        List<ComparableParameter> parameterList = new ArrayList<>();

        for (Map.Entry<String, List<String>> parameter : params.entrySet()) {
            if (!"jwt".equals(parameter.getKey())) {
                parameterList.add(new ComparableParameter(parameter));
            }
        }

        Collections.sort(parameterList);
        return percentEncode(getParameters(parameterList));
    }

    private static class ComparableParameter implements Comparable<ComparableParameter> {
        ComparableParameter(Map.Entry<String, List<String>> parameter) throws UnsupportedEncodingException {
            this.parameter = parameter;
            String name = parameter.getKey();
            List<String> sortedValues = parameter.getValue();
            Collections.sort(sortedValues);
            String value = StringUtils.join(sortedValues, ',');
            // ' ' is used because it comes before any character
            // that can appear in a percentEncoded string.
            this.key = percentEncode(name) + ' ' + percentEncode(value);
        }

        final Map.Entry<String, List<String>> parameter;

        private final String key;

        public int compareTo(ComparableParameter that) {
            return this.key.compareTo(that.key);
        }

        @Override
        public String toString() {
            return key;
        }
    }

    private static String percentEncode(String str) throws UnsupportedEncodingException {
        return str == null ? "" : URLEncoder.encode(str, "UTF-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
    }

    /**
     * Write a form-urlencoded document into the given stream, containing the
     * given sequence of name/parameter pairs.
     */
    private static String percentEncode(Iterable<? extends Map.Entry<String, List<String>>> parameters) throws IOException {
        ByteArrayOutputStream into = new ByteArrayOutputStream();
        if (parameters != null) {
            boolean first = true;
            for (Map.Entry<String, List<String>> parameter : parameters) {
                if (first) {
                    first = false;
                } else {
                    into.write('&');
                }
                into.write(percentEncode(parameter.getKey()).getBytes());
                into.write('=');
                List<String> percentEncodedValues = new ArrayList<String>(parameter.getValue().size());

                for (String value : parameter.getValue()) {
                    percentEncodedValues.add(percentEncode(value));
                }

                into.write(StringUtils.join(percentEncodedValues, ',').getBytes());
            }
        }
        return into.toString("UTF-8");
    }

    private static List<Map.Entry<String, List<String>>> getParameters(Collection<ComparableParameter> parameters) {
        if (parameters == null) {
            return null;
        }
        List<Map.Entry<String, List<String>>> list = new ArrayList<>(parameters.size());
        for (ComparableParameter parameter : parameters) {
            list.add(parameter.parameter);
        }
        return list;
    }
}
