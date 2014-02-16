import java.awt.geom.Point2D;

/**
 * A 2D Point using floats.
 * @author Andrew Goldin
 */
public class Point extends Point2D.Float implements Comparable<Point> {
	
	private static final long serialVersionUID = 1L;
	
	private float angle, distance;
	
	/**
	 * Constructs a new point.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */
	public Point(float x, float y) {
		super(x, y);
		angle = 0.0f;
		distance = 0.0f;
	}
	
	/**
	 * Copy constructor.
	 * @param other the Point to copy
	 */
	public Point(Point other) {
		super(other.x, other.y);
		angle = other.getAngle();
		distance = other.getDistance();
	}
	
	/**
	 * Sets the Point's angle and distance relative to another
	 * point (for sorting purposes and convex hull computation)
	 * @param other the other Point
	 */
	public void setAngleAndDistance(Point other) {
		angle = (float) Math.toDegrees(Math.atan2(this.y - other.y, this.x - other.x));
		distance = (float) Math.sqrt((this.x - other.x) * (this.x - other.x) + (this.y - other.y) * (this.y - other.y));
	}
	
	/**
	 * Sets the angle of the point, relative to an arbitrary point.
	 * @param a the angle, in degrees
	 */
	public void setAngle(float a) {
		angle = a;
	}
	
	/**
	 * Sets the distance of the point, relative to an arbitrary point.
	 * @param d the distance
	 */
	public void setDistance(float d) {
		distance = d;
	}
	
	/**
	 * Returns the angle of the point.
	 * @return the angle
	 */
	public float getAngle() {
		return angle;
	}
	
	/**
	 * Returns the distance of the point.
	 * @return the distance
	 */
	public float getDistance() {
		return distance;
	}
	
	/**
	 * Computes the distance between two points.
	 * @param a the first point
	 * @param b the second point
	 * @return the distance between Points a and b
	 */
	public static float computeDistance(Point a, Point b) {
		return (float) Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
	}
	
	/**
	 * Computes the angle between two points.
	 * @param a the first point
	 * @param b the second point
	 * @return the angle between Points a and b
	 */
	public static float computeAngle(Point a, Point b) {
		return (float) Math.toDegrees(Math.atan2(a.y - b.y, a.x - b.x));
	}
	
	/**
	 * Compares points by angle and distance.
	 * @return positive if this angle is larger than other's angle, or the angles
	 * are the same but this distance is larger than other's distance. Returns
	 * negative if this angle is smaller than other's angle, or the angles are the
	 * same but this distance is smaller than the other's distance. Zero if equal.
	 */
	public int compareTo(Point other) {
		if (this.angle > other.angle) return 1;
		else if (this.angle < other.angle) return -1;
		else if (this.distance > other.distance) return 1;
		else if (this.distance < other.distance) return -1;
		else return 0;
	}
	
}