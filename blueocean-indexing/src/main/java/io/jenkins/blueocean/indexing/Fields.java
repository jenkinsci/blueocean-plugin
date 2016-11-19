package io.jenkins.blueocean.indexing;

final class Fields {
    public static String sortField(String field) {
        return field + "_sort";
    }

    Fields() {}
}
