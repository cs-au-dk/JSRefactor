package dk.brics.jspointers.test;

import java.io.File;
import java.util.Comparator;

import org.junit.runner.Description;

/**
 * Describes one of the test cases.
 * <p/>
 * Implements {@link Comparable}, and compares using the class name.
 */
public class TestCase {
	private Description description;
	private File file;
	private String name;
	
    public Description getDescription() {
        return description;
    }
    public void setDescription(Description description) {
        this.description = description;
    }
    
    public File getFile() {
        return file;
    }
    public void setFile(File file) {
        this.file = file;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Compares test cases based on their name.
     */
    public static final Comparator<TestCase> NameComparator = new Comparator<TestCase>() {
        @Override
        public int compare(TestCase o1, TestCase o2) {
            if (o1 == o2)
                return 0;
            if (o1 == null)
                return -1;
            if (o2 == null)
                return 1;
            if (o1.getName() == o2.getName())
                return 0;
            if (o1.getName() == null)
                return -1;
            if (o2.getName() == null)
                return 1;
            return o1.getName().compareTo(o2.getName());
        }
    };
    
}
