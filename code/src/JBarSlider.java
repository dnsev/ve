package com.github.dnsev.videncode;

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;



public class JBarSlider extends JComponent implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;

	public class ChangeEvent extends javax.swing.event.ChangeEvent {
		private static final long serialVersionUID = 1L;

		public static final int DRAGGING = 1;
		public static final int DRAG_COMPLETED = 2;
		public static final int CHANGED = 4;
		public static final int CLICKED = 8;
		public static final int JUMPED = 16;

		private int typeFlags;

		private ChangeEvent(JBarSlider o, int typeFlags) {
			super(o);

			this.typeFlags = typeFlags;
		}

		public final int getChangeFlags() {
			return this.typeFlags;
		}
		public final JBarSlider getSlider() {
			return (JBarSlider) this.getSource();
		}
	}

	public static final int COLOR_STATE_NORMAL = 0;
	public static final int COLOR_STATE_HOVER = 1;
	public static final int COLOR_STATE_DEPRESSED = 2;
	public static final int COLOR_STATE_DISABLED = 3;

	public class Colors {
		public Color[] borderLight = new Color[]{ Color.white , Color.white , Color.white , Color.white };
		public Color[] borderDark = new Color[]{ Color.darkGray , Color.darkGray , Color.darkGray , Color.darkGray };
		public Color[] barBorderLight = new Color[]{ Color.white , Color.white , Color.white , Color.white};
		public Color[] barBorderDark = new Color[]{ Color.darkGray , Color.darkGray , Color.darkGray , Color.darkGray };
		public Color[] grabbers = new Color[]{ Color.gray , Color.gray , Color.gray , Color.gray };
		public Color[] background = new Color[]{ Color.lightGray , Color.lightGray , Color.lightGray , Color.lightGray };
	}


	private Colors colors = new Colors();

	private double[] valueRange = new double[]{ 0.0 , 1.0 };
	private double position = 0.0;
	private double snap = 1.0;

	private boolean grabbing = false;
	private int grabSize = 32;
	private int grabOffset = 0;
	private int dragFlags = 0;

	private boolean disabled = false;
	private boolean hovered = false;

	private Dimension preferredSize = new Dimension(256, 24);

	protected EventListenerList listeners = new EventListenerList();



	public JBarSlider(double minimum, double maximum, double position) {
		super();

		if (position < minimum) position = minimum;
		if (position > maximum) position = maximum;

		this.valueRange[0] = minimum;
		this.valueRange[1] = maximum;
		this.position = position;

		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

	public double getPosition() {
		return this.position;
	}
	public double getMinimum() {
		return this.valueRange[0];
	}
	public double getMaximum() {
		return this.valueRange[1];
	}

	public void setPosition(double position) {
		if (position < this.valueRange[0]) position = this.valueRange[0];
		if (position > this.valueRange[1]) position = this.valueRange[1];

		this.position = position;

		this.repaint();
	}
	public void setRange(double minimum, double maximum, double position) {
		if (minimum < maximum) {
			this.valueRange[0] = minimum;
			this.valueRange[1] = maximum;
		}
		else {
			this.valueRange[1] = minimum;
			this.valueRange[0] = maximum;
		}

		this.setPosition(position);
	}

	@Override
	protected void paintComponent(Graphics g) {
		int c1 = (this.disabled ? COLOR_STATE_DISABLED : ((this.hovered || this.grabbing) ? COLOR_STATE_HOVER : COLOR_STATE_NORMAL));
		int c2 = (this.disabled ? COLOR_STATE_DISABLED : COLOR_STATE_DEPRESSED);

		// Regions
		Rectangle rect = this.getRegionBounds();
		Rectangle grabber = this.getGrabber();

		// Backgrounds
		g.setColor(this.colors.background[c1]);
		g.fillRect(rect.x, rect.y, rect.width, rect.height);

		g.setColor(this.colors.grabbers[this.grabbing ? c2 : c1]);
		g.fillRect(grabber.x, grabber.y, grabber.width, grabber.height);

		// Outer border
		g.setColor(this.colors.borderLight[c1]);
		g.drawLine(rect.x, rect.y, rect.x + rect.width - 1, rect.y);
		g.drawLine(rect.x, rect.y, rect.x, rect.y + rect.height - 1);
		g.setColor(this.colors.borderDark[c1]);
		g.drawLine(rect.x, rect.y + rect.height - 1, rect.x + rect.width - 1, rect.y + rect.height - 1);
		g.drawLine(rect.x + rect.width - 1, rect.y, rect.x + rect.width - 1, rect.y + rect.height - 1);

		// Bar border
		g.setColor(this.colors.barBorderLight[c1]);
		g.drawLine(grabber.x, rect.y, grabber.x + grabber.width, rect.y);
		g.drawLine(grabber.x - 1, rect.y, grabber.x - 1, rect.y + rect.height - 1);
		g.setColor(this.colors.barBorderDark[c1]);
		g.drawLine(grabber.x, rect.y + rect.height - 1, grabber.x + grabber.width, rect.y + rect.height - 1);
		g.drawLine(grabber.x + grabber.width, rect.y, grabber.x + grabber.width, rect.y + rect.height - 1);
	}

	private Rectangle getRegionBounds() {
		Dimension sz = new Dimension(this.getParent().getSize().width, this.preferredSize.height);
		Insets insets = this.getInsets();
		return new Rectangle(insets.left, insets.top, sz.width - insets.left - insets.right, sz.height - insets.top - insets.bottom);
	}
	private Rectangle getGrabber() {
		Rectangle r = this.getRegionBounds();

		r.x = (int) ((r.width - this.grabSize) * ((this.position - this.valueRange[0]) / (this.valueRange[1] - this.valueRange[0])));
		r.width = this.grabSize;

		return r;
	}

	private void setCurs(int c) {
		this.setCursor(Cursor.getPredefinedCursor(c));
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (this.disabled) return;

		Rectangle grabber = this.getGrabber();

		this.grabbing = false;
		boolean signalGrabbingChange = false;

		if (e.getY() >= grabber.y && e.getY() < grabber.y + grabber.height) {
			if (e.getX() >= grabber.x && e.getX() < grabber.x + grabber.width) {
				this.grabbing = true;
				this.dragFlags = 0;
				this.grabOffset = grabber.x - e.getX();
				this.setCurs(Cursor.MOVE_CURSOR);
			}
			else {
				this.grabbing = true;
				this.dragFlags = 0;
				this.grabOffset = grabber.width / -2;
				this.setCurs(Cursor.MOVE_CURSOR);

				signalGrabbingChange = true;

				this.mouseDragged(e);
			}
		}

		if (this.grabbing) {
			this.signalChange(ChangeEvent.CLICKED | this.dragFlags | (signalGrabbingChange ? ChangeEvent.JUMPED : 0));
		}
	}
	@Override
	public void mouseDragged(MouseEvent e) {
		if (this.grabbing) {
			Rectangle r = this.getRegionBounds();
			int flags = 0;
			double pre = this.position;

			int left = e.getX() + this.grabOffset;
			this.position = left / (double) (r.width - this.grabSize) * (this.valueRange[1] - this.valueRange[0]);
			if (this.position < this.valueRange[0]) this.position = this.valueRange[0];
			else if (this.position > this.valueRange[1]) this.position = this.valueRange[1];

			if (e.isShiftDown() && this.position > this.valueRange[0] && this.position < this.valueRange[1]) {
				this.position = Math.round(this.position / this.snap) * this.snap;
			}

			if (this.position != pre) {
				flags |= ChangeEvent.CHANGED;
				this.dragFlags |= flags;
			}

			this.signalChange(ChangeEvent.DRAGGING | flags);

			this.repaint();
		}
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		this.grabbing = false;

		this.signalChange(ChangeEvent.DRAG_COMPLETED | this.dragFlags);

		this.setCurs(Cursor.DEFAULT_CURSOR);
	}
	@Override
	public void mouseMoved(MouseEvent e) {

	}
	@Override
	public void mouseClicked(MouseEvent e) {
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		this.hovered = true;
		this.repaint();
	}
	@Override
	public void mouseExited(MouseEvent e) {
		this.hovered = false;
		this.repaint();
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

	@Override
	public String getToolTipText(MouseEvent event) {
		return "";
	}

	public Colors getColors() {
		return this.colors;
	}


	@Override
	public void setEnabled(boolean enabled) {
		this.disabled = !enabled;
	}
	@Override
	public boolean isEnabled() {
		return !this.disabled;
	}

    public void addChangeListener(ChangeListener l) {
        this.listeners.add(ChangeListener.class, l);
    }
    public void removeChangeListener(ChangeListener l) {
        this.listeners.remove(ChangeListener.class, l);
    }
	private void signalChange(int type) {
		ChangeEvent changeEvent = new ChangeEvent(this, type);
		Object[] objs = listeners.getListenerList();
		for (int i = objs.length - 2; i >= 0; i -= 2) {
			if (objs[i] == ChangeListener.class) {
				((ChangeListener) objs[i + 1]).stateChanged(changeEvent);
			}
		}
	}
}
