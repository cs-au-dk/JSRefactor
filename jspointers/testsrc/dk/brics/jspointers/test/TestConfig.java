package dk.brics.jspointers.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TestConfig {
    /**
     * Folder containing test cases. Should not include a trailing slash.
     * @return a pathname without trailing slash
     */
    String testFolder();
    
    /**
     * Folder to store output in. Should not include a trailing slash.
     * @return a pathname without trailing slash
     */
    String outputFolder();
}
