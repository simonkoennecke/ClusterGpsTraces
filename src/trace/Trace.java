package trace;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import java.awt.geom.Point2D;

public class Trace extends IterInterface<Point> implements Iterable<Point>, Cloneable{
	/**
	 * Versionsnummer Manager Klasse 
	 */
	private static AtomicInteger vIdMgnt = new AtomicInteger();
	/**
	 * Versionsnummer zu der die Klasse gehört
	 */
	private Integer vId;
	
	/**
	 * Name des Traces aus dem GPX File
	 */
	private String name;
	/**
	 * Eine Liste von Punkten die Reihenfolge gibt die Richtung an.
	 */
	private ArrayList<Point> trace = new ArrayList<Point>();
	/**
	 * 
	 */
	private Point2D[] aryTracePoints; 
	/**
	 * In subTraces werden die Teilstrecken von Trace abgespeichert. 
	 */
	private Traces subTraces = new Traces();
	/**
	 * hier wird die maximale lon oder lat gespeichert
	 */
	private Point maxPt = new Point(0,0), minPt = new Point(180,180);
	/**
	 * 
	 * traversierung=true von 0 bis n und bei false n bis 0
	 */
	private boolean displayOnScreen = true, traversierung=true;
	
	
	public Trace(){
		this("",null);
	}

	public Trace(String _name){
		this(_name,null);
	}
	
	public Trace(String _name, Integer _vId){
		name = _name;
		if(_vId == null)
			_vId = vIdMgnt.getAndIncrement();
		setVersionId(_vId);
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
		Iter<Trace, Point> iter = new Iter<Trace, Point>(this, traversierung);
		return iter;
	}
	
	public double getDistance(){
		double d = 0;
		if(size() < 2){
			return d;
		}
		Point p = trace.get(0);
		for(Point pt : this){
			d += PtOpSphere.distance(p, pt);
			p = pt;
		}		
		return d;
	}
	/**
	 * Ermittelt die minimale Distanz die zwischen zwei Punkten auf der Spur bestehen kann
	 * @return Minimale Distanz in Meter
	 */
	public double getMinDistance(){
		double d = Double.MAX_VALUE;
		Point p = null;
		for(Point pt : this){
			if(p != null)
				d = Math.min(d, PtOpPlane.distance(p, pt));
			p = pt;
		}		
		return d;
	}
	public double getMeanCardinalDirection(){
		double d = 0;
		Point p = null;
		for(Point pt : this){
			if(p != null)
				d += PtOpSphere.cardinalDirection(p, pt);
			p = pt;
		}
		d = d / trace.size();
		return d;
	}

	public boolean isDisplay() {
		return displayOnScreen;
	}

	public void setDisplay(boolean displayOnScreen) {
		this.displayOnScreen = displayOnScreen;
	}

	public Traces getSubTraces() {
		return subTraces;
	}
	public Trace addSubTraces(Integer _vId) {
		return this.subTraces.addTrace("Sub Trace " + name, _vId);
	}
	public Trace addSubTraces() {
		return this.subTraces.addTrace("Sub Trace " + name, getVersionId());
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
	public void setTraversierung(boolean traversierung){
		this.traversierung = traversierung;
	}
	public boolean getTraversierung(){
		return traversierung;
	}
	
	public String toString(){
		final DecimalFormat df =   new DecimalFormat  ( "###0.00" );
		final DecimalFormat d =   new DecimalFormat  ( "000" );
		return "Trace (" + d.format(vId) + " , " + d.format(subTraces.size()) + ", " + d.format(trace.size()) + ", " +  df.format(getDistance())  + ")";
	}
	
	public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

	public Integer getVersionId() {
		return vId;
	}

	public void setVersionId(Integer vId) {
		if(vId == null)
			this.vId = vIdMgnt.getAndIncrement();
		else
			this.vId = vId;
		
	}
	public static Integer getIncrementVersionId(){
		return vIdMgnt.getAndIncrement();
	}
	public static Integer getDecrementVersionId(){
		return vIdMgnt.getAndDecrement();
	}

	public Point2D[] getPoints() {
		if(aryTracePoints == null && trace.size() > 0){
			aryTracePoints = new Point2D[trace.size()];
			
			int i = 0;
	        for(Point pt: trace){
	        	aryTracePoints[i] = new Point2D.Double(pt.getLon(), pt.getLat());
	        	i++;
	        }
	        this.setPoints(aryTracePoints);
		}
		return aryTracePoints;
	}

	public void setPoints(Point2D[] aryTracePoints) {		
		this.aryTracePoints = aryTracePoints;
	}	
}
