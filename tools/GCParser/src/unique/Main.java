package unique;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Reads lines from the standard-input and write to the standard-output. It collects hashes of all written lines in an external file. 
 * It outputs each line only once.
 * 
 * This program is used to read log-files into logstash and to prevent from adding duplicates.
 * 
 * @author lochmann
 *
 */
public class Main {

	/**
	 */
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
		System.err.println("Usage: <ignorefile>");

		File ignorefile = new File(args[0]);
		HashSet<Fingerprint> fingerprints = null;
		if (ignorefile != null) {
			try {
				ObjectInputStream oin = new ObjectInputStream(new GZIPInputStream(new FileInputStream(ignorefile)));
				try {
					Object o = oin.readObject();
					fingerprints = (HashSet<Fingerprint>) o;
				} finally {
					oin.close();
				}
			} catch (Exception e) {
				fingerprints = new HashSet<>();
			}
		}

		System.err.println("Loaded " + fingerprints.size() + " fingerprints from " + ignorefile.length() + " bytes.");

		int lines = 0;
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		String line;
		while ((line = r.readLine()) != null) {

			Fingerprint hash = hashIt(line);

			if (!fingerprints.contains(hash)) {
				System.out.println(line);
				lines++;
			}
			fingerprints.add(hash);
		}

		if (ignorefile != null) {
			try {
				ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(ignorefile)));
				try {
					out.writeObject(fingerprints);
				} finally {
					out.close();
				}
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}

		System.exit(lines);
	}

	private static Fingerprint hashIt(String line) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(line.getBytes("UTF-8"));
		byte[] digest = md.digest();
		return new Fingerprint(digest);
	}

}
