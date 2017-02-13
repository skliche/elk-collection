package gclog;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import kl.libs.lightweightxml.Document;
import kl.libs.lightweightxml.LightweightXMLParser;
import kl.libs.lightweightxml.Node;

/** 
 * Read a Gargabe Collection log from the IBM JVM and output a tab-separated file, with each row containing
 * type, duration, total memory, used memory, and free memory.
 * 
 * @author lochmann
 *
 */
public class Main {

	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

	public static void main(String[] args) throws IOException, ParseException {
		String node = "unknown";
		System.err.println("Usage: [-node <nodename>] <inputfile1> <inputfile2> <...>");
		int i = 0;
		while (i < args.length) {
			if (args[i].equals("-node")) {
				i++;
				node = args[i];
			} else {
				processFile(node, args[i]);
			}
			i++;
		}
	}

	private static void processFile(String nodename, String filename) throws IOException, ParseException {
		Document doc = null;
		FileInputStream in = new FileInputStream(filename);
		try {
			doc = LightweightXMLParser.parseXML(in, true);
		} finally {
			in.close();
		}

		List<Node> nodes = doc.evaluateXPath("verbosegc/gc-end");
		for (Node node : nodes) {
			double durationms = Double.parseDouble(node.getAttribute("durationms"));
			Date timestamp = df.parse(node.getAttribute("timestamp"));
			String type = node.getAttribute("type");
			Node meminfo = node.evaluateXPathSingle("mem-info");
			long free = Long.parseLong(meminfo.getAttribute("free"));
			long total = Long.parseLong(meminfo.getAttribute("total"));

			System.out.format(
					"%s\t%s\t%s\t%.0f\t%d\t%d\t%d%n",
						df.format(timestamp),
						nodename,
						type,
						durationms,
						total,
						total - free,
						free);
		}
	}
}
