package dk.brics.jsparser.html;

import java.io.File;
import java.util.List;

import org.jdom.input.SAXBuilder;

public class TestHtml {
	public static void main(String[] args) throws Exception {
		File file = new File(args[0]);
		List<JavaScriptSource> sources = ExtractFromHtml.extract(file);
		for (JavaScriptSource src : sources) {
			System.out.println("Source fragment: " + src.getKind());
			System.out.println(src.getSource());
			System.out.println();
		}
	}
}
