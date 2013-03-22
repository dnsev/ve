package com.github.dnsev.videncode;

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;



public class JRangeSlider extends JComponent implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;

	public class ChangeEvent extends javax.swing.event.ChangeEvent {
		private static final long serialVersionUID = 1L;

		public static final int DRAGGING = 1;
		public static final int DRAG_COMPLETED = 2;
		public static final int LOW_CHANGED = 4;
		public static final int HIGH_CHANGED = 8;
		public static final int CLICKED = 16;
		public static final int CLICKED_LOW = 32;
		public static final int CLICKED_HIGH = 64;
		public static final int EXPAND_TO_FULL = 128;

		private int typeFlags;

		private ChangeEvent(JRangeSlider o, int typeFlags) {
			super(o);

			this.typeFlags = typeFlags;
		}

		public final int getChangeFlags() {
			return this.typeFlags;
		}
		public final JRangeSlider getSlider() {
			return (JRangeSlider) this.getSource();
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
		public Color[] bar = new Color[]{ new Color(220, 220, 220) , new Color(220, 220, 220) , new Color(220, 220, 220) , new Color(220, 220, 220) };
	}


	private static final int GRABBING_NONE = 0;
	private static final int GRABBING_LEFT = 1;
	private static final int GRABBING_RIGHT = 2;
	private static final int GRABBING_MIDDLE = 4;

	private Colors colors = new Colors();

	private double[][] valueRange = new double[][]{
		new double[]{ 0.0 , 1.0 },
		new double[]{ 0.0 , 1.0 }
	};
	private double[] values = new double[]{ 0.0 , 1.0 };
	private double snap = 1.0;

	private int grabFlags = 0;
	private int grabSize = 32;
	private int grabOffset = 0;
	private int dragFlags = 0;

	private boolean disabled = false;
	private boolean hovered = false;
	private boolean usable = true;
	private boolean resizable = true;

	private int valueIndex = 0;

	private Dimension preferredSize = new Dimension(256, 24);

	protected EventListenerList listeners = new EventListenerList();



	public JRangeSlider(double minimum, double maximum, double low, double high) {
		super();

		if (low < minimum) low = minimum;
		if (high > maximum) high = maximum;
		if (low > high) low = high;

		this.valueRange[0][0] = minimum;
		this.valueRange[0][1] = maximum;
		this.valueRange[0][0] = minimum;
		this.valueRange[0][1] = maximum;
		this.values[0] = low;
		this.values[1] = high;

		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

	public double getLow() {
		return this.values[0];
	}
	public double getHigh() {
		return this.values[1];
	}
	public double getMinimum() {
		return this.valueRange[0][0];
	}
	public double getMaximum() {
		return this.valueRange[0][1];
	}
	public double getValue(int id) {
		if (id <= 0) return this.getLow();
		else return this.getHigh();
	}

	public void setValue(int id, double value) {
		if (id <= 0) this.setLow(value);
		else this.setHigh(value);
	}
	public void setLow(double low) {
		if (low < this.valueRange[this.valueIndex][0]) low = this.valueRange[this.valueIndex][0];
		if (low > this.values[1]) low = this.values[1];

		this.values[0] = low;

		this.repaint();
	}
	public void setHigh(double high) {
		if (high < this.values[0]) high = this.values[0];
		if (high > this.valueRange[this.valueIndex][1]) high = this.valueRange[this.valueIndex][1];

		this.values[1] = high;

		this.repaint();
	}
	public void setRange(double minimum, double maximum) {
		this.valueRange[0][0] = minimum;
		this.valueRange[0][1] = maximum;
		this.valueIndex = 0;

		this.repaint();
	}
	public void setRange(double minimum, double maximum, double low, double high) {
		if (low < minimum) low = minimum;
		if (high > maximum) high = maximum;
		if (low > high) low = high;

		this.valueRange[0][0] = minimum;
		this.valueRange[0][1] = maximum;
		this.values[0] = low;
		this.values[1] = high;
		this.valueIndex = 0;

		this.repaint();
	}
	public void setSecondaryRange(boolean enabled) {
		this.valueIndex = (enabled ? 1 : 0);
	}
	public void setSecondaryRange(boolean enabled, double minimum, double maximum) {
		this.valueIndex = (enabled ? 1 : 0);

		if (minimum < this.valueRange[0][0]) minimum = this.valueRange[0][0];
		if (maximum > this.valueRange[0][1]) maximum = this.valueRange[0][1];
		if (minimum > maximum) {
			minimum = maximum;
		}

		this.valueRange[1][0] = minimum;
		this.valueRange[1][1] = maximum;
	}

	protected void paintComponent(Graphics g) {
		int c1 = (this.disabled ? COLOR_STATE_DISABLED : ((this.hovered || this.grabFlags != GRABBING_NONE) ? COLOR_STATE_HOVER : COLOR_STATE_NORMAL));
		int c2 = (this.disabled ? COLOR_STATE_DISABLED : COLOR_STATE_DEPRESSED);

		// Regions
		Rectangle rect = this.getRegionBounds();
		Rectangle left = this.getLeftGrab();
		Rectangle right = this.getRightGrab();

		// Backgrounds
		g.setColor(this.colors.background[c1]);
		g.fillRect(rect.x, rect.y, rect.width, rect.height);

		g.setColor(this.colors.grabbers[this.grabFlags == GRABBING_LEFT ? c2 : c1]);
		g.fillRect(left.x, left.y, left.width, left.height);
		g.setColor(this.colors.grabbers[this.grabFlags == GRABBING_RIGHT ? c2 : c1]);
		g.fillRect(right.x, right.y, right.width, right.height);

		g.setColor(this.colors.bar[this.grabFlags == GRABBING_MIDDLE ? c2 : c1]);
		g.fillRect(left.x + left.width, left.y, right.x - (left.x + left.width), left.height);

		// Outer border
		g.setColor(this.colors.borderLight[c1]);
		g.drawLine(rect.x, rect.y, rect.x + rect.width - 1, rect.y);
		g.drawLine(rect.x, rect.y, rect.x, rect.y + rect.height - 1);
		g.setColor(this.colors.borderDark[c1]);
		g.drawLine(rect.x, rect.y + rect.height - 1, rect.x + rect.width - 1, rect.y + rect.height - 1);
		g.drawLine(rect.x + rect.width - 1, rect.y, rect.x + rect.width - 1, rect.y + rect.height - 1);

		// Separators
		g.setColor(this.colors.barBorderLight[c1]);
		g.drawLine(right.x - 1, rect.y, right.x - 1, rect.y + rect.height - 1);
		g.setColor(this.colors.barBorderDark[c1]);
		g.drawLine(left.x + left.width, rect.y, left.x + left.width, rect.y + rect.height - 1);

		// Bar border
		g.setColor(this.colors.barBorderLight[c1]);
		g.drawLine(left.x, rect.y, right.x + right.width, rect.y);
		g.drawLine(left.x - 1, rect.y, left.x - 1, rect.y + rect.height - 1);
		g.setColor(this.colors.barBorderDark[c1]);
		g.drawLine(left.x, rect.y + rect.height - 1, right.x + right.width, rect.y + rect.height - 1);
		g.drawLine(right.x + right.width, rect.y, right.x + right.width, rect.y + rect.height - 1);
	}

	private Rectangle getRegionBounds() {
		Dimension sz = this.getPreferredSize();
		Insets insets = this.getInsets();
		return new Rectangle(insets.left, insets.top, sz.width - insets.left - insets.right, sz.height - insets.top - insets.bottom);
	}
	private Rectangle getLeftGrab() {
		Rectangle r = this.getRegionBounds();

		r.x = (int) ((r.width - this.grabSize * 2) * ((this.values[0] - this.valueRange[0][0]) / (this.valueRange[0][1] - this.valueRange[0][0])));
		r.width = this.grabSize;

		return r;
	}
	private Rectangle getRightGrab() {
		Rectangle r = this.getRegionBounds();

		double w = (this.valueRange[0][1] - this.valueRange[0][0]);
		if (w > 0.0) {
			r.x = (int) (this.grabSize + (r.width - this.grabSize * 2) * ((this.values[1] - this.valueRange[0][0]) / w));
			r.width = this.grabSize;
		}
		else {
			r.x = r.width - this.grabSize;
			r.width = this.grabSize;
		}

		return r;
	}

	private void setCurs(int c) {
		this.setCursor(Cursor.getPredefinedCursor(c));
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (!this.usable || this.disabled) return;

		Rectangle left = this.getLeftGrab();
		Rectangle right = this.getRightGrab();

		this.grabFlags = GRABBING_NONE;

		if (e.getY() >= left.y && e.getY() < left.y + left.height) {
			if (this.resizable && e.getX() >= right.x && e.getX() < right.x + right.width) {
				this.grabFlags |= GRABBING_RIGHT;
				this.dragFlags = ChangeEvent.CLICKED_HIGH;
				this.grabOffset = right.x - e.getX();
				this.setCurs(Cursor.W_RESIZE_CURSOR);
			}
			else if (this.resizable && e.getX() >= left.x && e.getX() < left.x + left.width) {
				this.grabFlags |= GRABBING_LEFT;
				this.dragFlags = ChangeEvent.CLICKED_LOW;
				this.grabOffset = left.x - e.getX();
				this.setCurs(Cursor.E_RESIZE_CURSOR);
			}
			else if (e.getX() >= left.x && e.getX() < right.x + right.width) {
				this.grabFlags |= GRABBING_MIDDLE;
				this.dragFlags = 0;
				this.grabOffset = (left.x + left.width) - e.getX();
				this.setCurs(Cursor.MOVE_CURSOR);
			}
		}

		if (this.grabFlags != GRABBING_NONE) {
			this.signalChange(ChangeEvent.CLICKED | this.dragFlags);
		}
	}
	@Override
	public void mouseDragged(MouseEvent e) {
		if (this.grabFlags == GRABBING_NONE) return;

		Rectangle r = this.getRegionBounds();

		int flags = 0;

		switch (this.grabFlags) {
			case GRABBING_LEFT:
			{
				double pre = this.values[0];

				int left = e.getX() + this.grabOffset;
				this.values[0] = left / (double) (r.width - this.grabSize * 2) * (this.valueRange[0][1] - this.valueRange[0][0]);
				if (this.values[0] < this.valueRange[this.valueIndex][0]) this.values[0] = this.valueRange[this.valueIndex][0];
				else if (this.values[0] > this.values[1]) this.values[0] = this.values[1];

				if (e.isShiftDown() && this.values[0] > this.valueRange[this.valueIndex][0] && this.values[0] < this.valueRange[this.valueIndex][1]) {
					this.values[0] = Math.round(this.values[0] / this.snap) * this.snap;
				}

				if (this.values[0] != pre) {
					flags |= ChangeEvent.LOW_CHANGED;
				}
			}
			break;
			case GRABBING_RIGHT:
			{
				double pre = this.values[1];

				int right = e.getX() - this.grabSize + this.grabOffset;
				this.values[1] = right / (double) (r.width - this.grabSize * 2) * (this.valueRange[0][1] - this.valueRange[0][0]);
				if (this.values[1] < this.values[0]) this.values[1] = this.values[0];
				else if (this.values[1] > this.valueRange[this.valueIndex][1]) this.values[1] = this.valueRange[this.valueIndex][1];

				if (e.isShiftDown() && this.values[1] > this.valueRange[this.valueIndex][0] && this.values[1] < this.valueRange[this.valueIndex][1]) {
					this.values[1] = Math.round(this.values[1] / this.snap) * this.snap;
				}

				if (this.values[1] != pre) {
					flags |= ChangeEvent.HIGH_CHANGED;
				}
			}
			break;
			default:
			{
				double pre = this.values[0];

				double range = this.values[1] - this.values[0];
				int left = e.getX() - this.grabSize + this.grabOffset;
				this.values[0] = left / (double) (r.width - this.grabSize * 2) * (this.valueRange[0][1] - this.valueRange[0][0]);
				if (this.values[0] < this.valueRange[this.valueIndex][0]) this.values[0] = this.valueRange[this.valueIndex][0];
				this.values[1] = this.values[0] + range;
				if (this.values[1] > this.valueRange[this.valueIndex][1]) {
					this.values[1] = this.valueRange[this.valueIndex][1];
					this.values[0] = this.values[1] - range;
				}

				if (this.values[0] != pre) {
					flags |= ChangeEvent.LOW_CHANGED | ChangeEvent.HIGH_CHANGED;
				}
			}
			break;
		}

		this.dragFlags |= flags;

		this.signalChange(ChangeEvent.DRAGGING | flags);

		this.repaint();
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		this.grabFlags = GRABBING_NONE;

		this.signalChange(ChangeEvent.DRAG_COMPLETED | this.dragFlags);

		this.setCurs(Cursor.DEFAULT_CURSOR);
	}
	@Override
	public void mouseMoved(MouseEvent e) {

	}
	@Override
	public void mouseClicked(MouseEvent e) {
		if (!this.usable || this.disabled) return;

		if (e.getClickCount() == 2 && this.resizable) {
			int flags = ChangeEvent.EXPAND_TO_FULL;
			if (this.values[0] != this.valueRange[this.valueIndex][0]) {
				this.values[0] = this.valueRange[this.valueIndex][0];
				flags |= ChangeEvent.LOW_CHANGED;
			}
			if (this.values[1] != this.valueRange[this.valueIndex][1]) {
				this.values[1] = this.valueRange[this.valueIndex][1];
				flags |= ChangeEvent.HIGH_CHANGED;
			}
			this.signalChange(flags);
			this.repaint();
		}
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


	public void setUsable(boolean usable) {
		this.usable = usable;
	}
	public void setResizable(boolean resizable) {
		this.resizable = resizable;
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
