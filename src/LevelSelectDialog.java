import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class LevelSelectDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	// Selected map file path — null means the user cancelled
	private String selectedMapPath = null;

	private JRadioButton level1Btn;
	private JRadioButton level2Btn;
	private JRadioButton level3Btn;
	private JRadioButton customBtn;

	public LevelSelectDialog(JFrame parent) {
		// TODO Auto-generated constructor stub
		super(parent, "Select Level", true); // true = modal (blocks the main window)
		setResizable(false);
		setLayout(new BorderLayout(10, 10));

		// --- TOP: title label ---
		JLabel titleLabel = new JLabel("Select the level you want to play:", SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial", Font.BOLD, 13));
		titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
		add(titleLabel, BorderLayout.NORTH);

		// --- CENTRE: radio buttons ---
		JPanel radioPanel = new JPanel();
		radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));
		radioPanel.setBorder(BorderFactory.createEmptyBorder(5, 30, 5, 30));

		level1Btn = new JRadioButton("Level 1  —  Easy    (Brick Walls Only)");
		level2Btn = new JRadioButton("Level 2  —  Medium  (Steel + Bush + Water)");
		level3Btn = new JRadioButton("Level 3  —  Hard    (Heavy Steel + Water)");
		customBtn = new JRadioButton("Custom Map...  (Load from File)");

		level1Btn.setSelected(true); // Default: Level 1

		// ButtonGroup — only one can be selected at a time
		ButtonGroup group = new ButtonGroup();
		group.add(level1Btn);
		group.add(level2Btn);
		group.add(level3Btn);
		group.add(customBtn);

		radioPanel.add(Box.createVerticalStrut(5));
		radioPanel.add(level1Btn);
		radioPanel.add(Box.createVerticalStrut(8));
		radioPanel.add(level2Btn);
		radioPanel.add(Box.createVerticalStrut(8));
		radioPanel.add(level3Btn);
		radioPanel.add(Box.createVerticalStrut(8));
		radioPanel.add(customBtn);
		radioPanel.add(Box.createVerticalStrut(5));

		add(radioPanel, BorderLayout.CENTER);

		// --- BOTTOM: OK / Cancel buttons ---
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		JButton cancelBtn = new JButton("Cancel");
		JButton okBtn     = new JButton("OK");

		cancelBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectedMapPath = null; // null = cancelled
				dispose();
			}
		});

		okBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(level1Btn.isSelected()) {
					selectedMapPath = "maps" + File.separator + "level1.csv";
				} else if(level2Btn.isSelected()) {
					selectedMapPath = "maps" + File.separator + "level2.csv";
				} else if(level3Btn.isSelected()) {
					selectedMapPath = "maps" + File.separator + "level3.csv";
				} else if(customBtn.isSelected()) {
					// Custom map — open file chooser
					JFileChooser fileChooser = new JFileChooser("maps");
					fileChooser.setDialogTitle("Select Map File");
					fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Map File (*.csv)", "csv"));

					int result = fileChooser.showOpenDialog(LevelSelectDialog.this);
					if(result == JFileChooser.APPROVE_OPTION) {
						selectedMapPath = fileChooser.getSelectedFile().getAbsolutePath();
					} else {
						return; // No file selected — keep dialog open
					}
				}
				dispose();
			}
		});

		buttonPanel.add(cancelBtn);
		buttonPanel.add(okBtn);
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 5));
		add(buttonPanel, BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(parent);
	}

	// GameFrame calls this to get the chosen path
	public String getSelectedMapPath() {
		return selectedMapPath;
	}
}
