package io.jenkins.blueocean.blueocean_github_pipeline;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * @author Vivek Pandey
 */
@ExportedBean
public class GithubScmFileContent{
    private static final String ENCODED_CONTENT ="encodedContent";
    private static final String ENCODING ="encoding";

    private final String sha;
    private final String name;
    private final String owner;
    private final String repo;
    private final String path;
    private final Number size;
    private final Object encodedContent;
    private final String encoding;

    private GithubScmFileContent(String sha,
                         String name,
                         String owner,
                         String repo,
                         String path,
                         Number size,
                         Object encodedContent, String encoding) {
        this.sha = sha;
        this.name = name;
        this.path = path;
        this.size = size;
        this.encodedContent = encodedContent;
        this.encoding = encoding;
        this.owner = owner;
        this.repo = repo;
    }

    @Exported
    public String getSha() {
        return sha;
    }

    @Exported
    public String getName() {
        return name;
    }

    @Exported
    public String getPath() {
        return path;
    }

    @Exported(skipNull = true)
    public Number getSize() {
        return size;
    }

    @Exported
    public String getType() {
        return "file";
    }

    @Exported(name= ENCODED_CONTENT, skipNull = true)
    public Object getEncodedContent() {
        return encodedContent;
    }

    @Exported
    public String getOwner() {
        return owner;
    }

    @Exported
    public String getRepo() {
        return repo;
    }

    @Exported(name= ENCODING, skipNull = true)
    public String getEncoding() {
        return encoding;
    }

    public static class Builder{
        private  String sha;
        private  String name;
        private  String owner;
        private  String repo;
        private  String path;
        private  Number size;
        private  Object encodedContent;
        private  String encoding;

        public Builder sha(String sha){
            this.sha = sha;
            return this;
        }

        public Builder name(String name){
            this.name = name;
            return this;
        }

        public Builder owner(String owner){
            this.owner = owner;
            return this;
        }

        public Builder repo(String repo){
            this.repo = repo;
            return this;
        }

        public Builder path(String path){
            this.path = path;
            return this;
        }

        public Builder size(Number size){
            this.size = size;
            return this;
        }

        public Builder encodedContent(Object encodedContent){
            this.encodedContent = encodedContent;
            return this;
        }

        public Builder encoding(String encoding){
            this.encoding = encoding;
            return this;
        }

        public GithubScmFileContent build(){
            return new GithubScmFileContent(sha,name,owner,repo,path,size,encodedContent,encoding);
        }
    }
}
