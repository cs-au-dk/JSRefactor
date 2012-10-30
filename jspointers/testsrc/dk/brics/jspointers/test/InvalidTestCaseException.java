package dk.brics.jspointers.test;

/**
 * Thrown when a test case was malformed.
 * 
 * @author Asger
 */
@SuppressWarnings("serial")
public class InvalidTestCaseException extends RuntimeException {

    public InvalidTestCaseException() {
    }

    public InvalidTestCaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidTestCaseException(String message) {
        super(message);
    }

    public InvalidTestCaseException(Throwable cause) {
        super(cause);
    }
    
}
