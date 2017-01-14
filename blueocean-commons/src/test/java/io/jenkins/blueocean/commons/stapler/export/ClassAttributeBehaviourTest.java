package io.jenkins.blueocean.commons.stapler.export;

import org.junit.Test;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Kohsuke Kawaguchi
 */
public class ClassAttributeBehaviourTest {

    private ExportConfig config = new ExportConfig()
            .withClassAttribute(ClassAttributeBehaviour.IF_NEEDED.simple());

    private <T> String write(T bean) throws IOException {
        StringWriter w = new StringWriter();
        Model model = new ModelBuilder().get(bean.getClass());
        model.writeTo(bean, new TreePruner.ByDepth(-999), Flavor.JSON.createDataWriter(bean, w, config));
        return w.toString().replace('\"','\'');
    }


    @ExportedBean
    public static class Point {
        @Exported
        public int x;
        @Exported
        public int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    @ExportedBean
    public static class Point3D extends Point {
        @Exported
        public int z;

        public Point3D(int x, int y, int z) {
            super(x,y);
            this.z = z;
        }
    }


    /**
     * Point type is inferred in the array but Point3D needs to be explicit
     */
    @Test
    public void arrayItem() throws Exception {
        String s = write(new ArrayItem());
        assertEquals("{'_class':'ArrayItem','foo':[{'x':1,'y':2},{'_class':'Point3D','x':4,'y':5,'z':6}]}",s);
    }

    @ExportedBean
    public static class ArrayItem {
        @Exported
        public Point[] foo = new Point[]{ new Point(1,2), new Point3D(4,5,6) };
    }


    /**
     * Infer collection item type from generics
     */
    @Test
    public void collectionItem() throws Exception {
        String s = write(new CollectionItem());
        assertEquals("{'_class':'CollectionItem','foo':[{'x':1,'y':2},{'_class':'Point3D','x':4,'y':5,'z':6}]}",s);
    }

    @ExportedBean
    public static class CollectionItem {
        @Exported
        public List<Point> foo = Arrays.asList(new Point(1,2), new Point3D(4,5,6));
    }


    /**
     * Infer collection item type from generics
     */
    @Test
    public void collectionItemWithTypeVariable() throws Exception {
        String s = write(new CollectionItemWithTypeVariable());
        assertEquals("{'_class':'CollectionItemWithTypeVariable','foo':[{'x':1,'y':2},{'_class':'Point3D','x':4,'y':5,'z':6}],'goo':[{'x':9,'y':8},{'_class':'Point3D','x':7,'y':6,'z':5}]}",s);
    }

    @ExportedBean
    public static class CollectionItemWithTypeVariable<T extends Point> {
        @Exported
        public List<T> foo = (List)Arrays.asList(new Point(1,2), new Point3D(4,5,6));
        @Exported
        public List<? extends Point> goo = Arrays.asList(new Point(9,8), new Point3D(7,6,5));
    }
}
