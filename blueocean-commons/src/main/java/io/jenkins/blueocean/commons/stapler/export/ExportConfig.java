package io.jenkins.blueocean.commons.stapler.export;

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
    public boolean prettyPrint;

    private ClassAttributeBehaviour classAttribute = ClassAttributeBehaviour.IF_NEEDED;

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
}
