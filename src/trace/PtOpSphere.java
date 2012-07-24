package trace;

import java.lang.Math;

/**
 * Diese Punkt Operation beziehen sich auf die sphärische Koordinaten.
 * Wir gehen von aus das trace.Point Lan und Lat angaben sind.
 * @author Simon
 */
public class PtOpSphere {
	public static double earth_radius = 6378137; //in Meter
	public static double earth_perimeter = 40000; //in Kilometer
	/**
	 * Calculate the angle between two points
	 * @param p1 Point on a surface of a sphere
	 * @param p2 Point on a surface of a sphere
	 * @return Angle from p1 and p2 on surface of a sphere in Radians
	 */
	public static double angle(Point p1, Point p2){
		return Math.acos(
				Math.sin(p1.getRadLat()) * Math.sin(p2.getRadLat()) + 
				Math.cos(p1.getRadLat()) * Math.cos(p2.getRadLat()) * 
				Math.cos(p2.getRadLon() -  p1.getRadLon())
			);
			
	}
	/**
	 * Calculate the distance from one point to anther on surface of a sphere (and no ellipsoids!)
	 * Formel: dist = 6378.388 * acos(sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2) * cos(lon2 - lon1))
	 * @param p1 Point on a surface of a sphere
	 * @param p2 Point on a surface of a sphere
	 * @return Distance from p1 to p2 on surface of a sphere in Meter
	 */
	public static double distance(Point p1, Point p2){
		return earth_radius * angle(p1,p2);
	}
	/**
	 * Calculate the cardinal Direction to North
	 * Formel: ATAN2(COS(lat1)*SIN(lat2)-SIN(lat1)*COS(lat2)*COS(lon2-lon1), SIN(lon2-lon1)*COS(lat2))
	 * @param p1 Point on a surface of a sphere
	 * @param p2 Point on a surface of a sphere
	 * @return The cardinal Direction (Himmelrichtung) to North in Radians
	 */
	public static double cardinalDirection(Point p1, Point p2){
		/*Quelle: http://de.wikipedia.org/wiki/Kurswinkel
		double e = angle(p1, p2);
		return Math.acos(
						(Math.sin(p2.getRadLon()) - (Math.sin(p1.getRadLon())*Math.cos(e)) )/
						(Math.cos(p1.getRadLon())*Math.sin(e))
						);
		*/
		//Quelle: http://www.mennobieringa.nl/downloads/class-geo-php-gps-calculations-made-easy/
		double dLon = p2.getRadLon()-p1.getRadLon();
		double dPhi = Math.log(Math.tan(p2.getRadLat()/2+Math.PI/4)/Math.tan(p1.getRadLat()/2+Math.PI/4));
		if (Math.abs(dLon) > Math.PI) 
			dLon = (dLon>0 )? -(2*Math.PI-dLon) : (2*Math.PI+dLon);
		double brng = Math.atan2(dLon, dPhi);
		return (Math.toDegrees(brng)+360) % 360;
	}
	/**
	 * Helper Function to find the min lat/lon and max lat/lon.
	 * @param pt Point to check if one Coordinate Max or Min then the maxPt or minPt
	 * @param maxPt save the max Lon or Lat Coordinate
	 * @param minPt save the min Lon or Lat Coordinate
	 */
	public static void checkExtrema(Point pt, Point maxPt, Point minPt){
		if(pt.getLat() > maxPt.getLat())
			maxPt.setLat(pt.getLat());
		if(pt.getLon() > maxPt.getLon())
			maxPt.setLon(pt.getLon());
		if(pt.getLat() < minPt.getLat())
			minPt.setLat(pt.getLat());
		if(pt.getLon() < minPt.getLon())
			minPt.setLon(pt.getLon());
	}
	
	public static double dotProduct(Point p1, Point p2){
		return p1.getLat()*p2.getLat()+p1.getLon()*p2.getLon();
	}
	public static double crossProduct(Point p1, Point p2){
		return p1.getLat()*p2.getLon()-p1.getLon()*p2.getLat();
	}
	public static double crs(Point p1, Point p2){
		/*http://williams.best.vwh.net/avform.htm#Crs*/
		return Math.atan2(
						Math.sin(p1.getRadLon()-p2.getRadLon())*Math.cos(p2.getRadLat()),
						Math.cos(p1.getRadLat())*Math.sin(p2.getRadLat())-Math.sin(p1.getRadLat())*Math.cos(p2.getRadLat())*Math.cos(p1.getRadLon()-p2.getRadLon()))
					% 2*Math.PI;
	}
	public static double asin(double x){
		//asin(x)=2*atan(x/(1+sqrt(1-x*x)))
		return  2*Math.atan(x/(1+Math.sqrt(1-x*x)));
	}
	public static double acos(double x){
		if(x >= 0)
			return 2*Math.atan(Math.sqrt((1-x)/(1+x))); 
		else
			return Math.PI - 2*Math.atan(Math.sqrt((1+x)/(1-x)));
	}
	public static double dist(Point p1, Point p2){
		//d=2*asin(sqrt((sin((lat1-lat2)/2))^2 + cos(lat1)*cos(lat2)*(sin((lon1-lon2)/2))^2))
		return 2*asin(
				Math.sqrt(
						Math.pow((Math.sin(p1.getRadLat()-p2.getRadLat())/2),2)
						) 
						+ 
						Math.cos(p1.getRadLat())*Math.cos(p2.getRadLat())*
						Math.pow((Math.sin((p1.getRadLon()-p2.getRadLon())/2)),2));
	}
	public static double alongTrackDist(Point p1, Point p2, Point pt){
		/*http://williams.best.vwh.net/avform.htm#XTE*/
		double XTD = asin(Math.sin(dist(p1,pt))*Math.sin(crs(p1,pt)-crs(p1,p2)));
		return earth_radius * XTD;// asin(Math.sqrt( Math.pow((Math.sin(dist(p1,pt))),2) - Math.pow((Math.sin(XTD)),2) )/Math.cos(XTD));
	}
	public static void translateToXY(Point point) {
		//x = r * sin(lat) * cos(lon)
		point.setX(earth_radius * Math.cos(point.getLon()) * Math.sin(point.getLat()));
		//y = r * sin(lon) sin(lat)
		point.setY(earth_radius * Math.sin(point.getLat()) * Math.sin(point.getLon()));
	}
	
}
