package dk.brics.jspointers.test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Finds test cases from the file system and parses their annotations.
 * <p/>
 * TestCase objects will have all fields except the description field initialized.
 */
public class TestCaseCollector {
    public static List<TestCase> getTestCases(File dir) throws IOException {
        ArrayList<TestCase> result = new ArrayList<TestCase>();
        collectFiles(dir, result);
        return result;
    }
    public static TestCase createSingleFileTestCase(File file) throws IOException {
        TestCase test = new TestCase();
        test.setName(file.getName());
        test.setFile(file);
        return test;
    }
    
    private static void collectFiles(File dir, Collection<TestCase> result) throws IOException {
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().startsWith("."))
                    return false; // ignore .svn
                if (pathname.isDirectory())
                    return true;
                if (pathname.isFile() && pathname.getName().endsWith(".js"))
                    return true;
                return false;
            }
        });
        for (File file : files) {
            if (file.isDirectory()) {
                collectFiles(file, result);
            } else {
                result.add(createSingleFileTestCase(file));
            }
        }
    }
    
}
