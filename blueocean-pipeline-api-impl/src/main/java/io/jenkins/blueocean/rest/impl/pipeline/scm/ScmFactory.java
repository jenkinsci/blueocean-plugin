package io.jenkins.blueocean.rest.impl.pipeline.scm;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import io.jenkins.blueocean.rest.Reachable;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SCM factory to get {@link Scm}
 *
 * @author Vivek Pandey
 */
public abstract class ScmFactory implements ExtensionPoint{
    public abstract @CheckForNull Scm getScm(@Nonnull String id, @Nonnull Reachable parent);

    public abstract @Nonnull Scm getScm(Reachable parent);

    public static @CheckForNull Scm resolve(@Nonnull String id, @Nonnull Reachable parent){
        for(ScmFactory scmFactory : ScmFactory.all()){
            Scm scm = scmFactory.getScm(id, parent);
            if(scm != null){
                return scm;
            }
        }
        return null;
    }

    public static @Nonnull List<Scm> resolve(@Nonnull Reachable parent){
        return ScmFactory.all().stream()
            .map( scmFactory -> scmFactory.getScm( parent ) )
            .collect( Collectors.toList() );
    }

    public static ExtensionList<ScmFactory> all(){
        return ExtensionList.lookup(ScmFactory.class);
    }
}
