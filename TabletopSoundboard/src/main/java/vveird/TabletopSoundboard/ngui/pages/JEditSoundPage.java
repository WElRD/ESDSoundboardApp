package vveird.TabletopSoundboard.ngui.pages;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import vveird.TabletopSoundboard.AudioApp;
import vveird.TabletopSoundboard.config.AppConfiguration;
import vveird.TabletopSoundboard.config.Sound;
import vveird.TabletopSoundboard.config.Sound.Type;
import vveird.TabletopSoundboard.ngui.components.IconSelectorPanel;
import vveird.TabletopSoundboard.ngui.components.JSoundMetadataTemplatePanel;
import vveird.TabletopSoundboard.ngui.layout.UIColumnLayout;
import vveird.TabletopSoundboard.ngui.util.ColorScheme;
import vveird.TabletopSoundboard.player.AudioPlayer;
import vveird.TabletopSoundboard.plugins.PluginManager;
import vveird.TabletopSoundboard.plugins.data.SoundPluginMetadata;
import vveird.TabletopSoundboard.plugins.data.SoundPluginMetadataTemplate;
import vveird.TabletopSoundboard.plugins.data.exceptions.ValueNotInMetadataListException;
import vveird.TabletopSoundboard.plugins.data.exceptions.WrongSoundPluginMetadataTypeException;

public class JEditSoundPage extends Page {

	private static File lastDirectory = null;

	Logger logger = LogManager.getLogger(JEditSoundPage.class);

	public static BufferedImage OK = null;
	public static BufferedImage FALSE = null;
	public static BufferedImage DEFAULT = null;

