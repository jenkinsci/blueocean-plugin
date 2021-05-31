package io.blueocean.ath.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        List<String> newFolders = Stream.concat(this.folders.stream(), Arrays.stream(folders)).collect(Collectors.toList());
        return new Folder( Collections.unmodifiableList(new ArrayList<>( newFolders)));
    }

    public String getPath(String pipeline) {
        return this.append(pipeline).getPath();
    }
}
