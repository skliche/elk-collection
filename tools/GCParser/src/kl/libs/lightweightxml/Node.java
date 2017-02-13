package kl.libs.lightweightxml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Node extends Document {

	protected String tagName;
	protected HashMap<String, String> attributes;

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public Node(String name) {
		this(name, new HashMap<String, String>());
	}

	public Node(String name, HashMap<String, String> attributes) {
		this.tagName = name;
		this.attributes = attributes;
	}

	public String getAttribute(String attributeName) {
		return this.attributes.get(attributeName);
	}

	public void setAttribute(String attributeName, String attributeValue) {
		this.attributes.put(attributeName, attributeValue);
	}

	public String getAttribute(String attributeName, String defaultValue) {
		String s = this.attributes.get(attributeName);
		if (s == null) {
			s = defaultValue;
		}
		return s;
	}

	public String getAttributesAsString() {
		StringBuffer sb = new StringBuffer();
		Iterator<Map.Entry<String, String>> i = this.attributes.entrySet()
				.iterator();
		while (i.hasNext()) {
			Map.Entry<String, String> e = i.next();
			sb.append('@');
			sb.append(e.getKey());
			sb.append("=\"");
			sb.append(e.getValue());
			sb.append("\"");
		}
		return sb.toString();
	}

	public String toString() {
		return "\r\n<" + tagName + getAttributesAsString() + ">"
				+ children.toString() + "</" + tagName + ">";
	}

	private String getAttributesForXML() {
		StringBuffer sb = new StringBuffer();
		Iterator<Map.Entry<String, String>> i = this.attributes.entrySet()
				.iterator();
		while (i.hasNext()) {
			Map.Entry<String, String> e = i.next();
			sb.append(' ');
			sb.append(e.getKey());
			String v = e.getValue();
			if (v.contains("\"")) {
				sb.append("='");
				sb.append(e.getValue());
				sb.append("'");
			} else {
				sb.append("=\"");
				sb.append(e.getValue());
				sb.append("\"");
			}
		}
		return sb.toString();
	}

	public String toXML() {
		StringBuffer result = new StringBuffer();
		result.append("\r\n<" + tagName + getAttributesForXML() + ">");
		//result.append("\r\n<" + tagName  + ">");
		for (XMLItem child : children) {
			if (child instanceof Text) {
				result.append(((Text) child).getText());
			} else {
				result.append(((Node) child).toXML());
			}
		}
		result.append("</" + tagName + ">");
		return result.toString();
	}

	public String toHTML() {
		StringBuffer result = new StringBuffer();

		if (children.size() == 0) {
			return "<" + tagName + getAttributesForXML() + "/>";
		}

		result.append("\r\n<" + tagName + getAttributesForXML() + ">");
		for (XMLItem child : children) {
			if (child instanceof Text) {
				result.append(((Text) child).getText());
			} else {
				result.append(((Node) child).toHTML());
			}
		}
		result.append("</" + tagName + ">");
		return result.toString();
	}

	public String getText() {
		if (children.size() == 1 && children.get(0) instanceof Text) {
			return ((Text) children.get(0)).getText();
		} else {
			StringBuffer sb = new StringBuffer();
			Iterator<XMLItem> i = children.iterator();
			while (i.hasNext()) {
				XMLItem o = i.next();
				if (o instanceof Text) {
					sb.append(((Text) o).getText());
				}
			}
			return sb.toString();
		}
	}

	public String getConcatenatedText() {
		StringBuffer sb = new StringBuffer();
		Iterator<XMLItem> i = children.iterator();
		while (i.hasNext()) {
			XMLItem o = i.next();
			if (o instanceof Text) {
				sb.append(((Text) o).getText());
				sb.append(' ');
			} else {
				sb.append(((Node) o).getConcatenatedText());
			}
		}
		return sb.toString();

	}
	
	private void addChildren(Node n, List<Node> list) {
		for(XMLItem i: n.getChildren()) {
			if(i instanceof Node) {
				list.add((Node) i);
				addChildren((Node) i, list);			
			}
		}
	}
	
	public List<Node> getTreeIteratorOfNodes() {
		List<Node> elements = new LinkedList<Node>();
		addChildren(this, elements);
		return elements;
	}

	private static int einr = 0;

	private static String nrOfTabs(int i) {
		String s = "";
		for (int j = 0; j < i; j++) {
			s += "\t";
		}
		return s;
	}

	public void writeToStream(OutputStream out) throws IOException {
		StringBuffer s = new StringBuffer();
		s.append(nrOfTabs(einr) + "<" + this.tagName);
		Iterator<Map.Entry<String, String>> i = this.attributes.entrySet()
				.iterator();
		while (i.hasNext()) {
			Map.Entry<String, String> entry = i.next();
			s.append(" " + entry.getKey() + "=\"" + entry.getValue() + "\"");
		}
		s.append(">\r\n");
		out.write(s.toString().getBytes("UTF-8"));
		Iterator<XMLItem> j = this.children.iterator();
		einr++;
		while (j.hasNext()) {
			XMLItem o = j.next();
			if (o instanceof Node) {
				((Node) o).writeToStream(out);
			} else {
				((Text) o).writeToStream(out);
			}
		}
		einr--;
		out.write((nrOfTabs(einr) + "</" + this.tagName + ">")
				.getBytes("UTF-8"));
	}
}