	static {
		try {
			OK = ImageIO.read(AudioApp.class.getResourceAsStream("/icons/ok.png"));
			FALSE = ImageIO.read(AudioApp.class.getResourceAsStream("/icons/false.png"));
			DEFAULT = ImageIO.read(AudioApp.class.getResourceAsStream("/icons/audio.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	DefaultListModel<String> dlm;
	private JTextField tfName;
	private JLabel lblNameOk;
	private JButton btnFinish;
	private JTextField tfAudio;
	private AudioPlayer player = null;
	private IconSelectorPanel iPanel;
	private JRadioButton rdbtnAmbience;
	private JPanel pnStatus;
	private JTextField tfTags;
	private JComboBox cbPlugins;
	private JButton btnAddPluginMetadata;

	private Map<String, List<SoundPluginMetadataTemplate>> availableTemplates = null;

	private Map<String, List<SoundPluginMetadataTemplate>> configuredTemplates = null;

	private Map<String, JPanel> configuredTemplatePanels = null;
	private JPanel pnSound;
	private JPanel pnContent;
	Sound s;
	private JLabel lblSpotifyPlaylist;

	/**
	 * Create the panel.
	 */
	public JEditSoundPage(Sound s) {
		setLayout(new BorderLayout(0, 0));
		setSize(new Dimension(656, 435));
		setOpaque(false);

		this.s = Objects.requireNonNull(s);

		configuredTemplates = new HashMap<>();

		configuredTemplatePanels = new HashMap<>();

		dlm = new DefaultListModel<>();

		pnStatus = new JPanel();
//		panel_4.add(pnStatus, BorderLayout.SOUTH);
		pnStatus.setLayout(new BorderLayout(0, 0));

		JButton btnBack = new JButton("Back");
		pnStatus.add(btnBack, BorderLayout.WEST);

		btnFinish = new JButton("Finish");
		pnStatus.add(btnFinish, BorderLayout.EAST);
		pnStatus.setOpaque(false);

		pnContent = new JPanel();
		pnContent.setLayout(new UIColumnLayout());
		pnContent.setOpaque(false);

		pnSound = new JPanel();
		pnSound.setLayout(null);
		pnSound.setOpaque(false);
		pnSound.setPreferredSize(new Dimension(640, 260));
		pnContent.add(pnSound);
		
		JViewport viewport = new JViewport();
		viewport.setOpaque(false);
		viewport.setView(pnContent);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(null);
		scrollPane.setViewport(viewport);
		scrollPane.getViewport().setOpaque(false);
		scrollPane.setOpaque(false);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(0, 0, 700, 460);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(scrollPane, BorderLayout.CENTER);

		JLabel lblName = new JLabel("Name");
		lblName.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblName.setBounds(10, 11, 46, 11);
		pnSound.add(lblName);

		tfName = new JTextField(s.getName());
		tfName.setEditable(false);
		tfName.setForeground(ColorScheme.FOREGROUND_COLOR);
		tfName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		tfName.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		tfName.setBackground(ColorScheme.MAIN_BACKGROUND_COLOR.darker());
		tfName.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				assertName();
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				assertName();
			}

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				assertName();
			}
		});
		tfName.setBounds(10, 22, 400, 25);
		pnSound.add(tfName);
		tfName.setColumns(10);

		lblNameOk = new JLabel(new ImageIcon(FALSE));
		lblNameOk.setBounds(420, 22, 20, 20);
		pnSound.add(lblNameOk);

		iPanel = new IconSelectorPanel(DEFAULT);
		iPanel.setBounds(10, 58, 160, 151);
		if(s.getCover() != null)
			iPanel.setImage(s.getCover());
		pnSound.add(iPanel);

		tfAudio = new JTextField(!s.isSpotifySound() ? s.getFilePaths()[0] : "");
		tfAudio.setForeground(ColorScheme.FOREGROUND_COLOR);
		tfAudio.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		tfAudio.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		tfAudio.setBackground(ColorScheme.MAIN_BACKGROUND_COLOR.darker());
		tfAudio.setEditable(false);
		tfAudio.setBounds(180, 58, 260, 25);
		pnSound.add(tfAudio);
		tfAudio.setColumns(10);

		JButton btnSelectAudio = new JButton("Select Audio");
		btnSelectAudio.setEnabled(!s.isSpotifySound());
		btnSelectAudio.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		btnSelectAudio.setOpaque(false);
		btnSelectAudio.setBounds(180, 94, 260, 23);
		pnSound.add(btnSelectAudio);

		btnSelectAudio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				openSound();
			}
		});
		JButton btnPlay = new JButton("Play");
		btnPlay.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		btnPlay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (player != null && player.isActive())
					stopAudio();
				else
					playAudio();
			}

			private void stopAudio() {
				player.stop(false);
				player.close();
//				player = null;
				btnPlay.setText("Play");
			}

			private void playAudio() {
				if (player != null && player.isActive())
					player.close();
				File file = new File(tfAudio.getText());
				if (file.exists()) {
					player = new AudioPlayer(
							new Sound(tfName.getText(), file.toString(), null,
									rdbtnAmbience.isSelected() ? Type.AMBIENCE : Type.EFFECT, null),
							AudioApp.getConfiguration().getMixerInfo());
					try {
						player.open();
						float gain = AppConfiguration.linearToDecibel(AudioApp.getConfiguration().masterGain) + 2;
						logger.debug("Setting gain for ambience player: " + gain);
						player.setGain(gain);
						player.start();
						btnPlay.setText("Playing");
					} catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
						logger.error("Error opening audio file: " + player.getAudioFile());
						logger.error(e);
						e.printStackTrace();
					}
				}
			}
		});
		btnPlay.setBounds(180, 128, 91, 45);
		pnSound.add(btnPlay);

		rdbtnAmbience = new JRadioButton("Ambience");
		rdbtnAmbience.setOpaque(false);
		rdbtnAmbience.setBounds(277, 124, 109, 23);
		pnSound.add(rdbtnAmbience);
		rdbtnAmbience.setSelected(true);

		JRadioButton rdbtnEffect = new JRadioButton("Effect");
		rdbtnEffect.setOpaque(false);
		rdbtnEffect.setBounds(277, 147, 109, 23);
		pnSound.add(rdbtnEffect);
		ButtonGroup bg = new ButtonGroup();
		bg.add(rdbtnAmbience);
		bg.add(rdbtnEffect);
		if (s.getType() == Type.AMBIENCE)
			rdbtnAmbience.setSelected(true);
		else
			rdbtnEffect.setSelected(true);

		tfTags = new JTextField(s.getTags() != null ? String.join(" ", s.getTags()) : "");
		tfTags.setForeground(ColorScheme.FOREGROUND_COLOR);
		tfTags.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		tfTags.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		tfTags.setBackground(ColorScheme.MAIN_BACKGROUND_COLOR.darker());
		tfTags.setBounds(180, 184, 260, 25);
		pnSound.add(tfTags);
		tfTags.setColumns(10);

		JLabel lblTags = new JLabel("Tags");
		lblTags.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTags.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblTags.setBounds(394, 171, 46, 11);
		pnSound.add(lblTags);

		JLabel lblPlugins = new JLabel("Plugin Metadata");
		lblPlugins.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblPlugins.setBounds(10, 220, 206, 11);
		pnSound.add(lblPlugins);
		availableTemplates = PluginManager.getSoundPluginMetadataTemplates();
		cbPlugins = new JComboBox(availableTemplates.keySet().toArray(new Object[0]));
		cbPlugins.setBounds(10, 234, 376, 22);
		pnSound.add(cbPlugins);

		btnAddPluginMetadata = new JButton("Add");
		btnAddPluginMetadata.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addPluginMetadata((String) cbPlugins.getSelectedItem(),
						availableTemplates.get(cbPlugins.getSelectedItem()));
			}
		});
		btnAddPluginMetadata.setOpaque(false);
		btnAddPluginMetadata.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		btnAddPluginMetadata.setBounds(394, 234, 46, 23);
		pnSound.add(btnAddPluginMetadata);
		
		lblSpotifyPlaylist = new JLabel("Spotify Playlist");
		lblSpotifyPlaylist.setVisible(s.isSpotifySound());
		lblSpotifyPlaylist.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSpotifyPlaylist.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblSpotifyPlaylist.setBounds(324, 9, 86, 11);
		pnSound.add(lblSpotifyPlaylist);
		Map<String, List<SoundPluginMetadata>> metadataSound = s.getMetadata();
		Map<String, List<SoundPluginMetadataTemplate>> metadataFinal = new HashMap<>();
		if(metadataSound != null) {
			for (String classNameMetadataSound : metadataSound.keySet()) {
				 List<SoundPluginMetadata> metadataIn = metadataSound.get(classNameMetadataSound);
				 for (SoundPluginMetadata soundPluginMetadata : metadataIn) {
					 if (metadataFinal.containsKey(soundPluginMetadata.pluginName)) {
							metadataFinal.get(soundPluginMetadata.pluginName).add(soundPluginMetadata.createTemplate());
						} else {
							List<SoundPluginMetadataTemplate> list = new LinkedList<>();
							list.add(soundPluginMetadata.createTemplate());
							metadataFinal.put(soundPluginMetadata.pluginName, list);
						}
				 }
			}
			int i = 0;
			for (String disp : metadataFinal.keySet()) {
				addPluginMetadata(disp != null ? disp : String.valueOf(i++), metadataFinal.get(disp));
			}
		}
	}

	private void addPluginMetadata(String plugin, List<SoundPluginMetadataTemplate> templates) {
		logger.debug("Adding Metadata for " + plugin);
		int panelStartY = 270;
		int templateStartY = 30;
		int templateIncrease = 35;
		if (configuredTemplates.containsKey(plugin))
			return;
		configuredTemplates.put(plugin, templates);
		JPanel pnPlugin = new JPanel();
		pnPlugin.setLayout(null);
		pnPlugin.setOpaque(false);

		JLabel lblPluginName = new JLabel(plugin);
		lblPluginName.setBounds(10, 11, 300, 14);
		lblPluginName.setFont(lblPluginName.getFont().deriveFont(Font.BOLD));
		pnPlugin.add(lblPluginName);

		JButton btnRemovePlugin = new JButton("X");
		btnRemovePlugin.setMargin(new Insets(1, 1, 1, 1));
		btnRemovePlugin.setBounds(410, 9, 20, 20);
		pnPlugin.add(btnRemovePlugin);
		btnRemovePlugin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JPanel p = configuredTemplatePanels.remove(plugin);
				configuredTemplates.remove(plugin);
				pnContent.remove(p);
				pnContent.revalidate();
				pnContent.repaint();
			}
		});

		int tIndex = 0;
		for (SoundPluginMetadataTemplate template : templates) {
			JSoundMetadataTemplatePanel pn = new JSoundMetadataTemplatePanel(template);
			pn.setBounds(10, templateStartY + (templateIncrease * tIndex++), 430, 30);
			pn.setOpaque(false);
			pnPlugin.add(pn);
		}
		pnPlugin.setBounds(10, (configuredTemplates.size() * panelStartY) + ((configuredTemplates.size() - 1) * 10),
				430, templateStartY + (templates.size() * templateIncrease));
		pnPlugin.setPreferredSize(new Dimension(430, templateStartY + (templates.size() * templateIncrease)));
		pnContent.add(pnPlugin);
		pnContent.revalidate();
		pnContent.repaint();
		configuredTemplatePanels.put(plugin, pnPlugin);
	}

	private void openSound() {
		final JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setFileFilter(new FileNameExtensionFilter("Audio files", new String[] { "mp3", "wav" }));
		if (lastDirectory != null)
			fc.setCurrentDirectory(lastDirectory);
		int returnValue = fc.showOpenDialog(this);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fc.getSelectedFile();
			lastDirectory = selectedFile.getParentFile();
			Mp3File song;
			if (selectedFile.toString().toLowerCase().endsWith(".mp3"))
				try {
					song = new Mp3File(selectedFile.getAbsolutePath());
					if (song.hasId3v2Tag()) {
						ID3v2 id3v2tag = song.getId3v2Tag();
						byte[] imageData = id3v2tag.getAlbumImage();
						if (imageData != null)
							try {
								BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageData));
								iPanel.setImage(img);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					}
				} catch (UnsupportedTagException e1) {
					e1.printStackTrace();
				} catch (InvalidDataException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			tfAudio.setText(selectedFile.getAbsoluteFile().toString());
		}
	}

	private void assertName() {
		if (AudioApp.getSoundLibrary().stream().anyMatch(s -> s.getName().equalsIgnoreCase(tfName.getText().trim()))
				|| tfName.getText().isEmpty()) {
			lblNameOk.setIcon(new ImageIcon(FALSE));
			btnFinish.setEnabled(false);
		} else {
			lblNameOk.setIcon(new ImageIcon(OK));
			btnFinish.setEnabled(true);
		}
		lblNameOk.validate();
		lblNameOk.repaint();
	}

	@Override
	protected void okAction() {
		try {
			BufferedImage cover = iPanel.getImage();
			cover = cover == null ? DEFAULT : cover;
			String name = tfName.getText().trim();
			String audioFile = tfAudio.getText();
			String[] tags = tfTags.getText().split(" ");
			Sound.Type type = rdbtnAmbience.isSelected() ? Type.AMBIENCE : Type.EFFECT;
			Map<String, List<SoundPluginMetadata>> metadata = new HashMap<>();
			for (String meta : configuredTemplates.keySet()) {
				List<SoundPluginMetadata> data = new LinkedList<>();
				JPanel p = configuredTemplatePanels.get(meta);
				for (int i = 0; i < p.getComponentCount(); i++) {
					if (p.getComponent(i) instanceof JSoundMetadataTemplatePanel) {
						JSoundMetadataTemplatePanel tp = (JSoundMetadataTemplatePanel) p.getComponent(i);
						SoundPluginMetadata md = tp.createMetadata();
						data.add(md);
					}
				}
				if (data.size() > 0)
					metadata.put(data.get(0).pluginClass, data);
			}
//			Sound s = new Sound(name, new String[] {audioFile}, cover, type, tags, metadata);
			s.setFilePaths(new String[] {audioFile});
			s.setCover(cover);
			s.setTags(Arrays.asList(tags));
			s.setType(type);
			s.setMetadata(metadata);
			AudioApp.saveSound(s);
			pageViewer.back();
		} catch (IOException e1) {
			logger.error(e1);
			JOptionPane.showMessageDialog(null, "Could not save Soundboard: " + e1.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		} catch (ValueNotInMetadataListException | WrongSoundPluginMetadataTypeException e) {
			logger.error(e);
			JOptionPane.showMessageDialog(null, "Metadata are not correct: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	@Override
	protected void cancelAction() {
		getPageViewer().back();
	}
}
