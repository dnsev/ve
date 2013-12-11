package com.github.dnsev.videncode;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.swing.text.Document;

import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.HttpURLConnection;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.imageio.*;
import javax.imageio.stream.*;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

import java.util.regex.Pattern;
import java.util.regex.Matcher;



public final class GUI extends JFrame {
	private static GUI instance = null;
	private static final long serialVersionUID = 1L;

	private static final int SCREENSHOT_ID_FIRST = 0;
	private static final int SCREENSHOT_ID_LAST = 1;
	private static final int SCREENSHOT_ID_CUSTOM = 2;

	private static class Extension {
		private String ext = "";
		private String wildcard = "*";

		public Extension(String ext) {
			this.ext = ext;
		}

		private void setWildcard(String wildcard) {
			this.wildcard = wildcard;
		}

		public String getExtension() {
			return this.ext;
		}

		@Override
		public String toString() {
			return this.ext.replaceAll("\\*", this.wildcard);
		}
	}
	private static int[] colorTransformMatrix = new int[]{
		1 , 0 , 0 ,
		0 , 1 , 0 ,
		0 , 0 , 1
	};
	private static double[] colorScaleVector = new double[]{
		1.0 , 1.0 , 1.0
	};


	private static final class Color2 extends Color {
		private static final long serialVersionUID = 1L;
		private int[] rgb = new int[3];

		public Color2(int r, int g, int b) {
			super(
				Math.min(Math.max((int) Math.floor(((r * GUI.colorTransformMatrix[0] + g * GUI.colorTransformMatrix[1] + b * GUI.colorTransformMatrix[2]) / (GUI.colorTransformMatrix[0] + GUI.colorTransformMatrix[1] + GUI.colorTransformMatrix[2])) * GUI.colorScaleVector[0]), 0), 255),
				Math.min(Math.max((int) Math.floor(((r * GUI.colorTransformMatrix[3] + g * GUI.colorTransformMatrix[4] + b * GUI.colorTransformMatrix[5]) / (GUI.colorTransformMatrix[3] + GUI.colorTransformMatrix[4] + GUI.colorTransformMatrix[5])) * GUI.colorScaleVector[1]), 0), 255),
				Math.min(Math.max((int) Math.floor(((r * GUI.colorTransformMatrix[6] + g * GUI.colorTransformMatrix[7] + b * GUI.colorTransformMatrix[8]) / (GUI.colorTransformMatrix[6] + GUI.colorTransformMatrix[7] + GUI.colorTransformMatrix[8])) * GUI.colorScaleVector[2]), 0), 255)
			);
			this.rgb[0] = r;
			this.rgb[1] = g;
			this.rgb[2] = b;
		}
	}

