import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

/**
 * Represents an edge, or a line segment defined by two points.
 * @author Andrew Goldin
 */
public class Edge {
	
	private final float EPSILON = 0.001f;
	
	public Point p, q;
	private Point pScaled, qScaled;
	private boolean valid;
	
	/**
	 * Creates a new edge with two points.
	 * @param a the first point
	 * @param b the second point
	 */
	public Edge(Point a, Point b) {
		p = new Point(a);
		q = new Point(b);
		valid = true;
	}
	
	/**
	 * Returns the midpoint of the edge.
	 * @return a new Point representing the midpoint of the edge
	 */
	public Point getMidPoint() {
		return new Point((p.x + q.x) / 2.0f, (p.y + q.y) / 2.0f);
	}
	
	/**
	 * Determines whether a given edge intersects this one.
	 * @param other the edge to check for intersection
	 * @return true if the edges intersect, false otherwise
	 */
	public boolean intersects(Edge other) {
		Point p1 = this.p, p2 = this.q;
		Point p3 = other.p, p4 = other.q;
		boolean otherCrossesThis = dir(p1, p2, p3) * dir(p1, p2, p4) < 0.0f;
		boolean thisCrossesOther = dir(p3, p4, p1) * dir(p3, p4, p2) < 0.0f;
		return otherCrossesThis && thisCrossesOther;
	}
	
	/**
	 * Determines if a given point lies somewhere on the edge segment.
	 * @param c the Point to check
	 * @return true if the edge contains the given point, false otherwise
	 */
	public boolean containsPoint(Point c) {
		if (c.equals(p) || c.equals(q)) return false;
		float cross = (c.y - p.y) * (q.x - p.x) - (c.x - p.x) * (q.y - p.y);
		if (Math.abs(cross) > EPSILON) return false;
		float dot = (c.x - p.x) * (q.x - p.x) + (c.y - p.y) * (q.y - p.y);
		if (dot < 0.0f) return false;
		float sqlenqp = (q.x - p.x) * (q.x - p.x) + (q.y - p.y) * (q.y - p.y);
		if (dot > sqlenqp) return false;
		return true;
	}
	
	// right if less than 0, left if greater
	private float dir(Point p1, Point p2, Point p3) {
		float ux = -(p2.x - p1.x);
		float uy = p2.y - p1.y;
		float vx = -(p3.x - p1.x);
		float vy = p3.y - p1.y;
		return ux * vy - uy * vx;
	}
	
	/**
	 * Draws the edge on the screen.
	 * @param g the graphics context
	 * @param scale the scale factor
	 * @param offsetX pixel offset on x axis
	 * @param offsetY pixel offset on y axis
	 */
	public void draw(Graphics g, float scale, float offsetX, float offsetY) {
		Graphics2D g2 = (Graphics2D) g;
		pScaled = new Point((p.x - offsetX) * scale + RoboPath.PIXEL_OFFSET_X, (p.y - offsetY) * scale + RoboPath.PIXEL_OFFSET_Y);
		qScaled = new Point((q.x - offsetX) * scale + RoboPath.PIXEL_OFFSET_X, (q.y - offsetY) * scale + RoboPath.PIXEL_OFFSET_Y);
		g2.draw(new Line2D.Float(pScaled, qScaled));
	}
	
	/**
	 * Sets the validity of the edge for vgraph purposes.
	 * @param b true to set the edge to valid, false to invalidate
	 */
	public void setValid(boolean b) {
		valid = b;
	}
	
	/**
	 * Returns the validity of the edge, for vgraph purposes.
	 * @return true if edge is valid, false otherwise
	 */
	public boolean isValid() {
		return valid;
	}
	
	/**
	 * Determines if this edge is equal to another.
	 * @param other the other Edge
	 * @return true if both edges contain the same endpoints, false otherwise
	 */
	public boolean equals(Edge other) {
		return this.p.equals(other.p) && this.q.equals(other.q);
	}
	
}