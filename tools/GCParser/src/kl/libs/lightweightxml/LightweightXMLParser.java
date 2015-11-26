package kl.libs.lightweightxml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A XML and HTML parser which can handle misformatted files as well as possible.
 * 
 * @author lochmann
 * 
 */
public class LightweightXMLParser {

	/** The verbosity mode of the parser */
	private static boolean verbose = true;

	/** Set the verbosity of the parser */
	public static void setVerboseMode(boolean onoff) {
		verbose = onoff;
	}

	/**
	 * Html-tags with no closing ones
	 */
	public static Set<String> htmlTags = new HashSet<String>();
	static {
		htmlTags.add("br");
		htmlTags.add("img");
		htmlTags.add("input");
		htmlTags.add("p");
		htmlTags.add("li");
		htmlTags.add("meta");
		htmlTags.add("link");
		htmlTags.add("hr");
	}

	/** Parses an XML document given as a byte[] */
	public static Document parseXML(byte[] b, boolean ignoreNamespaces) throws IOException {
		return parseXML(new ByteArrayInputStream(b), ignoreNamespaces);
	}

	/** Parses an XML document from an InputStream */
	public static Document parseXML(InputStream inst, boolean ignoreNamespaces) throws IOException {
		return parseXML(inst, ignoreNamespaces, "US-ASCII");
	}

	/** Parses an HTML document from an InputStream */
	public static Document parseHTML(InputStream inst, boolean ignoreNamespaces, String defaultCharset)
			throws IOException {
		return parseXML(inst, ignoreNamespaces, defaultCharset, htmlTags, null);
	}

	/** Parses an HTML document from an InputStream and handle tags with no closing tags */
	public static Document parseHTML(InputStream inst, boolean ignoreNamespaces, String defaultCharset,
			Set<String> htmlTagsWithNoClosingOne) throws IOException {
		return parseXML(inst, ignoreNamespaces, defaultCharset, htmlTagsWithNoClosingOne, null);
	}

	/** Parses an HTML document and ignores the given set of tags */
	public static Document parseHTML(InputStream inst, boolean ignoreNamespaces, String defaultCharset,
			Set<String> htmlTagsWithNoClosingOne, Set<String> tagsToIgnore) throws IOException {
		return parseXML(inst, ignoreNamespaces, defaultCharset, htmlTagsWithNoClosingOne, tagsToIgnore);
	}

	/** Parses an XML document */
	public static Document parseXML(InputStream inst, boolean ignoreNamespaces, String defaultCharset)
			throws IOException {
		return parseXML(inst, ignoreNamespaces, defaultCharset, null, null);
	}

