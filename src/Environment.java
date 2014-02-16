import java.awt.*;
import java.util.*;
import java.io.*;

/**
 * Represents an environment for the robot, containing the boundary, obstacles, start and goal points.
 * @author Andrew Goldin
 */
public class Environment {
	
	private Poly boundary, startShape, goalShape;
	private Poly[] obstacles, grownObstacles, superGrownObstacles;
	private Point startPoint, goalPoint;
	private Point[] shortestPath;
	private Edge[] visibilityGraph;
	private float minX, minY;
	private boolean grown, superGrown, graphComputed, pathComputed;
	
	/**
	 * Creates a new Environment.
	 * @param boundary polygon representing the outer boundary of the environment
	 * @param obstacles all obstacles in the environment
	 * @param startx starting x position
	 * @param starty starting y position
	 * @param goalx x position of goal
	 * @param goaly y position of goal
	 */
	public Environment(Poly boundary, Poly[] obstacles, float startx, float starty, float goalx, float goaly) {
		this.boundary = boundary;
		this.obstacles = obstacles;
		startPoint = new Point(startx, starty);
		goalPoint = new Point(goalx, goaly);
		
		float robotRadius = Poly.ROBOT_DIAMETER / 2.0f;
		float[] startPtsX = {startPoint.x - robotRadius, startPoint.x - robotRadius, startPoint.x + robotRadius, startPoint.x + robotRadius};
		float[] startPtsY = {startPoint.y - robotRadius, startPoint.y + robotRadius, startPoint.y + robotRadius, startPoint.y - robotRadius};
		startShape = new Poly(startPtsX, startPtsY, startPtsX.length, true);
		float[] goalPtsX = {goalPoint.x - robotRadius, goalPoint.x - robotRadius, goalPoint.x + robotRadius, goalPoint.x + robotRadius};
		float[] goalPtsY = {goalPoint.y - robotRadius, goalPoint.y + robotRadius, goalPoint.y + robotRadius, goalPoint.y - robotRadius};
		goalShape = new Poly(goalPtsX, goalPtsY, goalPtsX.length, true);
		
		minX = boundary.getMinX();
		minY = boundary.getMinY();
		
		grown = false;
		graphComputed = false;
		pathComputed = false;
	}
	
	/**
	 * Grows all obstacles by the size of the iRobot Create, for pathfinding purposes.
	 * @param safe if true, grows the obstacles by 1.5x the size of the robot for determining a safer paths
	 */
	public void growObstacles(boolean safe) {
		grownObstacles = new Poly[obstacles.length];
		superGrownObstacles = new Poly[obstacles.length];
		Point[] startPoints = startShape.getPoints().clone();
		float[] sx = new float[startPoints.length];
		for (int i = 0; i < sx.length; i++) sx[i] = startPoints[i].x / 2.0f;
		float[] sy = new float[startPoints.length];
		for (int i = 0; i < sy.length; i++) sy[i] = startPoints[i].y / 2.0f;
		Poly superShape = new Poly(sx, sy, sx.length, true);
		for (int i = 0; i < obstacles.length; i++) {
			grownObstacles[i] = obstacles[i].grow(startShape);
			if (safe) superGrownObstacles[i] = grownObstacles[i].grow(superShape);
		}
		grown = true;
		if (safe) superGrown = true;
		else superGrown = false;
	}
	
	/**
	 * Returns the list of grown obstacles in the environment.
	 * @return a Poly array representing the obstacles after growth
	 */
	public Poly[] getGrownObstacles() {
		return grownObstacles;
	}
	
	/**
	 * Returns the list of obstacles grown with the safe method.
	 * @return a Poly array representing the obstacles after safe growth
	 */
	public Poly[] getSuperGrownObstacles() {
		return superGrownObstacles;
	}
	
	/**
	 * Internally computes the visibility graph of all obstacles, i.e. the set of all
	 * edges between all obstacle vertices that do not intersect any obstacles.
	 * @param obstacles the list of obstacles
	 */
	public void computeVisibilityGraph(Poly[] obstacles) {
		// all points
		ArrayList<Point> allPoints = new ArrayList<Point>();
		allPoints.add(startPoint);
		allPoints.add(goalPoint);
		Point[] currentObstacle;
		for (int i = 0; i < obstacles.length; i++) {
			currentObstacle = obstacles[i].getPoints();
			for (int j = 0; j < currentObstacle.length; j++) {
				allPoints.add(currentObstacle[j]);
			}
		}
		ArrayList<Edge> allEdges = new ArrayList<Edge>();
		for (int i = 0; i < allPoints.size(); i++) {
			for (int j = 0; j < allPoints.size(); j++) {
				allEdges.add((new Edge(allPoints.get(i), allPoints.get(j))));
			}
		}
		
		// this isn't working for some reason
		ArrayList<Edge> validEdges = new ArrayList<Edge>();
		for (int i = 0; i < obstacles.length; i++) {
			for (int j = 0; j < allEdges.size(); j++) {
				if (boundary.intersects(allEdges.get(j))) {
					allEdges.get(j).setValid(false);
				}
				if (obstacles[i].intersects(allEdges.get(j))) {
					allEdges.get(j).setValid(false);
				}
				if (obstacles[i].containsPoint(allEdges.get(j).getMidPoint())) {
					allEdges.get(j).setValid(false);
				}
			}
		}
		for (int i = 0; i < allEdges.size(); i++) {
			if (allEdges.get(i).isValid()) {
				validEdges.add(allEdges.get(i));
			}
		}
		visibilityGraph = validEdges.toArray(new Edge[validEdges.size()]);
		graphComputed = true;
	}
	
