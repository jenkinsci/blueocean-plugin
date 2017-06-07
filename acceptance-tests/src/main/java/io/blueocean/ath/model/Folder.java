package io.blueocean.ath.model;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Folder {
    private List<String> folders;

    public Folder(String ... folders) {
        this.folders = ImmutableList.copyOf(folders);
    }

    public Folder(List folders) {
        this.folders = ImmutableList.copyOf(folders);
    }

    public static Folder folders(String ...folders){
        return new Folder(folders);
    }

    public String getPath() {
        return Joiner.on("/").join(folders);
    }

    public String get(int i) {
        return folders.get(i);
    }

    public List<String> getFolders() {
        return folders;
    }

    public Folder append(String ...folders) {
        List<String> newFolders = Stream.concat(this.folders.stream(), Arrays.stream(folders)).collect(Collectors.toList());
        return new Folder(ImmutableList.copyOf(newFolders));
    }

    public String getPath(String pipeline) {
        return this.append(pipeline).getPath();
    }
}
