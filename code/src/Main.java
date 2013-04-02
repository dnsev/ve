package com.github.dnsev.videncode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

import java.util.Locale;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;



public class Main {
	private static File appDir = new File("");
	private static String settingsFilename = "settings.json";
	private static String version = "0";
	private static char decimal = '.';
	private static char comma = ',';

	// Main
	public static void main(String[] args) {
		// Locale
		Locale.setDefault(Locale.ENGLISH); // Force English; gets rid of some obnoxious number formatting issues
		try {
			DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance();
			DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
			Main.decimal = symbols.getDecimalSeparator();
			Main.comma = symbols.getGroupingSeparator();
		}
		catch (Exception e) {}

		// Get app dir
		String appDir = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		if (appDir.toLowerCase().endsWith(".exe") || appDir.toLowerCase().endsWith(".jar")) {
			appDir = new File(appDir).getParent();
		}
		else {
			appDir += "/" + Main.class.getName().replace('.', '/');
			appDir = new File(appDir).getParent();
		}
		Main.appDir = new File(appDir);

		// Get the version
		String v = Main.loadVersion("version");
		if (v != null) Main.version = v;

		// Get the settings file
		JSON.Node node = Main.loadSettings(Main.settingsFilename);

		// Get video data
		Videncode ve = new Videncode(node);
		GUI.init(ve, node);
	}

	public static final File getAppDir() {
		return Main.appDir;
	}

	private static final JSON.Node loadSettings(final String settingsFilename) {
		File f = new File(Main.appDir, settingsFilename);
		InputStream stream = null;
		try {
			stream = new FileInputStream(f);
		}
		catch (Exception e) {
			stream = null;
		}
		if (stream == null) {
			stream = Main.class.getResourceAsStream("/" + settingsFilename);
		}

		if (stream != null) {
			int length;
			byte[] buffer = new byte[256];
			StringBuilder sb = new StringBuilder();
			try {
				while ((length = stream.read(buffer)) >= 0) {
					sb.append(new String(buffer, 0, length));
				}
			}
			catch (Exception e) {
			}

			// Close
			try {
				stream.close();
			}
			catch (Exception e) {
			}

			// Parse settings
			JSON.Node node = null;
			try {
				node = JSON.parse(sb.toString());
			}
			catch (JSON.Exception e) {
				node = null;
			}

			return node;
		}
		return null;
	}
	public static final void saveSettings(JSON.Node node) {
		if (node == null) return;

		try {
			// Open
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(Main.getAppDir(), Main.settingsFilename)));

			// Write
			String settings = JSON.toString(node, new StringBuilder(), "    ", " ", " ", new boolean[]{true,false});
			byte[] bytes = settings.getBytes("UTF-8");
			out.write(bytes);

			// Close
			out.close();
		}
		catch (IOException e) {
		}
	}

	private static final String loadVersion(final String versionFilename) {
		InputStream stream = Main.class.getResourceAsStream("/" + versionFilename);

		if (stream != null) {
			int length;
			byte[] buffer = new byte[256];
			StringBuilder sb = new StringBuilder();
			try {
				while ((length = stream.read(buffer)) >= 0) {
					sb.append(new String(buffer, 0, length));
				}
			}
			catch (Exception e) {
			}

			// Close
			try {
				stream.close();
			}
			catch (Exception e) {
			}

			// Parse settings
			return sb.toString();
		}
		return null;
	}

	public static final String getVersion() {
		return Main.version;
	}

	public static final char getComma() {
		return Main.comma;
	}
	public static final char getDecimal() {
		return Main.decimal;
	}

}

