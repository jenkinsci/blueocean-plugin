package io.jenkins.blueocean.indexing;


import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import hudson.model.Item;
import hudson.model.Run;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueRun;
import jenkins.model.Jenkins;
import org.apache.commons.lang.SystemUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

class Index<T> implements Closeable {

    @SuppressWarnings("unchecked")
    public static Index<BluePipeline> openItems() throws IOException {
        return openInternal(Jenkins.getInstance().getRootDir());
    }

    @SuppressWarnings("unchecked")
    public static Index<BlueRun> openRuns(Item item) throws IOException {
        return openInternal(item.getRootDir());
    }

    private static Index openInternal(File root) throws IOException {
        File file = new File(root, "index");
        if (!file.exists()) {
            file.mkdirs();
        }
        // On Linux we can safely use NIOFSDirectory
        return new Index(SystemUtils.IS_OS_WINDOWS ? new SimpleFSDirectory(file.toPath()) : new NIOFSDirectory(file.toPath()));
    }

    private final FSDirectory index;

    public Index(FSDirectory index) {
        this.index = index;
    }

    /**
     * Add documents to index
     * @param items the items to add to the index
     * @param transformer to transform the item to a document
     * @throws IOException
     */
    public void addDocuments(Iterable<T> items, Function<T, Document> transformer) throws IOException {
        try (StandardAnalyzer analyzer = new StandardAnalyzer()) {
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            try (IndexWriter writer = new IndexWriter(index, config)) {
                writer.addDocuments(Iterables.transform(items, transformer));
            }
        }
    }

    public Iterable<T> query(Query query, int limit, @Nullable Sort sort, final Function<Document, T> transformer) throws IOException {
        try (IndexReader reader = DirectoryReader.open(index)) {
            final IndexSearcher searcher = new IndexSearcher(reader);
            final TopDocs docs = searcher.search(query, limit, sort);
            final List<ScoreDoc> scoreDocs = Arrays.asList(docs.scoreDocs);
            return ImmutableList.copyOf(Iterables.transform(scoreDocs, new Function<ScoreDoc, T>() {
                @Nullable
                @Override
                public T apply(@Nullable ScoreDoc input) {
                    int documentId = input.doc;
                    try {
                        Document document = searcher.doc(documentId);
                        return transformer.apply(document);
                    } catch (IOException e) {
                        throw new IllegalStateException("Could not load document <" + documentId + "> from index <" + index.toString() + ">", e);
                    }
                }
            }));
        }
    }

    public void delete(Term... term) throws IOException {
        try (StandardAnalyzer analyzer = new StandardAnalyzer()) {
            try (IndexWriter writer = new IndexWriter(index, new IndexWriterConfig(analyzer))) {
                writer.deleteDocuments(term);
            }
        }
    }

    public void deleteIndex() throws IOException {
        index.close();
        Files.deleteIfExists(index.getDirectory());
    }

    @Override
    public void close() throws IOException {
        index.close();
    }

    public int size() throws IOException {
        try (IndexReader reader = DirectoryReader.open(index)) {
            return reader.numDocs();
        }
    }

    @Override
    public String toString() {
        return "Index{" +
            "index=" + index +
            '}';
    }
}
