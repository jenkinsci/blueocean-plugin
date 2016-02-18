package io.jenkins.blueocean.rest;

/**
 * Wrapper over http body content
 *
 * @author Vivek Pandey
 */
public class Body {

    private Object content;
    public Body() {
    }

    public boolean isSet(){
        return content != null;
    }

    public Object get(){
        return content;
    }

    public void set(Object content){
        this.content = content;
    }

    public boolean notSet() {
        return content == null;
    }

}
