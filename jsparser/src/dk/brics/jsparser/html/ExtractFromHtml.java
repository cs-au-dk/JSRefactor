package dk.brics.jsparser.html;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.contrib.input.LineNumberElement;
import org.jdom.contrib.input.LineNumberSAXBuilder;
import org.w3c.tidy.Tidy;

/**
 * Extracts JavaScript source from HTML files.
 */
public class ExtractFromHtml {
	
	public static List<JavaScriptSource> extract(File file) throws IOException {
		Tidy t = newTidy();
		try {
//		    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		    StringWriter buffer = new StringWriter();
	        t.parse(new FileReader(file), buffer);
	        LineNumberSAXBuilder b = new LineNumberSAXBuilder();
	        Document doc = b.build(new StringReader(buffer.toString()));
			return extract(doc, file.getName(), file.getParentFile());
		} catch (JDOMException e) {
            throw new RuntimeException(e);
        }
	}
	public static List<JavaScriptSource> extract(Document document, String filename, File basedir) {
		return extract(document.getRootElement(), filename, basedir);
	}
	public static List<JavaScriptSource> extract(Element element, String filename, File basedir) {
		List<JavaScriptSource> list = new ArrayList<JavaScriptSource>();
		visit(element, filename, basedir, list, 2);
		return list;
	}
	
	private static String getTextInsideElement(Element element) {
	    return element.getText();
	}
	
	@SuppressWarnings("unchecked")
	private static int visit(Element element, String filename, File basedir, List<JavaScriptSource> list, int lineOffset) {
		int lineNr = ((LineNumberElement)element).getStartLine() - lineOffset;
	    
		// TODO handle framesets??
		// TODO more elaborate html model
		if (element.getName().equalsIgnoreCase("script")) {
			if (element.getAttributeValue("src") == null) {
			    lineOffset += 2; // //<[CDATA[, and //]]>
				list.add(new JavaScriptSource(filename, lineNr-1, 0, getTextInsideElement(element), JavaScriptSource.Kind.SCRIPT));
			} else {
	            lineOffset += 3; // probably empty, so three line-breaks usually get inserted
			    String srcname = element.getAttributeValue("src");
				list.add(new JavaScriptSource(srcname, 0, 0, loadFromURL(srcname, basedir), JavaScriptSource.Kind.SCRIPT));
			}
		}
		
		else if (element.getName().equalsIgnoreCase("style")) {
		    lineOffset += 2; // /*<[CDATA[[*/, and /*]]>/*
		}
		
		for (String attr : EVENT_ATTRIBUTES) {
			if (element.getAttributeValue(attr) != null) {
				String val = element.getAttributeValue(attr);
				list.add(new JavaScriptSource(filename, lineNr, 0, val, JavaScriptSource.Kind.EVENT_ATTRIBUTE));
			}
		}
		
		if (element.getName().equalsIgnoreCase("a")) {
			if (element.getAttributeValue("href") != null) {
				String href = element.getAttributeValue("href");
				href = href.trim();
				if (href.toLowerCase().startsWith("javascript:")) {
					String code = href.substring("javascript:".length());
					list.add(new JavaScriptSource(filename, lineNr, 0, code, JavaScriptSource.Kind.LINK_HREF));
				}
			}
		}
		
		for (Object child : element.getChildren())  {
		    if (child instanceof Element) {
		        lineOffset = visit((Element)child, filename, basedir, list, lineOffset);
		    }
		}
		return lineOffset;
	}
	
	private static String loadFromURL(String url, File basedir) {
		try {
			StringBuilder contents = new StringBuilder();
			BufferedReader reader = new BufferedReader(new FileReader(new File(basedir, url)));
			try {
				for (String line = reader.readLine(); line!=null; line = reader.readLine()) {
					contents.append(line).append("\n");
				}
			} finally {
				reader.close();
			}
			return contents.toString();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	
	private static final String[] EVENT_ATTRIBUTES = new String[] {
		"onload", "onunload",
		"onblur", "onchange", "onfocus", "onreset", "onselect", "onsubmit",
		"onabort",
		"onkeydown","onkeypress","onkeyup",
		"onclick","ondblclick","onmousedown","onmousemove","onmouseout","onmouseover","onmouseup",
		"onresize"
	};
	
	/**
     * Configures a new JTidy instance.
     */
    private static Tidy newTidy() {
    	// copied from TAJS
        Tidy tidy = new Tidy();
        tidy.setDropEmptyParas(false);
        tidy.setDropFontTags(false);
        tidy.setDropProprietaryAttributes(false);
        tidy.setTrimEmptyElements(false);
        tidy.setXHTML(true);
        tidy.setIndentAttributes(false);
        tidy.setIndentCdata(false);
        tidy.setIndentContent(false);
        tidy.setQuiet(true);
        tidy.setShowWarnings(false);
        tidy.setShowErrors(0);
        tidy.setEncloseBlockText(false);
        tidy.setEscapeCdata(false);
        tidy.setDocType("omit");
        Properties prop = new Properties();
        prop.put("new-blocklevel-tags", "canvas");
        tidy.getConfiguration().addProps(prop);
        return tidy;
    }
}