	public static final void init(final Videncode ve, final JSON.Node node) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				(GUI.instance = new GUI(ve, node)).setVisible(true);
			}
		});
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (GUI.instance != null) {
					GUI.instance.close();
					GUI.instance = null;
				}
			}
		});
	}

	private static final class ImageQuality {
		private int quality = 0;
		private String label = "";

		public ImageQuality(int quality, final String label) {
			this.quality = quality;
			this.label = label;
		}

		public final int getQuality() {
			return this.quality;
		}

		@Override
		public final String toString() {
			return this.label;
		}
	}
	private static final class ImageScale {
		private static final Pattern[] patterns = new Pattern[]{
			Pattern.compile("\\s*([0-9]+)\\s*x\\s*([0-9]+)\\s*p?(\\s+.*)?"),
			Pattern.compile("\\s*([0-9]+)\\s*p?(\\s+.*)?"),
			Pattern.compile("\\s*No(\\s+.*)?")
		};

		private Dimension size = new Dimension(-1, 0);
		private String label = "";

		public ImageScale(final String label) {
			this.label = label;
			this.size = ImageScale.parseSize(this.label);
		}
		public ImageScale(int height, final String label) {
			this.size.height = height;
			this.label = label;
		}

		public final int getResolution() {
			return this.size.height;
		}
		public final Dimension getSize() {
			return this.size;
		}

		public static final Dimension parseSize(final String str) {
			Dimension d = new Dimension(-1, 0);

			Matcher m = ImageScale.patterns[0].matcher(str);
			if (m.matches()) {
				try {
					d.width = Integer.parseInt(m.group(1));
					d.height = Integer.parseInt(m.group(2));
				}
				catch (NumberFormatException e) {}
				return d;
			}

			m = ImageScale.patterns[1].matcher(str);
			if (m.matches()) {
				try {
					d.height = Integer.parseInt(m.group(1));
					d.width = -1;
				}
				catch (NumberFormatException e) {}
				return d;
			}

			m = ImageScale.patterns[2].matcher(str);
			if (m.matches()) {
				d.height = 0;
				d.width = -1;
			}

			return d;
		}

		@Override
		public final String toString() {
			return this.label;
		}
	}

	private final class ColorList {
		private final Color background = new Color2(0xf4, 0xef, 0xf8);
		private final Color backgroundDark = new Color2(0xb8, 0x85, 0xd5);
		private final Color backgroundDarkest = new Color2(0x7b, 0x1c, 0xb1);
		private final Color text = new Color2(0x28, 0x01, 0x3f);
		private final Color textAlt1 = new Color2(0x40, 0x03, 0x63);
		private final Color textLight1 = new Color2(0xde, 0xbd, 0xf1);
		private final Color textLight2 = new Color2(0xb8, 0x72, 0xe1);
		private final Color textLight3 = new Color2(0xea, 0xdd, 0xf8);
		private final Color textShadow1 = new Color2(0x3d, 0x0e, 0x58);
		private final Color textShadow2 = new Color2(0x3d, 0x0e, 0x58);
	};
	private static final class FontList {
		private static final Font attr(Font font, TextAttribute key, Object value) {
			Map<TextAttribute, Object>  attributes = new HashMap<TextAttribute, Object>();
			attributes.put(key, value);
			return font.deriveFont(attributes);
		}

		private final Font title = new Font("Verdana", Font.BOLD | Font.ITALIC, 40);
		private final Font titleVersion = new Font("Verdana", Font.BOLD | Font.ITALIC, 16);
		private final Font tabLabel = new Font("Verdana", Font.PLAIN, 20);
		private final Font text = new Font("Verdana", Font.PLAIN, 16);
		private final Font textUnderline = FontList.attr(new Font("Verdana", Font.PLAIN, 16), TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		private final Font textItalic = new Font("Verdana", Font.ITALIC, 16);
		private final Font textBold = new Font("Verdana", Font.BOLD, 16);
		private final Font textSmall = new Font("Verdana", Font.PLAIN, 12);
		private final Font textBoldStrikethru = FontList.attr(new Font("Verdana", Font.BOLD, 16), TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
		private final Font logText = new Font("Courier New", Font.PLAIN, 12);
		private final Font textHuge = new Font("Verdana", Font.PLAIN, 40);
		private final Font textNoSync = new Font("Verdana", Font.PLAIN, 64);
	};

	private ColorList colors = null;
	private FontList fonts = null;
	private String defaultTab = "Video";

	private String[] imageExtensions = new String[]{
		".png",
		".jpg",
		".jpeg",
		".gif",
	};
	private String[] audioExtensions = new String[]{
		".mp3",
		".ogg",
		".oga",
		".flac",
		".wav",
	};
	private String[] videoExtensions = null;
	private Extension[] outputExtensions = new Extension[]{
		new Extension(".ve*")
	};
	private String outputExtensionDefault = this.outputExtensions[0].getExtension();

	private Videncode videncode = null;
	private Image noimage = null;
	private JTabbedPane tabManager = null;
	private JPanel tabAdvanced = null;
	private boolean ffmpegStartupCheck = true;
	private boolean updateStartupCheck = true;
	private boolean useLocalLook = true;
	private boolean tabAdvancedEnabled = false;


	//{ Image
	private JCheckBox imageSourceCheckbox = null;
	private JImage imagePreview = null;
	private JPanel imagePreviewTimeSliderContainer = null;
	private JLabel imagePreviewFileSizeDisplay = null;
	private JLabel imagePreviewFileSizeTrueDisplay = null;
	private JLabel imagePreviewDimensionsDisplay = null;
	private JLabel imagePreviewAvailableSpaceDisplay = null;
	private JLabel imagePreviewAvailableSpaceTrueDisplay = null;
	private ButtonGroup imagePreviewSourceGroup = null;
	private JRadioButton[] imagePreviewSourceButtons = new JRadioButton[]{ null , null };
	private JComboBox<ImageScale> imageScaleComboBox = null;
	private boolean imageScaleComboBoxModifying = false;
	private JComboBox<ImageQuality> imageQualityComboBox = null;
	private boolean imageQualityComboBoxModifying = false;
	private File imagePreviewLastSearchDir = Main.getAppDir();
	private JBarSlider imagePreviewTimeSlider = null;
	private boolean imagePreviewTimecodeLocationModifying = false;
	private JTextFieldCustom imagePreviewTimecodeLocation = null;
	private JLabel fileLabelImage = null;
	//}

	//{ Video
	private JCheckBox videoSourceCheckbox = null;
	private boolean videoBaselineResolutionComboBoxModifying = false;
	private JComboBox<ImageScale> videoBaselineResolutionComboBox = null;
	private boolean videoBaselineFrameRateComboBoxModifying = false;
	private JComboBox<Videncode.FrameRate> videoBaselineFrameRateComboBox = null;
	private boolean videoAutoQualityProfileComboBoxModifying = false;
	private JComboBox<Videncode.AutoQualityProfile> videoAutoQualityProfileComboBox = null;
	private int videoImagePreviewId = 0;
	private JImage videoImagePreview = null;
	private ButtonGroup videoImagePreviewButtonGroup = null;
	private JRadioButton[] videoImagePreviewButtons = new JRadioButton[]{ null , null };
	private JLabel videoResolutionDisplay = null;
	private JLabel videoFrameRateDisplay = null;
	private File videoLastSearchDir = Main.getAppDir();
	private JRangeSlider videoEncodingDurationRangeslider = null;
	private JTextFieldCustom[] videoEncodingDurationTimecode = new JTextFieldCustom[]{ null , null };
	private boolean videoEncodeAutoQualityEnabledDefault = true;
	private boolean videoEncodeAutoQualityEnabled = this.videoEncodeAutoQualityEnabledDefault;
	private JCheckBox videoEncodeAutoQualitySelect = null;
	private JRadioButton[] videoEncodeModeButtons = new JRadioButton[]{ null , null };
	private JLabel fileLabelVideo = null;
	private JLabel videoBitrateDisplay = null;

	private JLabel[] timecodeStart = new JLabel[]{ null , null };
	private JLabel[] timecodeEnd = new JLabel[]{ null , null };

	private JLabel videoTempEncodeStatusText = null;
	private JButton videoTempEncodeActivateButton = null;
	private JProgressBarCustom videoTempEncodeProgressBar = null;

	private JLabel videoTempFileSizeLabel = null;
	//}

	//{ Audio
	private File audioLastSearchDir = Main.getAppDir();
	private JLabel fileLabelAudio = null;

	private JCheckBox audioSourceCheckbox = null;
	private ButtonGroup audioSourceButtonGroup = null;
	private JRadioButton[] audioSourceButtons = new JRadioButton[]{ null , null };

	private JRangeSlider audioEncodingDurationRangeslider = null;
	private JLabel audioTimecodeStart = null;
	private JLabel audioTimecodeEnd = null;
	private JTextFieldCustom[] audioEncodingDurationTimecode = new JTextFieldCustom[]{ null , null };

	private boolean audioBitrateSelectionComboBoxModifying = false;
	private JComboBox<Videncode.Bitrate> audioBitrateSelectionComboBox = null;
	private boolean audioChannelSelectionComboBoxModifying = false;
	private JComboBox<Videncode.AudioChannelCount> audioChannelSelectionComboBox = null;
	private boolean audioSampleRateSelectionComboBoxModifying = false;
	private JComboBox<Videncode.AudioSampleRate> audioSampleRateSelectionComboBox = null;

	private JButton[] audioPreviewClipButtons = new JButton[]{ null , null };

	private JLabel audioTempFilesizeLabel = null;
	private JLabel audioTempFilesize = null;

	private JLabel audioTempEncodeStatusText = null;
	private JButton audioTempEncodeActivateButton = null;
	private JProgressBarCustom audioTempEncodeProgressBar = null;
	//}

	//{ Sync
	private class SyncTabVars {
		private JPanel mainPanel = null;
		private JPanel disabledPanel = null;

		private JRangeSlider[] ranges = new JRangeSlider[]{ null , null };
		private JLabel[][] rangeTimecodes = new JLabel[][]{
			new JLabel[]{ null , null },
			new JLabel[]{ null , null }
		};
		private JTextFieldCustom[][] rangeEncodeTimecodes = new JTextFieldCustom[][]{
			new JTextFieldCustom[]{ null , null },
			new JTextFieldCustom[]{ null , null }
		};

		private JPanel[] optionPanels = new JPanel[]{ null , null };

		private JCheckBox[] audioStateLoop = new JCheckBox[]{ null , null };
		private JCheckBox[] audioFadeTransition = new JCheckBox[]{ null , null };

		private ButtonGroup[] videoStateGroups = new ButtonGroup[]{ null , null };
		private JRadioButton[][] videoState = new JRadioButton[][]{
			new JRadioButton[]{ null , null , null , null },
			new JRadioButton[]{ null , null , null , null }
		};
		private JCheckBox[] videoFadeTransition = new JCheckBox[]{ null , null };
	}
	private SyncTabVars sync = new SyncTabVars();
	//}

	//{ Encode
	private class EncodeTabVars {
		private JLabel imageStatusTextGood = null;
		private JLabel imageStatusTextMissing = null;
		private JLabel imageStatusTextNotEncoded = null;
		private JLabel imageStatusTextBadSize = null;

		private JLabel audioStatusTextGood = null;
		private JLabel audioStatusTextMissing = null;
		private JLabel audioStatusTextBadSize = null;
		private JLabel audioStatusTextNotEncoded = null;
		private JProgressBarCustom audioProgressBar = null;

		private JLabel videoStatusTextGood = null;
		private JLabel videoStatusTextMissing = null;
		private JLabel videoStatusTextBadSize = null;
		private JLabel videoStatusTextNotEncoded = null;
		private JProgressBarCustom videoProgressBar = null;

		private JLabel finalStatusTextIdle = null;
		private JLabel finalStatusTextWaiting = null;
		private JLabel finalStatusTextMuxing = null;
		private JButton finalStatusTextComplete = null;

		private JLabel statusDisplay = null;

		private JPanel errorPanel = null;
		private JLabel errorMessage = null;

		private JButton encodeButton = null;
		private JTextFieldCustom outputFilename = null;
		private JComboBox<Extension> outputFilenameExt = null;
		private boolean outputFilenameExtChanging = false;
		private JTextFieldCustom outputTag = null;
	}
	private EncodeTabVars encode = new EncodeTabVars();
	//}

	//{ Other
	private JLabel[] updateAvailableLabels = new JLabel[4];

	private Videncode.AutoQualityProfile encodingAutoQualityProfile = null;
	//}

	//{ Settings
	private class SettingsVars {
		private JComboBox<Integer> ffmpegThreadSelection = null;

		private JTextFieldCustom outputMaxSize = null;
		private JComboBox<Extension> outputFilenameExt = null;
		private boolean outputFilenameExtChanging = false;

		private JComboBox<String> guiMainTabSelection = null;
		private JCheckBox appUpdateCheckEnabled = null;
		private JCheckBox appUseLocalLookEnabled = null;
		private JCheckBox appShowAdvancedTab = null;

		private JCheckBox appVideoAutoQualityEnabled = null;
		private JComboBox<Videncode.AutoQualityProfile> appVideoAutoQuality = null;
		private JComboBox<String> appVideoEncodingMode = null;
		private JComboBox<ImageQuality> appImageQuality = null;
		private JComboBox<Videncode.Bitrate> appAudioKbps = null;
		private JComboBox<Videncode.AudioChannelCount> appAudioChannels = null;
		private JComboBox<Videncode.AudioSampleRate> appAudioSampleRate = null;

		private JButton saveButton = null;
	}
	private SettingsVars settings = new SettingsVars();
	//}

	//{ Advanced
	private class AdvancedVars {

		private JTextArea[][] logs = new JTextArea[][]{
			new JTextArea[]{ null , null },
			new JTextArea[]{ null , null },
			new JTextArea[]{ null , null }
		};

		private JTextArea[] ffmpegSettings = new JTextArea[]{
			null , null
		};

	}
	private AdvancedVars advanced = new AdvancedVars();
	//}

	// Constructor
	public GUI(Videncode ve, JSON.Node node) {
		super();
		final GUI self = this;

		this.loadSettings(node);

		this.colors = new ColorList();
		this.fonts = new FontList();

		this.videncode = ve;
		this.videncode.setGUIComponent(this.getRootPane());
		this.encodingAutoQualityProfile = this.videncode.getVideoAutoQualityProfileDefault();

		// Resources
		this.acquireResources();

		// Style
		if (this.useLocalLook) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			catch (Exception e) {
			}
		}
		System.setProperty("awt.useSystemAAFontSettings","on");
		System.setProperty("swing.aatext", "true");
		String[] types = new String[]{ "TextField" , "TextArea" , "CheckBox" , "ComboBox" , "RadioButton" , "Button" };
		for (int i = 0; i < types.length; ++i) {
			UIManager.put(types[i] + ".disabledBackground", this.colors.background);
			UIManager.put(types[i] + ".inactiveBackground", this.colors.background);
			UIManager.put(types[i] + ".disabledForeground", this.colors.textLight2);
			UIManager.put(types[i] + ".inactiveForeground", this.colors.textLight2);
			UIManager.put(types[i] + ".disabledText", this.colors.textLight2);
		}
		UIManager.put("TabbedPane.background", this.colors.background);
		UIManager.put("TabbedPane.contentAreaColor", this.colors.background);
		UIManager.put("TabbedPane.tabAreaBackground", this.colors.background);
		UIManager.put("TabbedPane.selected", this.colors.background);

		// Window setup
		this.setTitle("Videncode");
		this.setSize(1280, 720);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		super.setBackground(this.colors.backgroundDarkest);
		this.getRootPane().setBackground(this.colors.backgroundDarkest);
		this.getLayeredPane().setBackground(this.colors.backgroundDarkest);
		this.getContentPane().setBackground(this.colors.backgroundDarkest);

		// Icon
		try {
			ArrayList<Image> icons = new ArrayList<Image>();
			icons.add(new ImageIcon(this.getClass().getResource("/res/ve16.png")).getImage());
			icons.add(new ImageIcon(this.getClass().getResource("/res/ve32.png")).getImage());
			this.setIconImages(icons);
		}
		catch (Exception e) {
		}

		// Content
		JLayeredPane lp1;
		JPanel p0, p1, p2, p3;
		JTabbedPane tp1;
		JLabel t1;

		// Overlayer
		this.getContentPane().add((p0 = new JPanel()));
		p0.setLayout(new OverlayLayout(p0));
		p0.setOpaque(false);
		p0.setAlignmentX(Component.LEFT_ALIGNMENT);

		//{ Add the title
		Color[] titleColors1 = new Color[]{ this.colors.textLight1 , this.colors.textShadow1 };
		Color[] titleColors2 = new Color[]{ this.colors.textLight2 , this.colors.textShadow2 };
		int[][] titleOffsets1 = new int[][]{ new int[]{ 0 , 0 } , new int[]{ 3 , 0 } };
		int[][] titleOffsets2 = new int[][]{ new int[]{ 0 , 0 } , new int[]{ 2 , 0 } };
		for (int i = 0; i < 2; ++i) {
			p0.add((p1 = new JPanel()));
			p1.setLayout(null);
			p1.setOpaque(false);
			p1.setAlignmentX(Component.LEFT_ALIGNMENT);

			p1.add((p2 = new JPanel()));
			p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
			p2.setOpaque(false);
			p2.setAlignmentX(Component.LEFT_ALIGNMENT);
			p2.setBounds(0, 0, 640, 128);

			p2.add((t1 = new JLabel("Videncode")));
			t1.setFont(this.fonts.title);
			t1.setAlignmentY(Component.TOP_ALIGNMENT);
			t1.setForeground(titleColors1[i]);
			t1.setBorder(BorderFactory.createEmptyBorder(-8 + titleOffsets1[i][0], 0 + titleOffsets1[i][1], 0, 0));
			t1.setCursor(new Cursor(Cursor.HAND_CURSOR));
			t1.addMouseListener(new MouseListener() {
				@Override
				public final void mouseClicked(MouseEvent event) {
					self.openURL("http://dnsev.github.io/ve/");
				}

				@Override
				public final void mousePressed(MouseEvent event) {
				}
				@Override
				public final void mouseReleased(MouseEvent event) {
				}
				@Override
				public final void mouseEntered(MouseEvent event) {
				}
				@Override
				public final void mouseExited(MouseEvent event) {
				}
			});

			p2.add((t1 = new JLabel("v" + Main.getVersion())));
			t1.setFont(this.fonts.titleVersion);
			t1.setAlignmentY(Component.TOP_ALIGNMENT);
			t1.setForeground(titleColors2[i]);
			t1.setBorder(BorderFactory.createEmptyBorder(-1 + titleOffsets2[i][0], 3 + titleOffsets2[i][1], 0, 0));

			p2.add((t1 = new JLabel(" / ")));
			t1.setFont(this.fonts.titleVersion);
			t1.setAlignmentY(Component.TOP_ALIGNMENT);
			t1.setForeground(titleColors2[i]);
			t1.setBorder(BorderFactory.createEmptyBorder(-1 + titleOffsets2[i][0], 3 + titleOffsets2[i][1], 0, 0));
			t1.setVisible(false);
			this.updateAvailableLabels[i] = t1;

			p2.add((t1 = new JLabel("Update Available")));
			t1.setFont(this.fonts.titleVersion);
			t1.setAlignmentY(Component.TOP_ALIGNMENT);
			t1.setForeground(titleColors2[i]);
			t1.setBorder(BorderFactory.createEmptyBorder(-1 + titleOffsets2[i][0], 3 + titleOffsets2[i][1], 0, 0));
			t1.setCursor(new Cursor(Cursor.HAND_CURSOR));
			t1.setVisible(false);
			this.updateAvailableLabels[i + 2] = t1;
			t1.addMouseListener(new MouseListener() {
				@Override
				public final void mouseClicked(MouseEvent event) {
					self.openURL("http://dnsev.github.io/ve/#changes");
				}

				@Override
				public final void mousePressed(MouseEvent event) {
				}
				@Override
				public final void mouseReleased(MouseEvent event) {
				}
				@Override
				public final void mouseEntered(MouseEvent event) {
				}
				@Override
				public final void mouseExited(MouseEvent event) {
				}
			});
		}
		//}

		//{ Setup tabs
		p0.add((p1 = new JPanel()));
		p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
		p1.setOpaque(false);
		p1.setAlignmentX(Component.LEFT_ALIGNMENT);
		p1.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
		p1.setBackground(this.colors.background);

		tp1 = new JTabbedPane(JTabbedPane.TOP);
		tp1.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		tp1.setAlignmentX(Component.LEFT_ALIGNMENT);
		tp1.setFont(this.fonts.tabLabel);
		tp1.setForeground(this.colors.text);
		tp1.setBackground(this.colors.background);
		this.tabManager = tp1;

		tp1.setUI(new BasicTabbedPaneUI() {
			@Override
			protected void installDefaults() {
				super.installDefaults();
				this.lightHighlight = self.colors.backgroundDark;
				this.shadow = self.colors.backgroundDark;
				this.darkShadow = self.colors.background;
				this.focus = self.colors.backgroundDark;
				this.contentBorderInsets = new Insets(0, 0, 0, 0);
		   }
		});
		p1.add(tp1);
		//}

		//{ Tab : Advanced
		this.tabAdvanced = new JPanel();
		this.tabAdvanced.setLayout(new BoxLayout(this.tabAdvanced, BoxLayout.X_AXIS));
		this.tabAdvanced.setOpaque(false);
		//}

		//{ Tab : Encode
		JPanel encodePanel = new JPanel();
		encodePanel.setLayout(new BoxLayout(encodePanel, BoxLayout.X_AXIS));
		encodePanel.setOpaque(false);
		tp1.addTab("Encode", null, encodePanel);
		//}

		//{ Tab : Sync
		p3 = new JPanel();
		p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS));
		p3.setOpaque(false);
		tp1.addTab("Sync", null, p3);

		this.setupTabSync(p3);
		//}

		//{ Tab : Video
		p3 = new JPanel();
		p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS));
		p3.setOpaque(false);
		tp1.addTab("Video", null, p3);

		this.setupTabVideo(p3);
		//}

		//{ Tab : Image
		p3 = new JPanel();
		p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS));
		p3.setOpaque(false);
		tp1.addTab("Image", null, p3);

		this.setupTabImage(p3);
		//}

		// Update tabs
		for (int i = 0; i < tp1.getTabCount(); ++i) {
			if (tp1.getTitleAt(i).equals(this.defaultTab)) {
				tp1.setSelectedIndex(i);
			}
			tp1.setBackgroundAt(i, null);
		}
		this.setupTabEncode(encodePanel);
		this.setupTabAdvanced(this.tabAdvanced);
		if (this.tabAdvancedEnabled) {
			this.tabManager.insertTab("Advanced", null, this.tabAdvanced, null, 0);
		}

		// Trigger update events
		this.updateVideoAutoQualityProfileComboBox();
		this.updateVideoBaselineFrameRateComboBox();
		this.updateEncodeExtensionDefault();
		this.onVideoFileSourceChange();
		this.onVideoFileTempChange(false);
		this.onImageFileSourceChange();
		this.onImageFileTempChange(false);
		this.onAudioFileSourceChange();
		this.onAudioFileTempChange(false);
		this.onVideoPreviewChange(0, false);
		this.onVideoPreviewChange(1, false);
		this.onAudioPreviewChange(0, false);
		this.onAudioPreviewChange(1, false);

		// Create videncode listeners
		this.videncode.addChangeListener(new VidencodeEventListener(){
			@Override
			public final void onVidencodeChange(final VidencodeChangeEvent event) {
				switch (event.getEventType()) {
					case VIDEO_FILE_CHANGED:
						if ((event.getEventFlags() & VidencodeChangeEvent.SOURCE) != 0) {
							self.onVideoFileSourceChange();
						}
						if ((event.getEventFlags() & VidencodeChangeEvent.TEMPORARY) != 0) {
							self.onVideoFileTempChange((event.getEventFlags() & VidencodeChangeEvent.UPDATING) != 0);
						}
					break;
					case IMAGE_FILE_CHANGED:
						if ((event.getEventFlags() & VidencodeChangeEvent.SOURCE) != 0) {
							self.onImageFileSourceChange();
						}
						if ((event.getEventFlags() & VidencodeChangeEvent.TEMPORARY) != 0) {
							self.onImageFileTempChange((event.getEventFlags() & VidencodeChangeEvent.UPDATING) != 0);
						}
					break;
					case AUDIO_FILE_CHANGED:
						if ((event.getEventFlags() & VidencodeChangeEvent.SOURCE) != 0) {
							self.onAudioFileSourceChange();
						}
						if ((event.getEventFlags() & VidencodeChangeEvent.TEMPORARY) != 0) {
							self.onAudioFileTempChange((event.getEventFlags() & VidencodeChangeEvent.UPDATING) != 0);
						}
					break;
					case VIDEO_PREVIEW_CHANGED:
						if ((event.getEventFlags() & VidencodeChangeEvent.FIRST) != 0) {
							self.onVideoPreviewChange(0, (event.getEventFlags() & VidencodeChangeEvent.UPDATING) != 0);
						}
						if ((event.getEventFlags() & VidencodeChangeEvent.LAST) != 0) {
							self.onVideoPreviewChange(1, (event.getEventFlags() & VidencodeChangeEvent.UPDATING) != 0);
						}
					break;
					case AUDIO_PREVIEW_CHANGED:
						if ((event.getEventFlags() & VidencodeChangeEvent.FIRST) != 0) {
							self.onAudioPreviewChange(0, (event.getEventFlags() & VidencodeChangeEvent.UPDATING) != 0);
						}
						if ((event.getEventFlags() & VidencodeChangeEvent.LAST) != 0) {
							self.onAudioPreviewChange(1, (event.getEventFlags() & VidencodeChangeEvent.UPDATING) != 0);
						}
					break;
					case VIDEO_FILE_ENCODING_PROGRESS:
						self.onVideoProgressChange(event.getValue());
					break;
					case AUDIO_FILE_ENCODING_PROGRESS:
						self.onAudioProgressChange(event.getValue());
					break;
					case IMAGE_FILE_ENCODING_LOG:
						self.onEncodeLogEvent(
							0,
							(event.getEventFlags() & VidencodeChangeEvent.CLEAR) != 0,
							event.getText(0),
							event.getText(1),
							event.getText(2)
						);
					break;
					case AUDIO_FILE_ENCODING_LOG:
						self.onEncodeLogEvent(
							1,
							(event.getEventFlags() & VidencodeChangeEvent.CLEAR) != 0,
							event.getText(0),
							event.getText(1),
							event.getText(2)
						);
					break;
					case VIDEO_FILE_ENCODING_LOG:
						self.onEncodeLogEvent(
							2,
							(event.getEventFlags() & VidencodeChangeEvent.CLEAR) != 0,
							event.getText(0),
							event.getText(1),
							event.getText(2)
						);
					break;
					case ENCODE_STATUS:
						self.onEncodeStatusChange(
							(event.getEventFlags() & VidencodeChangeEvent.CLEAR) != 0,
							(event.getEventFlags() & VidencodeChangeEvent.ERROR) != 0,
							(event.getEventFlags() & VidencodeChangeEvent.MUXING) != 0,
							(event.getEventFlags() & VidencodeChangeEvent.RESET) != 0,
							(event.getEventFlags() & VidencodeChangeEvent.COMPLETE) != 0,
							event.getText(0)
						);
					break;
				}
			}
		});

		// Test
		if (this.ffmpegStartupCheck) {
			this.videncode.testFFmpegInstall(new Runnable() {
				@Override
				public final void run() {
					JOptionPane.showInputDialog(
						self.getRootPane(),
						"You don't seem to have ffmpeg/ffprobe installed\nCheck the homepage for details:",
						"Incomplete install",
						JOptionPane.ERROR_MESSAGE,
						null,
						null,
						"http://dnsev.github.io/ve/"
					);
				}
			});
		}
		if (this.updateStartupCheck) {
			this.updateCheck();
		}
	}
	private final void loadSettings(JSON.Node node) {
		if (node == null) return;

		// Color matrix
		try {
			ArrayList<JSON.Node> arr = node.getObject().get("gui").getObject().get("color_matrix").getArray();
			int len = Math.min(arr.size(), GUI.colorTransformMatrix.length);
			for (int i = 0; i < len; ++i) {
				GUI.colorTransformMatrix[i] = (int) arr.get(i).getInteger();
			}
		}
		catch (Exception e) {}

		// Color scales
		try {
			ArrayList<JSON.Node> arr = node.getObject().get("gui").getObject().get("color_scale").getArray();
			int len = Math.min(arr.size(), GUI.colorScaleVector.length);
			for (int i = 0; i < len; ++i) {
				GUI.colorScaleVector[i] = arr.get(i).getDouble();
			}
		}
		catch (Exception e) {}

		// Default tab
		try {
			String str = node.getObject().get("gui").getObject().get("default_tab").getString();
			if (str != null) this.defaultTab = str;
		}
		catch (Exception e) {}

		// Extensions
		try {
			ArrayList<JSON.Node> arr = node.getObject().get("app").getObject().get("extensions").getObject().get("image").getArray();
			if (arr != null) {
				ArrayList<String> exts = new ArrayList<String>();
				for (int i = 0; i < arr.size(); ++i) {
					String str = arr.get(i).getString();
					if (str != null) exts.add(str);
				}
				if (exts.size() > 0) {
					this.imageExtensions = exts.toArray(new String[exts.size()]);
				}
			}
			else {
				this.imageExtensions = null;
			}
		}
		catch (Exception e) {}
		try {
			ArrayList<JSON.Node> arr = node.getObject().get("app").getObject().get("extensions").getObject().get("audio").getArray();
			if (arr != null) {
				ArrayList<String> exts = new ArrayList<String>();
				for (int i = 0; i < arr.size(); ++i) {
					String str = arr.get(i).getString();
					if (str != null) exts.add(str);
				}
				if (exts.size() > 0) {
					this.audioExtensions = exts.toArray(new String[exts.size()]);
				}
			}
			else {
				this.audioExtensions = null;
			}
		}
		catch (Exception e) {}
		try {
			ArrayList<JSON.Node> arr = node.getObject().get("app").getObject().get("extensions").getObject().get("video").getArray();
			if (arr != null) {
				ArrayList<String> exts = new ArrayList<String>();
				for (int i = 0; i < arr.size(); ++i) {
					String str = arr.get(i).getString();
					if (str != null) exts.add(str);
				}
				if (exts.size() > 0) {
					this.videoExtensions = exts.toArray(new String[exts.size()]);
				}
			}
			else {
				this.videoExtensions = null;
			}
		}
		catch (Exception e) {}
		try {
			ArrayList<JSON.Node> arr = node.getObject().get("app").getObject().get("extensions").getObject().get("output").getArray();
			if (arr != null) {
				ArrayList<Extension> exts = new ArrayList<Extension>();
				for (int i = 0; i < arr.size(); ++i) {
					String str = arr.get(i).getString();
					if (str != null) exts.add(new Extension(str));
				}
				if (exts.size() > 0) {
					this.outputExtensions = exts.toArray(new Extension[exts.size()]);
				}
			}
		}
		catch (Exception e) {}

		// Auto quality on
		try {
			boolean b = node.getObject().get("settings").getObject().get("video_auto_quality_on").getBoolean();
			this.videoEncodeAutoQualityEnabledDefault = b;
			this.videoEncodeAutoQualityEnabled = b;
		}
		catch (Exception e) {}

		// Advanced
		try {
			boolean b = node.getObject().get("app").getObject().get("show_advanced_tab").getBoolean();
			this.tabAdvancedEnabled = b;
		}
		catch (Exception e) {}

		// Startup checks
		try {
			boolean b = node.getObject().get("ffmpeg").getObject().get("statup_check").getBoolean();
			this.ffmpegStartupCheck = b;
		}
		catch (Exception e) {}
		try {
			boolean b = node.getObject().get("app").getObject().get("update_check").getBoolean();
			this.updateStartupCheck = b;
		}
		catch (Exception e) {}
		try {
			boolean b = node.getObject().get("app").getObject().get("local_look").getBoolean();
			this.useLocalLook = b;
		}
		catch (Exception e) {}
	}
	public final void saveSettings(JSON.Node node) {
		if (node == null) return;

		node.get("gui")
		.set("color_matrix", JSON.node(GUI.colorTransformMatrix))
		.set("color_scale", JSON.node(GUI.colorScaleVector))
		.set("default_tab", JSON.node(this.defaultTab));

		node.get("app")
		.set("show_advanced_tab", JSON.node(new Boolean(this.tabAdvancedEnabled)))
		.set("update_check", JSON.node(new Boolean(this.updateStartupCheck)))
		.set("local_look", JSON.node(new Boolean(this.useLocalLook)));

		node.get("app").get("extensions")
		.set("image", JSON.node(this.imageExtensions))
		.set("audio", JSON.node(this.audioExtensions))
		.set("video", JSON.node(this.videoExtensions))
		.set("output", JSON.node(this.outputExtensions, new JSONObjectTransformer(){
			@Override
			public final Object transform(Object o) {
				return ((Extension) o).getExtension();
			}
		}));

		node.get("settings")
		.set("video_auto_quality_on", JSON.node(new Boolean(this.videoEncodeAutoQualityEnabledDefault)));

		node.get("ffmpeg")
		.set("startup_check", JSON.node(new Boolean(this.ffmpegStartupCheck)));
	}
	private final void openURL(String url) {
		boolean opened;
		try {
			URI uri = new URI(url);
			if (Desktop.isDesktopSupported()) {
				try {
					Desktop.getDesktop().browse(uri);
					opened = true;
				}
				catch (IOException e) {
					opened = false;
				}
			}
			else {
				opened = false;
			}
		}
		catch (URISyntaxException e) {
			opened = false;
		}
		if (!opened) {
			JOptionPane.showInputDialog(
				this.getRootPane(),
				"Webpage:",
				"Webpage",
				JOptionPane.PLAIN_MESSAGE,
				null,
				null,
				url
			);
		}
	}
	private final void parseChangeLog(String changelog) {
		String lines[] = changelog.split("\\r?\\n");
		String checkVersion = lines[0];
		String currentVersion = Main.getVersion();
		boolean updateNeeded = false;

		String[] versions1 = checkVersion.split("\\.");
		String[] versions2 = currentVersion.split("\\.");
		int maxLen = (versions1.length > versions2.length ? versions1.length : versions2.length);
		for (int i = 0; i < maxLen; ++i) {
			int v1 = 0, v2 = 0;
			if (versions1.length > i) {
				try { v1 = Integer.parseInt(versions1[i]); }
				catch (NumberFormatException e) {}
			}
			if (versions2.length > i) {
				try { v2 = Integer.parseInt(versions2[i]); }
				catch (NumberFormatException e) {}
			}

			if (v1 > v2) {
				updateNeeded = true;
				break;
			}
			else if (v2 > v1) {
				break;
			}
		}

		if (updateNeeded) {
			this.showUpdateText(checkVersion);
		}
	}
	private final void showUpdateText(String version) {
		for (int i = 0; i < this.updateAvailableLabels.length; ++i) {
			this.updateAvailableLabels[i].setVisible(true);

			if (i >= 2) {
				this.updateAvailableLabels[i].setText("Update Available (" + version + ")");
			}
		}
	}
	private final void updateCheck() {
		final GUI self = this;
		Thread t = new Thread() {
			@Override
			public final void run() {
				final String response = GUI.httpGET("http://dnsev.github.io/ve/changelog.txt");
				if (response != null) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public final void run() {
							self.parseChangeLog(response);
						}
					});
				}
			}
		};
		t.start();
	}
	private static final String httpGET(String stringUrl) {
		URL url;
		HttpURLConnection conn;
		BufferedReader rd;
		StringBuilder result = new StringBuilder();
		int len;
		char[] buffer = new char[256];
		try {
			url = new URL(stringUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((len = rd.read(buffer, 0, buffer.length)) >= 0) {
				result.append(buffer, 0, len);
			}
			rd.close();
		}
		catch (Exception e) {
			return null;
		}
		return result.toString();
	}

	private final void setupTimecodeLabels(JPanel container, JLabel timecodeFirst, JLabel timecodeLast, JTextFieldCustom[] ranges, ChangeListener[] rangeListeners) {
		final GUI self = this;
		JPanel panel;
		JLabel text;
		GridBagConstraints gc = new GridBagConstraints();

		container.add(panel = new JPanel());
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(false);

		gc.weightx = 0.5;
		gc.anchor = GridBagConstraints.LINE_START;
		panel.add(timecodeFirst, gc);
		timecodeFirst.setFont(this.fonts.text);
		timecodeFirst.setForeground(this.colors.textLight2);

		container.add(panel = new JPanel());
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(false);

		//{
			gc.weightx = 0.5;
			gc.gridx = 0;
			gc.gridy = 0;
			gc.gridwidth = 1;
			gc.anchor = GridBagConstraints.LINE_END;
			gc.fill = GridBagConstraints.BOTH;
			panel.add(ranges[0], gc);

			++gc.gridx;
			gc.weightx = 0.0;
			gc.fill = GridBagConstraints.NONE;
			panel.add(text = new JLabel(" - "), gc);
			text.setFont(this.fonts.text);
			text.setForeground(this.colors.text);

			++gc.gridx;
			gc.weightx = 0.5;
			gc.anchor = GridBagConstraints.LINE_START;
			gc.fill = GridBagConstraints.BOTH;
			panel.add(ranges[1], gc);

			for (int i = 0; i < 2; ++i) {
				ranges[i].setFont(this.fonts.text);
				ranges[i].setForeground(this.colors.textAlt1);
				ranges[i].setHorizontalAlignment(i == 0 ? JTextField.RIGHT : JTextField.LEFT);
				ranges[i].setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
				ranges[i].setOpaque(false);
				ranges[i].setPreferredSize(new Dimension(32, 20));
				ranges[i].addCustomChangeListener(rangeListeners[i]);
			}
		//}

		container.add(panel = new JPanel());
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(false);

		gc.gridx = 0;
		gc.weightx = 0.5;
		gc.fill = GridBagConstraints.NONE;
		gc.anchor = GridBagConstraints.LINE_END;
		panel.add(timecodeLast, gc);
		timecodeLast.setFont(this.fonts.text);
		timecodeLast.setForeground(this.colors.textLight2);
	}


	private final void setupTabImage(JPanel p) {
		final GUI self = this;

		JPanel leftPanel, leftPanelInner, titlePanel, rightPanel, rightPanelInner, rightPanelInner2, timePanel, cell;
		JLabel text;
		GridBagConstraints gc = new GridBagConstraints();


		p.setLayout(new BorderLayout());

		p.add((leftPanel = new JPanel()), BorderLayout.CENTER);
		leftPanel.setLayout(new BorderLayout());
		leftPanel.setOpaque(false);

		// Filename
		leftPanel.add((titlePanel = new JPanel()), BorderLayout.PAGE_START);
		titlePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		titlePanel.setBackground(this.colors.text);

		titlePanel.add(this.fileLabelImage = new JLabel(" "));
		this.fileLabelImage.setFont(this.fonts.text);
		this.fileLabelImage.setForeground(this.colors.textLight3);
		this.updateImageFile();

		// Preview image
		this.setupTabImagePreview(leftPanel, this.noimage);

		// Time slider
		leftPanel.add((leftPanelInner = new JPanel()), BorderLayout.PAGE_END);
		leftPanelInner.setLayout(new GridBagLayout());
		leftPanelInner.setOpaque(false);
		this.imagePreviewTimeSliderContainer = leftPanelInner;

		gc.weightx = 0.5;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.CENTER;
		gc.fill = GridBagConstraints.HORIZONTAL;
		this.setupTabImagePreviewTime(leftPanelInner, gc);

		//{ Times
		++gc.gridy;
		gc.anchor = GridBagConstraints.LINE_START;
		gc.fill = GridBagConstraints.HORIZONTAL;

		leftPanelInner.add((timePanel = new JPanel()), gc);
		timePanel.setLayout(new GridLayout(1, 3));
		timePanel.setOpaque(false);

		timePanel.add((cell = new JPanel()));
		cell.setLayout(new GridBagLayout());
		cell.setOpaque(false);
		gc.weightx = 0.5;
		gc.gridx = 0;
		gc.anchor = GridBagConstraints.LINE_START;
		gc.fill = GridBagConstraints.NONE;
		text = new JLabel(Videncode.timeToString(0.0, new int[]{0,2,2,2}));
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.textLight2);
		cell.add(text, gc);
		this.timecodeStart[0] = text;

		timePanel.add((cell = new JPanel()));
		cell.setLayout(new GridBagLayout());
		cell.setOpaque(false);
		gc.weightx = 0.5;
		gc.gridx = 0;
		gc.anchor = GridBagConstraints.CENTER;
		gc.fill = GridBagConstraints.HORIZONTAL;
		this.imagePreviewTimecodeLocation = new JTextFieldCustom();
		this.imagePreviewTimecodeLocation.setFont(this.fonts.text);
		this.imagePreviewTimecodeLocation.setForeground(this.colors.textAlt1);
		this.imagePreviewTimecodeLocation.setHorizontalAlignment(JTextField.CENTER);
		this.imagePreviewTimecodeLocation.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		this.imagePreviewTimecodeLocation.setOpaque(false);
		this.imagePreviewTimecodeLocation.addCustomChangeListener(new ChangeListener() {
			@Override
			public final void stateChanged(ChangeEvent event) {
				self.onImageTimeTextModify((JTextFieldCustom) event.getSource());
			}
		});
		cell.add(this.imagePreviewTimecodeLocation, gc);

		timePanel.add((cell = new JPanel()));
		cell.setLayout(new GridBagLayout());
		cell.setOpaque(false);
		gc.weightx = 0.5;
		gc.gridx = 0;
		gc.anchor = GridBagConstraints.LINE_END;
		gc.fill = GridBagConstraints.NONE;
		text = new JLabel();
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.textLight2);
		cell.add(text, gc);
		this.timecodeEnd[0] = text;
		//}

		// Image attributes
		p.add((rightPanel = new JPanel()), BorderLayout.LINE_END);
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.setOpaque(false);
		rightPanel.setPreferredSize(new Dimension(320, 1));

		TitledBorder b = BorderFactory.createTitledBorder(null, "Image", TitledBorder.CENTER, TitledBorder.TOP, this.fonts.text, this.colors.text);
		b.setBorder(BorderFactory.createLineBorder(this.colors.backgroundDark));

		rightPanel.add((rightPanelInner = new JPanel()));
		rightPanelInner.setLayout(new BorderLayout());
		rightPanelInner.setOpaque(false);
		rightPanelInner.setBorder(b);

		rightPanelInner.add((rightPanelInner2 = new JPanel()), BorderLayout.PAGE_START);
		rightPanelInner2.setLayout(new GridBagLayout());
		rightPanelInner2.setOpaque(false);

		this.setupTabImageAttributes(rightPanelInner2);
	}
	private final void setupTabImagePreview(JPanel container, Image image) {
		JPanel panel = new JPanel();
		container.add(panel, BorderLayout.CENTER);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setOpaque(false);

		this.imagePreview = new JImage(image);
		this.imagePreview.setBackground(this.colors.text);
		this.imagePreview.setPreferredSize(new Dimension(256, 320));
		panel.add(this.imagePreview);
	}
	private final void setupTabImagePreviewTime(JPanel container, GridBagConstraints gc) {
		this.imagePreviewTimeSlider = new JBarSlider(0.0, this.videncode.getVideoFileSourceDuration(), this.videncode.getImageFileSourceVideoTime());
		container.add(this.imagePreviewTimeSlider, gc);
		this.imagePreviewTimeSlider.setEnabled(true);
		this.imagePreviewTimeSlider.setPreferredSize(new Dimension(1280, 24));
		this.setBarSliderColors(this.imagePreviewTimeSlider);

		// Listener
		final GUI self = this;
		this.imagePreviewTimeSlider.addChangeListener(new ChangeListener(){
			@Override
			public final void stateChanged(ChangeEvent event) {
				self.onImageTimeUpdate((JBarSlider.ChangeEvent) event);
			}
		});
	}
	private final void setupTabImageAttributes(JPanel container) {
		final GUI self = this;

		JPanel pad, flow;
		JLabel text;
		JLine line;
		JButton button;
		GridBagConstraints gc = new GridBagConstraints();
		ActionListener listener;

		//{ Image size
		gc.weightx = 0.5;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.LINE_END;
		gc.fill = GridBagConstraints.NONE;
		container.add(text = new JLabel("File size"), gc);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++gc.gridx;
		gc.weightx = 0.0;
		container.add(pad = new JPanel(), gc);
		pad.setPreferredSize(new Dimension(10, 0));

		++gc.gridx;
		gc.weightx = 0.5;
		gc.anchor = GridBagConstraints.LINE_START;
		container.add(text = new JLabel(), gc);
		text.setFont(this.fonts.textBold);
		text.setForeground(this.colors.text);
		this.imagePreviewFileSizeDisplay = text;
		//}

		//{ Image true size
		gc.weightx = 0.5;
		gc.gridx = 0;
		++gc.gridy;
		gc.anchor = GridBagConstraints.LINE_END;
		gc.fill = GridBagConstraints.NONE;
		container.add(text = new JLabel("True size"), gc);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++gc.gridx;
		gc.weightx = 0.0;
		container.add(pad = new JPanel(), gc);
		pad.setPreferredSize(new Dimension(10, 0));

		++gc.gridx;
		gc.weightx = 0.5;
		gc.anchor = GridBagConstraints.LINE_START;
		container.add(text = new JLabel(), gc);
		text.setFont(this.fonts.textBold);
		text.setForeground(this.colors.text);
		this.imagePreviewFileSizeTrueDisplay = text;
		//}

		//{ Image dimensions
		gc.weightx = 0.5;
		gc.gridx = 0;
		++gc.gridy;
		gc.anchor = GridBagConstraints.LINE_END;
		gc.fill = GridBagConstraints.NONE;
		container.add(text = new JLabel("Dimensions"), gc);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++gc.gridx;
		gc.weightx = 0.0;
		container.add(pad = new JPanel(), gc);
		pad.setPreferredSize(new Dimension(10, 0));

		++gc.gridx;
		gc.weightx = 0.5;
		gc.anchor = GridBagConstraints.LINE_START;
		container.add(text = new JLabel(), gc);
		text.setFont(this.fonts.textBold);
		text.setForeground(this.colors.text);
		this.imagePreviewDimensionsDisplay = text;
		//}

		//{ Separator
		gc.weightx = 0.5;
		gc.gridx = 0;
		++gc.gridy;
		gc.gridwidth = 3;
		gc.anchor = GridBagConstraints.LINE_END;
		gc.fill = GridBagConstraints.HORIZONTAL;

		line = new JLine();
		container.add(line, gc);
		line.setPreferredSize(new Dimension(16, 16));
		line.getColors().borderLight = this.colors.background;
		line.getColors().borderDark = this.colors.backgroundDark;
		//}

		//{ Available space
		gc.weightx = 0.5;
		gc.gridx = 0;
		++gc.gridy;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.LINE_END;
		gc.fill = GridBagConstraints.NONE;
		container.add(text = new JLabel("Available"), gc);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++gc.gridx;
		gc.weightx = 0.0;
		container.add(pad = new JPanel(), gc);
		pad.setPreferredSize(new Dimension(10, 0));

		++gc.gridx;
		gc.weightx = 0.5;
		gc.anchor = GridBagConstraints.LINE_START;
		container.add(text = new JLabel(), gc);
		text.setFont(this.fonts.textBold);
		text.setForeground(this.colors.text);
		this.imagePreviewAvailableSpaceDisplay = text;
		//}

		//{ Available true space
		gc.weightx = 0.5;
		gc.gridx = 0;
		++gc.gridy;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.LINE_END;
		gc.fill = GridBagConstraints.NONE;
		container.add(text = new JLabel(" "), gc);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++gc.gridx;
		gc.weightx = 0.0;
		container.add(pad = new JPanel(), gc);
		pad.setPreferredSize(new Dimension(10, 0));

		++gc.gridx;
		gc.weightx = 0.5;
		gc.anchor = GridBagConstraints.LINE_START;
		container.add(text = new JLabel(), gc);
		text.setFont(this.fonts.textBold);
		text.setForeground(this.colors.text);
		this.imagePreviewAvailableSpaceTrueDisplay = text;
		//}

		//{ Separator
		gc.weightx = 0.5;
		gc.gridx = 0;
		++gc.gridy;
		gc.gridwidth = 3;
		gc.anchor = GridBagConstraints.LINE_END;
		gc.fill = GridBagConstraints.HORIZONTAL;

		line = new JLine();
		container.add(line, gc);
		line.setPreferredSize(new Dimension(16, 16));
		line.getColors().borderLight = this.colors.background;
		line.getColors().borderDark = this.colors.backgroundDark;
		//}

		//{ Output quality
		gc.weightx = 0.5;
		gc.gridx = 0;
		++gc.gridy;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.LINE_END;
		gc.fill = GridBagConstraints.NONE;
		container.add(text = new JLabel("Quality"), gc);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++gc.gridx;
		gc.weightx = 0.0;
		container.add(pad = new JPanel(), gc);
		pad.setPreferredSize(new Dimension(10, 0));

		++gc.gridx;
		gc.weightx = 0.5;
		gc.anchor = GridBagConstraints.LINE_START;
		gc.fill = GridBagConstraints.HORIZONTAL;
		this.imageQualityComboBox = new JComboBox<ImageQuality>();
		container.add(this.imageQualityComboBox, gc);
		this.imageQualityComboBox.setFont(this.fonts.textSmall);
		this.imageQualityComboBox.setForeground(this.colors.text);
		this.imageQualityComboBox.setOpaque(false);

		// Listener
		listener = new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				self.onImageQualityChange((ImageQuality) ((JComboBox) event.getSource()).getSelectedItem());
			}
		};
		this.imageQualityComboBox.addActionListener(listener);
		//}

		//{ Output scale
		gc.weightx = 0.5;
		gc.gridx = 0;
		++gc.gridy;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.LINE_END;
		gc.fill = GridBagConstraints.NONE;
		container.add(text = new JLabel("Scale"), gc);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++gc.gridx;
		gc.weightx = 0.0;
		container.add(pad = new JPanel(), gc);
		pad.setPreferredSize(new Dimension(10, 0));

		++gc.gridx;
		gc.weightx = 0.5;
		gc.anchor = GridBagConstraints.LINE_START;
		gc.fill = GridBagConstraints.HORIZONTAL;
		this.imageScaleComboBox = new JComboBox<ImageScale>();
		container.add(this.imageScaleComboBox, gc);
		this.imageScaleComboBox.setEditable(true);
		this.imageScaleComboBox.setFont(this.fonts.textSmall);
		this.imageScaleComboBox.setForeground(this.colors.text);
		this.imageScaleComboBox.setOpaque(false);

		// Listener
		listener = new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				try {
					self.onImageScaleChange((ImageScale) ((JComboBox) event.getSource()).getSelectedItem());
				}
				catch (ClassCastException e) {
					self.onImageScaleChange(new ImageScale((String) ((JComboBox) event.getSource()).getSelectedItem()));
				}
			}
		};
		this.imageScaleComboBox.addActionListener(listener);
		//}

		//{ Separator
		gc.weightx = 0.5;
		gc.gridx = 0;
		++gc.gridy;
		gc.gridwidth = 3;
		gc.anchor = GridBagConstraints.LINE_END;
		gc.fill = GridBagConstraints.HORIZONTAL;

		line = new JLine();
		container.add(line, gc);
		line.setPreferredSize(new Dimension(16, 16));
		line.getColors().borderLight = this.colors.background;
		line.getColors().borderDark = this.colors.backgroundDark;
		//}

		//{ Source
		// Listeners
		ActionListener a1 = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event) {
				self.setImageSourceState(Integer.parseInt(event.getActionCommand()) == 1, true);
			}
		};
		ActionListener a2 = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event) {
				self.setImageSourceState(true, true);
			}
		};

		this.imagePreviewSourceGroup = new ButtonGroup();

		gc.weightx = 0.5;
		gc.gridx = 0;
		++gc.gridy;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.LINE_END;
		gc.fill = GridBagConstraints.NONE;
		container.add(this.imageSourceCheckbox = new JCheckBox("Source"), gc);
		this.imageSourceCheckbox.setFont(this.fonts.text);
		this.imageSourceCheckbox.setForeground(this.colors.text);
		this.imageSourceCheckbox.setOpaque(false);
		this.imageSourceCheckbox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		this.imageSourceCheckbox.addActionListener(new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				self.onImageSourceCheckboxChange((JCheckBox) event.getSource(), event);
			}
		});

		++gc.gridx;
		gc.weightx = 0.0;
		container.add(pad = new JPanel(), gc);
		pad.setPreferredSize(new Dimension(10, 0));

		++gc.gridx;
		gc.weightx = 0.5;
		gc.anchor = GridBagConstraints.LINE_START;

		container.add(this.imagePreviewSourceButtons[0] = new JRadioButton(" Video"), gc);
		this.imagePreviewSourceGroup.add(this.imagePreviewSourceButtons[0]);
		if (this.videncode.isImageFromVideo()) {
			this.imagePreviewSourceGroup.setSelected(this.imagePreviewSourceButtons[0].getModel(), true);
		}
		this.imagePreviewSourceButtons[0].setFont(this.fonts.text);
		this.imagePreviewSourceButtons[0].setForeground(this.colors.text);
		this.imagePreviewSourceButtons[0].setToolTipText("Use a frame from the video as the preview");
		this.imagePreviewSourceButtons[0].setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 4));
		this.imagePreviewSourceButtons[0].setActionCommand(Integer.valueOf(0).toString());
		this.imagePreviewSourceButtons[0].addActionListener(a1);
		this.imagePreviewSourceButtons[0].setOpaque(false);

		++gc.gridy;
		container.add((flow = new JPanel()), gc);
		flow.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		flow.setOpaque(false);

		flow.add(this.imagePreviewSourceButtons[1] = new JRadioButton(""), gc);
		this.imagePreviewSourceGroup.add(this.imagePreviewSourceButtons[1]);
		if (!this.videncode.isImageFromVideo()) {
			this.imagePreviewSourceGroup.setSelected(this.imagePreviewSourceButtons[1].getModel(), true);
		}
		this.imagePreviewSourceButtons[1].setFont(this.fonts.text);
		this.imagePreviewSourceButtons[1].setForeground(this.colors.text);
		this.imagePreviewSourceButtons[1].setToolTipText("Use an external image as the preview");
		this.imagePreviewSourceButtons[1].setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 4));
		this.imagePreviewSourceButtons[1].setActionCommand(Integer.valueOf(1).toString());
		this.imagePreviewSourceButtons[1].addActionListener(a1);
		this.imagePreviewSourceButtons[1].setOpaque(false);

		flow.add(button = new JButton("External image"), gc);
		button.setFont(this.fonts.textSmall);
		button.setForeground(this.colors.text);
		button.setToolTipText("Search for an external image");
		button.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
		button.addActionListener(a2);
		button.setOpaque(false);
		//}
	}


	private final void setupTabVideo(JPanel p) {
		JPanel p1, p2, titlePanel, timePanel;
		JLabel t1, text;
		GridBagConstraints gc = new GridBagConstraints();

		// Layout two sections
		p.setLayout(new GridLayout(1,2));


		//{ Video
		TitledBorder b = BorderFactory.createTitledBorder(null, "Video", TitledBorder.CENTER, TitledBorder.TOP, this.fonts.text, this.colors.text);
		b.setBorder(BorderFactory.createLineBorder(this.colors.backgroundDark));

		p.add((p1 = new JPanel()));
		p1.setLayout(new BorderLayout());
		p1.setOpaque(false);
		p1.setBorder(b);

		p1.add((p2 = new JPanel()), BorderLayout.PAGE_END);
		p2.setLayout(new GridBagLayout());
		p2.setOpaque(false);


		// Filename
		p1.add((titlePanel = new JPanel()), BorderLayout.PAGE_START);
		titlePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		titlePanel.setBackground(this.colors.text);

		titlePanel.add(this.fileLabelVideo = new JLabel(" "));
		this.fileLabelVideo.setFont(this.fonts.text);
		this.fileLabelVideo.setForeground(this.colors.textLight3);

		//{ Preview
		this.setupTabVideoPreview(p1, this.noimage);
		//}

		//{ Range
		gc.weightx = 0.5;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 3;
		gc.anchor = GridBagConstraints.CENTER;
		gc.fill = GridBagConstraints.HORIZONTAL;

		this.setupTabVideoRange(p2, gc);
		//}

		//{ Times
		gc.gridwidth = 3;
		gc.weightx = 0.5;
		gc.gridx = 0;
		++gc.gridy;
		gc.anchor = GridBagConstraints.LINE_START;
		gc.fill = GridBagConstraints.HORIZONTAL;
		p2.add((timePanel = new JPanel()), gc);
		timePanel.setLayout(new GridLayout(1, 3));
		timePanel.setOpaque(false);

		final GUI self = this;
		this.setupTimecodeLabels(
			timePanel,
			this.timecodeStart[1] = new JLabel(Videncode.timeToString(0.0, new int[]{0,2,2,2})),
			this.timecodeEnd[1] = new JLabel(),
			new JTextFieldCustom[]{
				this.videoEncodingDurationTimecode[0] = new JTextFieldCustom(),
				this.videoEncodingDurationTimecode[1] = new JTextFieldCustom()
			},
			new ChangeListener[]{
				new ChangeListener() {
					@Override
					public final void stateChanged(ChangeEvent event) {
						self.onVideoEncodeDurationTextModify(0, (JTextFieldCustom) event.getSource());
					}
				},
				new ChangeListener() {
					@Override
					public final void stateChanged(ChangeEvent event) {
						self.onVideoEncodeDurationTextModify(1, (JTextFieldCustom) event.getSource());
					}
				}
			}
		);
		//}

		//{ Video stats
		gc.weightx = 0.01;
		gc.gridx = 0;
		++gc.gridy;
		gc.gridwidth = 2;
		gc.anchor = GridBagConstraints.PAGE_START;
		gc.fill = GridBagConstraints.BOTH;

		this.setupTabVideoData(p2, gc);
		//}

		//{ What image to preview
		gc.weightx = 0.5;
		gc.gridx += 2;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.FIRST_LINE_END;
		gc.fill = GridBagConstraints.BOTH;

		this.setupTabVideoPreviewFrameSelect(p2, gc);
		//}

		//{ Output settings
		gc.weightx = 0.5;
		gc.gridx = 0;
		++gc.gridy;
		gc.gridwidth = 3;
		gc.anchor = GridBagConstraints.PAGE_START;
		gc.fill = GridBagConstraints.BOTH;

		this.setupTabVideoOutputSettings(p2, gc);
		//}

		//}

		//{ Audio
		b = BorderFactory.createTitledBorder(null, "Audio", TitledBorder.CENTER, TitledBorder.TOP, this.fonts.text, this.colors.text);
		b.setBorder(BorderFactory.createLineBorder(this.colors.backgroundDark));

		p.add((p1 = new JPanel()));
		p1.setLayout(new BorderLayout());
		p1.setOpaque(false);
		p1.setBorder(b);

		this.setupTabAudio(p1);
		//}
	}
	private final void setupTabVideoPreview(JPanel container, Image image) {
		JPanel panel = new JPanel();
		container.add(panel, BorderLayout.CENTER);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setOpaque(false);

		this.videoImagePreview = new JImage(image);
		this.videoImagePreview.setBackground(this.colors.text);
		this.videoImagePreview.setPreferredSize(new Dimension(256, 320));
		panel.add(this.videoImagePreview);
	}
	private final void setupTabVideoRange(JPanel container, GridBagConstraints gc) {
		// Slider
		double[] ed = new double[]{ 0.0 , 0.0 };
		this.videoEncodingDurationRangeslider = new JRangeSlider(0.0, 0.0, ed[0], ed[1]);
		container.add(this.videoEncodingDurationRangeslider, gc);
		this.videoEncodingDurationRangeslider.setEnabled(true);
		this.videoEncodingDurationRangeslider.setPreferredSize(new Dimension(1280, 24));
		this.setRangeSliderColors(this.videoEncodingDurationRangeslider);

		// Listener
		final GUI self = this;
		this.videoEncodingDurationRangeslider.addChangeListener(new ChangeListener(){
			@Override
			public final void stateChanged(ChangeEvent event) {
				self.onVideoEncodeDurationUpdate((JRangeSlider.ChangeEvent) event);
			}
		});
	}
	private final void setupTabVideoSeparator(JPanel container, GridBagConstraints gc) {
		JLine line = new JLine();
		container.add(line, gc);
		line.setPreferredSize(new Dimension(16, 16));
		line.getColors().borderLight = this.colors.textLight1;
		line.getColors().borderDark = this.colors.text;
	}
	private final void setupTabVideoData(JPanel container, GridBagConstraints gc) {
		final GUI self = this;

		JPanel panel, gridPanel, leftPanel, rightPanel, pad, statusPanel;
		JLabel text;
		JButton button;

		// Create the panel
		TitledBorder b = BorderFactory.createTitledBorder(null, "Video Data", TitledBorder.LEFT, TitledBorder.TOP, this.fonts.text, this.colors.text);
		b.setBorder(BorderFactory.createLineBorder(this.colors.backgroundDark));

		container.add(gridPanel = new JPanel(), gc);
		gridPanel.setLayout(new GridLayout(1, 2));
		gridPanel.setOpaque(false);
		gridPanel.setBorder(b);

		gridPanel.add((panel = new JPanel()));
		panel.setLayout(new BorderLayout());
		panel.setOpaque(false);

		panel.add((leftPanel = new JPanel()), BorderLayout.PAGE_START);
		leftPanel.setLayout(new GridBagLayout());
		leftPanel.setOpaque(false);

		gridPanel.add((panel = new JPanel()));
		panel.setLayout(new BorderLayout());
		panel.setOpaque(false);

		panel.add((rightPanel = new JPanel()), BorderLayout.PAGE_START);
		rightPanel.setLayout(new GridBagLayout());
		rightPanel.setOpaque(false);


		// Location
		GridBagConstraints c = new GridBagConstraints();

		//{ Resolution
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		leftPanel.add(text = new JLabel("Resolution"), c);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++c.gridx;
		c.weightx = 0.0;
		leftPanel.add(pad = new JPanel(), c);
		pad.setPreferredSize(new Dimension(10, 0));

		++c.gridx;
		c.weightx = 0.5;
		c.anchor = GridBagConstraints.LINE_START;
		leftPanel.add(text = new JLabel(), c);
		text.setFont(this.fonts.textBold);
		text.setForeground(this.colors.text);
		this.videoResolutionDisplay = text;
		//}

		//{ Frame rate
		c.weightx = 0.5;
		c.gridx = 0;
		++c.gridy;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		leftPanel.add(text = new JLabel("Frame rate"), c);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++c.gridx;
		c.weightx = 0.0;
		leftPanel.add(pad = new JPanel(), c);
		pad.setPreferredSize(new Dimension(10, 0));

		++c.gridx;
		c.weightx = 0.5;
		c.anchor = GridBagConstraints.LINE_START;
		leftPanel.add(text = new JLabel(), c);
		text.setFont(this.fonts.textBold);
		text.setForeground(this.colors.text);
		this.videoFrameRateDisplay = text;
		//}


		//{ Source
		ActionListener a = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event) {
				self.loadVideoFromFile();
			}
		};


		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		rightPanel.add(this.videoSourceCheckbox = new JCheckBox("Source"), c);
		this.videoSourceCheckbox.setFont(this.fonts.text);
		this.videoSourceCheckbox.setForeground(this.colors.text);
		this.videoSourceCheckbox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		this.videoSourceCheckbox.addActionListener(new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				self.onVideoSourceCheckboxChange((JCheckBox) event.getSource(), event);
			}
		});

		++c.gridx;
		c.weightx = 0.0;
		rightPanel.add(pad = new JPanel(), c);
		pad.setPreferredSize(new Dimension(10, 0));

		++c.gridx;
		c.weightx = 0.5;
		c.anchor = GridBagConstraints.LINE_START;
		rightPanel.add(button = new JButton("Browse..."), c);
		button.setFont(this.fonts.textSmall);
		button.setForeground(this.colors.text);
		button.setToolTipText("Load a video file");
		button.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
		button.addActionListener(a);
		button.setOpaque(false);
		//}

		//{ Encode state
		c.weightx = 0.5;
		c.gridx = 0;
		++c.gridy;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		rightPanel.add(text = new JLabel("Status"), c);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++c.gridx;
		c.weightx = 0.0;
		rightPanel.add(pad = new JPanel(), c);
		pad.setPreferredSize(new Dimension(10, 0));

		++c.gridx;
		c.weightx = 0.5;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;

		rightPanel.add(statusPanel = new JPanel(), c);
		statusPanel.setLayout(new GridBagLayout());
		statusPanel.setOpaque(false);

		GridBagConstraints c2 = new GridBagConstraints();
		c2.weightx = 0.5;
		c2.gridx = 0;
		c2.gridy = 0;
		c2.gridwidth = 1;
		c2.anchor = GridBagConstraints.LINE_START;
		c2.fill = GridBagConstraints.VERTICAL;

		statusPanel.add(this.videoTempEncodeStatusText = new JLabel("Encoded"), c2);
		this.videoTempEncodeStatusText.setFont(this.fonts.textBold);
		this.videoTempEncodeStatusText.setForeground(this.colors.text);

		statusPanel.add(this.videoTempEncodeActivateButton = new JButton("Not encoded"), c2);
		this.videoTempEncodeActivateButton.setFont(this.fonts.textSmall);
		this.videoTempEncodeActivateButton.setForeground(this.colors.text);
		this.videoTempEncodeActivateButton.setToolTipText("Search for an external video file");
		this.videoTempEncodeActivateButton.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
		this.videoTempEncodeActivateButton.setOpaque(false);
		this.videoTempEncodeActivateButton.addActionListener(new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				self.onVideoEncodeButtonPress();
			}
		});

		statusPanel.add(this.videoTempEncodeProgressBar = new JProgressBarCustom(), c2);
		this.videoTempEncodeProgressBar.setFont(this.fonts.textSmall);
		this.videoTempEncodeProgressBar.setForeground(this.colors.text);
		this.videoTempEncodeProgressBar.setPreferredSize(new Dimension(128, 16));
		this.videoTempEncodeProgressBar.setEnabled(true);
		this.videoTempEncodeProgressBar.getColors().borderDark[0] = this.colors.backgroundDarkest;
		this.videoTempEncodeProgressBar.getColors().borderLight[0] = this.colors.background;
		this.videoTempEncodeProgressBar.getColors().background[0] = this.colors.textLight1;
		this.videoTempEncodeProgressBar.getColors().backgroundLoaded[0] = this.colors.textLight2;
		this.videoTempEncodeProgressBar.getColors().text[0] = this.colors.text;

		this.videoTempEncodeStatusText.setVisible(false);
		this.videoTempEncodeActivateButton.setVisible(true);
		this.videoTempEncodeProgressBar.setVisible(false);
		//}
	}
	private final void setupTabVideoPreviewFrameSelect(JPanel container, GridBagConstraints gc) {
		final GUI self = this;

		JPanel panel = new JPanel();
		this.videoImagePreviewButtonGroup = new ButtonGroup();

		// Create the panel
		TitledBorder b = BorderFactory.createTitledBorder(null, "Preview Frame", TitledBorder.RIGHT, TitledBorder.TOP, this.fonts.text, this.colors.text);
		b.setBorder(BorderFactory.createLineBorder(this.colors.backgroundDark));

		container.add(panel, gc);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setOpaque(false);
		panel.setBorder(b);
		panel.setPreferredSize(new Dimension(128, 0));

		// Listener
		ActionListener a = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event) {
				self.updateVideoPreviewIndex(Integer.parseInt(event.getActionCommand()), false);
			}
		};

		// Buttons
		String[] labels = new String[]{ "First " , "Last " };
		String[] tooltips = new String[]{ "Display the first frame to be encoded in the preview" , "Display the last frame to be encoded in the preview" };
		for (int i = 0; i < 2; ++i) {
			panel.add(this.videoImagePreviewButtons[i] = new JRadioButton(labels[i]));
			this.videoImagePreviewButtonGroup.add(this.videoImagePreviewButtons[i]);
			if (i == this.videoImagePreviewId) this.videoImagePreviewButtonGroup.setSelected(this.videoImagePreviewButtons[i].getModel(), true);
			this.videoImagePreviewButtons[i].setActionCommand(Integer.valueOf(i).toString());
			this.videoImagePreviewButtons[i].setFont(this.fonts.text);
			this.videoImagePreviewButtons[i].setForeground(this.colors.text);
			this.videoImagePreviewButtons[i].setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			this.videoImagePreviewButtons[i].setAlignmentX(Component.RIGHT_ALIGNMENT);
			this.videoImagePreviewButtons[i].setToolTipText(tooltips[i]);
			this.videoImagePreviewButtons[i].addActionListener(a);
			this.videoImagePreviewButtons[i].setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 4));
			this.videoImagePreviewButtons[i].setOpaque(false);
		}
	}
	private final void setupTabVideoOutputSettings(JPanel container, GridBagConstraints gc) {
		JPanel panel = new JPanel();
		JPanel qPanel;
		ButtonGroup group = new ButtonGroup();
		GridBagConstraints c = new GridBagConstraints();
		JLabel label;
		JCheckBox checkBox;

		// Create the panel
		TitledBorder b = BorderFactory.createTitledBorder(null, "Encoding Settings", TitledBorder.CENTER, TitledBorder.TOP, this.fonts.text, this.colors.text);
		b.setBorder(BorderFactory.createLineBorder(this.colors.backgroundDark));

		container.add(panel, gc);
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(false);
		panel.setBorder(b);

		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		//{ Min quality
		// Create the panel
		panel.add(qPanel = new JPanel(), c);
		qPanel.setLayout(new BoxLayout(qPanel, BoxLayout.Y_AXIS));
		qPanel.setOpaque(false);

		// Label
		qPanel.add(label = new JLabel("Encoding Mode"));
		label.setFont(this.fonts.text);
		label.setForeground(this.colors.text);
		label.setAlignmentX(Component.LEFT_ALIGNMENT);

		// Listener
		final GUI self = this;
		ActionListener a = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event) {
				self.onVideoEncodingModeChange(Integer.parseInt(event.getActionCommand()));
			}
		};
		for (int i = 0; i < this.videoEncodeModeButtons.length; ++i) {
			qPanel.add(this.videoEncodeModeButtons[i] = new JRadioButton(" " + this.videncode.getVideoEncodingProfiles()[i]));
			group.add(this.videoEncodeModeButtons[i]);
			this.videoEncodeModeButtons[i].setFont(this.fonts.text);
			this.videoEncodeModeButtons[i].setForeground(this.colors.text);
			this.videoEncodeModeButtons[i].setToolTipText(this.videncode.getVideoEncodingProfileDescriptions()[i]);
			this.videoEncodeModeButtons[i].setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 4));
			this.videoEncodeModeButtons[i].setOpaque(false);
			this.videoEncodeModeButtons[i].setActionCommand(Integer.valueOf(i).toString());
			this.videoEncodeModeButtons[i].addActionListener(a);
		}
		group.setSelected(this.videoEncodeModeButtons[this.videncode.getVideoFileTempQuality()].getModel(), true);
		//}

		//{ Baseline
		c.weightx = 0.5;
		++c.gridx;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.FIRST_LINE_END;
		c.fill = GridBagConstraints.HORIZONTAL;
		this.setupTabVideoOutputSettingsBaseline(panel, c);
		//}

		//{ Quality detection
		c.weightx = 0.5;
		++c.gridx;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.FIRST_LINE_END;
		c.fill = GridBagConstraints.HORIZONTAL;
		this.setupTabVideoOutputSettingsQualityDetection(panel, c);
		//}
	}
	private final void setupTabVideoOutputSettingsBaseline(JPanel container, GridBagConstraints gc) {
		final GUI self = this;

		JPanel panel, flowPanel, pad;
		JLabel label;
		GridBagConstraints c;

		// Create the panel
		container.add(panel = new JPanel(), gc);
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(false);

		c = new GridBagConstraints();
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		c.anchor = GridBagConstraints.PAGE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		//{ Label
		panel.add(flowPanel = new JPanel(), c);
		flowPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		flowPanel.setOpaque(false);

		flowPanel.add(label = new JLabel("Baseline ("));
		label.setFont(this.fonts.text);
		label.setForeground(this.colors.text);

		flowPanel.add(this.videoEncodeAutoQualitySelect = new JCheckBox("auto"));
		this.videoEncodeAutoQualitySelect.setFont(this.fonts.text);
		this.videoEncodeAutoQualitySelect.setForeground(this.colors.text);
		this.videoEncodeAutoQualitySelect.setToolTipText("Let the encoder attempt to auto-detect some decent settings while you change other stuff");
		this.videoEncodeAutoQualitySelect.setSelected(this.videoEncodeAutoQualityEnabled);
		this.videoEncodeAutoQualitySelect.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 4));
		this.videoEncodeAutoQualitySelect.setOpaque(false);
		// Listener
		this.videoEncodeAutoQualitySelect.addActionListener(new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				self.onVideoAutoQualityCheckboxChange((JCheckBox) event.getSource());
			}
		});

		flowPanel.add(label = new JLabel(")"));
		label.setFont(this.fonts.text);
		label.setForeground(this.colors.text);
		//}

		//{ Resolution combo box
		++c.gridy;
		c.gridwidth = 1;
		c.weightx = 0.0;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		panel.add(label = new JLabel("Resolution"), c);
		label.setFont(this.fonts.text);
		label.setForeground(this.colors.text);

		++c.gridx;
		c.weightx = 0.0;
		panel.add(pad = new JPanel(), c);
		pad.setPreferredSize(new Dimension(10, 0));

		c.weightx = 0.5;
		++c.gridx;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(this.videoBaselineResolutionComboBox = new JComboBox<ImageScale>(), c);
		this.videoBaselineResolutionComboBox.setEditable(true);
		this.videoBaselineResolutionComboBox.setFont(this.fonts.textSmall);
		this.videoBaselineResolutionComboBox.setForeground(this.colors.text);
		this.videoBaselineResolutionComboBox.setOpaque(false);

		// Listener
		this.videoBaselineResolutionComboBox.addActionListener(new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				try {
					self.onVideoBaselineResolutionChange((ImageScale) ((JComboBox) event.getSource()).getSelectedItem());
				}
				catch (ClassCastException e) {
					self.onVideoBaselineResolutionChange(new ImageScale((String) ((JComboBox) event.getSource()).getSelectedItem()));
				}
			}
		});
		//}

		//{ Framerate combo box
		c.gridx = 0;
		++c.gridy;
		c.gridwidth = 1;
		c.weightx = 0.0;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		panel.add(label = new JLabel("Frame rate"), c);
		label.setFont(this.fonts.text);
		label.setForeground(this.colors.text);

		++c.gridx;
		c.weightx = 0.0;
		panel.add(pad = new JPanel(), c);
		pad.setPreferredSize(new Dimension(10, 0));

		c.weightx = 0.5;
		++c.gridx;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(this.videoBaselineFrameRateComboBox = new JComboBox<Videncode.FrameRate>(), c);
		this.videoBaselineFrameRateComboBox.setEditable(true);
		this.videoBaselineFrameRateComboBox.setFont(this.fonts.textSmall);
		this.videoBaselineFrameRateComboBox.setForeground(this.colors.text);
		this.videoBaselineFrameRateComboBox.setOpaque(false);
		// Listener
		this.videoBaselineFrameRateComboBox.addActionListener(new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				try {
					self.onVideoBaselineFrameRateChange((Videncode.FrameRate) ((JComboBox) event.getSource()).getSelectedItem());
				}
				catch (ClassCastException e) {
					self.onVideoBaselineFrameRateChange(Videncode.FrameRate.create((String) ((JComboBox) event.getSource()).getSelectedItem()));
				}
			}
		});
		//}

		//{ Bitrate
		c.gridx = 0;
		++c.gridy;
		c.gridwidth = 1;
		c.weightx = 0.0;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		panel.add(label = new JLabel("Bitrate"), c);
		label.setFont(this.fonts.text);
		label.setForeground(this.colors.text);

		++c.gridx;
		c.weightx = 0.0;
		panel.add(pad = new JPanel(), c);
		pad.setPreferredSize(new Dimension(10, 0));

		c.weightx = 0.5;
		++c.gridx;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(flowPanel = new JPanel(), c);
		flowPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		flowPanel.setOpaque(false);

		flowPanel.add(this.videoBitrateDisplay = new JLabel());
		this.videoBitrateDisplay.setFont(this.fonts.textBold);
		this.videoBitrateDisplay.setForeground(this.colors.text);
		//}
	}
	private final void setupTabVideoOutputSettingsQualityDetection(JPanel container, GridBagConstraints gc) {
		final GUI self = this;

		JPanel panel, flowPanel;
		JCheckBox checkBox;
		JLabel label;
		GridBagConstraints c;

		// Create the panel
		container.add(panel = new JPanel(), gc);
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(false);

		c = new GridBagConstraints();
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.PAGE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		//{ Label
		panel.add(flowPanel = new JPanel(), c);
		flowPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		flowPanel.setOpaque(false);
		flowPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		flowPanel.add(label = new JLabel("Auto Quality"));
		label.setFont(this.fonts.text);
		label.setForeground(this.colors.text);
		//}

		//{ Quality combo box
		++c.gridy;
		c.gridwidth = 1;
		c.weightx = 0.0;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		panel.add(this.videoAutoQualityProfileComboBox = new JComboBox<Videncode.AutoQualityProfile>(), c);
		this.videoAutoQualityProfileComboBox.setFont(this.fonts.textSmall);
		this.videoAutoQualityProfileComboBox.setForeground(this.colors.text);
		this.videoAutoQualityProfileComboBox.setOpaque(false);

		// Listener
		this.videoAutoQualityProfileComboBox.addActionListener(new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				self.onVideoAutoQualityProfileChange((Videncode.AutoQualityProfile) ((JComboBox) event.getSource()).getSelectedItem());
			}
		});
		//}

		//{ File size
		++c.gridy;
		c.gridwidth = 1;
		c.weightx = 0.0;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		panel.add(label = new JLabel("File Size"), c);
		label.setFont(this.fonts.text);
		label.setForeground(this.colors.text);

		++c.gridy;
		c.gridwidth = 1;
		c.weightx = 0.0;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		panel.add(this.videoTempFileSizeLabel = new JLabel(), c);
		this.videoTempFileSizeLabel.setFont(this.fonts.textBold);
		this.videoTempFileSizeLabel.setForeground(this.colors.text);
		//}

	}


	private final void setupTabAudio(JPanel container) {
		JPanel titlePanel, panel, gPanel;
		GridBagConstraints gc = new GridBagConstraints();

		// Panel
		container.add((panel = new JPanel()), BorderLayout.PAGE_START);
		panel.setLayout(new BorderLayout());
		panel.setOpaque(false);

		//{ Filename
		panel.add((titlePanel = new JPanel()), BorderLayout.PAGE_START);
		titlePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		titlePanel.setBackground(this.colors.text);

		titlePanel.add(this.fileLabelAudio = new JLabel(" "));
		this.fileLabelAudio.setFont(this.fonts.text);
		this.fileLabelAudio.setForeground(this.colors.textLight3);
		//}

		// Possibly add a waveform here later

		// Panel
		panel.add((gPanel = new JPanel()), BorderLayout.PAGE_END);
		gPanel.setLayout(new GridBagLayout());
		gPanel.setOpaque(false);

		//{ Range
		gc.weightx = 0.5;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.CENTER;
		gc.fill = GridBagConstraints.HORIZONTAL;
		this.setupTabAudioRange(gPanel, gc);
		//}

		//{ Times
		++gc.gridy;
		gc.anchor = GridBagConstraints.CENTER;
		gc.fill = GridBagConstraints.HORIZONTAL;
		this.setupTabAudioTimes(gPanel, gc);
		//}

		//{ Stats
		++gc.gridy;
		gc.anchor = GridBagConstraints.CENTER;
		gc.fill = GridBagConstraints.HORIZONTAL;
		this.setupTabAudioStats(gPanel, gc);
		//}

	}
	private final void setupTabAudioRange(JPanel container, GridBagConstraints gc) {
		// Slider
		double[] ed = new double[]{ 0.0 , 0.0 };
		this.audioEncodingDurationRangeslider = new JRangeSlider(0.0, 0.0, ed[0], ed[1]);
		container.add(this.audioEncodingDurationRangeslider, gc);
		this.audioEncodingDurationRangeslider.setEnabled(true);
		this.audioEncodingDurationRangeslider.setPreferredSize(new Dimension(1280, 24));
		this.setRangeSliderColors(this.audioEncodingDurationRangeslider);
		this.audioEncodingDurationRangeslider.setUsable(false);

		// Listener
		final GUI self = this;
		this.audioEncodingDurationRangeslider.addChangeListener(new ChangeListener(){
			@Override
			public final void stateChanged(ChangeEvent event) {
				self.onAudioEncodeDurationUpdate((JRangeSlider.ChangeEvent) event);
			}
		});
	}
	private final void setupTabAudioTimes(JPanel container, GridBagConstraints gridCon) {
		JPanel timePanel, middlePanel;
		GridBagConstraints gc = new GridBagConstraints();
		JLabel text;

		// Panel
		container.add((timePanel = new JPanel()), gridCon);
		timePanel.setLayout(new GridLayout(1, 3));
		timePanel.setOpaque(false);

		final GUI self = this;
		this.setupTimecodeLabels(
			timePanel,
			this.audioTimecodeStart = new JLabel(Videncode.timeToString(0.0, new int[]{0,2,2,2})),
			this.audioTimecodeEnd = new JLabel(),
			new JTextFieldCustom[]{
				this.audioEncodingDurationTimecode[0] = new JTextFieldCustom(),
				this.audioEncodingDurationTimecode[1] = new JTextFieldCustom()
			},
			new ChangeListener[]{
				new ChangeListener() {
					@Override
					public final void stateChanged(ChangeEvent event) {
						self.onAudioEncodeDurationTextModify(0, (JTextFieldCustom) event.getSource());
					}
				},
				new ChangeListener() {
					@Override
					public final void stateChanged(ChangeEvent event) {
						self.onAudioEncodeDurationTextModify(1, (JTextFieldCustom) event.getSource());
					}
				}
			}
		);
	}
	private final void setupTabAudioStats(JPanel container, GridBagConstraints gridCon) {
		JPanel innerPanel, panel;
		GridBagConstraints gc = new GridBagConstraints();
		JLabel text;
		TitledBorder b;

		// Panel
		container.add((innerPanel = new JPanel()), gridCon);
		innerPanel.setLayout(new GridBagLayout());
		innerPanel.setOpaque(false);

		//{ Panels
		gc.gridwidth = 1;
		gc.weightx = 0.5;
		gc.gridx = 0;
		++gc.gridy;
		gc.anchor = GridBagConstraints.PAGE_START;
		gc.fill = GridBagConstraints.BOTH;

		// Create the panel
		b = BorderFactory.createTitledBorder(null, "Encoding Status", TitledBorder.LEFT, TitledBorder.TOP, this.fonts.text, this.colors.text);
		b.setBorder(BorderFactory.createLineBorder(this.colors.backgroundDark));
		innerPanel.add(panel = new JPanel(), gc);
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(false);
		panel.setBorder(b);
		this.setupTabAudioEncodingStatus(panel);

		gc.weightx = 0.5;
		++gc.gridx;
		gc.anchor = GridBagConstraints.PAGE_START;
		gc.fill = GridBagConstraints.BOTH;
		b = BorderFactory.createTitledBorder(null, "Preview Clips", TitledBorder.RIGHT, TitledBorder.TOP, this.fonts.text, this.colors.text);
		b.setBorder(BorderFactory.createLineBorder(this.colors.backgroundDark));
		innerPanel.add(panel = new JPanel(), gc);
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(false);
		panel.setBorder(b);
		this.setupTabAudioPreviews(panel);

		gc.weightx = 0.5;
		++gc.gridy;
		gc.gridx = 0;
		gc.gridwidth = 2;
		gc.anchor = GridBagConstraints.PAGE_START;
		gc.fill = GridBagConstraints.BOTH;
		b = BorderFactory.createTitledBorder(null, "Encoding Settings", TitledBorder.CENTER, TitledBorder.TOP, this.fonts.text, this.colors.text);
		b.setBorder(BorderFactory.createLineBorder(this.colors.backgroundDark));
		innerPanel.add(panel = new JPanel(), gc);
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(false);
		panel.setBorder(b);
		this.setupTabAudioAttributes(panel);
		//}
	}
	private final void setupTabAudioEncodingStatus(JPanel container) {
		final GUI self = this;

		JPanel pad, gridPanel;
		GridBagConstraints gc = new GridBagConstraints();
		JButton button;
		JLabel text;

		//{ Bitrates
		gc.weightx = 0.5;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.LINE_END;
		gc.fill = GridBagConstraints.NONE;
		container.add(this.audioTempFilesizeLabel = new JLabel(), gc);
		this.audioTempFilesizeLabel.setFont(this.fonts.text);
		this.audioTempFilesizeLabel.setForeground(this.colors.text);

		++gc.gridx;
		gc.weightx = 0.0;
		container.add(pad = new JPanel(), gc);
		pad.setPreferredSize(new Dimension(10, 0));

		++gc.gridx;
		gc.weightx = 0.5;
		gc.anchor = GridBagConstraints.LINE_START;
		gc.fill = GridBagConstraints.NONE;
		container.add(this.audioTempFilesize = new JLabel(), gc);
		this.audioTempFilesize.setFont(this.fonts.textBold);
		this.audioTempFilesize.setForeground(this.colors.text);
		//}

		//{ Encode state
		gc.weightx = 0.5;
		gc.gridx = 0;
		++gc.gridy;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.LINE_END;
		gc.fill = GridBagConstraints.NONE;
		container.add(text = new JLabel("Status"), gc);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++gc.gridx;
		gc.weightx = 0.0;
		container.add(pad = new JPanel(), gc);
		pad.setPreferredSize(new Dimension(10, 0));

		++gc.gridx;
		gc.weightx = 0.5;
		gc.anchor = GridBagConstraints.LINE_START;
		gc.fill = GridBagConstraints.HORIZONTAL;

		container.add(gridPanel = new JPanel(), gc);
		gridPanel.setLayout(new GridBagLayout());
		gridPanel.setOpaque(false);

		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.VERTICAL;

		gridPanel.add(this.audioTempEncodeStatusText = new JLabel("Encoded"), c);
		this.audioTempEncodeStatusText.setFont(this.fonts.textBold);
		this.audioTempEncodeStatusText.setForeground(this.colors.text);

		gridPanel.add(this.audioTempEncodeActivateButton = new JButton("Not encoded"), c);
		this.audioTempEncodeActivateButton.setFont(this.fonts.textSmall);
		this.audioTempEncodeActivateButton.setForeground(this.colors.text);
		this.audioTempEncodeActivateButton.setToolTipText("Search for an external audio file");
		this.audioTempEncodeActivateButton.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
		this.audioTempEncodeActivateButton.setOpaque(false);
		this.audioTempEncodeActivateButton.addActionListener(new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				self.onAudioEncodeButtonPress();
			}
		});

		gridPanel.add(this.audioTempEncodeProgressBar = new JProgressBarCustom(), c);
		this.audioTempEncodeProgressBar.setFont(this.fonts.textSmall);
		this.audioTempEncodeProgressBar.setForeground(this.colors.text);
		this.audioTempEncodeProgressBar.setPreferredSize(new Dimension(128, 16));
		this.audioTempEncodeProgressBar.setEnabled(true);
		this.audioTempEncodeProgressBar.getColors().borderDark[0] = this.colors.backgroundDarkest;
		this.audioTempEncodeProgressBar.getColors().borderLight[0] = this.colors.background;
		this.audioTempEncodeProgressBar.getColors().background[0] = this.colors.textLight1;
		this.audioTempEncodeProgressBar.getColors().backgroundLoaded[0] = this.colors.textLight2;
		this.audioTempEncodeProgressBar.getColors().text[0] = this.colors.text;

		this.audioTempEncodeStatusText.setVisible(false);
		this.audioTempEncodeActivateButton.setVisible(true);
		this.audioTempEncodeProgressBar.setVisible(false);
		//}
	}
	private final void setupTabAudioPreviews(JPanel container) {
		final GUI self = this;
		GridBagConstraints gc = new GridBagConstraints();
		JLabel text;

		// Panel
		gc.gridwidth = 1;
		gc.weightx = 0.5;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.anchor = GridBagConstraints.LINE_END;
		gc.fill = GridBagConstraints.NONE;


		// Buttons
		String[] labels = new String[]{ "Start Clip" , "End Clip" };
		String[] tooltips = new String[]{ "Play a preview clip of the start encoding time" , "Play a preview clip of the end encoding time" };
		for (int i = 0; i < 2; ++i) {
			container.add(this.audioPreviewClipButtons[i] = new JButton(labels[i]), gc);
			this.audioPreviewClipButtons[i].setActionCommand(Integer.valueOf(i).toString());
			this.audioPreviewClipButtons[i].setToolTipText(tooltips[i]);
			this.audioPreviewClipButtons[i].setFont(this.fonts.textSmall);
			this.audioPreviewClipButtons[i].setForeground(this.colors.text);
			this.audioPreviewClipButtons[i].setToolTipText("Search for an external audio file");
			this.audioPreviewClipButtons[i].setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
			this.audioPreviewClipButtons[i].setOpaque(false);
			this.audioPreviewClipButtons[i].addActionListener(new ActionListener(){
				@Override
				public final void actionPerformed(ActionEvent event) {
					self.onAudioPreviewClipButtonPress(Integer.parseInt(event.getActionCommand()));
				}
			});

			++gc.gridy;
		}
	}
	private final void setupTabAudioAttributes(JPanel container) {
		final GUI self = this;

		JPanel leftPanel, rightPanel, pad, flow;
		JLabel text;
		JLine line;
		JButton button;
		GridBagConstraints gc = new GridBagConstraints();
		ActionListener listener;

		gc.weightx = 0.5;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.LINE_END;
		gc.fill = GridBagConstraints.HORIZONTAL;
		container.add(leftPanel = new JPanel(), gc);
		leftPanel.setLayout(new GridBagLayout());
		leftPanel.setOpaque(false);

		++gc.gridx;
		container.add(rightPanel = new JPanel(), gc);
		rightPanel.setLayout(new GridBagLayout());
		rightPanel.setOpaque(false);


		//{ Bitrates
		gc.weightx = 0.5;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.LINE_END;
		gc.fill = GridBagConstraints.NONE;
		leftPanel.add(text = new JLabel("Bitrate"), gc);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++gc.gridx;
		gc.weightx = 0.0;
		leftPanel.add(pad = new JPanel(), gc);
		pad.setPreferredSize(new Dimension(10, 0));

		++gc.gridx;
		gc.weightx = 0.5;
		gc.anchor = GridBagConstraints.LINE_START;
		gc.fill = GridBagConstraints.NONE;
		this.audioBitrateSelectionComboBox = new JComboBox<Videncode.Bitrate>(this.videncode.getAudioBitrates());
		leftPanel.add(this.audioBitrateSelectionComboBox, gc);
		this.audioBitrateSelectionComboBox.setFont(this.fonts.textSmall);
		this.audioBitrateSelectionComboBox.setForeground(this.colors.text);
		this.audioBitrateSelectionComboBox.setOpaque(false);

		// Listener
		listener = new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				self.onAudioBitrateChange((Videncode.Bitrate) ((JComboBox) event.getSource()).getSelectedItem());
			}
		};
		this.audioBitrateSelectionComboBox.addActionListener(listener);
		//}

		//{ Channels
		gc.weightx = 0.5;
		gc.gridx = 0;
		++gc.gridy;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.LINE_END;
		gc.fill = GridBagConstraints.NONE;
		leftPanel.add(text = new JLabel("Channels"), gc);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++gc.gridx;
		gc.weightx = 0.0;
		leftPanel.add(pad = new JPanel(), gc);
		pad.setPreferredSize(new Dimension(10, 0));

		++gc.gridx;
		gc.weightx = 0.5;
		gc.anchor = GridBagConstraints.LINE_START;
		gc.fill = GridBagConstraints.NONE;
		this.audioChannelSelectionComboBox = new JComboBox<Videncode.AudioChannelCount>(this.videncode.getAudioChannels());
		leftPanel.add(this.audioChannelSelectionComboBox, gc);
		this.audioChannelSelectionComboBox.setFont(this.fonts.textSmall);
		this.audioChannelSelectionComboBox.setForeground(this.colors.text);
		this.audioChannelSelectionComboBox.setOpaque(false);

		// Listener
		listener = new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				self.onAudioChannelCountChange((Videncode.AudioChannelCount) ((JComboBox) event.getSource()).getSelectedItem());
			}
		};
		this.audioChannelSelectionComboBox.addActionListener(listener);
		//}

		//{ Sample rates
		gc.weightx = 0.5;
		gc.gridx = 0;
		++gc.gridy;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.LINE_END;
		gc.fill = GridBagConstraints.NONE;
		leftPanel.add(text = new JLabel("Sample rate"), gc);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++gc.gridx;
		gc.weightx = 0.0;
		leftPanel.add(pad = new JPanel(), gc);
		pad.setPreferredSize(new Dimension(10, 0));

		++gc.gridx;
		gc.weightx = 0.5;
		gc.anchor = GridBagConstraints.LINE_START;
		gc.fill = GridBagConstraints.NONE;
		this.audioSampleRateSelectionComboBox = new JComboBox<Videncode.AudioSampleRate>();
		leftPanel.add(this.audioSampleRateSelectionComboBox, gc);
		this.audioSampleRateSelectionComboBox.setFont(this.fonts.textSmall);
		this.audioSampleRateSelectionComboBox.setForeground(this.colors.text);
		this.audioSampleRateSelectionComboBox.setOpaque(false);
		for (int i = this.videncode.getAudioSampleRates().length - 1; i >= 0; --i) {
			this.audioSampleRateSelectionComboBox.addItem(this.videncode.getAudioSampleRates()[i]);
		}

		// Listener
		listener = new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				self.onAudioSampleRateChange((Videncode.AudioSampleRate) ((JComboBox) event.getSource()).getSelectedItem());
			}
		};
		this.audioSampleRateSelectionComboBox.addActionListener(listener);
		//}

		//{ Source
		// Listeners
		ActionListener a1 = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event) {
				self.setAudioSourceState(Integer.parseInt(event.getActionCommand()) == 1, true);
			}
		};
		ActionListener a2 = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event) {
				self.setAudioSourceState(true, true);
			}
		};

		this.audioSourceButtonGroup = new ButtonGroup();

		gc.weightx = 0.5;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.LINE_END;
		gc.fill = GridBagConstraints.NONE;
		rightPanel.add(this.audioSourceCheckbox = new JCheckBox("Source"), gc);
		this.audioSourceCheckbox.setFont(this.fonts.text);
		this.audioSourceCheckbox.setForeground(this.colors.text);
		this.audioSourceCheckbox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		this.audioSourceCheckbox.addActionListener(new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				self.onAudioSourceCheckboxChange((JCheckBox) event.getSource(), event);
			}
		});

		++gc.gridx;
		gc.weightx = 0.0;
		rightPanel.add(pad = new JPanel(), gc);
		pad.setPreferredSize(new Dimension(10, 0));

		++gc.gridx;
		gc.weightx = 0.5;
		gc.anchor = GridBagConstraints.LINE_START;

		rightPanel.add(this.audioSourceButtons[0] = new JRadioButton(" Video"), gc);
		this.audioSourceButtonGroup.add(this.audioSourceButtons[0]);
		if (this.videncode.isAudioFromVideo()) {
			this.audioSourceButtonGroup.setSelected(this.audioSourceButtons[0].getModel(), true);
		}
		this.audioSourceButtons[0].setFont(this.fonts.text);
		this.audioSourceButtons[0].setForeground(this.colors.text);
		this.audioSourceButtons[0].setToolTipText("Use audio from the video file");
		this.audioSourceButtons[0].setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 4));
		this.audioSourceButtons[0].setActionCommand(Integer.valueOf(0).toString());
		this.audioSourceButtons[0].addActionListener(a1);
		this.audioSourceButtons[0].setOpaque(false);

		++gc.gridy;
		rightPanel.add((flow = new JPanel()), gc);
		flow.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		flow.setOpaque(false);

		flow.add(this.audioSourceButtons[1] = new JRadioButton(""), gc);
		this.audioSourceButtonGroup.add(this.audioSourceButtons[1]);
		if (!this.videncode.isAudioFromVideo()) {
			this.audioSourceButtonGroup.setSelected(this.audioSourceButtons[1].getModel(), true);
		}
		this.audioSourceButtons[1].setFont(this.fonts.text);
		this.audioSourceButtons[1].setForeground(this.colors.text);
		this.audioSourceButtons[1].setToolTipText("Use an external audio file for sound");
		this.audioSourceButtons[1].setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 4));
		this.audioSourceButtons[1].setActionCommand(Integer.valueOf(1).toString());
		this.audioSourceButtons[1].addActionListener(a1);
		this.audioSourceButtons[1].setOpaque(false);

		flow.add(button = new JButton("External audio"), gc);
		button.setFont(this.fonts.textSmall);
		button.setForeground(this.colors.text);
		button.setToolTipText("Search for an external audio file");
		button.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
		button.addActionListener(a2);
		button.setOpaque(false);
		//}
	}


	private final void setupTabSync(JPanel container) {
		JPanel panel, outerPanel;
		JLabel text;
		GridBagConstraints gc = new GridBagConstraints();

		container.add(outerPanel = new JPanel());
		outerPanel.setLayout(new GridBagLayout());
		outerPanel.setOpaque(false);

		gc.weightx = 0.5;
		gc.weighty = 0.5;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.PAGE_START;
		gc.fill = GridBagConstraints.BOTH;
		outerPanel.add((this.sync.mainPanel = new JPanel()), gc);
		this.sync.mainPanel.setLayout(new BorderLayout());
		this.sync.mainPanel.setOpaque(false);

		this.sync.mainPanel.add((panel = new JPanel()), BorderLayout.CENTER);
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(false);


		// Timecodes
		gc.weightx = 0.5;
		gc.weighty = 0.0;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.PAGE_START;
		gc.fill = GridBagConstraints.BOTH;
		this.setupTabSyncTimecodes(panel, gc);
		this.setupTabSyncOptions(this.sync.mainPanel);

		// Status
		gc.weightx = 0.5;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.PAGE_START;
		gc.fill = GridBagConstraints.BOTH;
		outerPanel.add((this.sync.disabledPanel = new JPanel()), gc);
		this.sync.disabledPanel.setLayout(new BorderLayout());
		this.sync.disabledPanel.setOpaque(false);

		this.sync.disabledPanel.add((panel = new JPanel()), BorderLayout.CENTER);
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(false);

		gc.weightx = 0.5;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.CENTER;
		gc.fill = GridBagConstraints.NONE;

		panel.add(text = new JLabel("Sync not needed"), gc);
		text.setFont(this.fonts.textNoSync);
		text.setForeground(this.colors.text);
	}
	private final void setupTabSyncTimecodes(JPanel container, GridBagConstraints gc) {
		final GUI self = this;
		JPanel panel;
		JLabel text;
		GridBagConstraints c = new GridBagConstraints();

		container.add((panel = new JPanel()), gc);
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(false);
		//{
		c.weightx = 0.5;
		c.anchor = GridBagConstraints.PAGE_START;

		panel.add(text = new JLabel("Video Timing"), c);
		text.setFont(this.fonts.textBold);
		text.setForeground(this.colors.text);
		//}

		++gc.gridy;
		container.add((panel = new JPanel()), gc);
		panel.setLayout(new GridLayout(1, 3));
		panel.setOpaque(false);
		this.setupTabSyncTimecodeLabels(panel, 0);

		++gc.gridy;
		this.sync.ranges[0] = new JRangeSlider(0.0, 0.0, 0.0, 0.0);
		container.add(this.sync.ranges[0], gc);
		this.sync.ranges[0].setEnabled(true);
		this.sync.ranges[0].setPreferredSize(new Dimension(24, 24));
		this.sync.ranges[0].setResizable(false);
		this.setRangeSliderColors(this.sync.ranges[0]);
		this.sync.ranges[0].addChangeListener(new ChangeListener(){
			@Override
			public final void stateChanged(ChangeEvent event) {
				self.onSyncEncodeDurationUpdate(0, (JRangeSlider.ChangeEvent) event);
			}
		});

		++gc.gridy;
		this.sync.ranges[1] = new JRangeSlider(0.0, 0.0, 0.0, 0.0);
		container.add(this.sync.ranges[1], gc);
		this.sync.ranges[1].setEnabled(true);
		this.sync.ranges[1].setPreferredSize(new Dimension(24, 24));
		this.sync.ranges[1].setResizable(false);
		this.setRangeSliderColors(this.sync.ranges[1]);
		this.sync.ranges[1].addChangeListener(new ChangeListener(){
			@Override
			public final void stateChanged(ChangeEvent event) {
				self.onSyncEncodeDurationUpdate(1, (JRangeSlider.ChangeEvent) event);
			}
		});

		++gc.gridy;
		container.add((panel = new JPanel()), gc);
		panel.setLayout(new GridLayout(1, 3));
		panel.setOpaque(false);
		this.setupTabSyncTimecodeLabels(panel, 1);

		++gc.gridy;
		container.add((panel = new JPanel()), gc);
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(false);
		//{
		c.weightx = 0.5;
		c.anchor = GridBagConstraints.PAGE_START;

		panel.add(text = new JLabel("Audio Timing"), c);
		text.setFont(this.fonts.textBold);
		text.setForeground(this.colors.text);
		//}
	}
	private final void setupTabSyncTimecodeLabels(JPanel container, final int id) {
		final GUI self = this;
		this.setupTimecodeLabels(
			container,
			this.sync.rangeTimecodes[id][0] = new JLabel(Videncode.timeToString(0.0, new int[]{0,2,2,2})),
			this.sync.rangeTimecodes[id][1] = new JLabel(),
			new JTextFieldCustom[]{
				this.sync.rangeEncodeTimecodes[id][0] = new JTextFieldCustom(),
				this.sync.rangeEncodeTimecodes[id][1] = new JTextFieldCustom()
			},
			new ChangeListener[]{
				new ChangeListener() {
					@Override
					public final void stateChanged(ChangeEvent event) {
						self.onSyncEncodeDurationTextModify(id, 0, (JTextFieldCustom) event.getSource());
					}
				},
				new ChangeListener() {
					@Override
					public final void stateChanged(ChangeEvent event) {
						self.onSyncEncodeDurationTextModify(id, 1, (JTextFieldCustom) event.getSource());
					}
				}
			}
		);
	}
	private final void setupTabSyncOptions(JPanel container) {
		final GUI self = this;

		JPanel outerPanel, leftPanel, rightPanel, grid, gridCol;
		JLabel text;
		ButtonGroup group;
		GridBagConstraints gc = new GridBagConstraints();
		TitledBorder border;


		container.add((outerPanel = new JPanel()), BorderLayout.PAGE_END);
		outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
		outerPanel.setOpaque(false);

		outerPanel.add((this.sync.optionPanels[0] = new JPanel()), BorderLayout.PAGE_END);
		this.sync.optionPanels[0].setLayout(new GridLayout(1, 2));
		this.sync.optionPanels[0].setOpaque(false);


		//{ Audio settings
		this.sync.optionPanels[0].add((leftPanel = new JPanel()));
		leftPanel.setLayout(new BorderLayout());
		leftPanel.setOpaque(false);

		border = BorderFactory.createTitledBorder(null, "Audio Before Video", TitledBorder.CENTER, TitledBorder.TOP, this.fonts.text, this.colors.text);
		border.setBorder(BorderFactory.createLineBorder(this.colors.backgroundDark));
		leftPanel.setBorder(border);

		this.sync.optionPanels[0].add((rightPanel = new JPanel()));
		rightPanel.setLayout(new BorderLayout());
		rightPanel.setOpaque(false);

		border = BorderFactory.createTitledBorder(null, "Audio After Video", TitledBorder.CENTER, TitledBorder.TOP, this.fonts.text, this.colors.text);
		border.setBorder(BorderFactory.createLineBorder(this.colors.backgroundDark));
		rightPanel.setBorder(border);

		//{ Left
		leftPanel.add((grid = new JPanel()));
		grid.setLayout(new GridBagLayout());
		grid.setOpaque(false);

		gc.weightx = 0.5;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.CENTER;
		gc.fill = GridBagConstraints.NONE;
		grid.add(this.sync.audioStateLoop[0] = new JCheckBox("Loop before video starts playing"), gc);

		++gc.gridx;
		gc.anchor = GridBagConstraints.CENTER;
		grid.add(this.sync.audioFadeTransition[0] = new JCheckBox("Fade volume in"), gc);
		//}

		//{ Right
		rightPanel.add((grid = new JPanel()));
		grid.setLayout(new GridBagLayout());
		grid.setOpaque(false);

		gc.weightx = 0.5;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.CENTER;
		gc.fill = GridBagConstraints.NONE;
		grid.add(this.sync.audioStateLoop[1] = new JCheckBox("Loop after video stops playing"), gc);

		++gc.gridx;
		gc.anchor = GridBagConstraints.CENTER;
		grid.add(this.sync.audioFadeTransition[1] = new JCheckBox("Fade volume out"), gc);
		//}

		//{ Both
		for (int i = 0; i < this.sync.audioStateLoop.length; ++i) {
			final boolean start = (i == 0);
			this.sync.audioStateLoop[i].setFont(this.fonts.text);
			this.sync.audioStateLoop[i].setForeground(this.colors.text);
			this.sync.audioStateLoop[i].setSelected(true);
			this.sync.audioStateLoop[i].setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 4));
			this.sync.audioStateLoop[i].setOpaque(false);
			this.sync.audioStateLoop[i].setSelected(false);
			this.sync.audioStateLoop[i].addActionListener(new ActionListener(){
				@Override
				public final void actionPerformed(ActionEvent event) {
					self.onSyncAudioStateChange(start, ((JCheckBox) event.getSource()).isSelected() ? Videncode.SYNC_LOOP : Videncode.SYNC_NOTHING);
				}
			});
		}
		for (int i = 0; i < this.sync.audioFadeTransition.length; ++i) {
			final boolean start = (i == 0);
			this.sync.audioFadeTransition[i].setFont(this.fonts.text);
			this.sync.audioFadeTransition[i].setForeground(this.colors.text);
			this.sync.audioFadeTransition[i].setSelected(true);
			this.sync.audioFadeTransition[i].setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 4));
			this.sync.audioFadeTransition[i].setOpaque(false);
			this.sync.audioFadeTransition[i].setSelected(false);
			this.sync.audioFadeTransition[i].addActionListener(new ActionListener(){
				@Override
				public final void actionPerformed(ActionEvent event) {
					self.onSyncAudioFadeChange(start, ((JCheckBox) event.getSource()).isSelected());
				}
			});
		}
		//}

		//}


		outerPanel.add((this.sync.optionPanels[1] = new JPanel()), BorderLayout.PAGE_END);
		this.sync.optionPanels[1].setLayout(new GridLayout(1, 2));
		this.sync.optionPanels[1].setOpaque(false);

		//{ Video settings
		this.sync.optionPanels[1].add((leftPanel = new JPanel()));
		leftPanel.setLayout(new BorderLayout());
		leftPanel.setOpaque(false);

		border = BorderFactory.createTitledBorder(null, "Video Before Audio", TitledBorder.CENTER, TitledBorder.TOP, this.fonts.text, this.colors.text);
		border.setBorder(BorderFactory.createLineBorder(this.colors.backgroundDark));
		leftPanel.setBorder(border);

		this.sync.optionPanels[1].add((rightPanel = new JPanel()));
		rightPanel.setLayout(new BorderLayout());
		rightPanel.setOpaque(false);

		border = BorderFactory.createTitledBorder(null, "Video After Audio", TitledBorder.CENTER, TitledBorder.TOP, this.fonts.text, this.colors.text);
		border.setBorder(BorderFactory.createLineBorder(this.colors.backgroundDark));
		rightPanel.setBorder(border);

		//{ Left
		leftPanel.add((grid = new JPanel()));
		grid.setLayout(new GridBagLayout());
		grid.setOpaque(false);

		gc.weightx = 0.5;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.PAGE_START;
		gc.fill = GridBagConstraints.NONE;
		grid.add((gridCol = new JPanel()), gc);
		gridCol.setLayout(new GridBagLayout());
		gridCol.setOpaque(false);

		gc.weightx = 0.5;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.LINE_START;
		gc.fill = GridBagConstraints.NONE;
		gridCol.add(this.sync.videoState[0][Videncode.SYNC_LOOP] = new JRadioButton("Loop before audio starts playing"), gc);

		++gc.gridy;
		gc.anchor = GridBagConstraints.LINE_START;
		gridCol.add(this.sync.videoState[0][Videncode.SYNC_PREVIEW] = new JRadioButton("Display stopped video frame"), gc);

		++gc.gridy;
		gc.anchor = GridBagConstraints.LINE_START;
		gridCol.add(this.sync.videoFadeTransition[0] = new JCheckBox("Fade image in"), gc);


		gc.gridy = 0;
		++gc.gridx;
		gc.anchor = GridBagConstraints.PAGE_START;
		grid.add((gridCol = new JPanel()), gc);
		gridCol.setLayout(new GridBagLayout());
		gridCol.setOpaque(false);

		gc.weightx = 0.5;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.LINE_START;
		gc.fill = GridBagConstraints.NONE;
		gridCol.add(this.sync.videoState[0][Videncode.SYNC_EXTERNAL] = new JRadioButton("Display main image"), gc);

		++gc.gridy;
		gc.anchor = GridBagConstraints.LINE_START;
		gridCol.add(this.sync.videoState[0][Videncode.SYNC_NOTHING] = new JRadioButton("Display nothing"), gc);

		//}

		//{ Right
		rightPanel.add((grid = new JPanel()));
		grid.setLayout(new GridBagLayout());
		grid.setOpaque(false);

		gc.weightx = 0.5;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.PAGE_START;
		gc.fill = GridBagConstraints.NONE;
		grid.add((gridCol = new JPanel()), gc);
		gridCol.setLayout(new GridBagLayout());
		gridCol.setOpaque(false);

		gc.weightx = 0.5;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.LINE_START;
		gc.fill = GridBagConstraints.NONE;
		gridCol.add(this.sync.videoState[1][Videncode.SYNC_LOOP] = new JRadioButton("Loop after audio starts playing"), gc);

		++gc.gridy;
		gc.anchor = GridBagConstraints.LINE_START;
		gridCol.add(this.sync.videoState[1][Videncode.SYNC_PREVIEW] = new JRadioButton("Display stopped video frame"), gc);

		++gc.gridy;
		gc.anchor = GridBagConstraints.LINE_START;
		gridCol.add(this.sync.videoFadeTransition[1] = new JCheckBox("Fade image out"), gc);


		gc.gridy = 0;
		++gc.gridx;
		gc.anchor = GridBagConstraints.PAGE_START;
		grid.add((gridCol = new JPanel()), gc);
		gridCol.setLayout(new GridBagLayout());
		gridCol.setOpaque(false);

		gc.weightx = 0.5;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.LINE_START;
		gc.fill = GridBagConstraints.NONE;
		gridCol.add(this.sync.videoState[1][Videncode.SYNC_EXTERNAL] = new JRadioButton("Display main image"), gc);

		++gc.gridy;
		gc.anchor = GridBagConstraints.LINE_START;
		gridCol.add(this.sync.videoState[1][Videncode.SYNC_NOTHING] = new JRadioButton("Display nothing"), gc);

		//}

		//{ Both
		for (int i = 0; i < this.sync.videoState.length; ++i) {
			final boolean start = (i == 0);
			this.sync.videoStateGroups[i] = new ButtonGroup();

			for (int j = 0; j < this.sync.videoState[i].length; ++j) {
				this.sync.videoStateGroups[i].add(this.sync.videoState[i][j]);
				this.sync.videoState[i][j].setFont(this.fonts.text);
				this.sync.videoState[i][j].setForeground(this.colors.text);
				this.sync.videoState[i][j].setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 4));
				this.sync.videoState[i][j].setOpaque(false);
				this.sync.videoState[i][j].setActionCommand(Integer.valueOf(j).toString());
				this.sync.videoState[i][j].addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent event) {
						self.onSyncVideoStateChange(start, Integer.parseInt(event.getActionCommand()));
					}
				});
			}
		}
		for (int i = 0; i < this.sync.videoFadeTransition.length; ++i) {
			final boolean start = (i == 0);
			this.sync.videoFadeTransition[i].setFont(this.fonts.text);
			this.sync.videoFadeTransition[i].setForeground(this.colors.text);
			this.sync.videoFadeTransition[i].setSelected(true);
			this.sync.videoFadeTransition[i].setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 4));
			this.sync.videoFadeTransition[i].setOpaque(false);
			this.sync.videoFadeTransition[i].setSelected(false);
			this.sync.videoFadeTransition[i].addActionListener(new ActionListener(){
				@Override
				public final void actionPerformed(ActionEvent event) {
					self.onSyncVideoFadeChange(start, ((JCheckBox) event.getSource()).isSelected());
				}
			});
		}
		//}

		//}

	}


	private final void setupTabEncode(JPanel container) {
		JLabel text;
		JPanel panel, leftPanel, leftMain, rightPanel, pad;
		TitledBorder border;

		// Panels
		container.setLayout(new GridLayout(1, 2));

		container.add((panel = new JPanel()));
		panel.setLayout(new BorderLayout());
		panel.setOpaque(false);

		panel.add((leftPanel = new JPanel()), BorderLayout.PAGE_START);
		leftPanel.setLayout(new GridBagLayout());
		leftPanel.setOpaque(false);

		panel.add((leftMain = new JPanel()), BorderLayout.CENTER);
		leftMain.setLayout(new GridBagLayout());
		leftMain.setOpaque(false);

		container.add((panel = new JPanel()));
		panel.setLayout(new BorderLayout());
		panel.setOpaque(false);

		border = BorderFactory.createTitledBorder(null, "Settings", TitledBorder.CENTER, TitledBorder.TOP, this.fonts.text, this.colors.text);
		border.setBorder(BorderFactory.createLineBorder(this.colors.backgroundDark));
		panel.setBorder(border);

		panel.add((rightPanel = new JPanel()), BorderLayout.PAGE_START);
		rightPanel.setLayout(new GridBagLayout());
		rightPanel.setOpaque(false);

		// Status
		this.setupTabEncodeStatus(leftPanel);
		// Settings
		this.setupTabEncodeSettings(rightPanel);
	}
	private final void setupTabEncodeStatus(JPanel container) {
		final GUI self = this;
		JLabel text;
		JPanel panel, leftPanel, rightPanel, pad, borderPanel;
		GridBagConstraints c = new GridBagConstraints();
		TitledBorder border;

		container.setLayout(new BorderLayout());
		container.add((panel = new JPanel()), BorderLayout.PAGE_START);
		panel.setLayout(new GridLayout(1, 2));
		panel.setOpaque(false);

		panel.add(leftPanel = new JPanel());
		leftPanel.setLayout(new GridBagLayout());
		leftPanel.setOpaque(false);
		border = BorderFactory.createTitledBorder(null, "Encode", TitledBorder.CENTER, TitledBorder.TOP, this.fonts.text, this.colors.text);
		border.setBorder(BorderFactory.createLineBorder(this.colors.backgroundDark));
		leftPanel.setBorder(border);

		panel.add(rightPanel = new JPanel());
		rightPanel.setLayout(new GridBagLayout());
		rightPanel.setOpaque(false);
		border = BorderFactory.createTitledBorder(null, "Status", TitledBorder.CENTER, TitledBorder.TOP, this.fonts.text, this.colors.text);
		border.setBorder(BorderFactory.createLineBorder(this.colors.backgroundDark));
		rightPanel.setBorder(border);

		container.add(this.encode.errorPanel = new JPanel(), BorderLayout.CENTER);
		this.encode.errorPanel.setLayout(new GridBagLayout());
		this.encode.errorPanel.setOpaque(false);
		border = BorderFactory.createTitledBorder(null, "Errors", TitledBorder.CENTER, TitledBorder.TOP, this.fonts.text, this.colors.text);
		border.setBorder(BorderFactory.createLineBorder(this.colors.backgroundDark));
		this.encode.errorPanel.setBorder(border);

		//{ Encode
		JButton button;

		c.weightx = 0.5;
		c.weighty = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.PAGE_START;
		c.fill = GridBagConstraints.BOTH;
		leftPanel.add(borderPanel = new JPanel(), c);
		borderPanel.setLayout(new BorderLayout());
		borderPanel.setOpaque(false);

		this.setupTabEncodeButton(borderPanel);
		//}

		//{ Error
		this.encode.errorPanel.add(this.encode.errorMessage = new JLabel(), c);
		this.encode.errorMessage.setFont(this.fonts.text);
		this.encode.errorMessage.setForeground(this.colors.text);
		this.encode.errorPanel.setVisible(false);
		//}

		//{ Image status
		c.weightx = 0.5;
		c.weighty = 0.0;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		rightPanel.add(text = new JLabel("Image status"), c);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++c.gridx;
		c.weightx = 0.0;
		rightPanel.add(pad = new JPanel(), c);
		pad.setPreferredSize(new Dimension(10, 0));

		++c.gridx;
		c.weightx = 0.5;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.NONE;
		rightPanel.add(this.encode.imageStatusTextGood = new JLabel("Encoded"), c);
		this.encode.imageStatusTextGood.setFont(this.fonts.textBold);
		this.encode.imageStatusTextGood.setForeground(this.colors.text);

		rightPanel.add(this.encode.imageStatusTextMissing = new JLabel("No file"), c);
		this.encode.imageStatusTextMissing.setFont(this.fonts.textItalic);
		this.encode.imageStatusTextMissing.setForeground(this.colors.text);

		rightPanel.add(this.encode.imageStatusTextNotEncoded = new JLabel("Encoding"), c);
		this.encode.imageStatusTextNotEncoded.setFont(this.fonts.textBold);
		this.encode.imageStatusTextNotEncoded.setForeground(this.colors.text);

		rightPanel.add(this.encode.imageStatusTextBadSize = new JLabel("Too large"), c);
		this.encode.imageStatusTextBadSize.setFont(this.fonts.textBold);
		this.encode.imageStatusTextBadSize.setForeground(this.colors.text);
		//}
		//{ Audio status
		c.gridx = 0;
		++c.gridy;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		rightPanel.add(text = new JLabel("Audio status"), c);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++c.gridx;
		c.weightx = 0.0;
		rightPanel.add(pad = new JPanel(), c);
		pad.setPreferredSize(new Dimension(10, 0));

		++c.gridx;
		c.weightx = 0.5;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.NONE;


		rightPanel.add(this.encode.audioStatusTextGood = new JLabel("Encoded"), c);
		this.encode.audioStatusTextGood.setFont(this.fonts.textBold);
		this.encode.audioStatusTextGood.setForeground(this.colors.text);

		rightPanel.add(this.encode.audioStatusTextMissing = new JLabel("No file"), c);
		this.encode.audioStatusTextMissing.setFont(this.fonts.textItalic);
		this.encode.audioStatusTextMissing.setForeground(this.colors.text);

		rightPanel.add(this.encode.audioStatusTextBadSize = new JLabel("Too large"), c);
		this.encode.audioStatusTextBadSize.setFont(this.fonts.textBold);
		this.encode.audioStatusTextBadSize.setForeground(this.colors.text);

		rightPanel.add(this.encode.audioStatusTextNotEncoded = new JLabel("Not encoded"), c);
		this.encode.audioStatusTextNotEncoded.setFont(this.fonts.textBold);
		this.encode.audioStatusTextNotEncoded.setForeground(this.colors.text);

		rightPanel.add(this.encode.audioProgressBar = new JProgressBarCustom(), c);
		this.encode.audioProgressBar.setFont(this.fonts.textSmall);
		this.encode.audioProgressBar.setForeground(this.colors.text);
		this.encode.audioProgressBar.setPreferredSize(new Dimension(128, 16));
		this.encode.audioProgressBar.setEnabled(true);
		this.encode.audioProgressBar.getColors().borderDark[0] = this.colors.backgroundDarkest;
		this.encode.audioProgressBar.getColors().borderLight[0] = this.colors.background;
		this.encode.audioProgressBar.getColors().background[0] = this.colors.textLight1;
		this.encode.audioProgressBar.getColors().backgroundLoaded[0] = this.colors.textLight2;
		this.encode.audioProgressBar.getColors().text[0] = this.colors.text;
		//}
		//{ Video status
		c.gridx = 0;
		++c.gridy;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		rightPanel.add(text = new JLabel("Video status"), c);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++c.gridx;
		c.weightx = 0.0;
		rightPanel.add(pad = new JPanel(), c);
		pad.setPreferredSize(new Dimension(10, 0));

		++c.gridx;
		c.weightx = 0.5;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.NONE;


		rightPanel.add(this.encode.videoStatusTextGood = new JLabel("Encoded"), c);
		this.encode.videoStatusTextGood.setFont(this.fonts.textBold);
		this.encode.videoStatusTextGood.setForeground(this.colors.text);

		rightPanel.add(this.encode.videoStatusTextMissing = new JLabel("No file"), c);
		this.encode.videoStatusTextMissing.setFont(this.fonts.textItalic);
		this.encode.videoStatusTextMissing.setForeground(this.colors.text);

		rightPanel.add(this.encode.videoStatusTextBadSize = new JLabel("Too large"), c);
		this.encode.videoStatusTextBadSize.setFont(this.fonts.textBold);
		this.encode.videoStatusTextBadSize.setForeground(this.colors.text);

		rightPanel.add(this.encode.videoStatusTextNotEncoded = new JLabel("Not encoded"), c);
		this.encode.videoStatusTextNotEncoded.setFont(this.fonts.textBold);
		this.encode.videoStatusTextNotEncoded.setForeground(this.colors.text);

		rightPanel.add(this.encode.videoProgressBar = new JProgressBarCustom(), c);
		this.encode.videoProgressBar.setFont(this.fonts.textSmall);
		this.encode.videoProgressBar.setForeground(this.colors.text);
		this.encode.videoProgressBar.setPreferredSize(new Dimension(128, 16));
		this.encode.videoProgressBar.setEnabled(true);
		this.encode.videoProgressBar.getColors().borderDark[0] = this.colors.backgroundDarkest;
		this.encode.videoProgressBar.getColors().borderLight[0] = this.colors.background;
		this.encode.videoProgressBar.getColors().background[0] = this.colors.textLight1;
		this.encode.videoProgressBar.getColors().backgroundLoaded[0] = this.colors.textLight2;
		this.encode.videoProgressBar.getColors().text[0] = this.colors.text;
		//}
		//{ Mux status
		c.gridx = 0;
		++c.gridy;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		rightPanel.add(text = new JLabel("Output"), c);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++c.gridx;
		c.weightx = 0.0;
		rightPanel.add(pad = new JPanel(), c);
		pad.setPreferredSize(new Dimension(10, 0));

		++c.gridx;
		c.weightx = 0.5;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.NONE;
		rightPanel.add(this.encode.finalStatusTextIdle = new JLabel("Idle"), c);
		this.encode.finalStatusTextIdle.setFont(this.fonts.textItalic);
		this.encode.finalStatusTextIdle.setForeground(this.colors.text);

		rightPanel.add(this.encode.finalStatusTextWaiting = new JLabel("Waiting"), c);
		this.encode.finalStatusTextWaiting.setFont(this.fonts.textBold);
		this.encode.finalStatusTextWaiting.setForeground(this.colors.text);

		rightPanel.add(this.encode.finalStatusTextMuxing = new JLabel("Multiplexing"), c);
		this.encode.finalStatusTextMuxing.setFont(this.fonts.textBold);
		this.encode.finalStatusTextMuxing.setForeground(this.colors.text);

		rightPanel.add(this.encode.finalStatusTextComplete = new JButton("Complete"), c);
		this.encode.finalStatusTextComplete.setFont(this.fonts.textSmall);
		this.encode.finalStatusTextComplete.setForeground(this.colors.text);
		this.encode.finalStatusTextComplete.setToolTipText("Open file in explorer");
		this.encode.finalStatusTextComplete.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
		this.encode.finalStatusTextComplete.addActionListener(new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				self.onEncodeCompletePress();
			}
		});
		//}
		//{ Error status
		c.gridx = 0;
		++c.gridy;
		c.gridwidth = 3;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.NONE;
		rightPanel.add(this.encode.statusDisplay = new JLabel(" "), c);
		this.encode.statusDisplay.setFont(this.fonts.text);
		this.encode.statusDisplay.setForeground(this.colors.text);

		//}

	}
	private final void setupTabEncodeButton(JPanel container) {
		final GUI self = this;

		JPanel top;
		JLabel text;
		GridBagConstraints gc = new GridBagConstraints();

		container.add(top = new JPanel(), BorderLayout.PAGE_START);
		top.setLayout(new GridBagLayout());
		top.setOpaque(false);

		//{ Filename + ext
		gc.weightx = 0.0;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.LINE_END;
		gc.fill = GridBagConstraints.NONE;
		top.add(text = new JLabel("File name "), gc);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++gc.gridx;
		gc.weightx = 0.5;
		gc.fill = GridBagConstraints.BOTH;
		top.add(this.encode.outputFilename = new JTextFieldCustom(), gc);
		this.encode.outputFilename.setFont(this.fonts.text);
		this.encode.outputFilename.setForeground(this.colors.text);
		this.encode.outputFilename.setOpaque(false);
		this.encode.outputFilename.setBorder(BorderFactory.createLineBorder(this.colors.textLight1));
		this.encode.outputFilename.addCustomChangeListener(new ChangeListener() {
			@Override
			public final void stateChanged(ChangeEvent event) {
				self.onEncodeFileNameChange((JTextFieldCustom) event.getSource());
			}
		});

		++gc.gridx;
		gc.weightx = 0.0;
		gc.fill = GridBagConstraints.NONE;
		top.add(this.encode.outputFilenameExt = new JComboBox<Extension>(this.outputExtensions), gc);
		this.encode.outputFilenameExt.setFont(this.fonts.textSmall);
		this.encode.outputFilenameExt.setForeground(this.colors.text);
		this.encode.outputFilenameExt.setOpaque(false);
		this.encode.outputFilenameExt.addActionListener(new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				self.onEncodeExtensionChange((Extension) ((JComboBox) event.getSource()).getSelectedItem());
			}
		});
		//}

		//{ Tag
		gc.weightx = 0.0;
		gc.gridx = 0;
		++gc.gridy;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.LINE_END;
		gc.fill = GridBagConstraints.NONE;
		top.add(text = new JLabel("Media tag "), gc);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++gc.gridx;
		gc.weightx = 0.5;
		gc.gridwidth = 2;
		gc.fill = GridBagConstraints.BOTH;
		top.add(this.encode.outputTag = new JTextFieldCustom(), gc);
		this.encode.outputTag.setFont(this.fonts.text);
		this.encode.outputTag.setForeground(this.colors.text);
		this.encode.outputTag.setOpaque(false);
		this.encode.outputTag.setBorder(BorderFactory.createLineBorder(this.colors.textLight1));
		this.encode.outputTag.setDocument(new JTextFieldMaxLength(this.videncode.getOutputTagMaxLength()));
		this.encode.outputTag.addCustomChangeListener(new ChangeListener() {
			@Override
			public final void stateChanged(ChangeEvent event) {
				self.onEncodeTagChange((JTextFieldCustom) event.getSource());
			}
		});
		//}

		//{ Button
		container.add(this.encode.encodeButton = new JButton("Encode"), BorderLayout.CENTER);
		this.encode.encodeButton.setFont(this.fonts.textHuge);
		this.encode.encodeButton.setForeground(this.colors.text);
		this.encode.encodeButton.setOpaque(false);
		this.encode.encodeButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
		this.encode.encodeButton.addActionListener(new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				self.onEncodeButtonPress();
			}
		});
		//}

		container.add(top = new JPanel(), BorderLayout.PAGE_END);
		top.setLayout(new GridBagLayout());
		top.setOpaque(false);

		//{ Filename + ext
		gc.weightx = 0.5;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.CENTER;
		gc.fill = GridBagConstraints.NONE;
		top.add(text = new JLabel("Click here to test your files"), gc);
		text.setFont(this.fonts.textUnderline);
		text.setForeground(this.colors.textAlt1);
		text.setCursor(new Cursor(Cursor.HAND_CURSOR));
		text.addMouseListener(new MouseListener() {
			@Override
			public final void mouseClicked(MouseEvent event) {
				self.openURL("http://dnsev.github.io/ve/#api/test");
			}

			@Override
			public final void mousePressed(MouseEvent event) {
			}
			@Override
			public final void mouseReleased(MouseEvent event) {
			}
			@Override
			public final void mouseEntered(MouseEvent event) {
			}
			@Override
			public final void mouseExited(MouseEvent event) {
			}
		});
		//}


	}
	private final void setupTabEncodeSettings(JPanel container) {
		final GUI self = this;
		JPanel panel1, panel2, leftPanel, rightPanel;
		TitledBorder border;

		container.setLayout(new BorderLayout());
		container.add((panel1 = new JPanel()), BorderLayout.PAGE_START);
		panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));
		panel1.setOpaque(false);

		panel1.add((panel2 = new JPanel()));
		panel2.setLayout(new GridLayout(1, 2));
		panel2.setOpaque(false);

		panel2.add(leftPanel = new JPanel());
		leftPanel.setLayout(new GridBagLayout());
		leftPanel.setOpaque(false);
		border = BorderFactory.createTitledBorder(null, "App", TitledBorder.CENTER, TitledBorder.TOP, this.fonts.text, this.colors.text);
		border.setBorder(BorderFactory.createLineBorder(this.colors.backgroundDark));
		leftPanel.setBorder(border);
		this.setupTabEncodeSettingsApp(leftPanel);

		panel2.add(rightPanel = new JPanel());
		rightPanel.setLayout(new GridBagLayout());
		rightPanel.setOpaque(false);
		border = BorderFactory.createTitledBorder(null, "Output", TitledBorder.CENTER, TitledBorder.TOP, this.fonts.text, this.colors.text);
		border.setBorder(BorderFactory.createLineBorder(this.colors.backgroundDark));
		rightPanel.setBorder(border);
		this.setupTabEncodeSettingsOutput(rightPanel);


		panel1.add((panel2 = new JPanel()));
		panel2.setLayout(new GridLayout(1, 2));
		panel2.setOpaque(false);

		panel2.add(leftPanel = new JPanel());
		leftPanel.setLayout(new GridBagLayout());
		leftPanel.setOpaque(false);
		border = BorderFactory.createTitledBorder(null, "FFmpeg", TitledBorder.CENTER, TitledBorder.TOP, this.fonts.text, this.colors.text);
		border.setBorder(BorderFactory.createLineBorder(this.colors.backgroundDark));
		leftPanel.setBorder(border);
		this.setupTabEncodeSettingsFFmpeg(leftPanel);

		panel2.add(rightPanel = new JPanel());
		rightPanel.setLayout(new GridBagLayout());
		rightPanel.setOpaque(false);
		border = BorderFactory.createTitledBorder(null, "Image Defaults", TitledBorder.CENTER, TitledBorder.TOP, this.fonts.text, this.colors.text);
		border.setBorder(BorderFactory.createLineBorder(this.colors.backgroundDark));
		rightPanel.setBorder(border);
		this.setupTabEncodeSettingsImage(rightPanel);


		panel1.add((panel2 = new JPanel()));
		panel2.setLayout(new GridLayout(1, 2));
		panel2.setOpaque(false);

		panel2.add(leftPanel = new JPanel());
		leftPanel.setLayout(new GridBagLayout());
		leftPanel.setOpaque(false);
		border = BorderFactory.createTitledBorder(null, "Video Defaults", TitledBorder.CENTER, TitledBorder.TOP, this.fonts.text, this.colors.text);
		border.setBorder(BorderFactory.createLineBorder(this.colors.backgroundDark));
		leftPanel.setBorder(border);
		this.setupTabEncodeSettingsVideo(leftPanel);

		panel2.add(rightPanel = new JPanel());
		rightPanel.setLayout(new GridBagLayout());
		rightPanel.setOpaque(false);
		border = BorderFactory.createTitledBorder(null, "Audio Defaults", TitledBorder.CENTER, TitledBorder.TOP, this.fonts.text, this.colors.text);
		border.setBorder(BorderFactory.createLineBorder(this.colors.backgroundDark));
		rightPanel.setBorder(border);
		this.setupTabEncodeSettingsAudio(rightPanel);



		panel1.add((panel2 = new JPanel()));
		panel2.setLayout(new GridBagLayout());
		panel2.setOpaque(false);

		//{ Save
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.NONE;

		panel2.add(this.settings.saveButton = new JButton("Save Settings"), c);
		this.settings.saveButton.setFont(this.fonts.textSmall);
		this.settings.saveButton.setForeground(this.colors.text);
		this.settings.saveButton.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
		this.settings.saveButton.setEnabled(false);
		this.settings.saveButton.setOpaque(false);
		this.settings.saveButton.addActionListener(new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				self.onSaveSettingsButtonPress();
			}
		});
		//}
	}
	private final void setupTabEncodeSettingsFFmpeg(JPanel container) {
		final GUI self = this;
		JLabel text;
		JPanel pad;
		GridBagConstraints c = new GridBagConstraints();

		//{ Threads
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		container.add(text = new JLabel("Max threads"), c);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++c.gridx;
		c.weightx = 0.0;
		container.add(pad = new JPanel(), c);
		pad.setPreferredSize(new Dimension(10, 0));

		++c.gridx;
		c.weightx = 0.5;
		c.anchor = GridBagConstraints.LINE_START;
		ArrayList<Integer> values = new ArrayList<Integer>();
		int maxThreads = this.videncode.getOutputMaxThreads();
		int current = this.videncode.getVideoFileTempEncodingThreads();
		int sel = 0;
		for (int i = 0; i < maxThreads; ++i) {
			values.add(new Integer(i + 1));
			if (current == i + 1) sel = i;
		}
		container.add(this.settings.ffmpegThreadSelection = new JComboBox<Integer>(values.toArray(new Integer[values.size()])), c);
		this.settings.ffmpegThreadSelection.setFont(this.fonts.textSmall);
		this.settings.ffmpegThreadSelection.setForeground(this.colors.text);
		this.settings.ffmpegThreadSelection.setOpaque(false);
		this.settings.ffmpegThreadSelection.setSelectedIndex(sel);
		this.settings.ffmpegThreadSelection.addActionListener(new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				self.videncode.setVideoFileTempEncodingThreads(
					((Integer) ((JComboBox) event.getSource()).getSelectedItem()).intValue()
				);
				self.enableSaveSettingsButton();
			}
		});
		//}
	}
	private final void setupTabEncodeSettingsOutput(JPanel container) {
		final GUI self = this;
		JLabel text;
		JPanel pad;
		GridBagConstraints c = new GridBagConstraints();

		//{ Max size
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		container.add(text = new JLabel("Max file size"), c);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++c.gridx;
		c.weightx = 0.0;
		container.add(pad = new JPanel(), c);
		pad.setPreferredSize(new Dimension(10, 0));

		++c.gridx;
		c.weightx = 0.5;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.VERTICAL;
		container.add(this.settings.outputMaxSize = new JTextFieldCustom(), c);
		this.settings.outputMaxSize.setPreferredSize(new Dimension(64, 1));
		this.settings.outputMaxSize.setFont(this.fonts.text);
		this.settings.outputMaxSize.setForeground(this.colors.text);
		this.settings.outputMaxSize.setOpaque(false);
		this.settings.outputMaxSize.setBorder(BorderFactory.createLineBorder(this.colors.textLight1));
		this.settings.outputMaxSize.setText(Videncode.intToLabeledString(this.videncode.getOutputMaxFileSize(), 1024, new String[]{ "B" , "B" , "KB" , "MB" }));
		this.settings.outputMaxSize.addCustomChangeListener(new ChangeListener() {
			@Override
			public final void stateChanged(ChangeEvent event) {
				self.onOutputMaxSizeChange((JTextFieldCustom) event.getSource());
				self.enableSaveSettingsButton();
			}
		});
		//}

		//{
		c.weightx = 0.5;
		c.gridx = 0;
		++c.gridy;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		container.add(text = new JLabel("Default extension"), c);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++c.gridx;
		c.weightx = 0.0;
		container.add(pad = new JPanel(), c);
		pad.setPreferredSize(new Dimension(10, 0));

		++c.gridx;
		c.weightx = 0.5;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.VERTICAL;
		container.add(this.settings.outputFilenameExt = new JComboBox<Extension>(this.outputExtensions), c);
		this.settings.outputFilenameExt.setFont(this.fonts.textSmall);
		this.settings.outputFilenameExt.setForeground(this.colors.text);
		this.settings.outputFilenameExt.setOpaque(false);
		this.settings.outputFilenameExt.addActionListener(new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				self.onEncodeExtensionChangeDefault((Extension) ((JComboBox) event.getSource()).getSelectedItem());
				self.enableSaveSettingsButton();
			}
		});
		//}
	}
	private final void setupTabEncodeSettingsApp(JPanel container) {
		final GUI self = this;
		JLabel text;
		JPanel pad;
		GridBagConstraints c = new GridBagConstraints();

		//{ Default tab
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		container.add(text = new JLabel("Main tab"), c);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++c.gridx;
		c.weightx = 0.0;
		container.add(pad = new JPanel(), c);
		pad.setPreferredSize(new Dimension(10, 0));

		++c.gridx;
		c.weightx = 0.5;
		c.anchor = GridBagConstraints.LINE_START;
		ArrayList<String> values = new ArrayList<String>();
		int sel = 0;
		for (int i = this.tabManager.getTabCount() - 1; i >= 0; --i) {
			values.add(this.tabManager.getTitleAt(i));
			if (this.defaultTab.equals(this.tabManager.getTitleAt(i))) {
				sel = this.tabManager.getTabCount() - 1 - i;
			}
		}
		container.add(this.settings.guiMainTabSelection = new JComboBox<String>(values.toArray(new String[values.size()])), c);
		this.settings.guiMainTabSelection.setFont(this.fonts.textSmall);
		this.settings.guiMainTabSelection.setForeground(this.colors.text);
		this.settings.guiMainTabSelection.setOpaque(false);
		this.settings.guiMainTabSelection.setSelectedIndex(sel);
		this.settings.guiMainTabSelection.addActionListener(new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				self.defaultTab = (String) ((JComboBox) event.getSource()).getSelectedItem();
				self.enableSaveSettingsButton();
			}
		});
		//}

		//{ Update check
		c.weightx = 0.5;
		c.gridx = 0;
		++c.gridy;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		container.add(text = new JLabel("Update check"), c);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++c.gridx;
		c.weightx = 0.0;
		container.add(pad = new JPanel(), c);
		pad.setPreferredSize(new Dimension(10, 0));

		++c.gridx;
		c.weightx = 0.5;
		c.anchor = GridBagConstraints.LINE_START;
		container.add(this.settings.appUpdateCheckEnabled = new JCheckBox("enabled"), c);
		this.settings.appUpdateCheckEnabled.setFont(this.fonts.text);
		this.settings.appUpdateCheckEnabled.setForeground(this.colors.text);
		this.settings.appUpdateCheckEnabled.setSelected(this.updateStartupCheck);
		this.settings.appUpdateCheckEnabled.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 4));
		this.settings.appUpdateCheckEnabled.setOpaque(false);
		this.settings.appUpdateCheckEnabled.addActionListener(new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				self.updateStartupCheck = ((JCheckBox) event.getSource()).isSelected();
				self.enableSaveSettingsButton();
			}
		});
		//}

		//{ Look
		c.weightx = 0.5;
		c.gridx = 0;
		++c.gridy;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		container.add(text = new JLabel("Local look"), c);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++c.gridx;
		c.weightx = 0.0;
		container.add(pad = new JPanel(), c);
		pad.setPreferredSize(new Dimension(10, 0));

		++c.gridx;
		c.weightx = 0.5;
		c.anchor = GridBagConstraints.LINE_START;
		container.add(this.settings.appUseLocalLookEnabled = new JCheckBox("enabled"), c);
		this.settings.appUseLocalLookEnabled.setFont(this.fonts.text);
		this.settings.appUseLocalLookEnabled.setForeground(this.colors.text);
		this.settings.appUseLocalLookEnabled.setSelected(this.useLocalLook);
		this.settings.appUseLocalLookEnabled.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 4));
		this.settings.appUseLocalLookEnabled.setOpaque(false);
		this.settings.appUseLocalLookEnabled.addActionListener(new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				self.useLocalLook = ((JCheckBox) event.getSource()).isSelected();
				self.enableSaveSettingsButton();
			}
		});
		//}

		//{ Advanced
		c.weightx = 0.5;
		c.gridx = 0;
		++c.gridy;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		container.add(text = new JLabel("Advanced tab"), c);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++c.gridx;
		c.weightx = 0.0;
		container.add(pad = new JPanel(), c);
		pad.setPreferredSize(new Dimension(10, 0));

		++c.gridx;
		c.weightx = 0.5;
		c.anchor = GridBagConstraints.LINE_START;
		container.add(this.settings.appShowAdvancedTab = new JCheckBox("show"), c);
		this.settings.appShowAdvancedTab.setFont(this.fonts.text);
		this.settings.appShowAdvancedTab.setForeground(this.colors.text);
		this.settings.appShowAdvancedTab.setSelected(this.tabAdvancedEnabled);
		this.settings.appShowAdvancedTab.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 4));
		this.settings.appShowAdvancedTab.setOpaque(false);
		this.settings.appShowAdvancedTab.addActionListener(new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				self.tabAdvancedEnabled = ((JCheckBox) event.getSource()).isSelected();
				self.enableSaveSettingsButton();

				if (self.tabAdvancedEnabled) {
					if (self.tabAdvanced.getParent() == null) {
						self.tabManager.insertTab("Advanced", null, self.tabAdvanced, null, 0);
					}
				}
				else {
					if (self.tabAdvanced.getParent() != null) {
						self.tabManager.removeTabAt(0);
						self.tabManager.revalidate();
						self.tabManager.repaint();
					}
				}
			}
		});
		//}
	}
	private final void setupTabEncodeSettingsAudio(JPanel container) {
		final GUI self = this;
		JLabel text;
		JPanel pad;
		GridBagConstraints c = new GridBagConstraints();

		//{ Bitrates
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		container.add(text = new JLabel("Bitrate"), c);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++c.gridx;
		c.weightx = 0.0;
		container.add(pad = new JPanel(), c);
		pad.setPreferredSize(new Dimension(10, 0));

		++c.gridx;
		c.weightx = 0.5;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.NONE;
		int sel = 0;
		for (int i = 0; i < this.videncode.getAudioBitrates().length; ++i) {
			if (this.videncode.getAudioBitrateDefault().equals(this.videncode.getAudioBitrates()[i])) {
				sel = i;
				break;
			}
		}
		container.add(this.settings.appAudioKbps = new JComboBox<Videncode.Bitrate>(this.videncode.getAudioBitrates()), c);
		this.settings.appAudioKbps.setFont(this.fonts.textSmall);
		this.settings.appAudioKbps.setForeground(this.colors.text);
		this.settings.appAudioKbps.setOpaque(false);
		this.settings.appAudioKbps.setSelectedIndex(sel);
		this.settings.appAudioKbps.addActionListener(new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				self.videncode.setAudioBitrateDefault((Videncode.Bitrate) ((JComboBox) event.getSource()).getSelectedItem());
				self.enableSaveSettingsButton();
			}
		});
		//}

		//{ Channels
		c.weightx = 0.5;
		c.gridx = 0;
		++c.gridy;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		container.add(text = new JLabel("Channels"), c);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++c.gridx;
		c.weightx = 0.0;
		container.add(pad = new JPanel(), c);
		pad.setPreferredSize(new Dimension(10, 0));

		++c.gridx;
		c.weightx = 0.5;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.NONE;
		sel = 0;
		for (int i = 0; i < this.videncode.getAudioChannels().length; ++i) {
			if (this.videncode.getAudioChannelCountDefault().equals(this.videncode.getAudioChannels()[i])) {
				sel = i;
				break;
			}
		}
		container.add(this.settings.appAudioChannels = new JComboBox<Videncode.AudioChannelCount>(this.videncode.getAudioChannels()), c);
		this.settings.appAudioChannels.setFont(this.fonts.textSmall);
		this.settings.appAudioChannels.setForeground(this.colors.text);
		this.settings.appAudioChannels.setOpaque(false);
		this.settings.appAudioChannels.setSelectedIndex(sel);
		this.settings.appAudioChannels.addActionListener(new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				self.videncode.setAudioChannelCountDefault((Videncode.AudioChannelCount) ((JComboBox) event.getSource()).getSelectedItem());
				self.enableSaveSettingsButton();
			}
		});
		//}

		//{ Sample rates
		c.weightx = 0.5;
		c.gridx = 0;
		++c.gridy;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		container.add(text = new JLabel("Sample rate"), c);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++c.gridx;
		c.weightx = 0.0;
		container.add(pad = new JPanel(), c);
		pad.setPreferredSize(new Dimension(10, 0));

		++c.gridx;
		c.weightx = 0.5;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.NONE;
		container.add(this.settings.appAudioSampleRate = new JComboBox<Videncode.AudioSampleRate>(), c);
		this.settings.appAudioSampleRate.setFont(this.fonts.textSmall);
		this.settings.appAudioSampleRate.setForeground(this.colors.text);
		this.settings.appAudioSampleRate.setOpaque(false);
		sel = 0;
		for (int i = this.videncode.getAudioSampleRates().length - 1; i >= 0; --i) {
			this.settings.appAudioSampleRate.addItem(this.videncode.getAudioSampleRates()[i]);
			if (this.videncode.getAudioSampleRateDefault().equals(this.videncode.getAudioSampleRates()[i])) {
				sel = this.videncode.getAudioSampleRates().length - 1 - i;
			}
		}
		this.settings.appAudioSampleRate.setSelectedIndex(sel);
		this.settings.appAudioSampleRate.addActionListener(new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				self.videncode.setAudioSampleRateDefault((Videncode.AudioSampleRate) ((JComboBox) event.getSource()).getSelectedItem());
				self.enableSaveSettingsButton();
			}
		});
		//}

	}
	private final void setupTabEncodeSettingsVideo(JPanel container) {
		final GUI self = this;
		JLabel text;
		JPanel pad;
		GridBagConstraints c = new GridBagConstraints();

		//{ Auto quality enabled
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		container.add(text = new JLabel("Auto quality"), c);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++c.gridx;
		c.weightx = 0.0;
		container.add(pad = new JPanel(), c);
		pad.setPreferredSize(new Dimension(10, 0));

		++c.gridx;
		c.weightx = 0.5;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.NONE;
		container.add(this.settings.appVideoAutoQualityEnabled = new JCheckBox("enabled"), c);
		this.settings.appVideoAutoQualityEnabled.setFont(this.fonts.text);
		this.settings.appVideoAutoQualityEnabled.setForeground(this.colors.text);
		this.settings.appVideoAutoQualityEnabled.setSelected(this.videoEncodeAutoQualityEnabledDefault);
		this.settings.appVideoAutoQualityEnabled.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 4));
		this.settings.appVideoAutoQualityEnabled.setOpaque(false);
		this.settings.appVideoAutoQualityEnabled.addActionListener(new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				self.videoEncodeAutoQualityEnabledDefault = ((JCheckBox) event.getSource()).isSelected();
				self.enableSaveSettingsButton();
			}
		});

		//}

		//{ Auto quality profile
		c.weightx = 0.5;
		c.gridx = 0;
		++c.gridy;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		container.add(text = new JLabel(" "), c);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++c.gridx;
		c.weightx = 0.0;
		container.add(pad = new JPanel(), c);
		pad.setPreferredSize(new Dimension(10, 0));

		++c.gridx;
		c.weightx = 0.5;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.NONE;

		ArrayList<Videncode.AutoQualityProfile> values = new ArrayList<Videncode.AutoQualityProfile>();
		int sel = 0;
		for (int i = 0; i < this.videncode.getVideoAutoQualityProfiles().length; ++i) {
			values.add(this.videncode.getVideoAutoQualityProfiles()[i]);
			if (this.videncode.getVideoAutoQualityProfileDefault().equals(this.videncode.getVideoAutoQualityProfiles()[i])) sel = i;
		}
		container.add(this.settings.appVideoAutoQuality = new JComboBox<Videncode.AutoQualityProfile>(values.toArray(new Videncode.AutoQualityProfile[values.size()])), c);
		this.settings.appVideoAutoQuality.setFont(this.fonts.textSmall);
		this.settings.appVideoAutoQuality.setForeground(this.colors.text);
		this.settings.appVideoAutoQuality.setOpaque(false);
		this.settings.appVideoAutoQuality.setSelectedIndex(sel);
		this.settings.appVideoAutoQuality.addActionListener(new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				self.videncode.setVideoAutoQualityProfileDefault((Videncode.AutoQualityProfile) ((JComboBox) event.getSource()).getSelectedItem());
				self.enableSaveSettingsButton();
			}
		});
		//}

		//{ Encoding mode
		c.weightx = 0.5;
		c.gridx = 0;
		++c.gridy;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		container.add(text = new JLabel(" "), c);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++c.gridx;
		c.weightx = 0.0;
		container.add(pad = new JPanel(), c);
		pad.setPreferredSize(new Dimension(10, 0));

		++c.gridx;
		c.weightx = 0.5;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.NONE;

		ArrayList<String> values2 = new ArrayList<String>();
		sel = 0;
		for (int i = 0; i < this.videncode.getVideoEncodingProfiles().length; ++i) {
			values2.add(this.videncode.getVideoEncodingProfiles()[i]);
			if (this.videncode.getVideoEncodingProfileDefault() == i) sel = i;
		}
		container.add(this.settings.appVideoEncodingMode = new JComboBox<String>(values2.toArray(new String[values2.size()])), c);
		this.settings.appVideoEncodingMode.setFont(this.fonts.textSmall);
		this.settings.appVideoEncodingMode.setForeground(this.colors.text);
		this.settings.appVideoEncodingMode.setOpaque(false);
		this.settings.appVideoEncodingMode.setSelectedIndex(sel);
		this.settings.appVideoEncodingMode.addActionListener(new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				String str = ((String) ((JComboBox) event.getSource()).getSelectedItem());
				for (int i = 0; i < self.videncode.getVideoEncodingProfiles().length; ++i) {
					if (str.equals(self.videncode.getVideoEncodingProfiles()[i])) {
						self.videncode.setVideoEncodingProfileDefault(i);
						break;
					}
				}
				self.enableSaveSettingsButton();
			}
		});
		//}
	}
	private final void setupTabEncodeSettingsImage(JPanel container) {
		final GUI self = this;
		JLabel text;
		JPanel pad;
		GridBagConstraints c = new GridBagConstraints();

		//{ Default quality
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		container.add(text = new JLabel("Quality"), c);
		text.setFont(this.fonts.text);
		text.setForeground(this.colors.text);

		++c.gridx;
		c.weightx = 0.0;
		container.add(pad = new JPanel(), c);
		pad.setPreferredSize(new Dimension(10, 0));

		++c.gridx;
		c.weightx = 0.5;
		c.anchor = GridBagConstraints.LINE_START;
		ArrayList<ImageQuality> values = new ArrayList<ImageQuality>();
		int sel = 0;
		int selPos = 0;
		for (int i = this.videncode.getImageQualityRange()[0]; i <= this.videncode.getImageQualityRange()[1]; ++i) {
			values.add(new ImageQuality(i,
				i == 0 ? "Lossless" : (
					(i) + (i == 1 ? " (Best)" : (i == this.videncode.getImageQualityRange()[1] ? " (Worst)" : "")))
			));
			if (i == this.videncode.getImageQualityDefault()) sel = selPos;;
			++selPos;
		}
		container.add(this.settings.appImageQuality = new JComboBox<ImageQuality>(values.toArray(new ImageQuality[values.size()])), c);
		this.settings.appImageQuality.setFont(this.fonts.textSmall);
		this.settings.appImageQuality.setForeground(this.colors.text);
		this.settings.appImageQuality.setOpaque(false);
		this.settings.appImageQuality.setSelectedIndex(sel);
		this.settings.appImageQuality.addActionListener(new ActionListener(){
			@Override
			public final void actionPerformed(ActionEvent event) {
				self.videncode.setImageQualityDefault(((ImageQuality) ((JComboBox) event.getSource()).getSelectedItem()).getQuality());
				self.enableSaveSettingsButton();
			}
		});
		//}
	}


	private final void setupTabAdvanced(JPanel container) {
		JLabel text;
		JPanel panel, leftPanel, rightPanel, pad;
		TitledBorder border;

		// Panels
		container.setLayout(new GridLayout(1, 2));


		container.add((panel = new JPanel()));
		panel.setLayout(new BorderLayout());
		panel.setOpaque(false);

		panel.add((leftPanel = new JPanel()), BorderLayout.CENTER);
		leftPanel.setLayout(new GridBagLayout());
		leftPanel.setOpaque(false);


		container.add((panel = new JPanel()));
		panel.setLayout(new BorderLayout());
		panel.setOpaque(false);

		panel.add((rightPanel = new JPanel()), BorderLayout.CENTER);
		rightPanel.setLayout(new GridBagLayout());
		rightPanel.setOpaque(false);


		// Status
		this.setupTabAdvancedStatusLog(leftPanel);
		// Settings
		this.setupTabAdvancedEncodeSettings(rightPanel);
	}
	private final void setupTabAdvancedStatusLog(JPanel container) {
		GridBagConstraints gc = new GridBagConstraints();
		JLabel text;
		JPanel section;
		TitledBorder border;
		JScrollPane scroll;

		//{ Image logs
		gc.weightx = 0.5;
		gc.weighty = 0.5;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.anchor = GridBagConstraints.PAGE_START;
		gc.fill = GridBagConstraints.BOTH;
		container.add((section = new JPanel()), gc);
		section.setLayout(new GridLayout(1, 2));
		section.setOpaque(false);
		border = BorderFactory.createTitledBorder(null, "Image Encoding Logs", TitledBorder.CENTER, TitledBorder.TOP, this.fonts.text, this.colors.text);
		border.setBorder(BorderFactory.createLineBorder(this.colors.backgroundDark));
		section.setBorder(border);

		section.add(scroll = new JScrollPane(this.advanced.logs[0][0] = new JTextArea()));
		scroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 1), BorderFactory.createLineBorder(this.colors.textLight1)));
		scroll.setOpaque(false);
		this.advanced.logs[0][0].setOpaque(false);
		this.advanced.logs[0][0].setForeground(this.colors.text);
		this.advanced.logs[0][0].setFont(this.fonts.logText);
		this.advanced.logs[0][0].setWrapStyleWord(true);
		this.advanced.logs[0][0].setEditable(false);

		section.add(scroll = new JScrollPane(this.advanced.logs[0][1] = new JTextArea()));
		scroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 1, 0, 0), BorderFactory.createLineBorder(this.colors.textLight1)));
		scroll.setOpaque(false);
		this.advanced.logs[0][1].setOpaque(false);
		this.advanced.logs[0][1].setForeground(this.colors.text);
		this.advanced.logs[0][1].setFont(this.fonts.logText);
		this.advanced.logs[0][1].setWrapStyleWord(true);
		this.advanced.logs[0][1].setEditable(false);
		//}
		//{ Audio logs
		++gc.gridy;
		container.add((section = new JPanel()), gc);
		section.setLayout(new GridLayout(1, 2));
		section.setOpaque(false);
		border = BorderFactory.createTitledBorder(null, "Audio Encoding Logs", TitledBorder.CENTER, TitledBorder.TOP, this.fonts.text, this.colors.text);
		border.setBorder(BorderFactory.createLineBorder(this.colors.backgroundDark));
		section.setBorder(border);


		section.add(scroll = new JScrollPane(this.advanced.logs[1][0] = new JTextArea()));
		scroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 1), BorderFactory.createLineBorder(this.colors.textLight1)));
		scroll.setOpaque(false);
		this.advanced.logs[1][0].setOpaque(false);
		this.advanced.logs[1][0].setForeground(this.colors.text);
		this.advanced.logs[1][0].setFont(this.fonts.logText);
		this.advanced.logs[1][0].setWrapStyleWord(true);
		this.advanced.logs[1][0].setEditable(false);

		section.add(scroll = new JScrollPane(this.advanced.logs[1][1] = new JTextArea()));
		scroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 1, 0, 0), BorderFactory.createLineBorder(this.colors.textLight1)));
		scroll.setOpaque(false);
		this.advanced.logs[1][1].setOpaque(false);
		this.advanced.logs[1][1].setForeground(this.colors.text);
		this.advanced.logs[1][1].setFont(this.fonts.logText);
		this.advanced.logs[1][1].setWrapStyleWord(true);
		this.advanced.logs[1][1].setEditable(false);
		//}
		//{ Video logs
		++gc.gridy;
		container.add((section = new JPanel()), gc);
		section.setLayout(new GridLayout(1, 2));
		section.setOpaque(false);
		border = BorderFactory.createTitledBorder(null, "Video Encoding Logs", TitledBorder.CENTER, TitledBorder.TOP, this.fonts.text, this.colors.text);
		border.setBorder(BorderFactory.createLineBorder(this.colors.backgroundDark));
		section.setBorder(border);


		section.add(scroll = new JScrollPane(this.advanced.logs[2][0] = new JTextArea()));
		scroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 1), BorderFactory.createLineBorder(this.colors.textLight1)));
		scroll.setOpaque(false);
		this.advanced.logs[2][0].setOpaque(false);
		this.advanced.logs[2][0].setForeground(this.colors.text);
		this.advanced.logs[2][0].setFont(this.fonts.logText);
		this.advanced.logs[2][0].setWrapStyleWord(true);
		this.advanced.logs[2][0].setEditable(false);

		section.add(scroll = new JScrollPane(this.advanced.logs[2][1] = new JTextArea()));
		scroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 1, 0, 0), BorderFactory.createLineBorder(this.colors.textLight1)));
		scroll.setOpaque(false);
		this.advanced.logs[2][1].setOpaque(false);
		this.advanced.logs[2][1].setForeground(this.colors.text);
		this.advanced.logs[2][1].setFont(this.fonts.logText);
		this.advanced.logs[2][1].setWrapStyleWord(true);
		this.advanced.logs[2][1].setEditable(false);
		//}
	}
	private final void setupTabAdvancedEncodeSettings(JPanel container) {
		GridBagConstraints gc = new GridBagConstraints();
		GridBagConstraints gc2 = new GridBagConstraints();
		JLabel text;
		JPanel section;
		TitledBorder border;
		JScrollPane scroll;

		//{ Video
		gc.weightx = 0.5;
		gc.weighty = 0.5;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.anchor = GridBagConstraints.PAGE_START;
		gc.fill = GridBagConstraints.BOTH;
		container.add((section = new JPanel()), gc);
		section.setLayout(new GridBagLayout());
		section.setOpaque(false);
		border = BorderFactory.createTitledBorder(null, "FFmpeg Video Options", TitledBorder.CENTER, TitledBorder.TOP, this.fonts.text, this.colors.text);
		border.setBorder(BorderFactory.createLineBorder(this.colors.backgroundDark));
		section.setBorder(border);

		// Label
		gc2.weightx = 0.5;
		gc2.weighty = 0.0;
		gc2.gridx = 0;
		gc2.gridy = 0;
		gc2.anchor = GridBagConstraints.PAGE_START;
		gc2.fill = GridBagConstraints.BOTH;
		JTextArea textarea;
		section.add(textarea = new JTextArea(), gc2);
		textarea.setText("ffmpeg -y -v info -ss <start> -i <input> -an -map_metadata -1 -codec:v libvpx -t <length> -r <framerate> -s <scale> -cpu-used 0 -threads <threads> [-maxrate <bitrate>] [-minrate <bitrate>] [-b:v <bitrate>] [-bufsize <bitrate>] [-pass <pass_number>] [EXTRA_PARAMS_INSERTED_HERE] -f webm <output>");
		textarea.setFont(this.fonts.text);
		textarea.setForeground(this.colors.text);
		textarea.setAlignmentX(Component.LEFT_ALIGNMENT);
		textarea.setWrapStyleWord(true);
		textarea.setLineWrap(true);
		textarea.setEditable(false);
		textarea.setOpaque(false);

		++gc2.gridy;
		gc2.weighty = 0.5;
		section.add(scroll = new JScrollPane(this.advanced.ffmpegSettings[0] = new JTextArea()), gc2);
		scroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 1), BorderFactory.createLineBorder(this.colors.textLight1)));
		scroll.setOpaque(false);
		this.advanced.ffmpegSettings[0].setOpaque(false);
		this.advanced.ffmpegSettings[0].setForeground(this.colors.text);
		this.advanced.ffmpegSettings[0].setFont(this.fonts.logText);
		this.advanced.ffmpegSettings[0].setWrapStyleWord(true);

		//}
		//{ Audio
		++gc.gridy;
		container.add((section = new JPanel()), gc);
		section.setLayout(new GridBagLayout());
		section.setOpaque(false);
		border = BorderFactory.createTitledBorder(null, "FFmpeg Audio Options", TitledBorder.CENTER, TitledBorder.TOP, this.fonts.text, this.colors.text);
		border.setBorder(BorderFactory.createLineBorder(this.colors.backgroundDark));
		section.setBorder(border);

		// Label
		gc2.weightx = 0.5;
		gc2.weighty = 0.0;
		gc2.gridx = 0;
		gc2.gridy = 0;
		gc2.anchor = GridBagConstraints.PAGE_START;
		gc2.fill = GridBagConstraints.BOTH;
		section.add(textarea = new JTextArea(), gc2);
		textarea.setText("ffmpeg -y -v info -i <file> -map_metadata -1 -vn -codec:a libvorbis -ss <start> -t <length> -b:a <audio_bitrate> [-ar <audio_rate>] [-ac <audio_channels>] [EXTRA_PARAMS_INSERTED_HERE] -f ogg <output_file>");
		textarea.setFont(this.fonts.text);
		textarea.setForeground(this.colors.text);
		textarea.setAlignmentX(Component.LEFT_ALIGNMENT);
		textarea.setWrapStyleWord(true);
		textarea.setLineWrap(true);
		textarea.setEditable(false);
		textarea.setOpaque(false);

		++gc2.gridy;
		gc2.weighty = 0.5;
		section.add(scroll = new JScrollPane(this.advanced.ffmpegSettings[1] = new JTextArea()), gc2);
		scroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 1), BorderFactory.createLineBorder(this.colors.textLight1)));
		scroll.setOpaque(false);
		this.advanced.ffmpegSettings[1].setOpaque(false);
		this.advanced.ffmpegSettings[1].setForeground(this.colors.text);
		this.advanced.ffmpegSettings[1].setFont(this.fonts.logText);
		this.advanced.ffmpegSettings[1].setWrapStyleWord(true);

		//}

		// Events
		final GUI self = this;
		DocumentListener cl = new DocumentListener(){
			@Override
			public final void insertUpdate(DocumentEvent event) {
				this.onChange(event.getDocument());
			}
			@Override
			public final void removeUpdate(DocumentEvent event) {
				this.onChange(event.getDocument());
			}
			@Override
			public final void changedUpdate(DocumentEvent event) {
				this.onChange(event.getDocument());
			}


			private final void onChange(Document me) {
				String value = "";
				try {
					value = me.getText(0, me.getLength());
				}
				catch (Exception e) {
				}
				self.videncode.setExtraOptions(
					value, (me == self.advanced.ffmpegSettings[0].getDocument())
				);
			}
		};
		this.advanced.ffmpegSettings[0].getDocument().addDocumentListener(cl);
		this.advanced.ffmpegSettings[1].getDocument().addDocumentListener(cl);
	}


	// GUI settings
	private final void setRangeSliderColors(JRangeSlider slider) {
		slider.getColors().barBorderDark[0] =
			slider.getColors().barBorderDark[1] =
			slider.getColors().barBorderDark[2] = this.colors.backgroundDarkest;
		slider.getColors().borderDark[0] =
			slider.getColors().borderDark[1] =
			slider.getColors().borderDark[2] = this.colors.backgroundDark;
		slider.getColors().borderLight[0] =
			slider.getColors().borderLight[1] =
			slider.getColors().borderLight[2] =
			slider.getColors().barBorderLight[0] =
			slider.getColors().barBorderLight[1] =
			slider.getColors().barBorderLight[2] = this.colors.background;
		slider.getColors().grabbers[0] =
			slider.getColors().grabbers[1] =
			slider.getColors().grabbers[2] = this.colors.textLight2;
		slider.getColors().bar[0] =
			slider.getColors().bar[1] =
			slider.getColors().bar[2] = this.colors.textLight1;
		slider.getColors().background[0] =
			slider.getColors().background[1] =
			slider.getColors().background[2] = this.colors.textLight3;

		slider.getColors().barBorderDark[3] = this.colors.backgroundDark;
		slider.getColors().borderDark[3] = this.colors.backgroundDark;
		slider.getColors().borderLight[3] = this.colors.background;
		slider.getColors().barBorderLight[3] = this.colors.background;
		slider.getColors().grabbers[3] = this.colors.textLight1;
		slider.getColors().bar[3] = this.colors.textLight3;
		slider.getColors().background[3] = this.colors.textLight3;
	}
	private final void setBarSliderColors(JBarSlider slider) {
		slider.getColors().barBorderDark[0] =
			slider.getColors().barBorderDark[1] =
			slider.getColors().barBorderDark[2] = this.colors.backgroundDarkest;
		slider.getColors().borderDark[0] =
			slider.getColors().borderDark[1] =
			slider.getColors().borderDark[2] = this.colors.backgroundDark;
		slider.getColors().borderLight[0] =
			slider.getColors().borderLight[1] =
			slider.getColors().borderLight[2] =
			slider.getColors().barBorderLight[0] =
			slider.getColors().barBorderLight[1] =
			slider.getColors().barBorderLight[2] = this.colors.background;
		slider.getColors().grabbers[0] =
			slider.getColors().grabbers[1] =
			slider.getColors().grabbers[2] = this.colors.textLight2;
		slider.getColors().background[0] =
			slider.getColors().background[1] =
			slider.getColors().background[2] = this.colors.textLight3;

		slider.getColors().barBorderDark[3] = this.colors.backgroundDark;
		slider.getColors().borderDark[3] = this.colors.backgroundDark;
		slider.getColors().borderLight[3] = this.colors.background;
		slider.getColors().barBorderLight[3] = this.colors.background;
		slider.getColors().grabbers[3] = this.colors.textLight1;
		slider.getColors().background[3] = this.colors.textLight3;
	}


	// Events
	private final void onVideoFileSourceChange() {
		this.updateVideoExistence();
		this.updateVideoFile();
		this.updateVideoStatistics();

		this.autoSelectVideoQuality(false);

		this.updateVideoEncodingDurationSlider();
		this.updateVideoEncodingDurationDisplay();

		this.updateVideoBaselineResolutionComboBox();
		this.updateVideoBaselineFrameRateComboBoxSelection();

		this.updateAudioIfLockedToVideo(true);
		this.updateSyncStatus();
		this.updateEncodeButtonStatus();
		this.updateEncodeFilenameAndTag();
	}
	private final void onVideoFileTempChange(boolean updating) {
		this.updateVideoEncodingStatus(true);

		this.updateVideoTempFilesizeDisplay();
	}
	private final void onImageFileSourceChange() {
		this.updateImageExistence();
		this.updateImageFile();
		this.updateImageStatistics();
		this.updateTempImageStatistics();

		this.updateImageSpaceStatistics();
		this.updateImageQualityComboBox();
		this.updateImageScaleComboBox();

		double time = this.videncode.getImageFileSourceVideoTime();
		double duration = this.videncode.getVideoFileSourceDuration();

		this.updateImageTimeDisplay();
		this.imagePreviewTimeSlider.setRange(0.0, duration, time);
		this.updateEncodeFilenameAndTag();
	}
	private final void onImageFileTempChange(boolean updating) {
		Image img = this.videncode.getImageFileImageTemp();
		if (!(img == null && updating)) {
			this.displayImageOnTarget(this.imagePreview, img);

			this.updateImageFile();

			this.updateTempImageStatistics();
			this.updateImageSpaceStatistics();
			this.updateImageScaleComboBox();
			this.updateVideoEncodeButtonAndBitrate();
			this.autoSelectVideoQuality(true);
		}
		this.updateImageEncodingStatus();
		this.updateEncodeExtension();
		this.updateEncodeButtonStatus();
		this.updateEncodeFilenameAndTag();
	}
	private final void onAudioFileSourceChange() {
		this.updateAudioFile();
		this.updateAudioExistence();

		this.updateAudioEncodingDurationSlider();
		this.updateAudioEncodingDurationDisplay();
		this.updateAudioChannelSelectionComboBoxSelection();
		this.updateAudioBitrateSelectionComboBoxSelection();
		this.updateAudioSampleRateSelectionComboBoxSelection();

		this.updateAudioIfLockedToVideo(true);
		this.updateAudioEncodingDurationSliderUsability();

		this.updateAudioTempFilesizeDisplay();

		this.updateSyncStatus();
		this.updateEncodeButtonStatus();
		this.updateEncodeFilenameAndTag();
	}
	private final void onAudioFileTempChange(boolean updating) {
		this.updateAudioEncodingStatus(true);

		this.updateAudioTempFilesizeDisplay();
		this.updateVideoEncodeButtonAndBitrate();
		this.autoSelectVideoQuality(true);
	}
	private final void onVideoPreviewChange(int id, boolean updating) {
		if (id == this.videoImagePreviewId) {
			Image img = this.videncode.getVideoPreviewTempImage(id);
			if (!(img == null && updating)) {
				this.displayImageOnTarget(this.videoImagePreview, img);
			}
		}
	}
	private final void onAudioPreviewChange(int id, boolean updating) {
		boolean exists = this.videncode.getAudioPreviewTemp(id) != null;

		this.audioPreviewClipButtons[id].setEnabled(exists);
	}
	private final void onAudioProgressChange(double progress) {
		this.updateAudioEncodingStatus(false, true, progress, null);
	}
	private final void onVideoProgressChange(double progress) {
		this.updateVideoEncodingStatus(false, true, progress, null);
	}
	private final void onEncodeStatusChange(boolean cleared, boolean error, boolean muxing, boolean reset, boolean complete, String message) {
		if (reset) {
			this.encode.statusDisplay.setText(" ");
		}
		if (message != null) {
			if (error) {
				this.encode.statusDisplay.setText(message);
			}
			this.updateEncodeButtonStatus();
			this.updateVideoEncodingStatus(true);
			this.updateAudioEncodingStatus(true);
			this.updateEncodeEncodingStatus();
		}
	}

	// Shut down
	private final void close() {
		if (this.videncode != null) {
			this.videncode.clean();
		}
	}

	// Pre-setup
	private final void acquireResources() {
		this.noimage = Videncode.createImageFromFile(this.getClass().getResource("res/noimage.png"), this.getRootPane());
	}

	// Video actions
	private final void updateVideoExistence() {
		boolean exists = (this.videncode.getVideoFileSource() != null);

		this.videoSourceCheckbox.setEnabled(exists);
		this.videoSourceCheckbox.setSelected(exists);

		this.updateVideoEncodeButtonAndBitrate();

		this.videoEncodingDurationRangeslider.setEnabled(exists);
		for (int i = 0; i < this.videoEncodingDurationTimecode.length; ++i) {
			this.videoEncodingDurationTimecode[i].setEnabled(exists);
		}

		for (int i = 0; i < this.imagePreviewSourceButtons.length; ++i) {
			this.imagePreviewSourceButtons[i].setEnabled(exists);
		}
		for (int i = 0; i < this.audioSourceButtons.length; ++i) {
			this.audioSourceButtons[i].setEnabled(exists);
		}

		this.videoBaselineResolutionComboBox.setEnabled(exists);
		this.videoBaselineFrameRateComboBox.setEnabled(exists);
		this.videoEncodeAutoQualitySelect.setEnabled(exists);
		this.videoAutoQualityProfileComboBox.setEnabled(exists && this.videoEncodeAutoQualitySelect.isSelected());
		for (int i = 0; i < this.videoImagePreviewButtons.length; ++i) {
			this.videoImagePreviewButtons[i].setEnabled(exists);
		}
		for (int i = 0; i < this.videoEncodeModeButtons.length; ++i) {
			this.videoEncodeModeButtons[i].setEnabled(exists);
		}
		for (int i = 0; i < this.videoEncodeModeButtons.length; ++i) {
			this.videoEncodeModeButtons[i].setEnabled(exists);
		}
	}
	private final void updateVideoFile() {
		File f = this.videncode.getVideoFileSource();
		String n = (f == null ? "" : f.getName());

		this.fileLabelVideo.setText((n.length() == 0) ? " " : n);

		// Durations
		double duration = this.videncode.getVideoFileSourceDuration();
		for (int i = 0; i < this.timecodeEnd.length; ++i) {
			this.timecodeEnd[i].setText(Videncode.timeToString(duration, new int[]{0,2,2,2}));
		}
	}
	private final void updateVideoPreviewIndex(int id, boolean updateGUI) {
		if (id != this.videoImagePreviewId) {
			this.videoImagePreviewId = id;
			if (updateGUI) {
				this.videoImagePreviewButtonGroup.setSelected(this.videoImagePreviewButtons[this.videoImagePreviewId].getModel(), true);
			}

			Image img = this.videncode.getVideoPreviewTempImage(this.videoImagePreviewId);
			this.displayImageOnTarget(this.videoImagePreview, img);
		}
	}
	private final void updateVideoStatistics() {
		// Resolution
		Dimension d = this.videncode.getVideoFileSourceDimensions();
		this.videoResolutionDisplay.setText(d.width + "x" + d.height);

		// Framerate
		this.videoFrameRateDisplay.setText(this.videncode.getVideoFileSourceFrameRate().toShortString());
	}
	private final void updateVideoEncodingDurationSlider() {
		double duration = this.videncode.getVideoFileSourceDuration();
		double[] encodeDuration = this.videncode.getVideoFileSourceEncodeDuration();

		this.videoEncodingDurationRangeslider.setRange(0.0, duration, encodeDuration[0], encodeDuration[1]);
	}
	private final void updateVideoEncodingDurationDisplay() {
		double[] ed = this.videncode.getVideoFileSourceEncodeDuration();

		this.updateVideoEncodingDurationDisplay(ed[0], ed[1]);
	}
	private final void updateVideoEncodingDurationDisplay(double start, double finish) {
		this.videoEncodingDurationTimecode[0].setText(Videncode.timeToString(start, new int[]{0,2,2,2}));
		this.videoEncodingDurationTimecode[1].setText(Videncode.timeToString(finish, new int[]{0,2,2,2}));
	}
	private final void updateVideoBaselineResolutionComboBox() {
		boolean b = this.videoBaselineResolutionComboBoxModifying;
		this.videoBaselineResolutionComboBoxModifying = true;


		int[] resolutions = this.videncode.getResolutions();

		int istart = resolutions.length - 1;
		int sel = -1, selPos = 0;
		StringBuilder sb = new StringBuilder();

		Dimension[] sizes = new Dimension[2];
		boolean[] needed = new boolean[]{ true , true };
		String[] keys = new String[]{ "Source video" , null };
		sizes[0] = this.videncode.getVideoFileSourceDimensions();
		sizes[1] = this.videncode.getVideoFileTempDimensions();
		Dimension match = sizes[1];

		for (int i = 0; i < needed.length; ++i) {
			if (sizes[i].width <= 0 && sizes[i].height <= 0) {
				needed[i] = false;
				continue;
			}
			for (int j = i + 1; j < needed.length; ++j) {
				if (sizes[i].width == sizes[j].width && sizes[i].height == sizes[j].height) {
					needed[j] = false;
				}
			}
		}

		this.videoBaselineResolutionComboBox.removeAllItems();

		// Check for larger
		for (int j = 0; j < needed.length; ++j) {
			if (needed[j] && sizes[j].height > resolutions[istart]) {
				// Resolution
				sb.setLength(0);
				if (sizes[0].height != 0 && sizes[0].width * sizes[j].height / sizes[0].height == sizes[j].width) {
					// Same aspect ratio as video
					sb.append(sizes[j].height);
					sb.append('p');
				}
				else {
					sb.append(sizes[j].width);
					sb.append('x');
					sb.append(sizes[j].height);
				}
				if (keys[j] != null) {
					sb.append(" / ");
					sb.append(keys[j]);
				}
				this.videoBaselineResolutionComboBox.addItem(new ImageScale(sizes[j].height, sb.toString()));
				if (match.height == sizes[j].height) sel = selPos;
				++selPos;

				needed[j] = false;
			}
		}

		for (int i = istart; i >= 0; --i) {
			// Resolution
			sb.setLength(0);
			sb.append(resolutions[i]);
			sb.append('p');
			for (int j = 0; j < needed.length; ++j) { // Search for any matches
				if (needed[j] && sizes[j].height == resolutions[i] && sizes[0].height != 0 && sizes[0].width * sizes[j].height / sizes[0].height == sizes[j].width) {
					if (keys[j] != null) {
						sb.append(" / ");
						sb.append(keys[j]);
					}
					needed[j] = false;
					break;
				}
			}
			this.videoBaselineResolutionComboBox.addItem(new ImageScale(resolutions[i], sb.toString()));
			if (match.height == resolutions[i]) sel = selPos;
			++selPos;

			// Check for smaller
			for (int j = 0; j < needed.length; ++j) {
				if (needed[j] && sizes[j].height <= resolutions[i] && (i == 0 || sizes[j].height > resolutions[i - 1])) {
					// Resolution
					sb.setLength(0);
					if (sizes[0].height != 0 && sizes[0].width * sizes[j].height / sizes[0].height == sizes[j].width) {
						// Same aspect ratio as video
						sb.append(sizes[j].height);
						sb.append('p');
					}
					else {
						sb.append(sizes[j].width);
						sb.append('x');
						sb.append(sizes[j].height);
					}
					if (keys[j] != null) {
						sb.append(" / ");
						sb.append(keys[j]);
					}
					this.videoBaselineResolutionComboBox.addItem(new ImageScale(sizes[j].height, sb.toString()));
					if (match.height == sizes[j].height) sel = selPos;
					++selPos;

					needed[j] = false;
				}
			}
		}

		// Select
		if (sel >= 0) {
			this.videoBaselineResolutionComboBox.setSelectedIndex(sel);
		}
		else {
			this.videoBaselineResolutionComboBox.setSelectedItem(new ImageScale(match.width + "x" + match.height));
		}

		this.videoBaselineResolutionComboBoxModifying = b;
	}
	private final void updateVideoBaselineFrameRateComboBox() {
		boolean b = this.videoBaselineFrameRateComboBoxModifying;
		this.videoBaselineFrameRateComboBoxModifying = true;

		Videncode.FrameRate[] framerates = this.videncode.getFramerates();

		int istart = framerates.length - 1;

		this.videoBaselineFrameRateComboBox.removeAllItems();
		for (int i = istart; i >= 0; --i) {
			// Resolution
			this.videoBaselineFrameRateComboBox.addItem(framerates[i]);
		}

		this.updateVideoBaselineFrameRateComboBoxSelection();

		this.videoBaselineFrameRateComboBoxModifying = b;
	}
	private final void updateVideoBaselineFrameRateComboBoxSelection() {
		boolean b = this.videoBaselineFrameRateComboBoxModifying;
		this.videoBaselineFrameRateComboBoxModifying = true;

		Videncode.FrameRate fr = this.videncode.getVideoFileTempFrameRate();

		int sel = -1;
		for (int i = 0; i < this.videoBaselineFrameRateComboBox.getItemCount(); ++i) {
			if ((this.videoBaselineFrameRateComboBox.getItemAt(i)).equals(fr)) {
				sel = i;
				break;
			}
		}

		if (sel >= 0) {
			this.videoBaselineFrameRateComboBox.setSelectedIndex(sel);
		}
		else {
			this.videoBaselineFrameRateComboBox.setSelectedItem(Videncode.FrameRate.create(fr));
		}

		this.videoBaselineFrameRateComboBoxModifying = b;
	}
	private final void updateVideoAutoQualityProfileComboBox() {
		boolean b = this.videoAutoQualityProfileComboBoxModifying;
		this.videoAutoQualityProfileComboBoxModifying = true;

		Videncode.AutoQualityProfile[] qualities = this.videncode.getVideoAutoQualityProfiles();

		int sel = 0;
		this.videoAutoQualityProfileComboBox.removeAllItems();
		for (int i = 0; i < qualities.length; ++i) {
			// Resolution
			this.videoAutoQualityProfileComboBox.addItem(qualities[i]);
			if (qualities[i] == this.encodingAutoQualityProfile) {
				sel = i;
			}
		}
		this.videoAutoQualityProfileComboBox.setSelectedIndex(sel);

		this.videoAutoQualityProfileComboBoxModifying = b;
	}
	private final void updateVideoEncodingStatus(boolean updateVisibility) {
		this.updateVideoEncodingStatus(
			updateVisibility,
			this.videncode.isVideoFileTempEncoding(),
			this.videncode.getVideoFileTempEncodingPercent(),
			this.videncode.getVideoFileTemp()
		);
	}
	private final void updateVideoEncodingStatus(boolean updateVisibility, boolean isEncoding, double encodingPercent, File tempFile) {
		if (updateVisibility) {
			boolean[] visible = new boolean[]{ false , false , false };
			if (isEncoding) {
				visible[2] = true;
			}
			else if (tempFile != null) {
				visible[1] = true;
			}
			else {
				visible[0] = true;
			}
			this.videoTempEncodeActivateButton.setVisible(visible[0]);
			this.videoTempEncodeStatusText.setVisible(visible[1]);
			this.videoTempEncodeProgressBar.setVisible(visible[2]);


			File srcFile = this.videncode.getVideoFileSource();
			long tempFileSize = this.videncode.getVideoFileTempFileSize();
			long maxFileSize = this.videncode.getOutputMaxFileSize() - this.videncode.getOutputMetadataLength();
			boolean okaySize = (srcFile != null && tempFileSize < maxFileSize);

			this.encode.videoStatusTextMissing.setVisible(visible[0] && srcFile == null);
			this.encode.videoStatusTextNotEncoded.setVisible(visible[0] && srcFile != null);
			this.encode.videoStatusTextGood.setVisible(visible[1] && okaySize);
			this.encode.videoStatusTextBadSize.setVisible(visible[1] && !okaySize);
			this.encode.videoProgressBar.setVisible(visible[2]);
		}
		if (isEncoding) {
			String str = String.format("%.1f%%", encodingPercent * 100.0);

			this.videoTempEncodeProgressBar.setPosition(encodingPercent);
			this.videoTempEncodeProgressBar.setText(str);

			this.encode.videoProgressBar.setPosition(encodingPercent);
			this.encode.videoProgressBar.setText(str);
		}
		this.updateEncodeEncodingStatus();
	}
	private final void updateVideoTempFilesizeDisplay() {
		if (this.videncode.getVideoFileTemp() != null) {
			long maxVideoSpace = this.videncode.getOutputMaxFileSize() -
				this.videncode.getOutputMetadataLength() -
				this.videncode.getImageFileTempFileSize() -
				this.videncode.getAudioFileTempFileSize();
				this.videncode.getVideoFileTempFileSize();

			this.videoTempFileSizeLabel.setText(
				Videncode.numberToLabeledSize(this.videncode.getVideoFileTempFileSize(), 5, 1, 1000, 1024, new String[]{" byte", " bytes", " KB", " MB"})
			);
			this.videoTempFileSizeLabel.setFont(maxVideoSpace >= 0 ? this.fonts.textBold : this.fonts.textBoldStrikethru);
		}
		else {
			this.videoTempFileSizeLabel.setText("Not encoded");
		}
	}
	private final void updateVideoEncodeButtonAndBitrate() {
		String text;
		String bitrateText;
		File file, src;
		long size;
		long maxSize = this.videncode.getOutputMaxFileSize() - this.videncode.getOutputMetadataLength();
		boolean enabled = false;

		file = this.videncode.getImageFileTemp();
		if (file == null) {
			text = "No image";
			bitrateText = "No image";
		}
		else {
			size = this.videncode.getImageFileTempFileSize();
			maxSize -= size;
			if (maxSize < 0) {
				text = "No space";
				bitrateText = "Image too large";
			}
			else {
				src = this.videncode.getAudioFileSource();
				file = this.videncode.getAudioFileTemp();
				if (file == null && src != null) {
					text = "No sound";
					bitrateText = "Sound not encoded";
				}
				else {
					size = this.videncode.getAudioFileTempFileSize();
					maxSize -= size;
					if (maxSize < 0) {
						text = "No space";
						bitrateText = "Image+audio too large";
					}
					else {
						double[] encodeDuration = this.videncode.getVideoFileSourceEncodeDuration();
						double duration = encodeDuration[1] - encodeDuration[0];
						if (duration <= 0.0) {
							text = "No video";
							bitrateText = "No video";
						}
						else {
							// Okay
							enabled = true;
							text = "Not encoded";
							bitrateText = Videncode.numberToLabeledSize(maxSize * 8 / duration, 5, 1, 1000, 1000, new String[]{" bps", " bps", " kbps", " mbps"});
						}
					}
				}
			}
		}

		this.videoTempEncodeActivateButton.setText(text);
		this.videoTempEncodeActivateButton.setEnabled(enabled);
		this.videoBitrateDisplay.setText(bitrateText);
	}

	private final void autoSelectVideoQuality(boolean updateComboBoxes) {
		// Get auto quality
		if (this.videoEncodeAutoQualitySelect == null || this.encodingAutoQualityProfile == null) return;
		if (this.videoEncodeAutoQualitySelect.isSelected()) {
			double[] encodeDuration = this.videncode.getVideoFileSourceEncodeDuration();
			Dimension vidSize = this.videncode.getVideoFileSourceDimensions();
			Videncode.FrameRate framerate = this.videncode.getVideoFileTempFrameRate();
			long maxVideoSpace = this.videncode.getOutputMaxFileSize() - this.videncode.getOutputMetadataLength() - this.videncode.getImageFileTempFileSize() - this.videncode.getAudioFileTempFileSize();

			Videncode.AutoQuality quality = this.videncode.getAutoQuality(this.encodingAutoQualityProfile, vidSize, framerate.getFrameRate(), encodeDuration[1] - encodeDuration[0], maxVideoSpace);

			// Set (if larger)
			Dimension tempVidSize = this.videncode.getVideoFileTempDimensions();
		//	if (quality.resolution.width * quality.resolution.height < tempVidSize.width * tempVidSize.height) {
			this.videncode.setVideoFileTempDimensions(quality.resolution);
		//	}

			// Update
			this.updateVideoEncodeButtonAndBitrate();
			if (updateComboBoxes) {
				this.updateVideoBaselineResolutionComboBox();
				this.updateVideoBaselineFrameRateComboBoxSelection();
			}
		}
	}

	private final void loadVideoFromFile() {
		final GUI self = this;

		// Create the image selector
		JFileChooser fc = new JFileChooser(this.videoLastSearchDir);
		fc.setAcceptAllFileFilterUsed(false);
		if (this.videoExtensions != null) {
			fc.addChoosableFileFilter(new FileFilter(){
				@Override
				public final boolean accept(File file) {
					if (file.isDirectory()) return true;

					int i = file.getName().lastIndexOf('.');
					String ext = (i > 0 ? file.getName().substring(i).toLowerCase() : "");

					for (i = 0; i < self.videoExtensions.length; ++i) {
						if (ext.equals(self.videoExtensions[i])) return true;
					}
					return false;
				}

				@Override
				public final String getDescription() {
					return "Video files";
				}
			});
		}
		else {
			fc.addChoosableFileFilter(new FileFilter(){
				@Override
				public final boolean accept(File file) {
					return true;
				}

				@Override
				public final String getDescription() {
					return "Video files";
				}
			});
		}
		int ret = fc.showOpenDialog(this.getRootPane());

		if (ret == JFileChooser.APPROVE_OPTION) {
			// File chosen
			File f = fc.getSelectedFile();
			this.videoLastSearchDir = f.getParentFile();
			if (this.videoLastSearchDir == null) this.videoLastSearchDir = new File("");

			// Load the file data
			this.videncode.setVideoFileSource(f, true, new Runnable() {
				@Override
				public void run() {
					File file = self.videncode.getVideoFileSource();
					self.videncode.setImageFileSource(file, false);
					self.videncode.setAudioFileSource(file, false);
				}
			});
		}
	}

	private final void onVideoEncodeDurationUpdate(JRangeSlider.ChangeEvent event) {
		if ((event.getChangeFlags() & (JRangeSlider.ChangeEvent.LOW_CHANGED | JRangeSlider.ChangeEvent.HIGH_CHANGED)) != 0) {
			this.updateVideoEncodingDurationDisplay(event.getSlider().getLow(), event.getSlider().getHigh());
			if ((event.getChangeFlags() & JRangeSlider.ChangeEvent.DRAGGING) == 0) {
				// Set
				this.videncode.setVideoFileSourceEncodeDuration(event.getSlider().getLow(), event.getSlider().getHigh());

				// Updates
				this.updateAudioIfLockedToVideo(true);
				this.autoSelectVideoQuality(true);
				this.updateSyncStatus();
			}
			else {
				this.updateAudioIfLockedToVideo(false);
			}
		}
		else if ((event.getChangeFlags() & JRangeSlider.ChangeEvent.CLICKED) != 0) {
			if ((event.getChangeFlags() & JRangeSlider.ChangeEvent.CLICKED_LOW) != 0) {
				this.updateVideoPreviewIndex(0, true);
			}
			else if ((event.getChangeFlags() & JRangeSlider.ChangeEvent.CLICKED_HIGH) != 0) {
				this.updateVideoPreviewIndex(1, true);
			}
		}
	}
	private final void onVideoEncodeDurationTextModify(int id, JTextFieldCustom obj) {
		double[] value = new double[2];
		value[0] = this.videoEncodingDurationRangeslider.getLow();
		value[1] = this.videoEncodingDurationRangeslider.getHigh();

		this.videoEncodingDurationRangeslider.setValue(id, Videncode.parseTimecodeToSeconds(obj.getText(), value[id]));

		value[id] = this.videoEncodingDurationRangeslider.getValue(id);

		obj.setText(Videncode.timeToString(value[id], new int[]{0,2,2,2}));

		this.updateVideoPreviewIndex(id, true);

		// Set
		this.videncode.setVideoFileSourceEncodeDuration(value[0], value[1]);

		// Updates
		this.updateAudioIfLockedToVideo(true);
		this.autoSelectVideoQuality(true);
		this.updateSyncStatus();
	}
	private final void onVideoBaselineResolutionChange(final ImageScale s) {
		if (s == null || this.videoBaselineResolutionComboBoxModifying) return;

		// Change
		this.videncode.setVideoFileTempDimensions(s.getSize());

		// Updates
		this.autoSelectVideoQuality(true);
	}
	private final void onVideoBaselineFrameRateChange(final Videncode.FrameRate f) {
		if (f == null || this.videoBaselineFrameRateComboBoxModifying) return;

		// Change
		this.videncode.setVideoFileTempFrameRate(f);

		// Updates
		this.autoSelectVideoQuality(true);
	}
	private final void onVideoAutoQualityProfileChange(final Videncode.AutoQualityProfile p) {
		if (p == null || this.videoAutoQualityProfileComboBoxModifying) return;

		// Change
		this.encodingAutoQualityProfile = p;

		// Updates
		this.autoSelectVideoQuality(true);
	}
	private final void onVideoAutoQualityCheckboxChange(final JCheckBox b) {
		if (b == null) return;

		this.videoAutoQualityProfileComboBox.setEnabled(b.isSelected());

		if (b.isSelected()) {
			this.autoSelectVideoQuality(true);
		}
	}
	private final void onVideoSourceCheckboxChange(JCheckBox checkbox, ActionEvent event) {
		if (checkbox.isSelected()) {
			checkbox.setSelected(false);
		}
		else {
			if (this.videncode.isImageFromVideo()) {
				this.videncode.setImageFileSource(null, false);
			}
			if (this.videncode.isAudioFromVideo()) {
				this.videncode.setAudioFileSource(null, false);
			}
			this.videncode.setVideoFileSource(null, false);
		}
	}
	private final void onVideoEncodeButtonPress() {
		this.videncode.encodeVideoTempFile();
		this.updateVideoEncodingStatus(true);
	}
	private final void onVideoEncodingModeChange(int mode) {
		this.videncode.setVideoFileTempQuality(mode);
	}

	// Image actions
	private final void updateImageExistence() {
		boolean exists = (this.videncode.getImageFileSource() != null);

		this.imageSourceCheckbox.setEnabled(exists);
		this.imageSourceCheckbox.setSelected(exists);

		this.imageQualityComboBox.setEnabled(exists);
		this.imageScaleComboBox.setEnabled(exists && ((ImageQuality) this.imageQualityComboBox.getSelectedItem()).getQuality() != -1);

		this.imagePreviewTimeSlider.setEnabled(exists);
		this.imagePreviewTimecodeLocation.setEnabled(exists);

		if (exists) {
			this.imagePreviewSourceGroup.setSelected(this.imagePreviewSourceButtons[this.videncode.isImageFromVideo() ? 0 : 1].getModel(), true);
		}
		else {
			this.imagePreviewSourceGroup.clearSelection();
		}
	}
	private final void updateImageFile() {
		StringBuilder n = new StringBuilder();
		boolean isVideo = true;
		if (this.videncode.getImageFileSource() != null) {
			n.append(this.videncode.getImageFileSource().getName());

			isVideo = this.videncode.isImageFromVideo();
			if (isVideo) {
				n.append(" @ ");
				n.append(Videncode.timeToString(this.videncode.getImageFileSourceVideoTime(), new int[]{-1,-1,0,0}, new String[]{"h","m","s"}));
			}
		}
		if (this.imagePreviewTimeSliderContainer != null) this.imagePreviewTimeSliderContainer.setVisible(isVideo);

		this.fileLabelImage.setText((n.length() == 0) ? " " : n.toString());
	}
	private final void updateImageStatistics() {
		// Dimensions
		Dimension d = this.videncode.getImageFileSourceDimensions();

		StringBuilder sb = new StringBuilder();
		sb.append(d.width);
		sb.append('x');
		sb.append(d.height);
		this.imagePreviewDimensionsDisplay.setText(sb.toString());
	}
	private final void updateTempImageStatistics() {
		// File size
		long size = this.videncode.getImageFileTempFileSize();

		long maxSpace = this.videncode.getOutputMaxFileSize() -
			this.videncode.getOutputMetadataLength() - size;

		this.imagePreviewFileSizeDisplay.setText(
			Videncode.numberToLabeledSize(size, 5, 1, 1000, 1024, new String[]{" byte", " bytes", " KB", " MB"})
		);
		this.imagePreviewFileSizeTrueDisplay.setText(
			Videncode.numberToLabeledSize(size, 5, 1, 1000, 1024, new String[]{" byte", " bytes"})
		);
		this.imagePreviewFileSizeDisplay.setFont(maxSpace >= 0 ? this.fonts.textBold : this.fonts.textBoldStrikethru);
		this.imagePreviewFileSizeTrueDisplay.setFont(maxSpace >= 0 ? this.fonts.textBold : this.fonts.textBoldStrikethru);
	}
	private final void updateImageTimeDisplay() {
		this.updateImageTimeDisplay(this.videncode.getImageFileSourceVideoTime());
	}
	private final void updateImageTimeDisplay(double time) {
		boolean b = this.imagePreviewTimecodeLocationModifying;
		this.imagePreviewTimecodeLocationModifying = true;

		this.imagePreviewTimecodeLocation.setText(Videncode.timeToString(time, new int[]{0,2,2,2}));

		this.imagePreviewTimecodeLocationModifying = b;
	}
	private final void updateImageScaleComboBox() {
		boolean b = this.imageScaleComboBoxModifying;
		this.imageScaleComboBoxModifying = true;

		int[] resolutions = this.videncode.getResolutions();

		int istart = resolutions.length - 1;
		int sel = -1, selPos = 0;
		StringBuilder sb = new StringBuilder();

		Dimension[] sizes = new Dimension[4];
		boolean[] needed = new boolean[]{ true , true , true , true };
		String[] keys = new String[]{ "Source video" , "Source image" , "Output video" , null };
		sizes[0] = this.videncode.getVideoFileSourceDimensions();
		sizes[1] = this.videncode.getImageFileSourceDimensions();
		sizes[2] = this.videncode.getVideoFileTempDimensions();
		sizes[3] = this.videncode.getImageFileTempDimensions();
		Dimension match = sizes[3];

		for (int i = 0; i < needed.length; ++i) {
			if (sizes[i].width <= 0 && sizes[i].height <= 0) {
				needed[i] = false;
				continue;
			}
			for (int j = i + 1; j < needed.length; ++j) {
				if (sizes[i].width == sizes[j].width && sizes[i].height == sizes[j].height) {
					needed[j] = false;
				}
			}
		}

		this.imageScaleComboBox.removeAllItems();

		// Check for larger
		for (int j = 0; j < needed.length; ++j) {
			if (needed[j] && sizes[j].height > resolutions[istart]) {
				// Resolution
				sb.setLength(0);
				if (sizes[0].height != 0 && sizes[0].width * sizes[j].height / sizes[0].height == sizes[j].width) {
					// Same aspect ratio as video
					sb.append(sizes[j].height);
					sb.append('p');
				}
				else {
					sb.append(sizes[j].width);
					sb.append('x');
					sb.append(sizes[j].height);
				}
				if (keys[j] != null) {
					sb.append(" / ");
					sb.append(keys[j]);
				}
				this.imageScaleComboBox.addItem(new ImageScale(sizes[j].height, sb.toString()));
				if (match.height == sizes[j].height) sel = selPos;
				++selPos;

				needed[j] = false;
			}
		}

		for (int i = istart; i >= 0; --i) {
			// Resolution
			sb.setLength(0);
			sb.append(resolutions[i]);
			sb.append('p');
			for (int j = 0; j < needed.length; ++j) { // Search for any matches
				if (needed[j] && sizes[j].height == resolutions[i] && sizes[0].height != 0 && sizes[0].width * sizes[j].height / sizes[0].height == sizes[j].width) {
					if (keys[j] != null) {
						sb.append(" / ");
						sb.append(keys[j]);
					}
					needed[j] = false;
					break;
				}
			}
			this.imageScaleComboBox.addItem(new ImageScale(resolutions[i], sb.toString()));
			if (match.height == resolutions[i]) sel = selPos;
			++selPos;

			// Check for smaller
			for (int j = 0; j < needed.length; ++j) {
				if (needed[j] && sizes[j].height <= resolutions[i] && (i == 0 || sizes[j].height > resolutions[i - 1])) {
					// Resolution
					sb.setLength(0);
					if (sizes[0].height != 0 && sizes[0].width * sizes[j].height / sizes[0].height == sizes[j].width) {
						// Same aspect ratio as video
						sb.append(sizes[j].height);
						sb.append('p');
					}
					else {
						sb.append(sizes[j].width);
						sb.append('x');
						sb.append(sizes[j].height);
					}
					if (keys[j] != null) {
						sb.append(" / ");
						sb.append(keys[j]);
					}
					this.imageScaleComboBox.addItem(new ImageScale(sizes[j].height, sb.toString()));
					if (match.height == sizes[j].height) sel = selPos;
					++selPos;

					needed[j] = false;
				}
			}
		}

		// Select
		if (this.videncode.getImageFileSource() == null) {
			this.imageScaleComboBox.setSelectedItem(new ImageScale("No source"));
		}
		else {
			if (sel >= 0) {
				this.imageScaleComboBox.setSelectedIndex(sel);
			}
			else {
				this.imageScaleComboBox.setSelectedItem(new ImageScale(match.width + "x" + match.height));
			}
		}

		this.imageScaleComboBoxModifying = b;
	}
	private final void updateImageQualityComboBox() {
		boolean b = this.imageQualityComboBoxModifying;
		this.imageQualityComboBoxModifying = true;

		this.imageQualityComboBox.removeAllItems();
		int istart = (this.videncode.getImageFileSource() != this.videncode.getVideoFileSource() ? -1 : this.videncode.getImageQualityRange()[0]);
		for (
			int i = istart;
			i <= this.videncode.getImageQualityRange()[1];
			++i
		) {
			this.imageQualityComboBox.addItem(
				new ImageQuality(i, (i == -1 ? "Original" : (i == 0 ? "Lossless" :
					(i) + (i == 1 ? " (Best)" : (i == this.videncode.getImageQualityRange()[1] ? " (Worst)" : ""))
				)))
			);
		}
		this.imageQualityComboBox.setSelectedIndex(Math.max(0, this.videncode.getImageFileTempQuality() - istart));

		this.imageQualityComboBoxModifying = b;
	}
	private final void updateImageCustomSize(Image img) {
		if (img != null && this.videncode.getImageFileTempQuality() == -1) {
			this.updateImageScaleComboBox();
		}
	}
	private final void updateImageSpaceStatistics() {
		// Available space for video/audio
		long d = this.videncode.getOutputMaxFileSize() - this.videncode.getOutputMetadataLength() - this.videncode.getImageFileTempFileSize();
		if (d < 0) d = 0;
		this.imagePreviewAvailableSpaceDisplay.setText(
			Videncode.numberToLabeledSize(d, 5, 1, 1000, 1024, new String[]{" byte", " bytes", " KB", " MB"})
		);
		this.imagePreviewAvailableSpaceTrueDisplay.setText(
			Videncode.numberToLabeledSize(d, 5, 1, 1000, 1024, new String[]{" byte", " bytes"})
		);
	}
	private final void updateImageEncodingStatus() {
		boolean[] visible = new boolean[]{ false , false , false , false };
		File srcFile = this.videncode.getImageFileSource();
		if (srcFile == null) {
			visible[1] = true;
		}
		else {
			File tempFile = this.videncode.getImageFileTemp();
			if (tempFile == null) {
				visible[2] = true;
			}
			else {
				long tempFileSize = this.videncode.getImageFileTempFileSize();
				long maxFileSize = this.videncode.getOutputMaxFileSize() - this.videncode.getOutputMetadataLength();
				boolean okaySize = (tempFileSize < maxFileSize);
				if (okaySize) {
					visible[0] = true;
				}
				else {
					visible[3] = true;
				}
			}
		}


		this.encode.imageStatusTextGood.setVisible(visible[0]);
		this.encode.imageStatusTextMissing.setVisible(visible[1]);
		this.encode.imageStatusTextNotEncoded.setVisible(visible[2]);
		this.encode.imageStatusTextBadSize.setVisible(visible[3]);
	}

	private final void setImageSourceState(boolean external, boolean openSearch) {
		final GUI self = this;

		int i = external ? 1 : 0;
		if (!this.imagePreviewSourceButtons[i].isSelected()) {
			this.imagePreviewSourceGroup.setSelected(this.imagePreviewSourceButtons[i].getModel(), true);
		}

		if (external) {
			if (openSearch) {
				// Create the image selector
				JFileChooser fc = new JFileChooser(this.imagePreviewLastSearchDir);
				fc.setAcceptAllFileFilterUsed(false);
				if (this.imageExtensions != null) {
					fc.addChoosableFileFilter(new FileFilter(){
						@Override
						public final boolean accept(File file) {
							if (file.isDirectory()) return true;

							int i = file.getName().lastIndexOf('.');
							String ext = (i > 0 ? file.getName().substring(i).toLowerCase() : "");

							for (i = 0; i < self.imageExtensions.length; ++i) {
								if (ext.equals(self.imageExtensions[i])) return true;
							}
							return false;
						}

						@Override
						public final String getDescription() {
							return "Images files";
						}
					});
				}
				else {
					fc.addChoosableFileFilter(new FileFilter(){
						@Override
						public final boolean accept(File file) {
							return true;
						}

						@Override
						public final String getDescription() {
							return "Images files";
						}
					});
				}
				int ret = fc.showOpenDialog(this.getRootPane());

				if (ret == JFileChooser.APPROVE_OPTION) {
					// File chosen
					final File f = fc.getSelectedFile();
					this.imagePreviewLastSearchDir = f.getParentFile();
					if (this.imagePreviewLastSearchDir == null) this.imagePreviewLastSearchDir = new File("");

					// Change the image
					this.videncode.setImageFileSource(f, false);
				}
				else {
					// Switch back if no update
					if (this.videncode.isImageFromVideo()) {
						this.setImageSourceState(false, false);
					}
				}
			}
		}
		else {
			File f = this.videncode.getVideoFileSource();
			if (f == null) {
				this.imagePreviewSourceGroup.clearSelection();
			}
			this.videncode.setImageFileSource(f, false);
		}
	}

	private final void onImageTimeUpdate(JBarSlider.ChangeEvent event) {
		if ((event.getChangeFlags() & JBarSlider.ChangeEvent.CHANGED) != 0) {
			this.updateImageTimeDisplay(event.getSlider().getPosition());
			if ((event.getChangeFlags() & (JBarSlider.ChangeEvent.DRAGGING | JBarSlider.ChangeEvent.JUMPED)) == 0) {
				// Update frames
				this.videncode.setImageFileSourceVideoTime(event.getSlider().getPosition());
			}
		}
	}
	private final void onImageTimeTextModify(JTextFieldCustom obj) {
		if (this.imagePreviewTimecodeLocationModifying) return;

		double value = this.imagePreviewTimeSlider.getPosition();
		this.imagePreviewTimeSlider.setPosition(Videncode.parseTimecodeToSeconds(obj.getText(), value));
		value = this.imagePreviewTimeSlider.getPosition();

		obj.setText(Videncode.timeToString(value, new int[]{0,2,2,2}));

		// Set
		this.videncode.setImageFileSourceVideoTime(value);
	}
	private final void onImageQualityChange(final ImageQuality q) {
		if (q == null || this.imageQualityComboBoxModifying) return;

		this.imageScaleComboBox.setEnabled(q.getQuality() != -1 && this.videncode.getImageFileSource() != null);

		this.videncode.setImageFileTempQuality(q.getQuality());
	}
	private final void onImageScaleChange(final ImageScale s) {
		if (s == null || this.imageScaleComboBoxModifying) return;

		this.videncode.setImageFileTempDimensions(s.getSize());
	}
	private final void onImageSourceCheckboxChange(JCheckBox checkbox, ActionEvent event) {
		if (checkbox.isSelected()) {
			checkbox.setSelected(false);
		}
		else {
			this.videncode.setImageFileSource(null, false);
		}
	}

	// Audio actions
	private final void updateAudioExistence() {
		boolean exists = (this.videncode.getAudioFileSource() != null);

		this.audioSourceCheckbox.setEnabled(exists);
		this.audioSourceCheckbox.setSelected(exists);

		this.audioBitrateSelectionComboBox.setEnabled(exists);
		this.audioChannelSelectionComboBox.setEnabled(exists);
		this.audioSampleRateSelectionComboBox.setEnabled(exists);

		this.audioEncodingDurationRangeslider.setEnabled(exists);
		for (int i = 0; i < this.audioEncodingDurationTimecode.length; ++i) {
			this.audioEncodingDurationTimecode[i].setEnabled(exists);
		}

		this.audioTempEncodeActivateButton.setEnabled(exists);

		if (exists) {
			this.audioSourceButtonGroup.setSelected(this.audioSourceButtons[this.videncode.isAudioFromVideo() ? 0 : 1].getModel(), true);
		}
		else {
			this.audioSourceButtonGroup.clearSelection();
		}
	}
	private final void updateAudioFile() {
		StringBuilder n = new StringBuilder();
		boolean isVideo = true;
		if (this.videncode.getAudioFileSource() != null) {
			n.append(this.videncode.getAudioFileSource().getName());

			isVideo = this.videncode.isAudioFromVideo();
			if (isVideo) {
				double[] t = this.videncode.getAudioFileSourceEncodeDuration();
				n.append(" @ ");
				n.append(Videncode.timeToString(t[0], new int[]{-1,-1,0,0}, new String[]{"h","m","s"}));
				n.append(" - ");
				n.append(Videncode.timeToString(t[1], new int[]{-1,-1,0,0}, new String[]{"h","m","s"}));
			}
		}

		this.fileLabelAudio.setText((n.length() == 0) ? " " : n.toString());

		double duration = this.videncode.getAudioFileSourceDuration();
		this.audioTimecodeEnd.setText(Videncode.timeToString(duration, new int[]{0,2,2,2}));

		this.updateAudioEncodingDurationSliderUsability();

		this.updateAudioEncodingStatus(true);
	}
	private final void updateAudioEncodingDurationSlider() {
		double duration = this.videncode.getAudioFileSourceDuration();
		double[] encodeDuration = this.videncode.getAudioFileSourceEncodeDuration();

		this.updateAudioEncodingDurationSlider(duration, encodeDuration[0], encodeDuration[1]);
	}
	private final void updateAudioEncodingDurationSliderUsability() {
		this.audioEncodingDurationRangeslider.setUsable(
			!this.videncode.isAudioFromVideo()
		);
	}
	private final void updateAudioEncodingDurationSlider(double max, double low, double high) {
		this.audioEncodingDurationRangeslider.setRange(0.0, max, low, high);
	}
	private final void updateAudioEncodingDurationDisplay() {
		double[] ed = this.videncode.getAudioFileSourceEncodeDuration();

		this.updateAudioEncodingDurationDisplay(ed[0], ed[1]);
	}
	private final void updateAudioEncodingDurationDisplay(double start, double finish) {
		this.audioEncodingDurationTimecode[0].setText(Videncode.timeToString(start, new int[]{0,2,2,2}));
		this.audioEncodingDurationTimecode[1].setText(Videncode.timeToString(finish, new int[]{0,2,2,2}));
	}
	private final void updateAudioBitrateSelectionComboBoxSelection() {
		boolean b = this.audioBitrateSelectionComboBoxModifying;
		this.audioBitrateSelectionComboBoxModifying = true;

		Videncode.Bitrate br = this.videncode.getAudioFileTempBitrate();

		int sel = -1;
		for (int i = 0; i < this.audioBitrateSelectionComboBox.getItemCount(); ++i) {
			if ((this.audioBitrateSelectionComboBox.getItemAt(i)).equals(br)) {
				sel = i;
				break;
			}
		}

		if (sel >= 0) {
			this.audioBitrateSelectionComboBox.setSelectedIndex(sel);
		}

		this.audioBitrateSelectionComboBoxModifying = b;
	}
	private final void updateAudioChannelSelectionComboBoxSelection() {
		boolean b = this.audioChannelSelectionComboBoxModifying;
		this.audioChannelSelectionComboBoxModifying = true;

		Videncode.AudioChannelCount acc = this.videncode.getAudioFileTempChannels();

		int sel = -1;
		for (int i = 0; i < this.audioChannelSelectionComboBox.getItemCount(); ++i) {
			if ((this.audioChannelSelectionComboBox.getItemAt(i)).equals(acc)) {
				sel = i;
				break;
			}
		}

		if (sel >= 0) {
			this.audioChannelSelectionComboBox.setSelectedIndex(sel);
		}

		this.audioChannelSelectionComboBoxModifying = b;
	}
	private final void updateAudioSampleRateSelectionComboBoxSelection() {
		boolean b = this.audioSampleRateSelectionComboBoxModifying;
		this.audioSampleRateSelectionComboBoxModifying = true;

		Videncode.AudioSampleRate rate = this.videncode.getAudioFileTempSampleRate();

		int sel = -1;
		for (int i = 0; i < this.audioSampleRateSelectionComboBox.getItemCount(); ++i) {
			if ((this.audioSampleRateSelectionComboBox.getItemAt(i)).equals(rate)) {
				sel = i;
				break;
			}
		}

		if (sel >= 0) {
			this.audioSampleRateSelectionComboBox.setSelectedIndex(sel);
		}

		this.audioSampleRateSelectionComboBoxModifying = b;
	}
	private final void updateAudioIfLockedToVideo(boolean updateVidencode) {
		if (!this.isAudioLockedToVideo()) return;

		double duration = this.videoEncodingDurationRangeslider.getMaximum();
		double[] encodeDuration = new double[]{
			this.videoEncodingDurationRangeslider.getLow(),
			this.videoEncodingDurationRangeslider.getHigh()
		};

		this.updateAudioEncodingDurationDisplay(encodeDuration[0], encodeDuration[1]);
		this.updateAudioEncodingDurationSlider(duration, encodeDuration[0], encodeDuration[1]);

		if (updateVidencode) {
			this.videncode.setAudioFileSourceEncodeDuration(encodeDuration[0], encodeDuration[1]);
			this.updateAudioTempFilesizeDisplay();
		}

		this.updateAudioFile();
	}
	private final void updateAudioTempFilesizeDisplay() {
		File f = this.videncode.getAudioFileTemp();

		long size = 0;
		long maxSize = this.videncode.getOutputMaxFileSize() - this.videncode.getOutputMetadataLength();
		if (f != null) {
			size = this.videncode.getAudioFileTempFileSize();
		}
		else {
			size = this.videncode.getAudioFileTempApproximateSize();
		}

		this.audioTempFilesizeLabel.setText("File size");
		this.audioTempFilesize.setText(
			(f != null || size == 0 ? "" : "~") +
			Videncode.numberToLabeledSize(size, 5, 1, 1000, 1024, new String[]{" byte", " bytes", " KB", " MB"})
		);
		this.audioTempFilesize.setFont(f == null || size < maxSize ? this.fonts.textBold : this.fonts.textBoldStrikethru);
	}
	private final void updateAudioEncodingStatus(boolean updateVisibility) {
		this.updateAudioEncodingStatus(
			updateVisibility,
			this.videncode.isAudioFileTempEncoding(),
			this.videncode.getAudioFileTempEncodingPercent(),
			this.videncode.getAudioFileTemp()
		);
	}
	private final void updateAudioEncodingStatus(boolean updateVisibility, boolean isEncoding, double encodingPercent, File tempFile) {
		if (updateVisibility) {
			boolean[] visible = new boolean[]{ false , false , false };
			if (isEncoding) {
				visible[2] = true;
			}
			else if (tempFile != null) {
				visible[1] = true;
			}
			else {
				visible[0] = true;
			}
			this.audioTempEncodeActivateButton.setVisible(visible[0]);
			this.audioTempEncodeStatusText.setVisible(visible[1]);
			this.audioTempEncodeProgressBar.setVisible(visible[2]);


			File srcFile = this.videncode.getAudioFileSource();
			long tempFileSize = this.videncode.getAudioFileTempFileSize();
			long maxFileSize = this.videncode.getOutputMaxFileSize() - this.videncode.getOutputMetadataLength();
			boolean okaySize = (srcFile != null && tempFileSize < maxFileSize);

			this.encode.audioStatusTextMissing.setVisible(visible[0] && srcFile == null);
			this.encode.audioStatusTextNotEncoded.setVisible(visible[0] && srcFile != null);
			this.encode.audioStatusTextGood.setVisible(visible[1] && okaySize);
			this.encode.audioStatusTextBadSize.setVisible(visible[1] && !okaySize);
			this.encode.audioProgressBar.setVisible(visible[2]);
		}
		if (isEncoding) {
			String str = String.format("%.1f%%", encodingPercent * 100.0);

			this.audioTempEncodeProgressBar.setPosition(encodingPercent);
			this.audioTempEncodeProgressBar.setText(str);

			this.encode.audioProgressBar.setPosition(encodingPercent);
			this.encode.audioProgressBar.setText(str);
		}
		this.updateEncodeEncodingStatus();
	}

	private final boolean isAudioLockedToVideo() {
		return this.audioSourceButtons[0].isSelected();
	}

	private final void setAudioSourceState(boolean external, boolean openSearch) {
		final GUI self = this;

		int i = external ? 1 : 0;
		if (!this.audioSourceButtons[i].isSelected()) {
			this.audioSourceButtonGroup.setSelected(this.audioSourceButtons[i].getModel(), true);
		}

		if (external) {
			if (openSearch) {
				// Create the image selector
				JFileChooser fc = new JFileChooser(this.audioLastSearchDir);
				fc.setAcceptAllFileFilterUsed(false);
				if (this.audioExtensions != null) {
					fc.addChoosableFileFilter(new FileFilter(){
						@Override
						public final boolean accept(File file) {
							if (file.isDirectory()) return true;

							int i = file.getName().lastIndexOf('.');
							String ext = (i > 0 ? file.getName().substring(i).toLowerCase() : "");

							for (i = 0; i < self.audioExtensions.length; ++i) {
								if (ext.equals(self.audioExtensions[i])) return true;
							}
							return false;
						}

						@Override
						public final String getDescription() {
							return "Audio files";
						}
					});
				}
				else {
					fc.addChoosableFileFilter(new FileFilter(){
						@Override
						public final boolean accept(File file) {
							return true;
						}

						@Override
						public final String getDescription() {
							return "Audio files";
						}
					});
				}
				int ret = fc.showOpenDialog(this.getRootPane());

				if (ret == JFileChooser.APPROVE_OPTION) {
					// File chosen
					final File f = fc.getSelectedFile();
					this.audioLastSearchDir = f.getParentFile();
					if (this.audioLastSearchDir == null) this.audioLastSearchDir = new File("");

					// Change the image
					this.videncode.setAudioFileSource(f, false);
				}
				else {
					// Switch back if no update
					if (this.videncode.isAudioFromVideo()) {
						this.setAudioSourceState(false, false);
					}
				}
			}
		}
		else {
			File f = this.videncode.getVideoFileSource();
			if (f == null) {
				this.audioSourceButtonGroup.clearSelection();
			}
			this.videncode.setAudioFileSource(f, false);
		}
	}

	private final void onAudioEncodeDurationUpdate(JRangeSlider.ChangeEvent event) {
		if ((event.getChangeFlags() & (JRangeSlider.ChangeEvent.LOW_CHANGED | JRangeSlider.ChangeEvent.HIGH_CHANGED)) != 0) {
			this.updateAudioEncodingDurationDisplay(event.getSlider().getLow(), event.getSlider().getHigh());
			if ((event.getChangeFlags() & JRangeSlider.ChangeEvent.DRAGGING) == 0) {
				if (this.isAudioLockedToVideo()) {
					this.updateAudioIfLockedToVideo(false);
				}
				else {
					// Set
					this.videncode.setAudioFileSourceEncodeDuration(event.getSlider().getLow(), event.getSlider().getHigh());

					// Updates
					this.autoSelectVideoQuality(true);
					this.updateSyncStatus();
				}
			}
		}
	}
	private final void onAudioEncodeDurationTextModify(int id, JTextFieldCustom obj) {
		if (this.isAudioLockedToVideo()) {
			this.updateAudioIfLockedToVideo(false);
		}
		else {
			double[] value = new double[2];
			value[0] = this.audioEncodingDurationRangeslider.getLow();
			value[1] = this.audioEncodingDurationRangeslider.getHigh();

			this.audioEncodingDurationRangeslider.setValue(id, Videncode.parseTimecodeToSeconds(obj.getText(), value[id]));

			value[id] = this.audioEncodingDurationRangeslider.getValue(id);

			obj.setText(Videncode.timeToString(value[id], new int[]{0,2,2,2}));

			// Set
			this.videncode.setAudioFileSourceEncodeDuration(value[0], value[1]);

			// Updates
			this.autoSelectVideoQuality(true);
			this.updateSyncStatus();
		}
	}
	private final void onAudioBitrateChange(final Videncode.Bitrate b) {
		if (b == null || this.audioBitrateSelectionComboBoxModifying) return;

		this.videncode.setAudioFileTempBitrate(b);

		this.updateAudioTempFilesizeDisplay();
	}
	private final void onAudioChannelCountChange(final Videncode.AudioChannelCount acc) {
		if (acc == null || this.audioChannelSelectionComboBoxModifying) return;

		this.videncode.setAudioFileTempChannels(acc);
	}
	private final void onAudioSourceCheckboxChange(JCheckBox checkbox, ActionEvent event) {
		if (checkbox.isSelected()) {
			checkbox.setSelected(false);
		}
		else {
			this.videncode.setAudioFileSource(null, false);
		}
	}
	private final void onAudioSampleRateChange(Videncode.AudioSampleRate rate) {
		if (rate == null || this.audioSampleRateSelectionComboBoxModifying) return;

		this.videncode.setAudioFileTempSampleRate(rate);
	}
	private final void onAudioEncodeButtonPress() {
		this.videncode.encodeAudioTempFile();
		this.updateAudioEncodingStatus(true);
	}
	private final void onAudioPreviewClipButtonPress(int id) {
		Sound[] sounds = this.videncode.getAudioPreviewTempSounds();
		for (int i = 0; i < sounds.length; ++i) {
			sounds[i].stop();
		}
		sounds[id].play();
	}

	// Sync
	private final void updateSyncStatus() {
		this.updateSyncStatus(false, 0.0);
	}
	private final void updateSyncStatus(boolean updateValues, double start) {
		File[] files = new File[]{ this.videncode.getVideoFileSource() , this.videncode.getAudioFileSource() };

		double[] durations = new double[]{ this.videncode.getVideoFileSourceDuration() , this.videncode.getAudioFileSourceDuration() };
		double[][] times = new double[][]{ this.videncode.getVideoFileSourceEncodeDuration() , this.videncode.getAudioFileSourceEncodeDuration() };
		double[] syncOffset = new double[]{ this.videncode.getVideoFileSourceSyncOffset() , this.videncode.getAudioFileSourceSyncOffset() };
		double[] encodeDurations = new double[]{ (times[0][1] - times[0][0]) , (times[1][1] - times[1][0]) };
		double[] baseOffset = new double[]{ 0.0 , 0.0 };
		int maxEncodeDurationId = (encodeDurations[0] > encodeDurations[1] ? 0 : 1);
		baseOffset[1 - maxEncodeDurationId] = times[maxEncodeDurationId][0];
		double maxDuration = durations[maxEncodeDurationId];

		if (updateValues) {
			syncOffset[1 - maxEncodeDurationId] = start - baseOffset[1 - maxEncodeDurationId];
			if (maxEncodeDurationId == 1) {
				this.videncode.setVideoFileSourceSyncOffset(syncOffset[1 - maxEncodeDurationId]);
			}
			else {
				this.videncode.setAudioFileSourceSyncOffset(syncOffset[1 - maxEncodeDurationId]);
			}
		}

		boolean needsSync = (files[0] != null && files[1] != null && !Videncode.withinThreshold(encodeDurations[0], encodeDurations[1]));

		for (int i = 0; i < this.sync.ranges.length; ++i) {
			this.sync.ranges[i].setEnabled(needsSync);
			this.sync.ranges[i].setUsable(maxEncodeDurationId == 1 - i);
			this.sync.ranges[i].setRange(0.0, maxDuration);
			this.sync.ranges[i].setSecondaryRange(maxEncodeDurationId == 1 - i, times[maxEncodeDurationId][0], times[maxEncodeDurationId][1]);
			if (maxEncodeDurationId == 1 - i) {
				this.sync.ranges[i].setLow(syncOffset[i] + baseOffset[i]);
				this.sync.ranges[i].setHigh(syncOffset[i] + baseOffset[i] + (times[i][1] - times[i][0]));
			}
			else {
				this.sync.ranges[i].setLow(times[i][0]);
				this.sync.ranges[i].setHigh(times[i][1]);
			}
		}

		for (int i = 0; i < this.sync.rangeEncodeTimecodes.length; ++i) {
			for (int j = 0; j < this.sync.rangeEncodeTimecodes[i].length; ++j) {
				this.sync.rangeEncodeTimecodes[i][j].setEditable(maxEncodeDurationId == 1 - i);
				this.sync.rangeEncodeTimecodes[i][j].setEnabled(needsSync);
				this.sync.rangeEncodeTimecodes[i][j].setText(Videncode.timeToString(this.sync.ranges[i].getValue(j), new int[]{0,2,2,2}));
			}
		}

		for (int i = 0; i < this.sync.rangeTimecodes.length; ++i) {
			this.sync.rangeTimecodes[i][1].setText(Videncode.timeToString(maxDuration, new int[]{0,2,2,2}));
		}

		this.sync.mainPanel.setVisible(needsSync);
		this.sync.disabledPanel.setVisible(!needsSync);


		this.sync.videoStateGroups[0].setSelected(this.sync.videoState[0][this.videncode.getSyncVideoState(true)].getModel(), true);
		this.sync.videoStateGroups[1].setSelected(this.sync.videoState[1][this.videncode.getSyncVideoState(false)].getModel(), true);
		this.sync.videoFadeTransition[0].setSelected(this.videncode.getSyncVideoUseFade(true));
		this.sync.videoFadeTransition[1].setSelected(this.videncode.getSyncVideoUseFade(false));
		this.sync.audioStateLoop[0].setSelected(this.videncode.getSyncAudioState(true) == Videncode.SYNC_LOOP);
		this.sync.audioStateLoop[1].setSelected(this.videncode.getSyncAudioState(false) == Videncode.SYNC_LOOP);
		this.sync.audioFadeTransition[0].setSelected(this.videncode.getSyncAudioUseFade(true));
		this.sync.audioFadeTransition[1].setSelected(this.videncode.getSyncAudioUseFade(false));

		this.sync.optionPanels[0].setVisible(maxEncodeDurationId == 0);
		this.sync.optionPanels[1].setVisible(maxEncodeDurationId == 1);
	}

	private final void onSyncEncodeDurationUpdate(int id, JRangeSlider.ChangeEvent event) {
		if ((event.getChangeFlags() & (JRangeSlider.ChangeEvent.LOW_CHANGED | JRangeSlider.ChangeEvent.HIGH_CHANGED)) != 0) {
			if ((event.getChangeFlags() & JRangeSlider.ChangeEvent.DRAGGING) == 0) {
				this.updateSyncStatus(true, event.getSlider().getLow());
			}
			else {
				for (int j = 0; j < this.sync.rangeEncodeTimecodes[id].length; ++j) {
					this.sync.rangeEncodeTimecodes[id][j].setText(Videncode.timeToString(event.getSlider().getValue(j), new int[]{0,2,2,2}));
				}
			}
		}
	}
	private final void onSyncEncodeDurationTextModify(int id, int pos, JTextFieldCustom obj) {
		double[][] encodeTimes = new double[][]{ this.videncode.getVideoFileSourceEncodeDuration() , this.videncode.getAudioFileSourceEncodeDuration() };
		double[] encodeDurations = new double[]{ (encodeTimes[0][1] - encodeTimes[0][0]) , (encodeTimes[1][1] - encodeTimes[1][0]) };
		if (Videncode.withinThreshold(encodeDurations[0], encodeDurations[1]) || encodeDurations[id] > encodeDurations[1 - id]) return;

		double time = Videncode.parseTimecodeToSeconds(obj.getText(), this.sync.ranges[id].getValue(pos));
		double maxTime = encodeTimes[1 - id][1] - encodeDurations[id];

		if (pos == 1) time -= encodeDurations[id];
		if (time < encodeTimes[1 - id][0]) time = encodeTimes[1 - id][0];
		if (time > maxTime) time = maxTime;

		this.updateSyncStatus(true, time);
	}

	private final void onSyncAudioStateChange(boolean start, int state) {
		this.videncode.setSyncAudioState(start, state);
	}
	private final void onSyncAudioFadeChange(boolean start, boolean enabled) {
		this.videncode.setSyncAudioUseFade(start, enabled);
	}
	private final void onSyncVideoStateChange(boolean start, int state) {
		this.videncode.setSyncVideoState(start, state);
	}
	private final void onSyncVideoFadeChange(boolean start, boolean enabled) {
		this.videncode.setSyncVideoUseFade(start, enabled);
	}

	// Output
	private final void updateEncodeEncodingStatus() {
		int visibleId = 0;
		if (this.videncode.isOutputEncoding()) {
			visibleId = 1;
			if (this.videncode.isOutputMuxing()) {
				visibleId = 2;
			}
		}
		else if (this.videncode.getOutputFileLast() != null) {
			visibleId = 3;
		}

		this.encode.finalStatusTextIdle.setVisible(visibleId == 0);
		this.encode.finalStatusTextWaiting.setVisible(visibleId == 1);
		this.encode.finalStatusTextMuxing.setVisible(visibleId == 2);
		this.encode.finalStatusTextComplete.setVisible(visibleId == 3);
	}
	private final void updateEncodeButtonStatus() {
		File img = this.videncode.getImageFileTemp();
		File video = this.videncode.getVideoFileSource();
		File audio = this.videncode.getAudioFileSource();

		this.encode.encodeButton.setEnabled(img != null && (video != null || audio != null));

		this.encode.encodeButton.setText(this.videncode.isOutputEncoding() ? "Cancel" : "Encode");
	}
	private final void updateEncodeFilenameAndTag() {
		this.encode.outputFilename.setText(this.videncode.getOutputFilename());
		this.encode.outputTag.setText(this.videncode.getOutputTag());
	}
	private final void updateEncodeExtension() {
		boolean b = this.encode.outputFilenameExtChanging;
		this.encode.outputFilenameExtChanging = true;

		// Extension update
		String vidExt = this.videncode.getOutputExtension();
		File f = this.videncode.getImageFileTemp();
		String ext = (f == null ? "*" : Videncode.getFileExt(f));

		this.encode.outputFilenameExt.removeAllItems();
		for (int i = 0; i < this.outputExtensions.length; ++i) {
			this.outputExtensions[i].setWildcard(ext);
			this.encode.outputFilenameExt.addItem(this.outputExtensions[i]);
			if (this.outputExtensions[i].getExtension().equals(vidExt)) {
				this.encode.outputFilenameExt.setSelectedIndex(i);
			}
		}

		this.encode.outputFilenameExtChanging = b;
	}
	private final void updateEncodeExtensionDefault() {
		boolean b = this.settings.outputFilenameExtChanging;
		this.settings.outputFilenameExtChanging = true;

		// Extension update
		String vidExt = this.videncode.getOutputExtensionDefault();
		File f = this.videncode.getImageFileTemp();
		String ext = (f == null ? "*" : Videncode.getFileExt(f));

		this.settings.outputFilenameExt.removeAllItems();
		for (int i = 0; i < this.outputExtensions.length; ++i) {
			this.outputExtensions[i].setWildcard(ext);
			this.settings.outputFilenameExt.addItem(this.outputExtensions[i]);
			if (this.outputExtensions[i].getExtension().equals(vidExt)) {
				this.settings.outputFilenameExt.setSelectedIndex(i);
			}
		}

		this.settings.outputFilenameExtChanging = b;
	}

	private final void onEncodeExtensionChange(Extension ext) {
		if (ext == null || this.encode.outputFilenameExtChanging) return;

		this.videncode.setOutputExtension(ext.getExtension());
	}
	private final void onEncodeExtensionChangeDefault(Extension ext) {
		if (ext == null || this.settings.outputFilenameExtChanging) return;

		this.videncode.setOutputExtensionDefault(ext.getExtension());
	}
	private final void onEncodeButtonPress() {
		this.encode.statusDisplay.setText(" ");

		if (this.videncode.isOutputEncoding()) {
			this.videncode.stopEncoding();
		}
		else {
			this.videncode.encodeAll();
		}
		this.updateVideoEncodingStatus(true);
		this.updateAudioEncodingStatus(true);
		this.updateEncodeEncodingStatus();
		this.updateEncodeButtonStatus();
	}
	private final void onEncodeFileNameChange(JTextFieldCustom field) {
		this.videncode.setOutputFilename(field.getText());
	}
	private final void onEncodeTagChange(JTextFieldCustom field) {
		this.videncode.setOutputTag(field.getText());

		this.autoSelectVideoQuality(true);
		this.updateVideoEncodeButtonAndBitrate();
	}
	private final void onEncodeCompletePress() {
		if (Videncode.isWindows()) {
			File f = this.videncode.getOutputFileLast();
			if (f != null) {
				try {
					Process p = new ProcessBuilder("explorer.exe", "/select," + f.getAbsolutePath()).start();
				}
				catch (IOException e) {

				}
			}
		}
	}

	private final void onOutputMaxSizeChange(JTextFieldCustom field) {
		long d = Videncode.labeledStringToInt(field.getText(), 1024, new String[]{ "B" , "B" , "(KB|K)" , "(MB|M)" });
		if (d <= 0) d = 1;
		this.videncode.setOutputMaxFileSize(d);
		field.setText(Videncode.intToLabeledString(d, 1024, new String[]{ "B" , "B" , "KB" , "MB" }));
	}
	private final void onSaveSettingsButtonPress() {
		this.settings.saveButton.setEnabled(false);

		JSON.Node node = JSON.node();
		this.saveSettings(node);
		this.videncode.saveSettings(node);
		Main.saveSettings(node);
	}

	private final void enableSaveSettingsButton() {
		this.settings.saveButton.setEnabled(true);
	}


	// Shared
	private final void displayImageOnTarget(JImage target, final Image img) {
		if (img == null) {
			target.setImage(this.noimage);
			target.setScaling(false, false);
		}
		else {
			target.setImage(img);
			target.setScaling(true, true);
		}
	}

	private final void onEncodeLogEvent(int type, boolean clear, String cmdText, String infoText, String progressText) {
		if (clear) {
			// Clear
			for (int i = 0; i < this.advanced.logs[type].length; ++i) {
				this.advanced.logs[type][i].setText("");
			}
		}
		if (cmdText != null) {
			String separator = "----------------------------------------\n";
			String current;
			for (int i = 0; i < this.advanced.logs[type].length; ++i) {
				current = this.advanced.logs[type][i].getText();
				if (current.length() > i && current.charAt(current.length() - 1) != '\n') {
					this.advanced.logs[type][i].append("\n");
				}
				this.advanced.logs[type][i].append(separator);
				this.advanced.logs[type][i].append(cmdText);
				this.advanced.logs[type][i].append("\n");
				this.advanced.logs[type][i].append(separator);
			}
		}
		if (infoText != null) {
			this.advanced.logs[type][0].append(infoText);
		}
		if (progressText != null) {
			this.advanced.logs[type][1].append(progressText);
		}
	}

}


