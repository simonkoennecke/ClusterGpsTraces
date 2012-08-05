package graph;

import cluster.Cluster;
import processing.core.*;
import trace.*;

public class MainGraph extends PApplet {
	private static final long serialVersionUID = -224150035454082876L;
	private GpxFile gpx;
	int maxWindowWidth = 1024, maxWindowHeight = 800;
	
	Point maxPt, minPt;
	
	float lonFactor, latFactor;
	
	float xFactor, yFactor;
	
	
	public MainGraph(GpxFile _gpx, int w, int h){
		gpx = _gpx;
		setSize(w, h);
	}
	
	public void setup(){
		size(maxWindowWidth,maxWindowHeight);

		frameRate(1);
		smooth();
		noLoop();
	}
	static int windowBorder = 40;
	
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
	public void drawCluster(Cluster c){
		new DrawCluster(this, c).draw();
	}
	public void draw() {
		background(255);
		new DrawTraces(this, gpx).draw();
		translate(20, 20);
	  stroke(color(255,0,0));
	  if (mousePressed) {
	    line(mouseX,mouseY,pmouseX,pmouseY);
	  }
	}
}