import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.*;

public class GameFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private GamePanel  panel;
	private HUDPanel   hud;
	private TitlePanel titlePanel;
	private JPanel     rootPanel;

	// 0=Easy 1=Medium 2=Hard — persists across game restarts in this session
	private int currentDifficulty = 1;

	// Card names for the root CardLayout
	private static final String CARD_TITLE = "title";
	private static final String CARD_GAME  = "game";

	public GameFrame() {
		// TODO Auto-generated constructor stub
		setTitle("Battle City");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setLayout(new BorderLayout());

		// Create maps/ folder and write 3 default level files
		setupMapsFolder();

		// Create HUD panel (lives=3, score=0, level=1)
		hud = new HUDPanel(3, 0, 1);

		// Create game panel — receives HUD reference
		panel = new GamePanel(hud);

		// ── Game card: game area + HUD side by side ──────────────────
		JPanel gameCard = new JPanel(new BorderLayout());
		gameCard.add(panel, BorderLayout.CENTER);
		gameCard.add(hud,   BorderLayout.EAST);

		// ── Title card ───────────────────────────────────────────────
		titlePanel = new TitlePanel(new TitlePanel.TitleListener() {
			@Override
			public void onOnePlayer() {
				LevelSelectDialog dialog = new LevelSelectDialog(GameFrame.this);
				dialog.setVisible(true);
				String path = dialog.getSelectedMapPath();
				if(path != null) {
					launchGame(path);
				}
			}
			@Override
			public void onConstruction() {
				MapEditorFrame editor = new MapEditorFrame();
				editor.setVisible(true);
			}
		});

		// ── Root panel with CardLayout ────────────────────────────────
		rootPanel = new JPanel(new CardLayout());
		rootPanel.add(titlePanel, CARD_TITLE);
		rootPanel.add(gameCard,   CARD_GAME);

		add(rootPanel, BorderLayout.CENTER);

		// Menu bar
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Menu");

		JMenuItem newGameItem    = new JMenuItem("New Game");
		JMenuItem mapEditorItem  = new JMenuItem("Map Editor");
		JMenuItem optionsItem    = new JMenuItem("Options");
		JMenuItem highScoresItem = new JMenuItem("High Scores");
		JMenuItem helpItem       = new JMenuItem("Help");
		JMenuItem aboutItem      = new JMenuItem("About");
		JMenuItem exitItem       = new JMenuItem("Exit");

		// New Game → open level selection dialog, then switch to game card
		newGameItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				LevelSelectDialog dialog = new LevelSelectDialog(GameFrame.this);
				dialog.setVisible(true); // Modal: blocks until closed

				String path = dialog.getSelectedMapPath();
				if(path != null) {
					launchGame(path);
				}
			}
		});

		// Map Editor → open in a separate window
		mapEditorItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MapEditorFrame editor = new MapEditorFrame();
				editor.setVisible(true);
			}
		});

		// Options → difficulty selection dialog
		optionsItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				OptionsDialog dialog = new OptionsDialog(GameFrame.this, currentDifficulty);
				dialog.setVisible(true);
				if(dialog.isConfirmed()) {
					currentDifficulty = dialog.getSelectedDifficulty();
					panel.setDifficulty(currentDifficulty);
					String[] labels = {"Easy", "Medium", "Hard"};
					JOptionPane.showMessageDialog(GameFrame.this,
						"Difficulty set to: " + labels[currentDifficulty] + "\nTakes effect on the next new game.",
						"Options",
						JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});

		// High Scores → open the scoreboard dialog
		highScoresItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HighScoresDialog dialog = new HighScoresDialog(GameFrame.this);
				dialog.setVisible(true);
			}
		});

		// Help → show controls
		helpItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String helpText = "Controls:\n" + "  Move:   W / A / S / D  or  Arrow Keys\n" + "  Fire:   SPACE\n\n" + "Tiles:\n" + "  Brick  — destroyed by bullets\n" + "  Steel  — indestructible\n" + "  Bush   — tanks and bullets pass through\n" + "  Water  — bullets pass through, tanks cannot\n\n" + "Protect the Eagle!  Lose all lives or\n" + "let the Eagle be destroyed and it's Game Over.";
				JOptionPane.showMessageDialog(GameFrame.this, helpText, "Help", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		// About
		aboutItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String info = "Developer: Canat Koç\n" + "School Number: 20240702024\n" + "Email: canat.koc@std.yeditepe.edu.tr";
				JOptionPane.showMessageDialog(GameFrame.this, info, "About", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		// Exit
		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		menu.add(newGameItem);
		menu.add(mapEditorItem);
		menu.add(optionsItem);
		menu.add(highScoresItem);
		menu.add(helpItem);
		menu.add(aboutItem);
		menu.add(exitItem);

		menuBar.add(menu);
		setJMenuBar(menuBar);

		// Pause button → connected to GamePanel.togglePause()
		hud.getPauseButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.togglePause();
			}
		});

		pack();
		setLocationRelativeTo(null);
	}

	// ---------------------------------------------------------------
	// Loads a map, switches to the game card, and ensures the game thread is running
	private void launchGame(String path) {
		panel.setDifficulty(currentDifficulty);
		panel.loadMapFromFile(path);
		panel.startGameThread();
		CardLayout cl = (CardLayout) rootPanel.getLayout();
		cl.show(rootPanel, CARD_GAME);
		panel.requestFocusInWindow();
	}

	// ---------------------------------------------------------------
	// Creates maps/ directory and writes 3 default level files if missing
	private void setupMapsFolder() {
		File mapsDir = new File("maps");
		if(!mapsDir.exists()) {
			mapsDir.mkdir();
		}

		writeLevelFile("maps" + File.separator + "level1.csv", getLevel1Data());
		writeLevelFile("maps" + File.separator + "level2.csv", getLevel2Data());
		writeLevelFile("maps" + File.separator + "level3.csv", getLevel3Data());
	}

	// Creates the file only if it does not exist — leaves user-modified files intact
	private void writeLevelFile(String path, int[][] data) {
		File file = new File(path);
		if(file.exists()) {
			return;
		}

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			for(int row = 0; row < data.length; row++) {
				for(int col = 0; col < data[row].length; col++) {
					writer.write(String.valueOf(data[row][col]));
					if(col < data[row].length - 1) {
						writer.write(",");
					}
				}
				writer.newLine();
			}
		} catch(IOException ex) {
			ex.printStackTrace();
		} finally {
			if(writer != null) {
				try { writer.close(); } catch(IOException ex) { ex.printStackTrace(); }
			}
		}
	}

	// ---------------------------------------------------------------
	// Easy — brick walls only, wide corridors
	private int[][] getLevel1Data() {
		return new int[][] {
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0},
			{0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{0, 0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0},
			{0, 0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0},
			{0, 1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1},
			{0, 1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0},
			{0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0},
			{0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0}, // eagle top bricks (cols 6,7,8)
			{0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0}, // eagle side bricks | player col 4
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}  // eagle at col 7 (hard-coded)
		};
	}

	// Medium — steel + bush + water corners
	private int[][] getLevel2Data() {
		return new int[][] {
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{0, 2, 2, 0, 3, 3, 0, 1, 1, 0, 3, 3, 0, 2, 2, 0},
			{0, 2, 2, 0, 3, 3, 0, 1, 1, 0, 3, 3, 0, 2, 2, 0},
			{4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 4},
			{0, 0, 2, 0, 1, 0, 0, 2, 2, 0, 0, 1, 0, 2, 0, 0},
			{0, 0, 2, 0, 1, 0, 0, 2, 2, 0, 0, 1, 0, 2, 0, 0},
			{0, 3, 0, 0, 0, 2, 2, 0, 0, 2, 2, 0, 0, 0, 3, 0},
			{0, 3, 0, 0, 0, 2, 2, 0, 0, 2, 2, 0, 0, 0, 3, 0},
			{4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4},
			{0, 2, 0, 1, 0, 0, 2, 0, 0, 2, 0, 0, 1, 0, 2, 0},
			{0, 2, 0, 1, 0, 0, 2, 0, 0, 2, 0, 0, 1, 0, 2, 0},
			{0, 0, 0, 0, 2, 0, 1, 1, 1, 0, 0, 2, 0, 0, 0, 0}, // eagle top bricks
			{0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0}, // eagle side bricks | player col 4
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}  // eagle at col 7 (hard-coded)
		};
	}

	// Hard — heavy steel + water corridors + bush maze
	private int[][] getLevel3Data() {
		return new int[][] {
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{4, 4, 2, 2, 3, 3, 0, 0, 0, 0, 3, 3, 2, 2, 4, 4},
			{4, 4, 2, 2, 3, 3, 0, 0, 0, 0, 3, 3, 2, 2, 4, 4},
			{0, 0, 0, 0, 2, 0, 2, 2, 2, 2, 0, 2, 0, 0, 0, 0},
			{2, 0, 3, 0, 2, 0, 0, 3, 3, 0, 0, 2, 0, 3, 0, 2},
			{2, 0, 3, 0, 0, 0, 3, 0, 0, 3, 0, 0, 0, 3, 0, 2},
			{0, 2, 0, 3, 0, 2, 2, 0, 0, 2, 2, 0, 3, 0, 2, 0},
			{0, 2, 0, 3, 0, 2, 2, 0, 0, 2, 2, 0, 3, 0, 2, 0},
			{4, 0, 0, 0, 0, 0, 0, 4, 4, 0, 0, 0, 0, 0, 0, 4},
			{2, 0, 3, 0, 2, 0, 0, 2, 2, 0, 0, 2, 0, 3, 0, 2},
			{2, 0, 3, 0, 2, 0, 0, 2, 2, 0, 0, 2, 0, 3, 0, 2},
			{0, 0, 0, 2, 0, 3, 1, 1, 1, 3, 0, 2, 0, 0, 0, 0}, // eagle top bricks
			{0, 2, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 2, 0}, // eagle side bricks | player col 4
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}  // eagle at col 7 (hard-coded)
		};
	}

	// ---------------------------------------------------------------
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				GameFrame frame = new GameFrame();
				frame.setVisible(true);
				// Show title screen and start its blink thread
				frame.titlePanel.requestFocusInWindow();
				frame.titlePanel.startThread();
			}
		});
	}
}
