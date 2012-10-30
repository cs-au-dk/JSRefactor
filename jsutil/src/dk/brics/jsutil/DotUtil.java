package dk.brics.jsutil;

/**
 * Static methods that are helpful when generating files in Graphviz dot format.
 */
public class DotUtil {
    /**
     * Escapes a string so it can be used as a label in Graphviz dot.
     */
    public static String escapeLabel(String s) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
            case '"':
                b.append("\\\"");
                break;
            case '\\':
                b.append("\\\\");
                break;
            case '\b':
                b.append("\\b");
                break;
            case '\t':
                b.append("\\t");
                break;
            case '\n':
                b.append("\\n");
                break;
            case '\r':
                b.append("\\r");
                break;
            case '\f':
                b.append("\\f");
                break;
            case '<':
                b.append("\\<");
                break;
            case '>':
                b.append("\\>");
                break;
            case '{':
                b.append("\\{");
                break;
            case '}':
                b.append("\\}");
                break;
            default:
                if (c >= 0x20 && c <= 0x7e) {
                    b.append(c);
                } else {
                    b.append("\\u");
                    String t = Integer.toHexString(c & 0xffff);
                    for (int j = 0; j + t.length() < 4; j++) {
                        b.append('0');
                    }
                    b.append(t);
                }
            }
        }
        return b.toString();
    }
}
