package io.jenkins.blueocean;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.jvnet.maven.jellydoc.annotation.NoContent;
import org.kohsuke.stapler.framework.adjunct.AdjunctManager;
import org.kohsuke.stapler.framework.adjunct.AdjunctsInPage;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

@NoContent
public class ResourceTag extends TagSupport {
    private String[] includes;
    private String[] assumes;

    /**
     * Comma-separated adjunct names.
     */
    public void setIncludes(String _includes) {
        includes = parse(_includes);
    }

    /**
     * Comma-separated adjunct names that are externally included in the page
     * and should be suppressed.
     */
    public void setAssumes(String _assumes) {
        assumes = parse(_assumes);
    }

    private String[] parse(String s) {
        String[] r = s.split(",");
        for (int i = 0; i < r.length; i++)
            r[i] = r[i].trim();
        return r;
    }

    public void doTag(XMLOutput out) throws JellyTagException {

        ServletContext servletContext = (ServletContext)getContext().getVariable("servletContext");

        ResourceManager m = ResourceManager.get(servletContext);
        if(m==null) {
            LOGGER.log(Level.WARNING,"ResourceManager is not installed for this application. Skipping <resource> tags", new Exception());
            return;
        }

        try {
            AdjunctsInPage a = AdjunctsInPage.get();
            if (assumes!=null)
                a.assumeIncluded(assumes);
            if (includes!=null)
                a.generate(out, includes);
        } catch (IOException e) {
            throw new JellyTagException(e);
        } catch (SAXException e) {
            throw new JellyTagException(e);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(ResourceTag.class.getName());
}
