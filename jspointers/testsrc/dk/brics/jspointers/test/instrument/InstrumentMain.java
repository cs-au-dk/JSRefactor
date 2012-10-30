package dk.brics.jspointers.test.instrument;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import dk.brics.jsparser.AstUtil;
import dk.brics.jspointers.Main;
import dk.brics.jspointers.test.TestUtil;


public class InstrumentMain {
	public static void main(String[] args) throws Exception {
		File file = new File(args[0]);
		final Main main = new Main(false, file);
		
		InstrumentData data = new JsPointersInstrumentData(main);
		
		new Instrumenter(data).instrument();
		
		File out = new File("output/" + file.getName() + "-out/instrumented/" + file.getName());
		out.getParentFile().mkdirs();
		String code = AstUtil.toSourceString(main.getUserFiles().get(0).getAst());
		Writer writer = new BufferedWriter(new FileWriter(out));
		try {
			writer.write(TestUtil.readResource("dk/brics/jspointers/test/instrument/instrumentlib.js"));
			writer.write(code);
		} finally {
			writer.close();
		}
		
		System.out.println("Done");
	}
}