	/**
	 * Computes the shortest path from start to goal, represented as an
	 * ordered set of Points.
	 * @return an array of points representing the points in the shortest path
	 */
	public Point[] computeShortestPath() {
		PathFinder pf = new PathFinder(startPoint, goalPoint, visibilityGraph);
		shortestPath = pf.getShortestPath();
		pathComputed = true;
		return shortestPath;
	}
	
	/**
	 * Writes the shortest path to a file as a list of travel distances and
	 * turn angles for the robot.
	 * @param filename the name of the file to write to
	 */
	public void writePathToFile(String filename) {
		ArrayList<Float> data = new ArrayList<Float>();
		float prevAngle = 0.0f, nextAngle = 0.0f, turnAngle = 0.0f, distance = 0.0f;
		for (int i = 1; i < shortestPath.length; i++) {
			nextAngle = Point.computeAngle(shortestPath[i], shortestPath[i-1]);
			turnAngle = nextAngle - prevAngle;
			data.add(-turnAngle);
			prevAngle = nextAngle;
			distance = Point.computeDistance(shortestPath[i], shortestPath[i-1]);
			data.add(distance);
		}
		
		PrintWriter p = null;
		try {
			p = new PrintWriter(new File(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < data.size(); i++) {
			if (i < data.size() - 1) p.println(data.get(i));
			else p.print(data.get(i));
		}
		p.close();
	}
	
	/**
	 * Draws the environment with color coding.
	 * @param g the graphics context
	 * @param scale the scale factor
	 */
	public void draw(Graphics g, float scale) {
		g.setColor(Color.DARK_GRAY);
		boundary.draw(g, scale, minX, minY);
		g.setColor(Color.CYAN);
		for (int i = 0; i < obstacles.length; i++) {
			obstacles[i].draw(g, scale, minX, minY);
		}
		if (graphComputed) {
			g.setColor(Color.GRAY);
			for (int i = 0; i < visibilityGraph.length; i++) {
				visibilityGraph[i].draw(g, scale, minX, minY);
			}
		}
		if (grown) {
			g.setColor(Color.MAGENTA);
			for (int i = 0; i < grownObstacles.length; i++) {
				grownObstacles[i].draw(g, scale, minX, minY);
			}
		if (superGrown) {
			g.setColor(Color.PINK);
			for (int i = 0; i < superGrownObstacles.length; i++) {
				superGrownObstacles[i].draw(g, scale, minX, minY);
			}
		}
		}
		if (pathComputed) {
			g.setColor(Color.YELLOW);
			for (int i = 0; i < shortestPath.length - 1; i++) {
				(new Edge(shortestPath[i], shortestPath[i+1])).draw(g, scale, minX, minY);
			}
		}
		g.setColor(Color.GREEN);
		startShape.draw(g, scale, minX, minY);
		g.setColor(Color.RED);
		goalShape.draw(g, scale, minX, minY);
		g.setColor(Color.WHITE);
		
	}
	
	/**
	 * Creates a new environment from input files.
	 * @param worldFile text file defining the boundary and obstacles
	 * @param startGoalFile text file defining the start and goal points
	 * @return a new Environment object with the given properties
	 */
	public static Environment parseFiles(String worldFile, String startGoalFile) throws Exception {
		// wall and obstacles
		Scanner read = new Scanner(new File(worldFile));
		Poly wall = null;
		Poly[] objects = null;
		int numObjects = Integer.parseInt(read.nextLine());
		objects = new Poly[numObjects - 1];
		for (int i = 0; i < numObjects; i++) {
			int numVerts = Integer.parseInt(read.nextLine());
			//System.out.println("verts: " + numVerts);
			float[] vertsX = new float[numVerts], vertsY = new float[numVerts];
			for (int j = 0; j < numVerts; j++) {
				String[] pointString = read.nextLine().split(" ");
				//System.out.println(Arrays.toString(pointString));
				vertsX[j] = Float.parseFloat(pointString[0]);
				vertsY[j] = Float.parseFloat(pointString[1]);
			}
			if (i == 0) wall = new Poly(vertsX, vertsY, numVerts, false);
			else objects[i - 1] = new Poly(vertsX, vertsY, numVerts, false);
		}
		read.close();
		
		// start and goal points
		read = new Scanner(new File(startGoalFile));
		String[] startString = read.nextLine().split(" ");
		float startx = Float.parseFloat(startString[0]);
		float starty = Float.parseFloat(startString[1]);
		String[] goalString = read.nextLine().split(" ");
		float goalx = Float.parseFloat(goalString[0]);
		float goaly = Float.parseFloat(goalString[1]);
		read.close();
		
		return new Environment(wall, objects, startx, starty, goalx, goaly);
	}
	
}