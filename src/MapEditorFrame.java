import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MapEditorFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private MapEditorPanel editorPanel;

	public MapEditorFrame() {
		// TODO Auto-generated constructor stub
		setTitle("Battle City - Map Editor");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Does not close the main window
		setResizable(false);

		setLayout(new BorderLayout());

		// Centre: editor grid
		editorPanel = new MapEditorPanel();
		add(editorPanel, BorderLayout.CENTER);

		// Right: controls guide + colour legend + buttons
		JPanel guidePanel = buildGuidePanel();
		add(guidePanel, BorderLayout.EAST);

		pack();
		setLocationRelativeTo(null);
	}

	private JPanel buildGuidePanel() {
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(160, 600));
		panel.setBackground(new Color(40, 40, 40));
		panel.setLayout(null);

		// ---------------------------------------------------------------
		// Title
		JLabel title = new JLabel("MAP EDITOR");
		title.setForeground(Color.ORANGE);
		title.setFont(new Font("Arial", Font.BOLD, 13));
		title.setBounds(25, 12, 120, 20);
		panel.add(title);

		// ---------------------------------------------------------------
		// Keyboard controls
		JLabel controlsTitle = new JLabel("-- CONTROLS --");
		controlsTitle.setForeground(new Color(180, 180, 180));
		controlsTitle.setFont(new Font("Arial", Font.PLAIN, 10));
		controlsTitle.setBounds(15, 42, 135, 15);
		panel.add(controlsTitle);

		String[] controlKeys   = { "W A S D", "SPACE", "MOUSE CLICK" };
		String[] controlLabels = { "Move cursor", "Cycle tile", "Jump to cell" };

		for(int i = 0; i < controlKeys.length; i++) {
			JLabel keyLabel = new JLabel(controlKeys[i]);
			keyLabel.setForeground(Color.YELLOW);
			keyLabel.setFont(new Font("Monospaced", Font.BOLD, 11));
			keyLabel.setBounds(10, 62 + i * 32, 90, 20);
			panel.add(keyLabel);

			JLabel actionLabel = new JLabel(controlLabels[i]);
			actionLabel.setForeground(Color.WHITE);
			actionLabel.setFont(new Font("Arial", Font.PLAIN, 10));
			actionLabel.setBounds(10, 76 + i * 32, 140, 14);
			panel.add(actionLabel);
		}

		// ---------------------------------------------------------------
		// SPACE cycle order
		JLabel cycleTitle = new JLabel("-- SPACE CYCLE --");
		cycleTitle.setForeground(new Color(180, 180, 180));
		cycleTitle.setFont(new Font("Arial", Font.PLAIN, 10));
		cycleTitle.setBounds(10, 165, 145, 15);
		panel.add(cycleTitle);

		String[] tileNames  = { "Empty", "Brick", "Steel", "Water", "Bush" };
		Color[]  tileColors = {
			Color.BLACK,
			new Color(160,  82,  45),
			new Color(140, 140, 140),
			new Color( 30, 144, 255),
			new Color( 34, 139,  34)
		};

		for(int i = 0; i < tileNames.length; i++) {
			// Colour swatch
			JPanel swatch = new JPanel();
			swatch.setBackground(tileColors[i]);
			swatch.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));
			swatch.setBounds(10, 185 + i * 26, 18, 18);
			panel.add(swatch);

			// Arrow (loop symbol on last entry)
			JLabel arrow = new JLabel(i < tileNames.length - 1 ? "→" : "↺");
			arrow.setForeground(new Color(150, 150, 150));
			arrow.setFont(new Font("Arial", Font.PLAIN, 10));
			arrow.setBounds(130, 186 + i * 26, 20, 16);
			panel.add(arrow);

			// Tile name
			JLabel name = new JLabel(tileNames[i]);
			name.setForeground(Color.WHITE);
			name.setFont(new Font("Arial", Font.PLAIN, 11));
			name.setBounds(34, 186 + i * 26, 90, 16);
			panel.add(name);
		}

		// ---------------------------------------------------------------
		// Separator
		JSeparator sep = new JSeparator();
		sep.setBounds(10, 325, 140, 5);
		panel.add(sep);

		// ---------------------------------------------------------------
		// Save button
		JButton saveBtn = new JButton("Save");
		saveBtn.setBounds(20, 338, 120, 35);
		saveBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				editorPanel.saveMap();
			}
		});
		panel.add(saveBtn);

		// Load button
		JButton loadBtn = new JButton("Load");
		loadBtn.setBounds(20, 383, 120, 35);
		loadBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				editorPanel.loadMap();
			}
		});
		panel.add(loadBtn);

		// Clear button
		JButton clearBtn = new JButton("Clear");
		clearBtn.setBounds(20, 428, 120, 35);
		clearBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int confirm = JOptionPane.showConfirmDialog(
					MapEditorFrame.this,
					"Reset the map?",
					"Clear",
					JOptionPane.YES_NO_OPTION
				);
				if(confirm == JOptionPane.YES_OPTION) {
					editorPanel.clearGrid();
					editorPanel.requestFocusInWindow(); // Return focus to editor
				}
			}
		});
		panel.add(clearBtn);

		return panel;
	}
}
