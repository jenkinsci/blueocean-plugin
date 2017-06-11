package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server;

import io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketApi;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbPage;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbProject;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbRepo;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmRepositories;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmRepository;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmRepositoryContainer;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class BitbucketRepositoryContainer extends ScmRepositoryContainer {
    private final Link self;
    private final BitbucketApi api;
    private final BbProject project;

    public BitbucketRepositoryContainer(BbProject project, BitbucketApi api, Reachable parent) {
        this.self = parent.getLink().rel("repositories");
        this.api = api;
        this.project = project;
    }

    @Override
    public Link getLink() {
        return self;
    }

    @Override
    public ScmRepositories getRepositories() {
        return new BitbucketRepositories();
    }

    @Override
    public ScmRepository get(String name) {
        if(name == null){
            throw new ServiceException.BadRequestException("Repo slug is required parameter");
        }
        return api.getRepo(project.getKey(), name).toScmRepository(api, this);
    }

    public  class BitbucketRepositories extends ScmRepositories{
        private final Link self;
        private final boolean isLastPage;
        private final Integer nextPage;
        private final Integer pageSize;
        private final List<ScmRepository> repositories = new ArrayList<>();



        public BitbucketRepositories() {
            this.self = BitbucketRepositoryContainer.this.getLink().rel("repositories");
            StaplerRequest request = Stapler.getCurrentRequest();
            int pageNumber = 0;

            if (!StringUtils.isBlank(request.getParameter("pageNumber"))) {
                pageNumber = Integer.parseInt(request.getParameter("pageNumber"));
            }
            if(pageNumber <=0){
                pageNumber = 1;//default
            }
            int pageSize = 0;
            if (request.getParameter("pageSize") != null) {
                pageSize = Integer.parseInt(request.getParameter("pageSize"));
            }
            if(pageSize <=0){
                pageSize = 100;//default
            }

            BbPage<BbRepo> repos = api.getRepos(project.getKey(), pageNumber, pageSize);
            for (BbRepo repo : repos.getValues()) {
                repositories.add(repo.toScmRepository(api, this));
            }
            this.isLastPage = repos.isLastPage();
            this.pageSize = repos.getLimit();

            if (!repos.isLastPage()) {
                this.nextPage = pageNumber+1;
            }else{
                this.nextPage = null;
            }

        }

        @Override
        public Link getLink() {
            return self;
        }

        @Exported(name = "isLastPage")
        public boolean isLastPage() {
            return isLastPage;
        }

        @Exported(name = "nextPage")
        public Integer getNextPage() {
            return nextPage;
        }

        @Exported(name = "pageSize")
        public Integer getPageSize() {
            return pageSize;
        }

        @Override
        public Iterable<ScmRepository> getItems() {
            return repositories;
        }
    }
}
