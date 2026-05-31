import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class OptionsDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	// Currently selected difficulty: 0=Easy 1=Medium 2=Hard
	private int selectedDifficulty;
	private boolean confirmed = false;

	private JRadioButton radioEasy;
	private JRadioButton radioMedium;
	private JRadioButton radioHard;

	public OptionsDialog(JFrame parent, int currentDifficulty) {
		// TODO Auto-generated constructor stub
		super(parent, "Options", true);
		this.selectedDifficulty = currentDifficulty;

		setLayout(new BorderLayout(10, 10));
		setResizable(false);

		// ── Title label ─────────────────────────────────────────────
		JLabel titleLabel = new JLabel("Select Difficulty", SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
		titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 10));
		add(titleLabel, BorderLayout.NORTH);

		// ── Radio buttons ────────────────────────────────────────────
		radioEasy   = new JRadioButton("Easy   — fewer enemies on screen, slow fire rate");
		radioMedium = new JRadioButton("Medium — standard settings");
		radioHard   = new JRadioButton("Hard   — more enemies on screen, fast fire rate");

		ButtonGroup group = new ButtonGroup();
		group.add(radioEasy);
		group.add(radioMedium);
		group.add(radioHard);

		if(currentDifficulty == 0) {
			radioEasy.setSelected(true);
		} else if(currentDifficulty == 2) {
			radioHard.setSelected(true);
		} else {
			radioMedium.setSelected(true);
		}

		JPanel radioPanel = new JPanel(new GridLayout(3, 1, 4, 4));
		radioPanel.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
		radioPanel.add(radioEasy);
		radioPanel.add(radioMedium);
		radioPanel.add(radioHard);

		add(radioPanel, BorderLayout.CENTER);

		// ── OK / Cancel buttons ──────────────────────────────────────
		JButton okButton     = new JButton("OK");
		JButton cancelButton = new JButton("Cancel");

		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(radioEasy.isSelected()) {
					selectedDifficulty = 0;
				} else if(radioHard.isSelected()) {
					selectedDifficulty = 2;
				} else {
					selectedDifficulty = 1;
				}
				confirmed = true;
				dispose();
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				confirmed = false;
				dispose();
			}
		});

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		add(buttonPanel, BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(parent);
	}

	// Returns true if the user pressed OK (not Cancel / closed the dialog)
	public boolean isConfirmed() {
		return confirmed;
	}

	// Returns 0=Easy, 1=Medium, 2=Hard
	public int getSelectedDifficulty() {
		return selectedDifficulty;
	}
}
