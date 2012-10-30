package dk.brics.jspointers.test;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;

/**
 * A custom JUnit runner that finds all Java test cases in the <tt>testcases</tt> folder
 * and executes a test for each of them. To use <tt>AutoTester</tt>, a class implementing <tt>TestExecutor</tt>
 * must be annotated with <tt>@RunWith(AutoTester.class)</tt>, at which point that class can be started as a JUnit test.
 * 
 * @author Asger Feldthaus
 */
public class AutoTester extends Runner implements Filterable {
	private List<TestCase> tests;
	private Description description;
	private TestExecutor executor;
	private Properties previousResults = new Properties();
	private Properties newResults = new Properties();
	private File resultFile;
	private Class<?> executorClass;
	private List<TestExecutor> testExecutors = Collections.synchronizedList(new LinkedList<TestExecutor>());
	
	public AutoTester(Class<?> clazz) throws Exception {
	    this.executorClass = clazz;
	    TestConfig config = clazz.getAnnotation(TestConfig.class);
	    if (config == null) {
	        throw new RuntimeException("Test executor is missing @TestConfig annotation");
	    }
		tests = TestCaseCollector.getTestCases(new File(config.testFolder()));
		Collections.sort(tests, TestCase.NameComparator); // sort by name
		
		description = makeJUnitDescription();
		executor = (TestExecutor)clazz.getConstructor().newInstance();
		
		resultFile = new File(config.outputFolder() + "/" + clazz.getName() + "_recent.txt");
		resultFile.getParentFile().mkdirs();
		loadPreviousResults();
		
		executor.initialize(tests.size() == 1);
		applyTestFilter();
	}
	
	private Description makeJUnitDescription() {
	    Description d = Description.createSuiteDescription(executorClass);
	    for (TestCase test : tests) {
	        test.setDescription(Description.createTestDescription(executorClass, test.getName()));
	        d.addChild(test.getDescription());
	    }
	    return d;
	}
	
	private void applyTestFilter() {
		List<TestCase> filteredTests = new ArrayList<TestCase>();
		for (TestCase test : tests) {
			if (executor.shouldTestRun(test)) {
				filteredTests.add(test);
			}
		}
		// ditch the old description and use the filtered one only
		this.tests = filteredTests;
		this.description = makeJUnitDescription();
	}
	
	public void filter(Filter filter) {
		// By implementing Filterable, we can right-click individual tests and choose "Run"
		// to run only that test
		List<TestCase> filteredTests = new ArrayList<TestCase>();
		for (TestCase test : tests) {
			if (filter.shouldRun(test.getDescription())) {
				filteredTests.add(test);
			}
		}
		// ditch the old description and use the filtered one only
		this.tests = filteredTests;
		this.description = makeJUnitDescription();
	}
	
	@Override
	public Description getDescription() {
		return description;
	}
	
	TestExecutor createExecutor() {
        try {
            TestExecutor exec = (TestExecutor)executorClass.newInstance();
            exec.initialize(tests.size() == 1);
            testExecutors.add(exec);
            return exec;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
	}
	
	@Override
	public void run(RunNotifier notifier) {
        runSequential(notifier);
	}
	
	private void runSequential(RunNotifier notifier) {
		// execute all tests
		notifier.fireTestRunStarted(description);
		for (TestCase test : tests) {
			Throwable exception = null;
			
			try {
				notifier.fireTestStarted(test.getDescription()); // put this in try block so we can catch StoppedByUserException
				executor.executeTest(test);
			} catch (StoppedByUserException ex) { 
				return;
			} catch (Throwable t) {
				notifier.fireTestFailure(new Failure(test.getDescription(), t));
				exception = t;
			}
			notifier.fireTestFinished(test.getDescription());
			
			compareResults(test, exception);
		}
		
		executor.close();
		
		saveResults();
	}

	
	private void loadPreviousResults() {
		previousResults = new Properties();
		try {
			Reader reader = new FileReader(resultFile);
			previousResults.load(reader);
			reader.close();
		} catch (IOException ex) {
		}
		
		// when we only run one test, we want to preserve the old test results
		newResults.clear();
		newResults.putAll(previousResults);
		// NOTE: when renaming or deleting a test, this has the unfortunate consequence that 
		//		 its last result never gets deleted -- but this is not a serious issue
	}
	
	private void saveResults() {
		try {
			Writer writer = new FileWriter(resultFile);
			newResults.store(writer, "");
		} catch (IOException ex) {
		}
	}
	private void compareResults(TestCase test, Throwable exception) {
		String key = test.getName();
		String outcome = exception == null ? "OK" : exception.toString();
		if (previousResults.containsKey(key)) {
			String previous = previousResults.getProperty(key);
			if (!outcome.equals(previous)) {
				System.err.flush(); // make sure it does not get interleaved with an exception stacktrace
				System.out.println("CHANGED " + test.getName());
				System.out.println("\told = " + previous);
				System.out.println("\tnew = " + outcome);
				System.out.println();
				System.out.flush();
			}
		}
		newResults.setProperty(key, outcome);
	}
}
