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

	private static final int DIALOG_WIDTH  = 560;
	private static final int DIALOG_HEIGHT = 460;

	// [0]=name  [1]=score  [2]=date  [3]=time
	private ArrayList<String[]> entries;

	public HighScoresDialog(JFrame parent) {
		// TODO Auto-generated constructor stub
		super(parent, "High Scores", true);
		entries = new ArrayList<String[]>();

		loadScores();
		sortScores();

		setResizable(false);
		setLayout(new BorderLayout());

		ScoreBoardPanel boardPanel = new ScoreBoardPanel();
		boardPanel.setBackground(new Color(20, 20, 20));
		boardPanel.setPreferredSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT - 52));

		JPanel bottomPanel = new JPanel();
		bottomPanel.setBackground(new Color(20, 20, 20));

		JButton closeButton = new JButton("CLOSE");
		closeButton.setBackground(new Color(60, 60, 60));
		closeButton.setForeground(Color.WHITE);
		closeButton.setFont(new Font("Monospaced", Font.BOLD, 12));
		closeButton.setFocusPainted(false);
		closeButton.setBorder(BorderFactory.createLineBorder(new Color(160, 160, 160), 2));
		closeButton.setPreferredSize(new Dimension(120, 32));
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		bottomPanel.add(closeButton);

		add(boardPanel, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(parent);
	}

	// ---------------------------------------------------------------
	// Reads every line from scores.csv into entries
	// Supports both old format (name,score) and new format (name,score,date,time)
	private void loadScores() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader("scores.csv"));
			String line;
			while((line = reader.readLine()) != null) {
				String[] parts = line.split(",");
				if(parts.length >= 2) {
					String dateStr = parts.length >= 3 ? parts[2].trim() : "-";
					String timeStr = parts.length >= 4 ? parts[3].trim() : "-";
					entries.add(new String[]{ parts[0].trim(), parts[1].trim(), dateStr, timeStr });
				}
			}
		} catch(IOException ex) {
			// File may not exist yet — that is fine, list stays empty
		} finally {
			if(reader != null) {
				try { reader.close(); } catch(IOException ex) { ex.printStackTrace(); }
			}
		}
	}

	// ---------------------------------------------------------------
	// Sorts entries descending by score (highest first)
	private void sortScores() {
		Collections.sort(entries, new Comparator<String[]>() {
			@Override
			public int compare(String[] entryA, String[] entryB) {
				int scoreA = 0;
				int scoreB = 0;
				try { scoreA = Integer.parseInt(entryA[1]); } catch(NumberFormatException ex) { scoreA = 0; }
				try { scoreB = Integer.parseInt(entryB[1]); } catch(NumberFormatException ex) { scoreB = 0; }
				return scoreB - scoreA; // descending
			}
		});
	}

	// ---------------------------------------------------------------
	// Inner panel that paints the scoreboard with Java2D
	private class ScoreBoardPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			drawScoreBoard(g);
		}
	}

	// ---------------------------------------------------------------
	// All the actual drawing — called from ScoreBoardPanel.paintComponent
	private void drawScoreBoard(Graphics g) {
		int panelWidth = DIALOG_WIDTH;

		// Column X positions
		int colRank  = 24;
		int colName  = 86;
		int colScore = 258;
		int colDate  = 354;
		int colTime  = 468;

		// ---- Title ----
		g.setColor(new Color(220, 180, 0));
		g.setFont(new Font("Monospaced", Font.BOLD, 22));
		FontMetrics titleFm = g.getFontMetrics();
		String title = "HIGH SCORES";
		g.drawString(title, (panelWidth - titleFm.stringWidth(title)) / 2, 44);

		// Title underline
		g.setColor(new Color(90, 90, 90));
		g.fillRect(20, 54, panelWidth - 40, 2);

		// ---- Column headers ----
		g.setColor(new Color(160, 160, 160));
		g.setFont(new Font("Monospaced", Font.BOLD, 11));
		g.drawString("RANK",  colRank,  76);
		g.drawString("NAME",  colName,  76);
		g.drawString("SCORE", colScore, 76);
		g.drawString("DATE",  colDate,  76);
		g.drawString("TIME",  colTime,  76);

		// Header underline
		g.setColor(new Color(70, 70, 70));
		g.fillRect(20, 82, panelWidth - 40, 1);

		// ---- Score rows (top 10 only) ----
		int rowCount = entries.size() < 10 ? entries.size() : 10;
		for(int i = 0; i < rowCount; i++) {
			String[] entry  = entries.get(i);
			int      rowY   = 106 + i * 32;
			int      rowTop = rowY - 18;

			// Alternating row background
			if(i % 2 == 0) {
				g.setColor(new Color(35, 35, 35));
				g.fillRect(20, rowTop, panelWidth - 40, 28);
			}

			// Rank — gold / silver / bronze for top 3, grey otherwise
			if(i == 0) {
				g.setColor(new Color(255, 215, 0));
			} else if(i == 1) {
				g.setColor(new Color(192, 192, 192));
			} else if(i == 2) {
				g.setColor(new Color(205, 127, 50));
			} else {
				g.setColor(new Color(160, 160, 160));
			}
			g.setFont(new Font("Monospaced", Font.BOLD, 13));
			g.drawString(String.format("#%02d", i + 1), colRank, rowY);

			// Name — truncate at 10 chars
			String displayName = entry[0];
			if(displayName.length() > 10) {
				displayName = displayName.substring(0, 10);
			}
			g.setColor(Color.WHITE);
			g.setFont(new Font("Monospaced", Font.PLAIN, 13));
			g.drawString(displayName, colName, rowY);

			// Score — green
			int scoreVal = 0;
			try { scoreVal = Integer.parseInt(entry[1]); } catch(NumberFormatException ex) { scoreVal = 0; }
			g.setColor(new Color(80, 210, 80));
			g.setFont(new Font("Monospaced", Font.BOLD, 13));
			g.drawString(String.format("%05d", scoreVal), colScore, rowY);

			// Date — light blue
			g.setColor(new Color(130, 180, 230));
			g.setFont(new Font("Monospaced", Font.PLAIN, 12));
			g.drawString(entry[2], colDate, rowY);

			// Time — lighter grey
			g.setColor(new Color(170, 170, 170));
			g.drawString(entry[3], colTime, rowY);
		}

		// ---- Empty state ----
		if(entries.isEmpty()) {
			g.setColor(new Color(110, 110, 110));
			g.setFont(new Font("Monospaced", Font.ITALIC, 14));
			FontMetrics emptyFm = g.getFontMetrics();
			String emptyMsg = "No scores yet — go play!";
			g.drawString(emptyMsg, (panelWidth - emptyFm.stringWidth(emptyMsg)) / 2, 190);
		}
	}
}
