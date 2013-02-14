package graph;

import java.util.List;

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
	
	private boolean paintIntersections = false;
	private List<Point> intersections;
	
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
		Double x = (pt.getLon()-minPt.getLon()) * lonFactor;
		return x.floatValue()+(windowBorder/2);
	}
	public float lat(Point pt){
		Double y = (pt.getLat()-minPt.getLat()) * latFactor;
		return y.floatValue()+(windowBorder/2);
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
	public void setIntersections(List<Point> c){
		intersections = c;
		paintIntersections = true;		
	}
	public List<Point> getIntersections(){
		return intersections;		
	}
	public void draw() {
		background(255);
		if(paintMode == paintModeOption.Traces)
			new DrawTraces(this, gpx).draw();
		else if(paintMode == paintModeOption.Cluster)
			new DrawCluster(this, cluster).draw();
		if(paintIntersections){
			this.stroke(this.color(0,0,0,255));
			this.fill(this.color(255,0,255));
			for(Point p1 : intersections)
				this.rect(this.lon(p1), this.lat(p1), 2, 2);
		}
			
		//stroke(color(255,0,0));
	}
}