package dk.brics.jsrefactoring.evaluate;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import junit.framework.Assert;

import org.junit.Test;

import dk.brics.jsparser.node.PStmt;
import dk.brics.jsparser.node.Token;
import dk.brics.jsrefactoring.Diagnostic;
import dk.brics.jsrefactoring.InputFile;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.PrettyPrinter;
import dk.brics.jsrefactoring.extractmodule.ExtractModule;
import dk.brics.jsrefactoring.test.TestUtil;

public class ExtractModules {
	private static void test(File in, File out) {
		Master input = new Master(in);
		input = test(input);
		Master expected = new Master(out);
		if(input != null)
			Assert.assertEquals(TestUtil.pp(expected), TestUtil.pp(input));
	}
	
	static Master test(Master input) {
		File tmp;
		try {
			tmp = File.createTempFile("extractModule", ".js");
		} catch (IOException e) {
			Assert.fail(e.getMessage());
			return null;
		}
		TestUtil.writeFile(tmp, TestUtil.pp(input));
		try {
			outer:
			while(true) {
				input = new Master(tmp);
				for(InputFile f : input.getUserFiles()) {
					int start = -1, end = -1;
					Token startToken = null, endToken = null;
					String moduleName = null;
					LinkedList<PStmt> statements = f.getAst().getBody().getBlock().getStatements();
					for(int i=0;i<statements.size();++i) {
						PStmt stmt = statements.get(i);
						Token comment = TestUtil.precedingCommentToken(stmt);
						if(comment != null && comment.getText().startsWith("/* module ")) {
							start = i;
							startToken = comment;
							moduleName = comment.getText().substring("/* module ".length(), comment.getText().length()-3);
						}
						if(start != -1) {
							comment = TestUtil.followingCommentToken(stmt);
							if(comment != null &&  comment.getText().startsWith("/* end ")) {
								end = i;
								endToken = comment;
								ExtractModule refactoring = new ExtractModule(input, moduleName, f.getAst(), start, end);
								refactoring.execute();
								if(!refactoring.getDiagnostics().isEmpty()) {
									Diagnostic diag = refactoring.getDiagnostics().get(0);
									Assert.fail("At line " + diag.getStartLine() + ": " + diag.getMessage());
									return null;
								}
								PrettyPrinter.connect(startToken.getPrevious(), startToken.getNext());
								PrettyPrinter.connect(endToken.getPrevious(), endToken.getNext());
								TestUtil.writeFile(tmp, TestUtil.pp(input));
								continue outer;
							}
						}
					}
				}
				break outer;
			}
		} finally {
			//tmp.delete();
		}
		return input;
	}
	
	public String dump(Master input) {
		StringBuffer buf = new StringBuffer();
		for(InputFile f : input.getUserFiles())
			buf.append(f.getAst().toString());
		return buf.toString();
	}
	
	public void test(String set, String name) {
		test(new File(new File(Benchmarks.BASEDIR, set), name), new File(new File(new File(Benchmarks.BASEDIR, set), "extracted"), name));
	}
	
	@Test public void test_10k_world() { test("10k", "10k_world.html"); }
	@Test public void test_3d_maker() { test("10k", "3d_maker.html"); }
	@Test public void test_attractor() { test("10k", "attractor.html"); }
	
	// fails due to analysis imprecision: global variable mouseIsDown is accessed from function mousedown;
	// analysis thinks that function can be executed during module initialisation, since it is registered as an
	// event handler and module initialisation code calls some DOM functions
	@Test public void test_defend_yourself() { test("10k", "defend_yourself.html"); }
	
	@Test public void test_earth_night_lights() { test("10k", "earth_night_lights.html"); }
	
	// cannot analyse: filterrific.html
	
	// not such a great example: tons of global variables, hard to split into modules
	//@Test public void test_flatwar() { test("10k", "flatwar.html"); }
	
	// already modularised: floating_bubbles.html
	
	@Test public void test_fractal_landscape() { test("10k", "fractal_landscape.html"); }
	// already modularised: gravity.html
	// already modularised: heatmap.html
	
	// not such a great example: uses minified names
	//@Test public void test_last_man_standing() { test("10k", "last_man_standing.html"); }
	
	// already modularised: lines.html
	
	// TODO: minesweeper.html
	
	// cannot analyse: nbody.html
	
	// TODO: rgb_color_wheel.html
	
	// already modularised: sinuous_wheel.html
	
	@Test public void test_snowpar() { test("10k", "snowpar.html");  } // this benchmark contains annotations for modularisation
	
	// already modularised: stairs_to_heaven.html
	
	// cannot analyse: sudoku.html
	
	// TODO: tictactoe.html
	
	@Test public void test_zmeyko() { test("10k", "zmeyko.html"); }
	
	// already modularised: 3ddemo.html
	
	// already modularised: anotherworld.html
	
	// unsoundness: apophis.html
	
	@Test public void test_aquarium() { test("chrome", "aquarium.html"); }
	
	// unsoundness: bingbong.html
	
	@Test public void test_blob() { test("chrome", "blob.html"); }
	
	// unsoundness: bomomo.html

	// already modularised: breathinggalaxies.html
	
	// already modularised: browserball.html
	
	// too small: burncanvas
	
	@Test public void test_catchit() { test("chrome", "catchit.html"); }
	
	// already modularised: core.html
	
	// already modularised: jstouch.html
	
	// already (somewhat) modularised: keylight.html
	
	// already modularised: magnetic.html
	
	// already modularised: orangetunnel.html
	
	@Test public void test_planedeformations() { test("chrome", "planedeformations.html"); }
	
	// already modularised: plasma.html
	
	@Test public void test_raytracer() { test("chrome", "raytracer.html"); } // has annotations
	
	// unsoundness: tetris.html
	
	@Test public void test_benchpress() { test("google", "benchpress.js"); } // has annotations
	
	@Test public void test_richards() { test("google", "richards.js"); }
}
