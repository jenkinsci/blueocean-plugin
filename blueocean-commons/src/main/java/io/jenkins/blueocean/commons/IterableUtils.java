package io.jenkins.blueocean.commons;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class IterableUtils
{
    private IterableUtils(){
    }

    public static <T> T getFirst(Iterable<? extends T> iterable, @Nullable T defaultValue) {
        return getNext(iterable.iterator(), defaultValue);
    }

    public static <T> T getNext( Iterator<? extends T> iterator, @Nullable T defaultValue) {
        return iterator.hasNext() ? iterator.next() : defaultValue;
    }

    public static <T> T find( Iterable<? extends T> iterable,
                              Predicate<? super T> predicate, @Nullable T defaultValue) {
        Optional<? super T> opt = (Optional<T>) StreamSupport.stream(iterable.spliterator(), false).filter(predicate).findFirst();
        return (T)opt.orElse(defaultValue);
    }

    public static <T> Iterable<T> getIterable(Stream<T> stream){
        if(stream==null) {
            return Collections.emptyList();
        }
        return stream::iterator;
    }

    public static <T> int size(Iterable<? extends T> iterable) {
        if (iterable == null) {
            return 0;
        }
        return (int)StreamSupport.stream(iterable.spliterator(), false).count();
    }

    public static <T> Iterable<T> chainedIterable(Iterable<? extends T> iterable1, Iterable<? extends T> iterable2) {
        return getIterable(Stream.concat(
            StreamSupport.stream(iterable1.spliterator(), false),
            StreamSupport.stream(iterable2.spliterator(), false)));
    }
}
