RoboPathFinder
==============

A pathfinding system for the iRobot Create

Instructions to run the program:

Compile everything:

	javac *.java
	
Run RoboPath.java, with the map and start/goal text files as
command line arguments (map first, then start/goal positions):

	java RoboPath maps/hw3_world_obstacles_convex.txt maps/hw3_start_goal.txt
	
The program reads in the provided text files and uses them to construct the map.
The X and Y axes are inverted in the display (right is positive x, down is positive y).

Press buttons in one of two orders:

	1. Grow Obstacles -> Compute VGraph -> Compute Shortest Path
	
		- This option will grow the obstacles by the size of the robot and
		compute the visibility graph and shortest path as normal, using
		Dijkstra's algorithm.
	
	2. Grow Safe -> Compute Safe VGraph -> Compute Shortest Path
	
		- This option plays it a bit safer and grows the obstacles by 1.5
		times the size of the robot to try to ensure a collision-free path.

Once the shortest path is displayed, type a filename into the text field
and click "Write Path To File" to save the path to a text file as a list
of alternating turn angles and travel distances. Our MATLAB function reads
in this file and alternates between calls to turnAngle and travelDist to
move the robot. With instructor permission, our group would like to try
both paths (normal and safe) for the race.

We have also provided screenshots that show each step of the GUI in action.