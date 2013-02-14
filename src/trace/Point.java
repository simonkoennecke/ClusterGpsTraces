package trace;

public class Point implements Comparable<Point>{
	private double _lat;
	private double _lon;
	private double _radLat;
	private double _radLon;
	
	private double _x;
	private double _y;
	
	public Point(double lon, double lat){
		setLat(lat);
		setLon(lon);
		PtOpSphere.translateToXY(this);
	}
	public Point(double x, double y, boolean XY){
		setX(x);
		setY(y);
		PtOpPlane.translateToLatLon(this);
	}
	public double getLat(){
		return _lat;
	}
	public double getLon(){
		return _lon;
	}
	public void setLat(double lat){
		_lat = lat;
		_radLat = Math.toRadians(lat);
	}
	public void setLon(double lon){
		_lon = lon;
		_radLon = Math.toRadians(lon);
	}
	public double getRadLat(){
		return _radLat;
	}
	public double getRadLon(){
		return _radLon;
	}
	
	@Override
	public int compareTo(Point o) {
		int tmp=0;
		if(_lat == o.getLat() && _lon == o.getLon())
			tmp = 0;
		else if(_lat > o.getLat() || _lon > o.getLon())
			tmp = -1;
		else if(_lat < o.getLat() || _lon < o.getLon())
			tmp = 1;
		return tmp;
	}
	public String toString(){
		return "(Lon: "+_lon+", Lat: "+_lat+")";
	}
	public double getX() {
		return _x;
	}
	public void setX(double _x) {
		this._x = _x;
	}
	public double getY() {
		return _y;
	}
	public void setY(double _y) {
		this._y = _y;
	}
}
