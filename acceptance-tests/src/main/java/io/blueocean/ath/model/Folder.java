package io.blueocean.ath.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Folder {
    private List<String> folders;

    public Folder(String ... folders) {
        this.folders = Arrays.asList(folders);
    }

    public Folder(List folders) {
        this.folders = new ArrayList<>(folders);
    }

    public static Folder folders(String ...folders){
        return new Folder(folders);
    }

    public String getPath() {
        return String.join("/", folders);
    }

    public String getClassJobPath(){
        return String.join("/job/", folders);
    }

    public String get(int i) {
        return folders.get(i);
    }

    public List<String> getFolders() {
        return folders;
    }

    public Folder append(String ...folders) {
        List<String> newFolders = new ArrayList<>(this.folders);
        newFolders.addAll(Arrays.asList(folders));
        return new Folder(newFolders);
    }

    public String getPath(String pipeline) {
        return this.append(pipeline).getPath();
    }
}
