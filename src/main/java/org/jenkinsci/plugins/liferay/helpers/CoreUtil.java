
package org.jenkinsci.plugins.liferay.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class CoreUtil {

	public static boolean isNullOrEmpty(String error) {

		if (error == null || error.equals(""))
			return true;
		return false;
	}

	public static String readStreamToString(InputStream contents)
		throws IOException {

		if (contents == null) {
			return null;
		}

		final char[] buffer = new char[0x10000];

		StringBuilder out = new StringBuilder();

		Reader in = new InputStreamReader(contents, "UTF-8"); //$NON-NLS-1$

		int read;
		do {
			read = in.read(buffer, 0, buffer.length);
			if (read > 0) {
				out.append(buffer, 0, read);
			}
		}
		while (read >= 0);

		return out.toString();
	}

}
