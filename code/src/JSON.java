package com.github.dnsev.videncode;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;



public final class JSON {
	public static class Node {
		public enum Type {
			STRING,
			INTEGER,
			NUMBER,
			BOOLEAN,
			ARRAY,
			OBJECT,
			NULL,
		}

		private Type type = Type.NULL;
		private Object value = null;

		private Node() {
		}

		public final Type getType() {
			return this.type;
		}
		public final double getDouble() {
			return (this.type == Type.NUMBER) ? ((Double) this.value).doubleValue() : ((this.type == Type.INTEGER) ? ((Long) this.value).longValue() : 0.0);
		}
		public final boolean getBoolean() {
			return (this.type == Type.BOOLEAN) ? ((Boolean) this.value).booleanValue() : false;
		}
		public final long getInteger() {
			return (this.type == Type.NUMBER) ? (long) ((Double) this.value).doubleValue() : ((this.type == Type.INTEGER) ? ((Long) this.value).longValue() : 0);
		}
		public final String getString() {
			return (this.type == Type.STRING) ? (String) this.value : null;
		}
		@SuppressWarnings(value="unchecked")
		public final HashMap<String, JSON.Node> getObject() {
			return (this.type == Type.OBJECT) ? (HashMap<String, JSON.Node>) this.value : null;
		}
		@SuppressWarnings(value="unchecked")
		public final ArrayList<JSON.Node> getArray() {
			return (this.type == Type.ARRAY) ? (ArrayList<JSON.Node>) this.value : null;
		}

		public final Node get(final String key) {
			HashMap<String, JSON.Node> map = this.getObject();
			if (map == null) return null;

			if (map.containsKey(key)) {
				return map.get(key);
			}
			else {
				JSON.Node n = JSON.node();
				map.put(key, n);
				return n;
			}
		}
		public final Node set(final String key, final JSON.Node value) {
			HashMap<String, JSON.Node> map = this.getObject();
			if (map == null) return this;

			map.put(key, value);

			return this;
		}

	}

	public static final class Exception extends java.lang.Exception {
		private Exception(String message) {
			super(message);
		}

		private static final long serialVersionUID = 1L;
	}



	public static JSON.Node parse(String src) throws JSON.Exception {
		// Format check
		if (src.length() == 0 || src.charAt(0) != '{') {
			throw new JSON.Exception("Malformed JSON source");
		}

		// Get the value node
		Node n = new Node();
		int pos = 0;
		try {
			pos = JSON.parseValue(src, pos, n);
		}
		catch (StringIndexOutOfBoundsException e) {
			throw new JSON.Exception("Malformed JSON source");
		}

		// Extra stuff?
		while (pos < src.length() && JSON.isWhitespace(src.charAt(pos))) ++pos;
		if (pos < src.length()) {
			throw new JSON.Exception("Malformed JSON source");
		}

		// Form as object and return
		return n;
	}

