package unique;

import java.io.Serializable;
import java.util.Arrays;

public class Fingerprint implements Serializable {

	private static final long serialVersionUID = -6763021672644625192L;

	private final byte[] fingerprint;

	public Fingerprint(byte[] fingerprint) {
		this.fingerprint = fingerprint;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(fingerprint);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Fingerprint) {
			Fingerprint other = (Fingerprint) o;
			return Arrays.equals(this.fingerprint, other.fingerprint);
		}
		return false;
	}

}
