package kl.libs.lightweightxml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

public class Text implements XMLItem {
	protected String text;

	public String getText() {
		return translateHtmlCharCodes(text);
	}

	public void setText(String text) {
		this.text = text;
	}

	public Text(String s) {
		setText(s);
	}

	public String toString() {
		return text.replace('\r', ' ').replace('\n', ' ');
	}

	private static HashMap<String, String> table = new HashMap<String, String>();
	static {
		table.put("auml", "Ã¤");
		table.put("Auml", "Ã„");
		table.put("ouml", "Ã¶");
		table.put("Ouml", "Ã–");
		table.put("uuml", "Ã¼");
		table.put("Uuml", "Ãœ");
		table.put("amp", "&");
		table.put("lt", "<");
		table.put("gt", ">");
		table.put("quot", "\"");
		table.put("szlig", "ÃŸ");
		table.put("lt", "<");
		table.put("gt", ">");;
		table.put("quot", "\"");
		table.put("agrave", "Ã ");
		table.put("Agrave", "Ã€");
		table.put("acirc", "Ã¢");
		table.put("auml", "Ã¤");
		table.put("Auml", "Ã„");
		table.put("Acirc", "Ã‚");
		table.put("aring", "Ã¥");
		table.put("Aring", "Ã…");
		table.put("aelig", "Ã¦");
		table.put("AElig", "Ã†");
		table.put("ccedil", "Ã§");
		table.put("Ccedil", "Ã‡");
		table.put("eacute", "Ã©");
		table.put("Eacute", "Ã‰");
		table.put("egrave", "Ã¨");
		table.put("Egrave", "Ãˆ");
		table.put("ecirc", "Ãª");
		table.put("Ecirc", "ÃŠ");
		table.put("euml", "Ã«");
		table.put("Euml", "Ã‹");
		table.put("iuml", "Ã¯");
		table.put("Iuml", "Ã�");
		table.put("igrave", "Ã¬");
		table.put("Igrave", "ÃŒ");
		table.put("ocirc", "Ã´");
		table.put("Ocirc", "Ã”");
		table.put("ouml", "Ã¶");
		table.put("Ouml", "Ã–");
		table.put("oslash", "Ã¸");
		table.put("Oslash", "Ã˜");
		table.put("szlig", "ÃŸ");
		table.put("ugrave", "Ã¹");
		table.put("Ugrave", "Ã™");
		table.put("ucirc", "Ã»");
		table.put("Ucirc", "Ã›");
		table.put("uuml", "Ã¼");
		table.put("Uuml", "Ãœ");
		table.put("nbsp", " ");
		table.put("copy", "\u00a9");
		table.put("reg", "\u00ae");
		table.put("euro", "\u20a0");
		table.put("iacute", "Ã­");
		table.put("yacute", "Ã½");
		table.put("aacute", "Ã¡");
		table.put("uacute", "Ãº");
		table.put("eacute", "Ã©");
		table.put("oacute", "Ã³");
		table.put("Iacute", "Ã�");
		table.put("Yacute", "Ã�");
		table.put("Aacute", "Ã�");
		table.put("Uacute", "Ãš");
		table.put("Eacute", "Ã‰");
		table.put("Oacute", "Ã“");
		table.put("raquo", "Â»");
		table.put("laquo", "Â«");

	}

	public static String translateHtmlCharCodes(String s) {
		if (s == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer(s.length());
		StringBuffer help = new StringBuffer();
		int status = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (status == 0) {
				if (c == '&') {
					help = new StringBuffer();
					status = 1;
				} else {
					sb.append(c);
				}
			} else if (status == 1) {
				if (c == ';') {
					String code = help.toString();
					if (code.startsWith("#")) {
						try {
							int nr = Integer.parseInt(code.substring(1));
							sb.append((char) nr);
						} catch (Exception e) {
							sb.append('&');
							sb.append(help.toString());
							sb.append(';');
						}
					} else {
						String tableentry = (String) table.get(code);
						if (tableentry != null) {
							sb.append(tableentry);
						} else {
							sb.append("&" + code + ";");
						}
					}
					status = 0;
				} else {
					help.append(c);
					if (help.length() > 8) {
						sb.append('&');
						sb.append(help.toString());
						status = 0;
					}
				}
			}
		}
		return sb.toString();
	}

	public void writeToStream(OutputStream out) throws IOException {
		if (this.text != null) {
			out.write(this.text.getBytes("UTF-8"));
		}
	}

}