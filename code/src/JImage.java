package com.github.dnsev.videncode;

import java.awt.*;
import java.awt.event.*;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;



public class JImage extends JComponent {
	private static final long serialVersionUID = 1L;

	private Dimension preferredSize = new Dimension(256, 256);

	private Image image = null;

	private Color backgroundColor = Color.black;

	private boolean upscale = false;
	private boolean downscale = false;

	public JImage(Image image) {
		super();

		this.image = image;
	}

	protected void paintComponent(Graphics g) {
		Rectangle rect = this.getRegionBounds();

		g.setColor(this.backgroundColor);
		g.fillRect(rect.x, rect.y, rect.width, rect.height);

		if (this.image != null) {
			int w = this.image.getWidth(null);
			int h = this.image.getHeight(null);

			Rectangle imgRect;
			Rectangle imgCoords;

			// Scaling
			if (w > rect.width || h > rect.height) {
				if (downscale) {
					double scale = rect.height / (double) h;
					double xs = rect.width / (double) w;
					if (xs < scale) scale = xs;
					imgCoords = new Rectangle(0, 0, w, h);
					imgRect = new Rectangle(
						(int) (rect.x + rect.width / 2 - w * scale / 2),
						(int) (rect.y + rect.height / 2 - h * scale / 2),
						(int) (rect.x + rect.width / 2 + w * scale / 2),
						(int) (rect.y + rect.height / 2 + h * scale / 2)
					);
				}
				else {
					imgRect = new Rectangle(
						rect.x + rect.width / 2 - (w + 1) / 2,
						rect.y + rect.height / 2 - (h + 1) / 2,
						rect.x + rect.width / 2 + w / 2,
						rect.y + rect.height / 2 + h / 2
					);
					if (imgRect.x < 0) imgRect.x = 0;
					if (imgRect.width > rect.x + rect.width) imgRect.width = rect.x + rect.width;
					if (imgRect.y < 0) imgRect.y = 0;
					if (imgRect.height > rect.y + rect.height) imgRect.height = rect.y + rect.height;
					imgCoords = new Rectangle(
						(w + 1 - (imgRect.width - imgRect.x)) / 2,
						(h + 1 -(imgRect.height - imgRect.y)) / 2,
						w - (w - (imgRect.width - imgRect.x)) / 2,
						h - (h - (imgRect.height - imgRect.y)) / 2
					);
				}
			}
			else if (upscale) {
				double scale = rect.height / (double) h;
				double xs = rect.width / (double) w;
				if (xs < scale) scale = xs;
				imgCoords = new Rectangle(0, 0, w, h);
				imgRect = new Rectangle(
					(int) (rect.x + rect.width / 2 - w * scale / 2),
					(int) (rect.y + rect.height / 2 - h * scale / 2),
					(int) (rect.x + rect.width / 2 + w * scale / 2),
					(int) (rect.y + rect.height / 2 + h * scale / 2)
				);
			}
			else {
				imgCoords = new Rectangle(0, 0, w, h);
				imgRect = new Rectangle(
					rect.x + rect.width / 2 - (w + 1) / 2,
					rect.y + rect.height / 2 - (h + 1) / 2,
					rect.x + rect.width / 2 + w / 2,
					rect.y + rect.height / 2 + h / 2
				);
			}

			g.drawImage(
				this.image,
				imgRect.x,
				imgRect.y,
				imgRect.width,
				imgRect.height,
				imgCoords.x,
				imgCoords.y,
				imgCoords.width,
				imgCoords.height,
				null
			);
		}
	}

	private Rectangle getRegionBounds() {
		Dimension sz = this.getPreferredSize();
		Insets insets = this.getInsets();
		return new Rectangle(insets.left, insets.top, sz.width - insets.left - insets.right, sz.height - insets.top - insets.bottom);
	}

	@Override
	public Dimension getPreferredSize() {
		//return new Dimension(this.getParent().getSize().width, this.preferredSize.height);
		return new Dimension(this.getParent().getSize().width, this.getParent().getSize().height);
	}
	@Override
	public void setPreferredSize(Dimension preferredSize) {
		this.preferredSize = preferredSize;
	}

	@Override
	public void setBackground(Color c) {
		this.backgroundColor = c;
	}

	public final Image getImage() {
		return this.image;
	}

	public void setImage(Image image) {
		this.image = image;
		this.repaint();
	}
	public void setDownscaling(boolean downscale) {
		this.downscale = downscale;
		this.repaint();
	}
	public void setUpscaling(boolean upscale) {
		this.upscale = upscale;
		this.repaint();
	}
	public void setScaling(boolean upscale, boolean downscale) {
		this.upscale = upscale;
		this.downscale = downscale;
		this.repaint();
	}
}