	@SuppressWarnings(value="unchecked")
	public static String toString(final JSON.Node obj, StringBuilder tabbing, final String tabbingAdd, final String keySep, final String objSep, final boolean[] newlines) {
		assert(obj != null);

		StringBuilder sb = new StringBuilder();

		switch (obj.type) {
			case STRING:
				sb.append('"');
				sb.append(JSON.escape((String) obj.value));
				sb.append('"');
			break;
			case NUMBER:
				sb.append(((Double) obj.value).doubleValue());
			break;
			case INTEGER:
				sb.append(((Long) obj.value).longValue());
			break;
			case BOOLEAN:
				sb.append(((Boolean) obj.value).booleanValue());
			break;
			case ARRAY:
				sb.append('[');
				if (newlines[1]) sb.append('\n');
				else sb.append(objSep);

				tabbing.append(tabbingAdd);

				for (int i = 0; i < ((ArrayList) obj.value).size(); ++i) {
					if (i > 0) {
						sb.append(',');
						if (newlines[1]) sb.append('\n');
						else sb.append(objSep);
					}

					if (newlines[1]) sb.append(tabbing.toString());
					sb.append(JSON.toString((JSON.Node) ((ArrayList) obj.value).get(i), tabbing, tabbingAdd, keySep, objSep, newlines));
				}

				tabbing.setLength(tabbing.length() - tabbingAdd.length());

				if (newlines[1]) {
					sb.append('\n');
					sb.append(tabbing);
				}
				else sb.append(objSep);
				sb.append(']');
			break;
			case OBJECT:
				sb.append('{');
				if (newlines[0]) sb.append('\n');
				else sb.append(objSep);

				tabbing.append(tabbingAdd);

				boolean first = true;
				Iterator< Map.Entry<String, JSON.Node> > it = ((HashMap<String, JSON.Node>) obj.value).entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, JSON.Node> entry = it.next();

					if (!first) {
						sb.append(',');
						if (newlines[0]) sb.append('\n');
						else sb.append(objSep);
					}

					if (newlines[0]) sb.append(tabbing);
					sb.append('"');
					sb.append(JSON.escape(entry.getKey()));
					sb.append("\":");
					sb.append(keySep);
					sb.append(JSON.toString(entry.getValue(), tabbing, tabbingAdd, keySep, objSep, newlines));

					first = false;
				}

				tabbing.setLength(tabbing.length() - tabbingAdd.length());

				if (newlines[0]) {
					sb.append('\n');
					sb.append(tabbing.toString());
				}
				else sb.append(objSep);
				sb.append('}');
			break;
			case NULL:
				sb.append("null");
			break;
		}

