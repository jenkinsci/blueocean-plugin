package io.jenkins.blueocean.rest.impl.pipeline.scm;

import hudson.model.Item;
import io.jenkins.blueocean.rest.impl.pipeline.ScmContentProvider;
import io.jenkins.blueocean.rest.model.BlueScmSource;

/**
 * @author cliffmeyers
 */
public class ScmSourceImpl extends BlueScmSource {

    private final Item item;

    public ScmSourceImpl(Item item) {
        this.item = item;
    }

    @Override
    public String getId() {
        ScmContentProvider provider = ScmContentProvider.resolve(item);
        if (provider != null) {
            return provider.getScmId();
        }
        return null;
    }

    @Override
    public String getApiUrl() {
        ScmContentProvider provider = ScmContentProvider.resolve(item);
        if (provider != null) {
            return provider.getApiUrl(item);
        }
        return null;
    }

}
