package com.github.dnsev.videncode;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;



public final class JTextFieldMaxLength extends PlainDocument {
	private static final long serialVersionUID = 1L;

	private int limit = 1;

	public JTextFieldMaxLength(int limit) {
		super();
		this.limit = limit;
	}

	public final void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
		if (str == null) return;

		if ((getLength() + str.length()) <= this.limit) {
			super.insertString(offset, str, attr);
		}
	}
}


