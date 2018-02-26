package io.jenkins.blueocean;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.ExtensionPoint;

/**
 * Participates in the rendering of HTML pages for all pages of Hudson.
 *
 * <p>
 * This class provides a few hooks to augment the HTML generation process of Hudson, across
 * all the HTML pages that Hudson delivers.
 *
 * <p>
 * For example, if you'd like to add a Google Analytics stat to Hudson, then you need to inject
 * a small script fragment to all Hudson pages. This extension point provides a means to do that.
 *
 * <h2>Life-cycle</h2>
 * <p>
 * Plugins that contribute this extension point
 * should implement a new decorator and put {@link Extension} on the class.
 *
 * <h2>Associated Views</h2>
 *
 *
 * <h3>header.jelly</h3>
 * <p>
 * This page is added right before the &lt;/head&gt; tag. Convenient place for additional stylesheet, &lt;meta&gt; tags, etc.
 *
 * <pre>
 * &lt;j:jelly xmlns:j="jelly:core" xmlns:st="jelly:stapler"&gt;
 *     &lt;script&gt;
 *         //your JS code
 *     &lt;/script&gt;
 * &lt;/j:jelly&gt;
 * </pre>
 *
 * <h3>httpHeaders.jelly</h3>
 *
 * For example, this httpHeader.jelly adds HTTP X-MY-HEADER
 *
 * <pre>
 *
 * &lt;?jelly escape-by-default='true'?&gt;
 * &lt;j:jelly xmlns:j="jelly:core" xmlns:st="jelly:stapler"&gt;
 *    &lt;st:header name="X-MY-HEADER" value="${it.someValue}"/&gt;
 * &lt;/j:jelly&gt;
 *
 * </pre>
 * <p>
 * This is a generalization of the X-Jenkins header that aids auto-discovery.
 * This fragment can write additional &lt;st:header name="..." value="..." /&gt; tags that go along with it.
 *
 * @author Vivek Pandey
 */
public abstract class BluePageDecorator implements ExtensionPoint {

    public static ExtensionList<BluePageDecorator> all() {
        return ExtensionList.lookup(BluePageDecorator.class);
    }
}
