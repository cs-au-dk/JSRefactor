package dk.brics.jspointers.test;

import java.io.File;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import dk.brics.jspointers.Main;

public class Test10K {
	
	private void run(String name) {
		try {
			// TODO refactor Main
			new Main(true, new File("test/10k/"+name+".html"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test(timeout=8000)
	public void snake() {
		run("10k_snake");
	}
	@Test(timeout=8000)
	public void world() {
		run("10k_world");
	}
	@Test(timeout=8000)
	public void _3d_maker() {
		run("3d_maker");
	}
	@Test(timeout=8000)
	public void attractor() {
		run("attractor");
	}
	@Test(timeout=8000)
	public void canvas_aquarium() {
		run("canvas_aquarium");
	}
	@Test(timeout=8000)
	public void defend_yourself() {
		run("defend_yourself");
	}
	@Test(timeout=8000)
	public void earth_night_lights() {
		run("earth_night_lights");
	}
	@Test(timeout=8000)
	public void filterrific() {
		run("filterrific");
	}
	@Test(timeout=8000)
	public void flatwar() {
		run("flatwar");
	}
	@Test(timeout=8000)
	public void floating_bubles() {
		run("floating_bubbles");
	}
	@Test(timeout=8000)
	public void fractal_landscape() {
		run("fractal_landscape");
	}
	@Test(timeout=8000)
	public void gravity() {
		run("gravity");
	}
	@Test(timeout=8000)
	public void heatmap() {
		run("heatmap");
	}
	@Test(timeout=8000)
	public void last_man_standing() {
		run("last_man_standing");
	}
	
	@Ignore
	@Test(timeout=8000)
	public void lines() {
		// bad precision and speed due to trivial dynamic access
		// only occurs because the file is compressed,so it is not a big issue
		run("lines");
	}

	@Test(timeout=8000)
	public void minesweeper() {
		run("minesweeper");
	}

	@Test(timeout=8000)
	public void nbody() {
		run("nbody");
	}

	@Test(timeout=8000)
	public void notecards() {
		run("notecards");
	}

	@Test(timeout=8000)
	public void rgb_color_wheel() {
		run("rgb_color_wheel");
	}

	@Test(timeout=8000)
	public void sinuous() {
		run("sinuous");
	}

	@Test(timeout=8000)
	public void snowpar() {
		run("snowpar");
	}

	@Test(timeout=8000)
	public void stairs_to_heaven() {
		run("stairs_to_heaven");
	}

	@Test(timeout=8000)
	public void sudoku() {
		run("sudoku");
	}

	@Test(timeout=8000)
	public void tictactoe() {
		run("tictactoe");
	}

	@Test(timeout=8000)
	public void tictactoe2() {
		run("tictactoe2");
	}

	@Test(timeout=8000)
	public void zmeyko() {
		run("zmeyko");
	}
	
	
}
