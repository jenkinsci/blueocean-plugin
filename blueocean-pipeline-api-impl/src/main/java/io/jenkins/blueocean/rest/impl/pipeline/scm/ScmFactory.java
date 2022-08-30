package io.jenkins.blueocean.rest.impl.pipeline.scm;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import io.jenkins.blueocean.rest.Reachable;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SCM factory to get {@link Scm}
 *
 * @author Vivek Pandey
 */
public abstract class ScmFactory implements ExtensionPoint{
    public abstract @CheckForNull Scm getScm(@NonNull String id, @NonNull Reachable parent);

    public abstract @NonNull Scm getScm(Reachable parent);

    public static @CheckForNull Scm resolve(@NonNull String id, @NonNull Reachable parent){
        for(ScmFactory scmFactory : ScmFactory.all()){
            Scm scm = scmFactory.getScm(id, parent);
            if(scm != null){
                return scm;
            }
        }
        return null;
    }

    public static @NonNull List<Scm> resolve(@NonNull Reachable parent){
        return ScmFactory.all().stream()
            .map( scmFactory -> scmFactory.getScm( parent ) )
            .collect( Collectors.toList() );
    }

    public static ExtensionList<ScmFactory> all(){
        return ExtensionList.lookup(ScmFactory.class);
    }
}
