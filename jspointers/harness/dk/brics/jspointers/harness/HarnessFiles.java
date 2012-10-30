package dk.brics.jspointers.harness;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HarnessFiles {
	public static final String[] NAMES = new String[] {
		"Array.js",
		"Boolean.js",
		"Date.js",
		"Error.js",
		"Function.js",
		"Global.js",
		"JSON.js",
		"Math.js",
		"Number.js",
		"Object.js",
		"RegExp.js",
		"String.js",
		"DOM.js"
	};
	
	/**
	 * A list with an URL to each harness file.
	 */
	public static List<URL> getHarnessFiles() {
		List<URL> list = new ArrayList<URL>();
		for (String name : NAMES) {
			list.add(HarnessFiles.class.getClassLoader().getResource(HarnessFiles.class.getPackage().getName().replace('.', '/') + '/' + name));
		}
		return list;
	}
	
}