		return sb.toString();
	}
	public static String toString(final JSON.Node obj) {
		return JSON.toString(obj, new StringBuilder(), "    ", " ", " ", new boolean[]{true,true});
	}

	public static String escape(String str) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < str.length(); ++i) {
			switch (str.charAt(i)) {
				case '"':
				case '\\':
					sb.append('\\').append(str.charAt(i));
				break;
				case '\b':
					sb.append("\\b");
				break;
				case '\f':
					sb.append("\\f");
				break;
				case '\n':
					sb.append("\\n");
				break;
				case '\r':
					sb.append("\\r");
				break;
				case '\t':
					sb.append("\\t");
				break;
				default:
					if (str.charAt(i) >= 128) {
						sb.append("\\u");
						int pos = sb.length();
						sb.append(Integer.toHexString((int) str.charAt(i)));
						int len = sb.length() + 4;
						while (sb.length() < len) sb.insert(pos, '0');
					}
					else {
						sb.append(str.charAt(i));
					}
				break;
			}
		}

		// Done
		return sb.toString();
	}

	private static int parseObject(HashMap<String, JSON.Node> map, String src, int pos) throws JSON.Exception {
		assert(map != null);
		assert(src.charAt(pos) == '{');

		// Skip the {
		++pos;

		// Loop over keys
		boolean first = true;
		while (true) {
			// Skip whitespace
			while (pos < src.length() && JSON.isWhitespace(src.charAt(pos))) ++pos;
			if (first && src.charAt(pos) == '}') {
				++pos;
				break;
			}

			// Must be quoted
			if (!JSON.isQuote(src.charAt(pos))) {
				throw new JSON.Exception("Expected key string; found " + src.charAt(pos));
			}
			// Parse key and colon
			StringBuilder key = new StringBuilder();
			pos = JSON.parseQuotedValue(src, pos, key);

			// Find colon
			while (JSON.isWhitespace(src.charAt(pos))) ++pos;
			if (src.charAt(pos) != ':') {
				throw new JSON.Exception("Expected key separator; found " + src.charAt(pos));
			}
			++pos;

			// Value
			Node n = new Node();
			pos = JSON.parseValue(src, pos, n);
			map.put(key.toString(), n);

			// Skip whitespace
			while (JSON.isWhitespace(src.charAt(pos))) ++pos;

			// Delimiter or }
			if (src.charAt(pos) == '}') {
				++pos;
				break;
			}
			else if (src.charAt(pos) != ',') {
				throw new JSON.Exception("Expected object delimiter; found " + src.charAt(pos));
			}
			++pos;

			first = false;
		}

		// Done
		return pos;
	}
	private static int parseArray(ArrayList<JSON.Node> array, String src, int pos) throws JSON.Exception {
		assert(array != null);
		assert(src.charAt(pos) == '[');

		// Skip the {
		++pos;

		// Loop over values
		boolean first = true;
		while (true) {
			if (first && src.charAt(pos) == ']') {
				++pos;
				break;
			}

			// Value
			Node n = new Node();
			pos = JSON.parseValue(src, pos, n);
			array.add(n);

			// Skip whitespace
			while (JSON.isWhitespace(src.charAt(pos))) ++pos;

			// Delimiter or }
			if (src.charAt(pos) == ']') {
				++pos;
				break;
			}
			else if (src.charAt(pos) != ',') {
				throw new JSON.Exception("Expected object delimiter; found " + src.charAt(pos));
			}
			++pos;

			first = false;
		}

		// Done
		return pos;
	}
	private static int parseQuotedValue(String src, int pos, StringBuilder value) throws JSON.Exception {
		assert(JSON.isQuote(src.charAt(pos)));

		char quote = src.charAt(pos++);
		boolean escaped = false;

		while (pos < src.length()) {
			if (escaped) {
				switch (src.charAt(pos)) {
					case '"':
					case '\\':
					case '/':
						value.append(src.charAt(pos));
					break;
					case 'b':
						value.append('\b');
					break;
					case 'f':
						value.append('\f');
					break;
					case 'n':
						value.append('\n');
					break;
					case 'r':
						value.append('\r');
					break;
					case 't':
						value.append('\t');
					break;
					case 'u':
					{
						try {
							value.append((char) Integer.parseInt(src.substring(pos + 1, pos + 5), 16));
						}
						catch (NumberFormatException e) {
							throw new JSON.Exception("Bad unicode sequence");
						}
						catch (IndexOutOfBoundsException e) {
							throw new JSON.Exception("Bad unicode sequence");
						}
						pos += 4;
					}
					break;
					default:
						throw new JSON.Exception("Invalid escape sequence: \\" + src.charAt(pos));
				}
				escaped = false;
			}
			else {
				if (src.charAt(pos) == quote) break;
				if (src.charAt(pos) == '\\') escaped = true;
				else value.append(src.charAt(pos));
			}
			++pos;
		}
		if (++pos > src.length()) {
			throw new JSON.Exception("Unterminated string");
		}

		return pos;
	}
	private static int parseValue(String src, int pos, Node value) throws JSON.Exception {
		// Clear whitespace
		while (JSON.isWhitespace(src.charAt(pos))) ++pos;

		// Parse value
		if (JSON.isQuote(src.charAt(pos))) {
			StringBuilder val = new StringBuilder();
			pos = JSON.parseQuotedValue(src, pos, val);
			value.value = new String(val.toString());
			value.type = Node.Type.STRING;
		}
		else {
			if (src.charAt(pos) == '{') {
				// Object
				HashMap<String, JSON.Node> map = new HashMap<String, JSON.Node>();
				value.value = map;
				value.type = Node.Type.OBJECT;

				// Parse
				pos = JSON.parseObject(map, src, pos);
			}
			else if (src.charAt(pos) == '[') {
				// Array
				ArrayList<JSON.Node> array = new ArrayList<JSON.Node>();
				value.value = array;
				value.type = Node.Type.ARRAY;

				// Parse
				pos = JSON.parseArray(array, src, pos);
			}
			else if (JSON.isAlphabetic(src.charAt(pos))) {
				// true/false/null
				StringBuilder val = new StringBuilder();
				do {
					val.append(src.charAt(pos++));
				}
				while (pos < src.length() && JSON.isAlphabetic(src.charAt(pos)) || JSON.isNumeric(src.charAt(pos)));

				// Validate
				if (val.toString().equals("true")) {
					value.value = new Boolean(true);
					value.type = Node.Type.BOOLEAN;
				}
				else if (val.toString().equals("false")) {
					value.value = new Boolean(false);
					value.type = Node.Type.BOOLEAN;
				}
				else if (val.toString().equals("null")) {
					value.value = null;
					value.type = Node.Type.NULL;
				}
				else {
					throw new JSON.Exception("Invalid literal");
				}
			}
			else {
				// Number
				int start = pos;
				while (pos < src.length() && src.charAt(pos) != ',' && src.charAt(pos) != ']' && src.charAt(pos) != '}') ++pos;

				try {
					double d = Double.parseDouble(src.substring(start, pos));
					if (((long) d) == d) {
						value.value = new Long((long) d);
						value.type = Node.Type.INTEGER;
					}
					else {
						value.value = new Double(d);
						value.type = Node.Type.NUMBER;
					}
				}
				catch (NumberFormatException e) {
					throw new JSON.Exception("Invalid number");
				}
			}
		}

		// done
		return pos;
	}

	private static boolean isQuote(char c) {
		return c == '\'' || c == '"';
	}
	private static boolean isWhitespace(char c) {
		return c <= 32;
	}
	private static boolean isAlphabetic(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'Z' && c <= 'Z') || c == '_';
	}
	private static boolean isNumeric(char c) {
		return (c >= '0' && c <= '9');
	}

	public static final Node node() {
		return JSON.node(new HashMap<String, JSON.Node>());
	}
	public static final Node node(int[] array) {
		if (array == null) return JSON.node((String) null);

		ArrayList<JSON.Node> list = new ArrayList<JSON.Node>();

		for (int i = 0; i < array.length; ++i) {
			list.add(JSON.node(new Long(array[i])));
		}

		return JSON.node(list);
	}
	public static final Node node(long[] array) {
		if (array == null) return JSON.node((String) null);

		ArrayList<JSON.Node> list = new ArrayList<JSON.Node>();

		for (int i = 0; i < array.length; ++i) {
			list.add(JSON.node(new Long(array[i])));
		}

		return JSON.node(list);
	}
	public static final Node node(float[] array) {
		if (array == null) return JSON.node((String) null);

		ArrayList<JSON.Node> list = new ArrayList<JSON.Node>();

		for (int i = 0; i < array.length; ++i) {
			list.add(JSON.node(new Double(array[i])));
		}

		return JSON.node(list);
	}
	public static final Node node(double[] array) {
		if (array == null) return JSON.node((String) null);

		ArrayList<JSON.Node> list = new ArrayList<JSON.Node>();

		for (int i = 0; i < array.length; ++i) {
			list.add(JSON.node(new Double(array[i])));
		}

		return JSON.node(list);
	}
	public static final <T> Node node(T[] array) {
		if (array == null) return JSON.node((String) null);

		ArrayList<JSON.Node> list = new ArrayList<JSON.Node>();

		for (int i = 0; i < array.length; ++i) {
			list.add(JSON.node(array[i]));
		}

		return JSON.node(list);
	}
	public static final <T> Node node(T[] array, JSONObjectTransformer transformer) {
		ArrayList<JSON.Node> list = new ArrayList<JSON.Node>();

		for (int i = 0; i < array.length; ++i) {
			list.add(JSON.node(transformer.transform(array[i])));
		}

		return JSON.node(list);
	}
	public static final Node node(Object value) {
		JSON.Node n = new JSON.Node();

		if (value == null) {
			n.type = JSON.Node.Type.NULL;
			n.value = null;
		}
		else if (value instanceof String) {
			n.type = JSON.Node.Type.STRING;
			n.value = value;
		}
		else if (value instanceof Integer) {
			n.type = JSON.Node.Type.INTEGER;
			n.value = new Long(((Integer) value).intValue());
		}
		else if (value instanceof Long) {
			n.type = JSON.Node.Type.INTEGER;
			n.value = value;
		}
		else if (value instanceof Float) {
			n.type = JSON.Node.Type.NUMBER;
			n.value = new Double(((Float) value).floatValue());
		}
		else if (value instanceof Double) {
			n.type = JSON.Node.Type.NUMBER;
			n.value = value;
		}
		else if (value instanceof Boolean) {
			n.type = JSON.Node.Type.BOOLEAN;
			n.value = value;
		}
		else if (value instanceof ArrayList) {
			n.type = JSON.Node.Type.ARRAY;
			n.value = value;
		}
		else if (value instanceof HashMap) {
			n.type = JSON.Node.Type.OBJECT;
			n.value = value;
		}
		else {
			n.type = JSON.Node.Type.NULL;
			n.value = null;
		}

		return n;
	}

}


