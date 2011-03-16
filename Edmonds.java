import java.util.Collections;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Stack;

/*
 * An implementation of the staircase algorithm according to Edmonds et al. 
 * Given a set of data points, it finds all maximal rectangles in O(nm) time.
 * 
 * @author Emanuel Ferm
 */
public class Edmonds {
	private static final int CONTAINER_WIDTH = 100;
	private static final int CONTAINER_HEIGHT = 100;
	
	/*
	 * The main loop, going through all data values once.
	 * 
	 * @param points, a list of points
	 */
	public static List<Rectangle> staircase(List<Point> points) {

		List<Rectangle> maxRects = new LinkedList<Rectangle>();
		
		int width = CONTAINER_WIDTH;
		int height = CONTAINER_HEIGHT;

		// dummy point outside container, to iterate over the bottom row
		points.add(new Point(0,height));
		ListIterator<Point> iterator = points.listIterator();
		
		int[] yr = new int[width+1];

		Stack<Point> staircase = new Stack<Point>();
		Point previous = new Point(-1,-1);
		
		while (iterator.hasNext()) {
			Point point = iterator.next();
			
			// skip a data point if it's a duplicate of the previous one
			if (point.equals(previous) && iterator.hasNext()) {
				previous = point;
				continue;
			}
			
			int x = 0;
			yr[width] = point.getY()-1; // shift right-most boundary point
			staircase.clear(); // assert empty staircase
			
			// loop through each non-zero place in the row, i.e. previous 
			// data points' yr-values and thus candidates for staircases
			for (int i = 0; i < yr.length; i++) {
				
				// Consider each projection of a data point above current row.
				// Don't consider multiple data points on the row, i.e. skip 
				// points to the right if the row has already been considered. 
				// Otherwise the stack is fucked up.
				if (yr[i] != 0 && yr[i] != point.getY()) {
					
					// if point on last row, continuously shift it to i-1 pos
					if (point.getY() == height) {
						// replace the dummy-point as right-most possible xstar
						points.set(points.size()-1,new Point(i-1,point.getY()));
						point = points.get(points.size()-1);
					}
					
					staircase.push(new Point(x,0));
					
					// maximal rectangles can only occur when they are 
					// bounded by a data point in the y+1 row
					if (i >= point.getX()) {
						int xstar = point.getX();
						int ystar = yr[i];
						Point bottomright = new Point(i, point.getY());
						x = getMaximal(maxRects, staircase, yr, xstar, ystar, 
								bottomright, height);
					}
					
					Point p;
					// if the new top step is lower than (or equal to) the 
					// old top step, remove them and save the x-position
					while (!staircase.empty() && yr[i] >= 
								(p = staircase.peek()).getY()) {
						staircase.pop();
						x = p.getX();
					}
					
					// put the new top step in place, with the last popped 
					// x-value and the new y-value.
					staircase.push(new Point(x,yr[i]));
					x = i;
				}
				
				// even if multiple data points on the same row, there might be 
				// y[i] values to push on the stack
				else if (yr[i] != 0 && yr[i] == point.getY()) {
					staircase.push(new Point(x,yr[i]));
					x = i;
				}
			}
			yr[point.getX()] = point.getY();
			previous = point;
		}
			
		points.remove(points.size()-1);
		Collections.sort(maxRects); // optional extra operation
		return maxRects;
	}
	
	/*
	 * Takes a staircase, extracts all maximal rectangles and then stores them 
	 * in the given data structure. Returns the last popped off x-value.
	 * 
	 * @param maxRects, the data structure to store the rectangles in
	 * @param staircase, the staircase
	 * @param xstar, the bounding x-value as proposed in Edmonds et al.
	 * @param ystar, the bounding y-value as proposed in Edmonds et al.
	 * @param bottomright, the identifier for the staircase
	 * @param bottom, the bottom-most y-coordinate
	 * @return the last popped off x-value
	 */
	private static int getMaximal(List<Rectangle> maxRects, 
				Stack<Point> staircase, int[] yr, int xstar, int ystar, 
				Point bottomright, int bottom) {
		
		int xi = 0;
		int yi = 0;
		
		// leave lowest stairs, >= ystar, since they belong to staircase(x+1,y)
		while (!staircase.empty() && staircase.peek().getY() < ystar) {
			Point p = staircase.pop();
			xi = p.getX();
			yi = p.getY();
			
			// allow "thin" rectangles from bottom row
			if (xstar == xi && bottomright.getY() == bottom)
				xstar+=2; // so that xstar is > xi AND not == bottomright
			
			if (xi < xstar && yi < ystar && xstar != bottomright.getX()){
				int width = bottomright.getX() - xi;
				int height = bottomright.getY() - yi;
				
				maxRects.add(new Rectangle(p, width, height));
			}
		}
		return xi;
	}
}

/**
 * Suggested interface for a Point.
 * @author Emanuel Ferm
 */
abstract class Point {
	private int x;
	private int y;
	
	Point() {
		this(0,0);
	}
	
	Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	Point(Point p) {
		this(p.x, p.y);
	}
	
	int getX() {
		return x;
	}
	
	int getY() {
		return y;
	}
	
	@Override
	abstract public boolean equals(Object o);
}

/**
 * Suggested interface for a rectangle.
 * @author Emanuel Ferm
 */
abstract class Rectangle implements Comparable<Rectangle> {
	private Point p;
	private int width;
	private int height;
	
	Rectangle(int width, int height) {
		this(new Point(), width, height);
	}
	
	Rectangle(Point p, int width, int height) {
		this.p = p;
		this.width = width;
		this.height = height;
	}
	
	@Override
	abstract public int compareTo(Rectangle r);
}
