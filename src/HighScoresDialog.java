import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class HighScoresDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private ArrayList<String[]> entries;

	public HighScoresDialog(JFrame parent) {
		// TODO Auto-generated constructor stub
		super(parent, "High Scores", true);
		entries = new ArrayList<String[]>();

		loadScores();
		sortScores();

		setResizable(false);
		setLayout(new BorderLayout());
		getContentPane().setBackground(new Color(30, 30, 30));

		JLabel titleLabel = new JLabel("TOP 10 HIGH SCORES", SwingConstants.CENTER);
		titleLabel.setForeground(new Color(220, 180, 0));
		titleLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
		titleLabel.setBorder(BorderFactory.createEmptyBorder(12, 0, 8, 0));
		titleLabel.setOpaque(true);
		titleLabel.setBackground(new Color(30, 30, 30));
		add(titleLabel, BorderLayout.NORTH);

		JTextArea textArea = new JTextArea(buildScoreText());
		textArea.setEditable(false);
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
		textArea.setBackground(new Color(30, 30, 30));
		textArea.setForeground(new Color(210, 210, 210));
		textArea.setBorder(BorderFactory.createEmptyBorder(4, 16, 4, 16));

		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setBorder(null);
		scrollPane.setPreferredSize(new Dimension(380, 280));
		scrollPane.getViewport().setBackground(new Color(30, 30, 30));
		add(scrollPane, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setBackground(new Color(30, 30, 30));
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(6, 0, 10, 0));

		JButton closeButton = new JButton("Close");
		closeButton.setFont(new Font("Monospaced", Font.PLAIN, 12));
		closeButton.setFocusPainted(false);
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		bottomPanel.add(closeButton);
		add(bottomPanel, BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(parent);
	}

	private String buildScoreText() {
		if(entries.isEmpty()) {
			return "No scores yet.";
		}

		int rowCount;
		if(entries.size() < 10) {
			rowCount = entries.size();
		} else {
			rowCount = 10;
		}

		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < rowCount; i++) {
			String[] entry = entries.get(i);
			String name = entry[0];
			String score = entry[1];
			String date = entry[2];
			sb.append(String.format("%2d. %-12s %6s    %s%n", i + 1, name, score, date));
		}
		return sb.toString();
	}

	private void loadScores() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader("scores.csv"));
			String line;
			while((line = reader.readLine()) != null) {
				String[] parts = line.split(",");
				if(parts.length >= 2) {
					String dateStr;
					if(parts.length >= 3) {
						dateStr = parts[2].trim();
					} else {
						dateStr = "-";
					}
					entries.add(new String[]{ parts[0].trim(), parts[1].trim(), dateStr });
				}
			}
		} catch(IOException ex) {
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch(IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	private void sortScores() {
		Collections.sort(entries, new Comparator<String[]>() {
			@Override
			public int compare(String[] entryA, String[] entryB) {
				int scoreA = 0;
				int scoreB = 0;
				try {
					scoreA = Integer.parseInt(entryA[1]);
				} catch(NumberFormatException ex) {
					scoreA = 0;
				}
				try {
					scoreB = Integer.parseInt(entryB[1]);
				} catch(NumberFormatException ex) {
					scoreB = 0;
				}
				return scoreB - scoreA;
			}
		});
	}
}
