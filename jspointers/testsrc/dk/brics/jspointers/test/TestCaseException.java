package dk.brics.jspointers.test;

/**
 * Thrown and caught by {@link RunAllTests} if a test case was invalid.
 */
public class TestCaseException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public TestCaseException(String message) {
		super(message);
	}
}
