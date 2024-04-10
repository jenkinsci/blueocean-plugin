package io.jenkins.blueocean.commons.stapler.export;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Controls the output behaviour.
 *
 * @author Kohsuke Kawaguchi
 */
public class ExportConfig {
    /**
     * @deprecated
     *      Use getter and setter
     */
    @SuppressFBWarnings(value="PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification="Preserve API compatibility")
    public boolean prettyPrint;

    private ClassAttributeBehaviour classAttribute = ClassAttributeBehaviour.IF_NEEDED;

    private ExportInterceptor exportInterceptor = ExportInterceptor.DEFAULT;

    private boolean skipIfFail = false;

    private Flavor flavor = Flavor.JSON;

    private boolean htmlEncode = false;

    /**
     * If true, output will be indented to make it easier for humans to understand.
     */
    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    /**
     * If true, output will be indented to make it easier for humans to understand.
     */
    public ExportConfig withPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
        return this;
    }

    public ClassAttributeBehaviour getClassAttribute() {
        return classAttribute;
    }

    /**
     * Controls the behaviour of the class attribute to be produced.
     */
    public ExportConfig withClassAttribute(ClassAttributeBehaviour cab) {
        if (cab==null)  throw new NullPointerException();
        this.classAttribute = cab;
        return this;
    }

    /**
     * Controls serialization of {@link @Exported} properties.
     */
    public ExportInterceptor getExportInterceptor() {
        return exportInterceptor;
    }

    public ExportConfig withExportInterceptor(ExportInterceptor interceptor){
        this.exportInterceptor = interceptor;
        return this;
    }

    public ExportConfig withSkipIfFail(boolean skipIfFail){
        this.skipIfFail = skipIfFail;
        return this;
    }

    /**
     * Turn on or off pretty printing of serialized data.
     */
    public boolean isSkipIfFail() {
        return skipIfFail;
    }

    /**
     * Gives {@link Flavor}
     */
    public Flavor getFlavor(){
        return flavor;
    }

    public ExportConfig withFlavor(Flavor flavor){
        this.flavor = flavor;
        return this;
    }

    /**
     * If true, output will escaped to be embedded into html
     */
    public boolean isHtmlEncode() {
        return htmlEncode;
    }

    /**
     * If true, output will escaped to be embedded into html
     */
    public ExportConfig withHtmlEncode(boolean htmlEncode) {
        this.htmlEncode = htmlEncode;
        return this;
    }
}
