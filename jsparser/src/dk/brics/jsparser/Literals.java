package dk.brics.jsparser;

import dk.brics.jsparser.node.AIdentifierPropertyName;
import dk.brics.jsparser.node.ANameExp;
import dk.brics.jsparser.node.ANumberPropertyName;
import dk.brics.jsparser.node.APropertyExp;
import dk.brics.jsparser.node.AStringPropertyName;
import dk.brics.jsparser.node.AVarDecl;
import dk.brics.jsparser.node.IFunction;
import dk.brics.jsparser.node.PPropertyName;
import dk.brics.jsparser.node.Token;

/**
 * Contains static methods for parsing number and string literals.
 */
public class Literals {
	public static char getQuoteSymbol(String str) {
		if (str.startsWith("'"))
			return '\'';
		else if (str.startsWith("\""))
			return '"';
		else
			throw new RuntimeException("String is not quoted: " + str);
	}
	
	/**
	 * Returns a string literal (including quotes) that will parse to the
	 * given argument.
	 * @param value any string
	 * @return a JavaScript string literal
	 */
	public static String unparseStringLiteral(String value, char quote) {
		StringBuilder b = new StringBuilder();
		for (int i=0; i<value.length(); i++) {
			char ch = value.charAt(i);
			if (ch == quote) {
				b.append("\\").append(ch);
			}
			else if (ch == '\\') {
				b.append("\\\\");
			}
			else {
				b.append(ch);
			}
		}
		return quote + b.toString() + quote;
	}
	
    /**
     * Parses an identifier by substituting unicode escape sequences with
     * their corresponding unicode characters.
     * @param identifier an identifier from source code
     * @return a string
     */
    public static String parseIdentifier(String identifier) {
        if (!identifier.contains("\\")) {
            return identifier;
        }
        StringBuilder b = new StringBuilder();
        for (int i=0; i<identifier.length(); i++) {
            char ch = identifier.charAt(i);
            if (ch == '\\') {
                if (i == identifier.length()) {
                    throw new IllegalArgumentException("Identifier ended with backslash: " + identifier);
                }
                if (identifier.charAt(i+1) == 'u') {
                    if (i >= identifier.length()-5) {
                        throw new IllegalArgumentException("Incomplete unicode escape sequence in identifier: " + identifier.substring(i));
                    }
                    char hex1 = identifier.charAt(i+2);
                    char hex2 = identifier.charAt(i+3);
                    char hex3 = identifier.charAt(i+4);
                    char hex4 = identifier.charAt(i+5);
                    char unicodeValue = (char)(4096*hexValue(hex1) + 256*hexValue(hex2) + 16*hexValue(hex3) + hexValue(hex4));
                    // TODO: Check that unicodeValue is a valid identifier symbol (and if this is first character then it must not be a digit)
                    b.append(unicodeValue);
                    i += 5;
                } else {
                    throw new IllegalArgumentException("Invalid escape sequence in identifier: " + identifier.substring(i,i+2));
                }
            } else {
                b.append(ch);
            }
        }
        return b.toString();
    }

    /**
     * Returns whether the given string is an octal number literal.
     * Octal number literals are prohibited in strict mode, and 
     * 
     * @param s any string.
     * @return true if the string is an octal literal
     */
    public static boolean isOctalNumberLiteral(String s) {
        return s.startsWith("0") && s.length() > 1;
    }

    /**
     * Converts a string to the JavaScript Number value is represents.
     * @param s decimal, hex, or octal number literal
     * @return a number
     */
    public static double parseNumberLiteral(String s) {
        if (s.startsWith("0x") || s.startsWith("0X")) {
            return Long.parseLong(s.substring(2), 16);
        } else if (s.startsWith("0") && s.length() > 1 && !s.contains(".")) {
            return Long.parseLong(s.substring(1), 8); // parse as octal 
        } else {
            return Double.parseDouble(s);
        }
    }

    /**
     * Equals {@link #parseStringLiteral(String, ErrorTolerance) parseStringLiteral}<tt>(s, FAIL)</tt>
     */
    public static String parseStringLiteral(String s) {
        return parseStringLiteral(s, ErrorTolerance.FAIL);
    }

    /**
     * Converts a string literal into the actual string it represents.
     * This also removes surrounding quotes (and expects them to be there).
     * For example, &lt;<tt>"fo\u006F"</tt>&gt; is converted to &lt;<tt>foo</tt>&gt;
     * (here using &lt;&gt; as delimiters to avoid confusion with quotes).
     * @param s a string literal (including its quotes)
     * @return a string
     */
    public static String parseStringLiteral(String s, ErrorTolerance tolerance) {
        return parseEscapeSequences(unquote(s), tolerance);
    }

    /**
     * Equals {@link #parseEscapeSequences(String, ErrorTolerance) parseEscapeSequences}<tt>(s, FAIL)</tt>.
     */
    public static String parseEscapeSequences(String s) {
        return parseEscapeSequences(s, ErrorTolerance.FAIL);
    }

