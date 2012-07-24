package trace;

import java.lang.Math;

/**
 * Diese Klasse geht von kartesische Koordinaten aus.
 * Dabei werden vorausgesetzt, dass die Klasse Trace.Point x und y Koordianten vorweisst. 
 * @author Simon
 *
 */
public class PtOpPlane {
	public static double earth_radius = 6378137; //in Meter
	public static double earth_perimeter = 40000; //in Kilometer
	/**
	 * Calculate the angle between two points
	 * @param p1 Point on a surface of a plain
	 * @param p2 Point on a surface of a plain
	 * @return Angle from p1 and p2 on surface of a plain in Radians
	 */
	public static double angle(Point p1, Point p2){
		//a = atan ( (y2 - y1) / (x2 - x1) )
		return Math.atan(
				(p2.getY() - p1.getY()) /
				(p2.getX() - p1.getX())
			);
			
	}
	/**
	 * Calculate the distance from one point to anther on surface of a plain
	 * Formel: dist = \sqrt( (p1.x - p2.x)^2 + (p1.y - p2.y)^2)
	 * @param p1 Point on a surface of a plain
	 * @param p2 Point on a surface of a plain
	 * @return Distance from p1 to p2 on surface of a plain in Meter
	 */
	public static double distance(Point p1, Point p2){
		return Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p2.getY() - p1.getY(), 2));
	}
	
	/**
	 * Calculate the cardinal Direction to North
	 * Formel: ATAN2(COS(lat1)*SIN(lat2)-SIN(lat1)*COS(lat2)*COS(lon2-lon1), SIN(lon2-lon1)*COS(lat2))
	 * @param p1 Point on a surface of a sphere
	 * @param p2 Point on a surface of a sphere
	 * @return The cardinal Direction (Himmelrichtung) to North in Radians
	 */
	public static double cardinalDirection(Point p1, Point p2){
		return PtOpSphere.cardinalDirection(p1, p2);
	}
	/**
	 * Helper Function to find the min x/y and max x/y.
	 * @param pt Point to check if one Coordinate Max or Min then the maxPt or minPt
	 * @param maxPt save the max x or y Coordinate
	 * @param minPt save the min x or y Coordinate
	 */
	public static void checkExtrema(Point pt, Point maxPt, Point minPt){
		if(pt.getX() > maxPt.getX())
			maxPt.setX(pt.getX());
		if(pt.getY() > maxPt.getY())
			maxPt.setY(pt.getY());
		if(pt.getX() < minPt.getX())
			minPt.setX(pt.getX());
		if(pt.getY() < minPt.getY())
			minPt.setY(pt.getY());
	}
	
	public static double dotProduct(Point p1, Point p2){
		return p1.getX()*p2.getX()+p1.getY()*p2.getY();
	}
	public static double crossProduct(Point p1, Point p2){
		return p1.getX()*p2.getY()-p1.getY()*p2.getX();
	}
	
	/**
	 * Die Distanz von pt zu der line p1 und p2
	 * @param p1
	 * @param p2
	 * @param pt
	 * @return Höhe vom Dreieck in Meter
	 */
	public static double alongTrackDist(Point p1, Point p2, Point pt){
		    //Area = |(1/2)(x1y2 + x2y3 + x3y1 - x2y1 - x3y2 - x1y3)|   *Area of triangle
		    //Base = sqrt((x1-x2)²+(x1-x2)²)                               *Base of Triangle*
		    //Area = .5*Base*H                                          *Solve for height
		    //Height = Area/.5/Base
		    double area = Math.abs(.5 * 
		    		(p1.getX() * p2.getY() + p2.getX() * pt.getY() + pt.getX() * p1.getY() 
		    	   - p2.getX() * p1.getY() - pt.getX() * p2.getY() - p1.getX() * pt.getY()));
		    double bottom = Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2));
		    double height = (area / bottom) * 2;
		    return height;
		    
	}
	public static void translateToLatLon(Point point) {
		//lon == arctan2(x,y);
		point.setLon(Math.atan2(point.getY(), point.getX()));
		//z = r * cos(lat)
		//lat = PI / 2 - arctan(z/sqrt(x^2+y^2))
		point.setLat(Math.PI / 2 - Math.atan((earth_radius * Math.cos(point.getLat())) / (Math.sqrt(Math.pow(point.getX(),2)+Math.pow(point.getY(),2))) ));
	}
	
}
