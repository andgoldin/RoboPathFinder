import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

/**
 * Main class for the iRobot Create pathfinder. A GUI Swing application.
 * @author Andrew Goldin
 */
public class RoboPath extends JFrame {

	private static final long serialVersionUID = 1L;
	
	public static final int PIXEL_OFFSET_X = 30, PIXEL_OFFSET_Y = 60;
	
	private final float SCALE = 65.0f;
	private Environment env;
	private JPanel buttonPanel;
	private JButton growButton, growSafeButton, graphNormalButton,
		graphSafeButton, pathButton, saveButton, clearButton;
	private JTextField saveFileField;
	private String mapFileName, startGoalFileName;
	
	private boolean mapDrawn, obstaclesGrown, safeGrown,
		graphDrawn, safeGraphDrawn, pathDrawn;
	
	/**
	 * Constructs a new RoboPath object with a given map file and start/goal definitions.
	 * @param mapFile the file defining the environment
	 * @param startGoalFile the file defining the start/goal points
	 */
	public RoboPath(String mapFile, String startGoalFile) {
		super("COMS W4733 - HW4 - Robot Path Planner");
		setSize(new Dimension(1020, 600));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setBackground(Color.BLACK);
		getContentPane().setLayout(new BorderLayout());
		
		mapFileName = mapFile;
		startGoalFileName = startGoalFile;
		openMap();
		
		mapDrawn = true;
		obstaclesGrown = false;
		safeGrown = false;
		graphDrawn = false;
		safeGraphDrawn = false;
		pathDrawn = false;
		
		ActionListener buttonListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("Grow Obstacles") && mapDrawn && !obstaclesGrown) {
					env.growObstacles(false);
					obstaclesGrown = true;
					safeGrown = false;
					pathDrawn = false;
					repaint();
				}
				else if (e.getActionCommand().equals("Grow Safe") && mapDrawn && !safeGrown) {
					env.growObstacles(true);
					safeGrown = true;
					obstaclesGrown = false;
					pathDrawn = false;
					repaint();
				}
				else if (e.getActionCommand().equals("Compute VGraph") && mapDrawn
						&& obstaclesGrown && !graphDrawn) {
					env.computeVisibilityGraph(env.getGrownObstacles());
					graphDrawn = true;
					safeGraphDrawn = false;
					pathDrawn = false;
					repaint();
				}
				else if (e.getActionCommand().equals("Compute Safe VGraph") && mapDrawn
						&& safeGrown && !safeGraphDrawn) {
					env.computeVisibilityGraph(env.getSuperGrownObstacles());
					safeGraphDrawn = true;
					graphDrawn = false;
					pathDrawn = false;
					repaint();
				}
				else if (e.getActionCommand().equals("Compute Shortest Path") && mapDrawn
						&& ((obstaclesGrown && graphDrawn) || (safeGrown && safeGraphDrawn)) && !pathDrawn) {
					env.computeShortestPath();
					pathDrawn = true;
					repaint();
				}
				else if (e.getActionCommand().equals("Write Path To File") && mapDrawn
						&& ((obstaclesGrown && graphDrawn) || (safeGrown && safeGraphDrawn)) && pathDrawn) {
					env.writePathToFile(saveFileField.getText());
					saveButton.setEnabled(false);
				}
				else if (e.getActionCommand().equals("Clear")) {
					obstaclesGrown = false;
					safeGrown = false;
					graphDrawn = false;
					safeGraphDrawn = false;
					pathDrawn = false;
					saveButton.setEnabled(true);
					openMap();
					repaint();
				}
			}
		};
		
		buttonPanel = new JPanel(new FlowLayout());

		growButton = new JButton("Grow Obstacles");
		growButton.addActionListener(buttonListener);
		growSafeButton = new JButton("Grow Safe");
		growSafeButton.addActionListener(buttonListener);
		graphNormalButton = new JButton("Compute VGraph");
		graphNormalButton.addActionListener(buttonListener);
		graphSafeButton = new JButton("Compute Safe VGraph");
		graphSafeButton.addActionListener(buttonListener);
		pathButton = new JButton("Compute Shortest Path");
		pathButton.addActionListener(buttonListener);
		saveButton = new JButton("Write Path To File");
		saveButton.addActionListener(buttonListener);
		clearButton = new JButton("Clear");
		clearButton.addActionListener(buttonListener);
		
		saveFileField = new JTextField("robot_path.txt");
		saveFileField.setColumns(8);
		
		buttonPanel.add(growButton);
		buttonPanel.add(graphNormalButton);
		buttonPanel.add(growSafeButton);
		buttonPanel.add(graphSafeButton);
		buttonPanel.add(pathButton);
		buttonPanel.add(saveFileField);
		buttonPanel.add(saveButton);
		buttonPanel.add(clearButton);
		
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	}
	
	/**
	 * Opens the environment map to be drawn.
	 */
	public void openMap() {
		try {
			env = Environment.parseFiles(mapFileName, startGoalFileName);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		mapDrawn = true;
	}
	
	/**
	 * Paints with the graphics context.
	 * @param g the graphics context
	 */
	public void paint(Graphics g) {
		super.paint(g);
		if (mapDrawn) {
			env.draw(g, SCALE);
		}
	}
	
	// main method
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("Invalid agrument list, exiting");
			System.exit(0);
		}
		else {
			//UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			RoboPath r = new RoboPath(args[0], args[1]);
			r.setVisible(true);
		}
	}

}