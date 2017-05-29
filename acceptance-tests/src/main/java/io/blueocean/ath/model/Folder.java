package io.blueocean.ath.model;

import com.google.common.base.Joiner;

import java.net.URLEncoder;

public class Folder {
    private String[] folders;

    public Folder(String ... folders) {
        this.folders = folders;
    }

    public static Folder folders(String ...folders){
        return new Folder(folders);
    }

    public String getPath() {
        return URLEncoder.encode(Joiner.on("/").join(folders));
    }

    public String get(int i) {
        return folders[i];
    }

    public String[] getFolders() {
        return folders;
    }

}
