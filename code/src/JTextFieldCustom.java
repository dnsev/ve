package com.github.dnsev.videncode;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class JTextFieldCustom extends JTextField {
	private static final long serialVersionUID = 1L;

	private String textLast = "";
	protected EventListenerList listeners = new EventListenerList();

	public JTextFieldCustom() {
		final JTextFieldCustom self = this;
		this.addActionListener(new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				self.signalCustomChange();
			}
		});
		this.addFocusListener(new FocusListener(){
			@Override
			public final void focusGained(FocusEvent event) {
			}
			@Override
			public final void focusLost(FocusEvent event) {
				self.signalCustomChange();
			}
		});
	}

	@Override
	public void setText(String text) {
		super.setText(text);
		this.textLast = text;
	}


	public void addCustomChangeListener(ChangeListener l) {
		this.listeners.add(ChangeListener.class, l);
	}
	public void removeCustomChangeListener(ChangeListener l) {
		this.listeners.remove(ChangeListener.class, l);
	}
	private void signalCustomChange() {
		if (this.getText().equals(this.textLast)) return;
		this.textLast = this.getText();

		ChangeEvent changeEvent = new ChangeEvent(this);
		Object[] objs = listeners.getListenerList();
		for (int i = objs.length - 2; i >= 0; i -= 2) {
			if (objs[i] == ChangeListener.class) {
				((ChangeListener) objs[i + 1]).stateChanged(changeEvent);
			}
		}
	}

};