    /**
     * Converts all occurences of escape sequences to the characters they
     * represent.
     * @param s a string
     * @see #parseStringLiteral(String,ErrorTolerance)
     */
    public static String parseEscapeSequences(String s, ErrorTolerance tolerance) {
        StringBuilder b = new StringBuilder();
        for (int i=0; i<s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
            case '\\':
                i++;
                if (i == s.length()) {
                    throw new IllegalArgumentException("String ended with backslash");
                }
                switch (s.charAt(i)) {
                case '\'':
                    b.append('\'');
                    break;
                case '\"':
                    b.append('"');
                    break;
                case '\\':
                    b.append('\\');
                    break;
                case 'b':
                    b.append('\b');
                    break;
                case 't':
                    b.append('\t');
                    break;
                case 'n':
                    b.append('\n');
                    break;
                case 'v':
                    b.append('\u000B');
                    break;
                case 'f':
                    b.append('\f');
                    break;
                case 'r':
                    b.append('\r');
                    break;
                case '0':
                    b.append('\0');
                    break;
                case 'u': {
                    if (i+4 >= s.length()) {
                        throw new IllegalArgumentException("Incomplete unicode escape sequence");
                    }
                    char hex1 = s.charAt(i+1);
                    char hex2 = s.charAt(i+2);
                    char hex3 = s.charAt(i+3);
                    char hex4 = s.charAt(i+4);
                    char unicodeValue = (char)(4096*hexValue(hex1) + 256*hexValue(hex2) + 16*hexValue(hex3) + hexValue(hex4));
                    b.append(unicodeValue);
                    i += 4;
                    break;
                }
                case 'x': {
                    if (i+2 >= s.length()) {
                        throw new IllegalArgumentException("Incomplete hex escape sequence");
                    }
                    char hex1 = s.charAt(i+1);
                    char hex2 = s.charAt(i+2);
                    char hexValue = (char)(16*hexValue(hex1) + hexValue(hex2));
                    b.append(hexValue);
                    i += 2;
                    break;
                }
                case '\n': // line continuation
                    break;
                case '\r': // line continuation
                    if (i+1 < s.length() && s.charAt(i+1) == '\n') {
                        i++; // skip whole line-break
                    }
                    break;
                default:
                    if (tolerance == ErrorTolerance.FAIL) {
                        throw new IllegalArgumentException("Invalid start of escape sequence: \\" + s.charAt(i));
                    } else {
                        b.append(ch); // just append the character to the string (ie. \q becomes q)
                    }
                }
                break;
            default:
                b.append(ch);
            }
        }
        return b.toString();
    }

    /**
     * Returns the hexadecimal numeric value of the given character.
     * Eg. A has hexadecimal numeric value 10. Works for both uppercase and lowercase characters.
     * @param ch a character between 0-9, a-f, or A-F
     * @return number between 0 and 15
     * @exception IllegalArgumentException if ch is not a hexadecimal character
     */
    public static int hexValue(char ch) {
        if ('0' <= ch && ch <= '9') {
            return ch - '0';
        } else if ('A' <= ch && ch <= 'F') {
            return 10 + ch - 'A';
        } else if ('a' <= ch && ch <= 'f') {
            return 10 + ch - 'a';
        } else {
            throw new IllegalArgumentException("Not a hexadecimal: " + ch);
        }
    }

    /**
     * Removes the quotes surrounding the given string.
     * @param s a quoted string
     * @return the text between the quotes
     * @exception IllegalArgumentException if the string is not surrounded by quotes
     */
    public static String unquote(String s) {
        if (!(s.startsWith("\"") && s.endsWith("\"")) && !(s.startsWith("'") && s.endsWith("'"))) {
            throw new IllegalArgumentException("String is not surrounded by quotes");
        }
        return s.substring(1, s.length()-1);
    }

    /**
     * Convenience wrapper around {@link Literals#parseIdentifier(String)}.
     * 
     * @param exp a name expression
     * @return the name expression's name
     */
	public static String getName(ANameExp exp) {
		return parseIdentifier(exp.getName().getText());
	}

    /**
     * Convenience wrapper around {@link Literals#parseIdentifier(String)}.
     * 
     * @param exp a name expression
     * @return the name expression's name
     */
	public static String getName(APropertyExp exp) {
		return parseIdentifier(exp.getName().getText());
	}

	public static String getName(IFunction fun) {
		Token name = fun.getName();
		return name == null ? null : parseIdentifier(name.getText());
	}

	public static String getName(AVarDecl vd) {
		return parseIdentifier(vd.getName().getText());
	}

	public static String getName(PPropertyName name) {
		switch(name.kindPPropertyName()) {
		case IDENTIFIER:
			return parseIdentifier(((AIdentifierPropertyName)name).getName().getText());
		case NUMBER:
			return ((ANumberPropertyName)name).getNumberLiteral().getText();
		default:
			return parseStringLiteral(((AStringPropertyName)name).getStringLiteral().getText());
		}
	}
}
