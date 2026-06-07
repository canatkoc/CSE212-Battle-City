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
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setResizable(false);
		setLayout(new BorderLayout());

		editorPanel = new MapEditorPanel();
		add(editorPanel, BorderLayout.CENTER);

		add(buildSidePanel(), BorderLayout.EAST);
		add(buildBottomBar(), BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(null);
	}

	private JPanel buildSidePanel() {
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(130, 600));
		panel.setBackground(new Color(40, 40, 40));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(16, 10, 16, 10));

		JLabel title = new JLabel("MAP EDITOR");
		title.setForeground(Color.ORANGE);
		title.setFont(new Font("Arial", Font.BOLD, 13));
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(title);

		panel.add(Box.createVerticalStrut(16));

		JLabel controlsLabel = new JLabel("-- Controls --");
		controlsLabel.setForeground(new Color(170, 170, 170));
		controlsLabel.setFont(new Font("Arial", Font.PLAIN, 10));
		controlsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(controlsLabel);

		panel.add(Box.createVerticalStrut(6));

		String[] lines = {
			"W A S D : move",
			" SPACE   : cycle",
			"Mouse   : jump"
		};
		for(int i = 0; i < lines.length; i++) {
			JLabel line = new JLabel(lines[i]);
			line.setForeground(Color.WHITE);
			line.setFont(new Font("Monospaced", Font.PLAIN, 10));
			line.setAlignmentX(Component.CENTER_ALIGNMENT);
			panel.add(line);
			panel.add(Box.createVerticalStrut(3));
		}

		panel.add(Box.createVerticalStrut(14));

		JLabel tilesLabel = new JLabel("-- Tiles --");
		tilesLabel.setForeground(new Color(170, 170, 170));
		tilesLabel.setFont(new Font("Arial", Font.PLAIN, 10));
		tilesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(tilesLabel);

		panel.add(Box.createVerticalStrut(6));

		String[] tileLines = {
			" 0 - Empty",
			" 1 - Brick",
			" 2 - Steel",
			"3 - Bush",
			" 4 - Water"
		};
		for(int i = 0; i < tileLines.length; i++) {
			JLabel tl = new JLabel(tileLines[i]);
			tl.setForeground(Color.WHITE);
			tl.setFont(new Font("Monospaced", Font.PLAIN, 10));
			tl.setAlignmentX(Component.CENTER_ALIGNMENT);
			panel.add(tl);
			panel.add(Box.createVerticalStrut(2));
		}

		panel.add(Box.createVerticalGlue());

		JButton saveBtn = new JButton("Save");
		saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		saveBtn.setMaximumSize(new Dimension(100, 30));
		saveBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				editorPanel.saveMap();
			}
		});
		panel.add(saveBtn);

		panel.add(Box.createVerticalStrut(6));

		JButton loadBtn = new JButton("Load");
		loadBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		loadBtn.setMaximumSize(new Dimension(100, 30));
		loadBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				editorPanel.loadMap();
			}
		});
		panel.add(loadBtn);

		panel.add(Box.createVerticalStrut(6));

		JButton clearBtn = new JButton("Clear");
		clearBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		clearBtn.setMaximumSize(new Dimension(100, 30));
		clearBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int answer = JOptionPane.showConfirmDialog(
					MapEditorFrame.this,
					"Reset the map?",
					"Clear",
					JOptionPane.YES_NO_OPTION
				);
				if(answer == JOptionPane.YES_OPTION) {
					editorPanel.clearGrid();
					editorPanel.requestFocusInWindow();
				}
			}
		});
		panel.add(clearBtn);

		return panel;
	}

	private JPanel buildBottomBar() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.setBackground(new Color(25, 25, 25));

		JLabel hint = new JLabel("SPACE cycles: Empty > Brick > Steel > Water > Bush > Empty");
		hint.setForeground(new Color(150, 150, 150));
		hint.setFont(new Font("Monospaced", Font.PLAIN, 11));
		panel.add(hint);

		return panel;
	}
}
