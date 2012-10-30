package dk.brics.jspointers.test;

/**
 * Executes test cases. The class should be annotated with <tt>@RunWith(AutoTester.class)</tt> so
 * it can be run with JUnit. See {@link #executeTest(TestCase)}.
 * <p/>
 * The class must have a constructor taking no arguments, which is the constructor used
 * by {@link AutoTester} to create it.
 * <p/>
 * All interaction with JUnit is handled by <tt>AutoTester</tt>.
 */
public interface TestExecutor {
	/**
	 * Executes a single test. If the method returns normally, the test is a success (green),
	 * if an <tt>AssertionError</tt> is thrown, the test is a failure (blue), and if another
	 * type of throwable is thrown, the test is an error (red).
	 * @param test a test-case to execute.
	 * @throws Throwable if the test-case was not successful
	 */
	void executeTest(TestCase test) throws Throwable;
	
	/**
	 * An executor may exclude a test from being run by return <tt>false</tt> here.
	 * This is run for each possible test case before any calls to {@link #executeTest} are made.
	 * @param test a test case
	 * @return <tt>true</tt> if the test should be run, <tt>false</tt> otherwise.
	 */
	boolean shouldTestRun(TestCase test);
	
	/**
	 * Called after filtering tests with {@link #shouldTestRun} but before any tests are executed.
	 * @param isSingleTest true if only one test is to be executed
	 */
	void initialize(boolean isSingleTest);
	
	/**
	 * Called after all tests have been executed.
	 */
	void close();
}
