package dk.brics.jsrefactoring.evaluate;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Benchmarks {
	final static String BASEDIR = "/home/xiemaisi/projects/JSRefactoring/benchmarks/";
	
	public static File getBenchmark(String set, String name) {
		return new File(new File(BASEDIR, set), name);
	}

	public static List<File> getBenchmarks() {
		List<File> res = new ArrayList<File>();
		res.addAll(getChromeBenchmarks());
		res.addAll(getGoogleBenchmarks());
		res.addAll(getMSIEBenchmarks());
		res.addAll(get10kBenchmarks());
		return res;
	}

	public static List<File> getChromeBenchmarks() {
		List<File> res = new ArrayList<File>();
		for(File benchmark : new File(BASEDIR, "chrome").listFiles()) {
			if(benchmark.isDirectory())
				continue;
			String name = benchmark.getName();
			// these use dynamic features
			if(name.equals("apophis.html") || name.equals("bingbong.html") || name.equals("tetris.html"))
				continue;
			res.add(benchmark);			
		}
		Collections.sort(res);
		return res;
	}

	public static List<File> getGoogleBenchmarks() {
		List<File> res = new ArrayList<File>();
		for(File benchmark : new File(BASEDIR, "google").listFiles()) {
			if(benchmark.isDirectory())
				continue;
			res.add(benchmark);			
		}
		Collections.sort(res);
		return res;
	}

	public static List<File> getMSIEBenchmarks() {
		List<File> res = new ArrayList<File>();
		for(File benchmark : new File(BASEDIR, "msie").listFiles()) {
			if(benchmark.isDirectory())
				continue;
			res.add(benchmark);			
		}
		Collections.sort(res);
		return res;
	}

	public static List<File> get10kBenchmarks() {
		List<File> res = new ArrayList<File>();
		for(File benchmark : new File(BASEDIR, "10k").listFiles()) {
			if(benchmark.isDirectory())
				continue;
			String name = benchmark.getName();
			// these take forever to analyse
			if(name.equals("filterrific.html") || name.equals("lines.html") || name.equals("nbody.html") || name.equals("sudoku.html"))
				continue;
			res.add(benchmark);
		}
		Collections.sort(res);
		return res;
	}
}
