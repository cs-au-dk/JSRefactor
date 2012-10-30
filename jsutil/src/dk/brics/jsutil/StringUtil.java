package dk.brics.jsutil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StringUtil {
    public static String escape(String s) {
        StringBuilder b = new StringBuilder();
        for (int i=0; i<s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isISOControl(c) || c=='\r' || c=='\n' || c=='\t') {
                b.append("\\u" + (int)c);
            } else {
                b.append(c);
            }
        }
        return b.toString();
    }
    public static String removePathInfo(String filename) {
        return new File(filename).getName();
    }
    public static String implode(Iterable<String> it, String delim) {
        StringBuilder b = new StringBuilder();
        boolean first = true;
        for (String s : it) {
            if (first) {
                first = false;
            } else {
                b.append(delim);
            }
            b.append(s);
        }
        return b.toString();
    }
    public static String escapeHtml(String s) {
      StringBuilder b = new StringBuilder();
      for (int i=0; i<s.length(); i++) {
          char c = s.charAt(i);
          switch (c) {
          case '<':
            b.append("&lt;");
            break;
          case '>':
            b.append("&gt;");
            break;
          case '&':
            b.append("&amp;");
            break;
          default:
            b.append(c);
          }
      }
      return b.toString();
    }
    
    public static List<String> getLines(String str) {
      List<String> list = new ArrayList<String>();
      StringBuilder b = new StringBuilder();
      boolean lastWasR = false;
      for (int i=0; i<str.length(); i++) {
        char c = str.charAt(i);
        switch (c) {
        case '\r':
          lastWasR = true;
          list.add(b.toString());
          b.setLength(0);
          break;
        case '\n':
          if (lastWasR) {
            lastWasR = false;
          } else {
            list.add(b.toString());
            b.setLength(0);
          }
          break;
        default:
          b.append(c);
          lastWasR = false;
        }
      }
      list.add(b.toString());
      return list;
    }
}