	/** Parses an XML document an ignores certain tags */
	public static Document parseXML(InputStream inst, boolean ignoreNamespaces, String defaultCharset,
			Set<String> tagsWithNoClosingOne, Set<String> tagsToIgnore) throws IOException {

		CharStream charStream;
		try {
			PushbackInputStream pin = new PushbackInputStream(inst, 5);
			byte[] fileStart = new byte[5];
			int l = pin.read(fileStart);

			String fileStartStr = new String(fileStart, 0, l, "US-ASCII");
			if (fileStartStr.equals("<?xml")) {
				StringBuffer sb = new StringBuffer();
				sb.append(new String(fileStart, "US-ASCII"));
				int i;
				while ((i = pin.read()) >= 0) {
					sb.append((char) i);
					if (i == '>') {
						break;
					}
				}
				String charset = defaultCharset;
				Matcher m = Pattern.compile(".*encoding=\"(.*?)\".*").matcher(sb);
				if (m.matches()) {
					charset = m.group(1);
				}
				charStream = new CharStream(pin, charset);
			} else {
				pin.unread(fileStart);
				charStream = new CharStream(pin, defaultCharset);
			}
		} catch (StringIndexOutOfBoundsException e) {
			e.printStackTrace();
			throw new IOException("No valid XML-File: " + e.getMessage());
		}

		Stack<Document> stack = new Stack<Document>();
		stack.add(new Document());
		boolean slashFound = false;
		int status = 0;
		Text lastText = null;
		StringBuffer sb = new StringBuffer();
		StringBuffer sbAttrName = new StringBuffer();
		HashMap<String, String> attributes = new HashMap<String, String>();
		StringBuffer sbAttrValue = new StringBuffer();
		char attrEndChar = '>';
		int lineNumber = 1;
		boolean ignore = false;
		/*
		 * 0 = Text lesen, auf '>' warten 1 = Tagname lese, auf '>' oder ' ' warten 2 = ' ' lesen, auf AttrName oder '>'
		 * warten 3 = AttrName lese, auf ' ' oder '=' warten, bei '>' aufh�ren 4 = ' ' lesen, auf '=' warten, bei '>'
		 * aufh�ren 5 = auf AttrValue warten, ' ' lesen, bei '>' aufh�ren 6 = AttrValue lesen, auf attrEndChar
		 * warten 7 = AttrValue lesen (html compatibilit�t), auf ' ' warten, bei '>' aufh�ren
		 */
		char c;
		while ((c = charStream.read()) != Character.MAX_VALUE) {
			if (c == '\n') {
				lineNumber++;
			}

			if (status == 0) {
				if (c == '<') {
					if (sb.length() > 0) {
						if (lastText != null) {
							lastText.text += sb.toString();
						} else {
							lastText = new Text(sb.toString());
							stack.peek().children.add(lastText);
						}
					}
					status = 1;
					sb = new StringBuffer();
				} else {
					sb.append(c);
				}
			} else if (status == 1) {
				if (Character.isWhitespace(c) || c == '>') {
					lastText = null;
					attributes = new HashMap<String, String>();
					status = 2;
				} else {
					sb.append(c);
					if (sb.toString().equals("![CDATA[")) {
						if (lastText == null) {
							lastText = new Text("");
							stack.peek().children.add(lastText);
						}
						lastText.text += readCData(charStream);
						status = 0;
						sb = new StringBuffer();
					} else if (sb.toString().equals("!--")) {
						readComment(charStream);
						status = 0;
						sb = new StringBuffer();
					}
				}
			} else if (status == 3) {
				if (Character.isWhitespace(c)) {
					status = 4;
				} else if (c == '=') {
					status = 5;
				} else if (c == '>') {
					status = 2;
				} else {
					sbAttrName.append(c);
				}
			} else if (status == 4) {
				if (c == '=') {
					status = 5;
				} else if (c == '>') {
					status = 2;
				} else if (!Character.isWhitespace(c)) {
					status = 2;
				}
			} else if (status == 5) {
				if (c == '"') {
					sbAttrValue = new StringBuffer();
					attrEndChar = c;
					status = 6;
				} else if (c == '\'') {
					sbAttrValue = new StringBuffer();
					attrEndChar = c;
					status = 6;
				} else if (c == '>') {
					status = 2;
				} else if (!Character.isWhitespace(c)) {
					sbAttrValue = new StringBuffer();
					sbAttrValue.append(c);
					status = 7;
				}
			} else if (status == 6) {
				if (c == attrEndChar) {
					attributes.put(sbAttrName.toString(), sbAttrValue.toString());
					status = 2;
					ignore = true;
				} else {
					sbAttrValue.append(c);
				}
			} else if (status == 7) {
				if (c <= ' ' || c == '>') {
					attributes.put(sbAttrName.toString(), sbAttrValue.toString());
					status = 2;
					if (c <= ' ') {
						ignore = true;
					}
				} else {
					sbAttrValue.append(c);
				}
			}
			if (!ignore && status == 2) {
				if (c == '>') {
					String tagName = sb.toString();
					char startChar = 0;
					if (tagName.length() > 0) {
						startChar = tagName.charAt(0);
					}

					String tagNameWithoutSlash = tagName;
					if (startChar == '/') {
						tagNameWithoutSlash = tagNameWithoutSlash.substring(1);
					}
					if (tagsToIgnore != null && tagsToIgnore.contains(tagNameWithoutSlash.toLowerCase())) {
						// ignore this tag completely
					} else if (startChar == '/') {
						Document doc = stack.peek();
						if (doc instanceof Node) {
							Node node = (Node) doc;
							if (node.tagName.equals(processNamespace(
									tagName.toLowerCase().substring(1),
										ignoreNamespaces))) {
								stack.pop();
							} else {
								if (verbose) {
									System.err.println("line " + lineNumber + ": " + tagName + " found. But "
											+ ((Node) stack.peek()).getTagName() + " expected.");
									System.err.print("Stack is: ");

									for (Document sd : stack) {
										if (sd instanceof Node) {
											System.err.print(((Node) sd).getTagName() + "/");
										}
									}
									System.err.println();
								}
								boolean erfolg = false;
								int sts;
								for (sts = stack.size() - 1; sts >= 1; sts--) {
									if (((Node) stack.get(sts)).getTagName().equals(
											processNamespace(tagName.toLowerCase().substring(1), ignoreNamespaces))) {
										erfolg = true;
										break;
									}
								}
								if (erfolg) {
									if (verbose) {
										System.err.println("Self correcting: ");
									}
									int stacks = stack.size();
									for (; sts < stacks; sts++) {
										Node sn = (Node) stack.pop();
										if (verbose) {
											System.err.println("\t removed: " + sn.getTagName());
										}
									}
									if (verbose) {
										System.err.println("Self correction successfull.");
									}
								}

							}
						}
					} else if (Character.isLetter(startChar) || (startChar == '!')) {
						Node node = new Node(processNamespace(sb.toString().toLowerCase(), ignoreNamespaces),
								attributes);

						stack.peek().children.add(node);
						if (!slashFound
								&& (tagsWithNoClosingOne == null || !tagsWithNoClosingOne.contains(node.getTagName()))) {

							stack.push(node);
						}
					}
					status = 0;
					sb = new StringBuffer();
				} else if (!Character.isWhitespace(c)) {
					status = 3;
					sbAttrName = new StringBuffer();
					sbAttrName.append(c);
				}
			}
			slashFound = c == '/';
			ignore = false;
		}
		return stack.firstElement();
	}

