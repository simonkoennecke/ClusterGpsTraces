package graph;

import java.awt.Dimension;
import java.util.List;

import merg.Grid;

import cluster.Cluster;
import processing.core.*;
import trace.*;

public class MainGraph extends PApplet {
	private static final long serialVersionUID = -224150035454082876L;
	
	private GpxFile gpx;
	
	private Cluster cluster;
	
	private int maxWindowWidth = 1024, maxWindowHeight = 800;
	
	private Point maxPt, minPt;
	
	private float lonFactor, latFactor;
	
	private float xFactor, yFactor;
	
	private static int windowBorder = 40;
	
	private enum paintModeOption {Traces, Cluster};
	
	private paintModeOption paintMode = paintModeOption.Traces;
	
	private boolean paintIntersections = false, paintGrid = false;
	
	private List<FindTraceIntersections.Circle> intersections;
	private Grid grid;
	
	public MainGraph(GpxFile _gpx, int w, int h){
		gpx = _gpx;
		setSize(w, h);
	}
	public void setGpxFile(GpxFile _gpx){
		gpx = _gpx;
		setSize(maxWindowHeight + windowBorder, maxWindowWidth + windowBorder);;
	    setup(); 
	    redraw();
		
	}
	public void setup(){
		size(maxWindowWidth,maxWindowHeight);

		frameRate(1);
		smooth();
		noLoop();
	}
	
	
	public void setSize(int w, int h){
		maxWindowHeight = h - windowBorder;
		maxWindowWidth = w - windowBorder;
		gpx.getTraces().calcExtrema();
		maxPt = gpx.getTraces().getMax();
		minPt = gpx.getTraces().getMin();
		Double tmp;
		
		tmp = (maxWindowWidth/ (maxPt.getLon() - minPt.getLon()));
		lonFactor =  tmp.floatValue();
		tmp = maxWindowHeight / (maxPt.getLat() - minPt.getLat());
		latFactor = tmp.floatValue();
		  
		tmp = (maxWindowWidth/ (maxPt.getX() - minPt.getX()));
		xFactor =  tmp.floatValue();
		tmp = maxWindowHeight / (maxPt.getY() - minPt.getY());
		yFactor = tmp.floatValue();
  
		resize(w, h);
	}
	
	public float lon(Point pt){
		//Double x = (pt.getLon()-minPt.getLon()) * lonFactor;
		//(return x.floatValue()+(windowBorder/2);
		return lon((float)pt.getLon());
	}
	public float lon(float x){
		x = (x - (float) minPt.getLon()) * lonFactor;
		return x+(windowBorder/2);
	}
	public float lat(Point pt){
		//Double y = (pt.getLat()-minPt.getLat()) * latFactor;
		//return y.floatValue()+(windowBorder/2);
		return lat((float)pt.getLat());
	}
	public float lat(float y){
		y = (y-(float)minPt.getLat()) * latFactor;
		return y+(windowBorder/2);
	}
	public float x(Point pt){
		Double x = (pt.getX()-minPt.getX()) * xFactor;
		return x.floatValue()+(windowBorder/2);
	}
	public float y(Point pt){
		Double y = (pt.getY()-minPt.getY()) * yFactor;
		return y.floatValue()+(windowBorder/2);
	}
	public void setCluster(Cluster c){
		cluster = c;
		paintMode = paintModeOption.Cluster;
		
	}
	public void setIntersections(List<FindTraceIntersections.Circle> c){
		intersections = c;
		paintIntersections = true;		
	}
	public List<FindTraceIntersections.Circle> getIntersections(){
		return intersections;		
	}
	public void draw() {
		Dimension dWindow = getSize();
		if(!(dWindow.width == maxWindowWidth && dWindow.height == maxWindowHeight))
			setSize(dWindow.width,dWindow.height);
		background(255);
		if(paintMode == paintModeOption.Traces)
			new DrawTraces(this, gpx).draw();
		else if(paintMode == paintModeOption.Cluster)
			new DrawCluster(this, cluster).draw();
		if(paintGrid){
			new GridGraph(this, grid).draw();		
		}
		if(paintIntersections){
			this.ellipseMode(CENTER);
			strokeWeight(3);
			this.stroke(this.color(255,0,0,255));
			this.fill(this.color(255,0,255));
			this.noFill();
			for(FindTraceIntersections.Circle c : intersections){
				if(!c.isOverlapping){
					float d = (float) (this.lon(new Point(c.pt.getLon()-c.r, c.pt.getLat())) - this.lon(new Point(c.pt.getLon()+c.r, c.pt.getLat())));
					d = Math.abs(d);
					d = d * 0.5f;
					this.ellipse(this.lon(c.pt), this.lat(c.pt), d,d);
				}
			}
			strokeWeight(1);
			this.stroke(this.color(0,0,0,255));
			this.fill(this.color(255,0,255));
			for(FindTraceIntersections.Circle c : intersections){
				if(c.isOverlapping){
					this.ellipse(this.lon(c.pt), this.lat(c.pt), 3,3);
				}
			}
		}
		
		//stroke(color(255,0,0));
	}
	public void dLine(Point p1, Point p2){
		this.line(this.lon(p1), this.lat(p1), this.lon(p2), this.lat(p2));
	}
	public void dLine(float p1Lon, float p1Lat, float p2Lon, float p2Lat){
		this.line(this.lon(p1Lon), this.lat(p1Lat), this.lon(p2Lon), this.lat(p2Lat));
	}
	public Grid getGrid() {
		return grid;
	}
	public void setGrid(Grid grid) {
		this.paintGrid = true;
		this.grid = grid;
	}
	public paintModeOption getPaintMode() {
		return paintMode;
	}
	public void setPaintMode(paintModeOption paintMode) {
		this.paintMode = paintMode;
	}
	public boolean isPaintIntersections() {
		return paintIntersections;
	}
	public void setPaintIntersections(boolean paintIntersections) {
		this.paintIntersections = paintIntersections;
	}
	public boolean isPaintGrid() {
		return paintGrid;
	}
	public void setPaintGrid(boolean paintGrid) {
		this.paintGrid = paintGrid;
	}
	
}