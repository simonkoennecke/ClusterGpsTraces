package trace;

import java.util.ArrayList;
import java.util.Iterator;

public class Trace extends IterInterface<Point> implements Iterable<Point>{
	/**
	 * Name des Traces aus dem GPX File
	 */
	private String name;
	/**
	 * Eine Liste von Punkten die Reihnfolge gibt die Richtung an.
	 */
	private ArrayList<Point> trace = new ArrayList<Point>();
	/**
	 * In subTraces werden die Teilstrecken von Trace abgespeichert. 
	 */
	private Traces subTraces = new Traces();
	//hier wird die maximale lon oder lat gespeichert
	private Point maxPt = new Point(0,0);
	private Point minPt = new Point(180,180);
	private boolean displayOnScreen = true;
	
	Trace(){
		
	}
	Trace(String _name){
		name = _name;
	}
	
	public Point get(int index){
		return trace.get(index);
	}
	public int size(){
		return trace.size();
	}
	
	public Point addPoint(double lon, double lat){
		Point pt = new Point(lon, lat);		
		return addPoint(pt);
	}
	public Point addPoint(Point pt){
		trace.add(pt);
		PtOpSphere.checkExtrema(pt, maxPt, minPt);
		PtOpPlane.checkExtrema(pt, maxPt, minPt);
		return pt;
	}
	
	public Point getMaxPt(){
		return maxPt;
	}
	public Point getMinPt(){
		return minPt;
	}
	
	

	@Override
	public Iterator<Point> iterator() {
		Iter<Trace, Point> iter = new Iter<Trace, Point>(this);
		return iter;
	}
	
	public double getDistance(){
		double d = 0;
		Point p = trace.get(0);
		for(Point pt : this){
			d += PtOpPlane.distance(p, pt);
			p = pt;
		}		
		return d;
	}

	public boolean getDisplay() {
		return displayOnScreen;
	}

	public void setDisplay(boolean displayOnScreen) {
		this.displayOnScreen = displayOnScreen;
	}

	public Traces getSubTraces() {
		return subTraces;
	}
	public Trace addSubTraces() {
		return this.subTraces.addTrace();
	}
	public Trace addSubTraces(Trace subTraces) {
		return this.subTraces.addTrace(subTraces);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String toString(){
		return "Trace (" + name + " , " + subTraces.size() + ", " + trace.size() + ")";
	}

}