	protected static String processNamespace(String s, boolean ignoreNamespaces) {
		if (ignoreNamespaces) {
			int i = s.indexOf(':');
			if (i == -1) {
				return s;
			}
			if (s.length() < i + 1) {
				return s;
			}
			return s.substring(i + 1);
		}
		return s;
	}

	protected static String readCData(CharStream in) throws IOException {
		StringBuffer sb = new StringBuffer();
		int status = 0;
		char c;
		while ((c = in.read()) != Character.MAX_VALUE) {
			if (status == 0) {
				if (c == ']') {
					status = 1;
				} else {
					sb.append(c);
				}
			} else if (status == 1) {
				if (c == ']') {
					status = 2;
				} else {
					status = 0;
					sb.append(']');
					sb.append(c);
				}
			} else if (status == 2) {
				if (c == '>') {
					return sb.toString();
				} else {
					status = 0;
					sb.append(']');
					sb.append(']');
					sb.append(c);
				}
			}
		}
		return sb.toString();
	}

	protected static String readComment(CharStream in) throws IOException {
		StringBuffer sb = new StringBuffer();
		int status = 0;
		char c;
		while ((c = in.read()) != Character.MAX_VALUE) {
			if (status == 0) {
				if (c == '-') {
					status = 1;
				} else {
					sb.append(c);
				}
			} else if (status == 1) {
				if (c == '-') {
					status = 2;
				} else {
					status = 0;
					sb.append('-');
					sb.append(c);
				}
			} else if (status == 2) {
				if (c == '>') {
					return sb.toString();
				} else {
					status = 0;
					sb.append('-');
					sb.append('-');
					sb.append(c);
				}
			}
		}
		return sb.toString();
	}
}
