package com.github.dnsev.videncode;

import java.awt.*;

import javax.swing.JComponent;



public class JLine extends JComponent {
	private static final long serialVersionUID = 1L;

	public class Colors {
		public Color borderLight = Color.white;
		public Color borderDark = Color.darkGray;
	}

	private Colors colors = new Colors();
	private Dimension preferredSize = new Dimension(256, 24);


	public JLine() {
		super();
	}


	protected void paintComponent(Graphics g) {
		// Regions
		Rectangle rect = this.getRegionBounds();

		// Outer border
		g.setColor(this.colors.borderDark);
		g.drawLine(rect.x, rect.y + (rect.height) / 2, rect.x + rect.width - 1, rect.y + (rect.height) / 2);
		g.setColor(this.colors.borderLight);
		g.drawLine(rect.x, rect.y + (rect.height) / 2 + 1, rect.x + rect.width - 1, rect.y + (rect.height) / 2 + 1);
	}

	private Rectangle getRegionBounds() {
		Dimension sz = this.getPreferredSize();
		Insets insets = this.getInsets();
		return new Rectangle(insets.left, insets.top, sz.width - insets.left - insets.right, sz.height - insets.top - insets.bottom);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(this.getParent().getSize().width, this.preferredSize.height);
	}
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(this.preferredSize.height, this.preferredSize.height);
	}
	@Override
	public void setPreferredSize(Dimension preferredSize) {
		this.preferredSize = preferredSize;
	}

	public Colors getColors() {
		return this.colors;
	}

}
