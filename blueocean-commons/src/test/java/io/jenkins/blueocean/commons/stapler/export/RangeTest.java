package io.jenkins.blueocean.commons.stapler.export;

import com.google.common.collect.Iterables;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class RangeTest extends Assert {
    String[] array = new String[]{"a", "b", "c", "d", "e", "f"};
    List<String> list = Arrays.asList(array);
    Set<String> set = new LinkedHashSet<>(list);

    @Test
    public void normalRange() {
        Range r = new Range(2,4);
        assertEquals("[c, d]", toS(r.apply(array)));
        assertEquals("[c, d]", toS(r.apply(list)));
        assertEquals("[c, d]", toS(r.apply(set)));
    }

    @Test
    public void maxOnlyRange() {
        Range r = new Range(-1,2);
        assertEquals("[a, b]", toS(r.apply(array)));
        assertEquals("[a, b]", toS(r.apply(list)));
        assertEquals("[a, b]", toS(r.apply(set)));
    }

    @Test
    public void minOnlyRange() {
        Range r = new Range(4,Integer.MAX_VALUE);
        assertEquals("[e, f]", toS(r.apply(array)));
        assertEquals("[e, f]", toS(r.apply(list)));
        assertEquals("[e, f]", toS(r.apply(set)));
    }

    private String toS(Iterable i) {
        return Iterables.toString(i);
    }
}
