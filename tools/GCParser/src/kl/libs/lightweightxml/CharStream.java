package kl.libs.lightweightxml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Read from an InputStream into a String with a given charset. Buffers the entire content in memory and enabled
 * char-wise reading.
 * 
 * @author lochmann
 * 
 */
public class CharStream {

	private int index = 0;
	private String buffer;

	public char read() {
		if (index >= buffer.length()) {
			return Character.MAX_VALUE;
		}
		return buffer.charAt(index++);
	}

	public CharStream(InputStream in) throws IOException {
		StringBuffer sb = new StringBuffer();
		int i;
		while ((i = in.read()) >= 0) {
			sb.append((char) i);
		}
		this.buffer = sb.toString();
	}

	public CharStream(InputStream in, String charset) throws IOException, UnsupportedEncodingException {
		ByteArrayOutputStream ba = new ByteArrayOutputStream();
		int l;
		byte[] b = new byte[1024];
		while ((l = in.read(b)) > 0) {
			ba.write(b, 0, l);
		}
		this.buffer = new String(ba.toByteArray(), charset);
	}
}
