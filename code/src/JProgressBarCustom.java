package com.github.dnsev.videncode;

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;



public class JProgressBarCustom extends JComponent {
	private static final long serialVersionUID = 1L;

	public static final int COLOR_STATE_NORMAL = 0;
	public static final int COLOR_STATE_DISABLED = 1;

	public class Colors {
		public Color[] borderLight = new Color[]{ Color.white , Color.white };
		public Color[] borderDark = new Color[]{ Color.darkGray , Color.darkGray };
		public Color[] background = new Color[]{ Color.gray , Color.gray };
		public Color[] backgroundLoaded = new Color[]{ Color.lightGray , Color.lightGray };
		public Color[] text = new Color[]{ Color.black , Color.black };
	}


	private Colors colors = new Colors();

	private double position = 0.0;

	private boolean disabled = false;

	private Dimension preferredSize = new Dimension(256, 24);

	private Font font = null;

	private EventListenerList listeners = new EventListenerList();

	private String text = "";


	public JProgressBarCustom() {
		super();

		this.repaint();
	}

	@Override
	public void setFont(Font font) {
		this.font = font;
		this.repaint();
	}
	public void setText(String text) {
		this.text = text;
		this.repaint();
	}

	public double getPosition() {
		return this.position;
	}

	public void setPosition(double position) {
		if (position < 0.0) position = 0.0;
		if (position > 1.0) position = 1.0;

		this.position = position;

		this.repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		int c = (this.disabled ? COLOR_STATE_DISABLED : COLOR_STATE_NORMAL);

		// Regions
		Rectangle rect = this.getRegionBounds();
		int w = (int) (rect.width * this.position);

		// Backgrounds
		g.setColor(this.colors.background[c]);
		g.fillRect(rect.x, rect.y, rect.width, rect.height);

		g.setColor(this.colors.backgroundLoaded[c]);
		g.fillRect(rect.x, rect.y, w, rect.height);

		// Outer border
		g.setColor(this.colors.borderLight[c]);
		g.drawLine(rect.x, rect.y, rect.x + rect.width - 1, rect.y);
		g.drawLine(rect.x, rect.y, rect.x, rect.y + rect.height - 1);
		g.setColor(this.colors.borderDark[c]);
		g.drawLine(rect.x, rect.y + rect.height - 1, rect.x + rect.width - 1, rect.y + rect.height - 1);
		g.drawLine(rect.x + rect.width - 1, rect.y, rect.x + rect.width - 1, rect.y + rect.height - 1);

		// Text
		g.setColor(this.colors.text[c]);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setFont(this.font);
		int fontWidth = g.getFontMetrics().stringWidth(this.text);
		int fontHeight = g.getFontMetrics().getDescent() - g.getFontMetrics().getAscent();
		g2d.drawString(this.text, rect.x + (rect.width - fontWidth) / 2, rect.y + (rect.height - fontHeight) / 2);
	}

	private Rectangle getRegionBounds() {
		Dimension sz = this.getPreferredSize();
		Insets insets = this.getInsets();
		return new Rectangle(insets.left, insets.top, sz.width - insets.left - insets.right, sz.height - insets.top - insets.bottom);
	}

	@Override
	public Dimension getPreferredSize() {
		return this.preferredSize;//new Dimension(this.getParent().getSize().width, this.preferredSize.height);
	}
	@Override
	public void setPreferredSize(Dimension preferredSize) {
		this.preferredSize = preferredSize;
	}

	@Override
	public String getToolTipText(MouseEvent event) {
		return "";
	}

	public Colors getColors() {
		return this.colors;
	}


    public void addChangeListener(ChangeListener l) {
        this.listeners.add(ChangeListener.class, l);
    }
    public void removeChangeListener(ChangeListener l) {
        this.listeners.remove(ChangeListener.class, l);
    }
	private void signalChange(int type) {
		ChangeEvent changeEvent = new ChangeEvent(this);
		Object[] objs = listeners.getListenerList();
		for (int i = objs.length - 2; i >= 0; i -= 2) {
			if (objs[i] == ChangeListener.class) {
				((ChangeListener) objs[i + 1]).stateChanged(changeEvent);
			}
		}
	}
}
