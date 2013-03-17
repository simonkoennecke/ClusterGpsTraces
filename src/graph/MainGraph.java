package graph;

import java.awt.Dimension;
import java.util.List;

import core.Debug;

import merg.Grid;

import cluster.Cluster;
import processing.core.*;
import trace.*;

public class MainGraph extends PApplet {
	private static final long serialVersionUID = -224150035454082876L;
	
	private GpxFile gpx;
	
	private int maxWindowWidth = 1024, maxWindowHeight = 800;
	
	private Point maxPt, minPt;
	
	private float lonFactor, latFactor;
	
	private float xFactor, yFactor;
	
	private static int windowBorder = 40;
	
	public static enum paintModeOption {Traces, Cluster};
	
	private paintModeOption paintMode = paintModeOption.Traces;
	
	private boolean paintIntersections = false, paintGrid = false;
	
	private List<FindTraceIntersections.Box> intersections;
	
	private GridGraph gridGraph = new GridGraph(this);
	
	private DrawTraces drawTraces = new DrawTraces(this);
	
	private DrawCluster drawCluster = new DrawCluster(this);
	
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
	
	public void setIntersections(List<FindTraceIntersections.Box> c){
		intersections = c;
		paintIntersections = true;		
	}
	public List<FindTraceIntersections.Box> getIntersections(){
		return intersections;		
	}
	public void draw() {
		Dimension dWindow = getSize();
		if(!(dWindow.width == maxWindowWidth && dWindow.height == maxWindowHeight))
			setSize(dWindow.width,dWindow.height);
		background(255);
		if(paintMode == paintModeOption.Traces){
			drawTraces.setGpx(gpx);
			drawTraces.draw();
		}
		else if(paintMode == paintModeOption.Cluster){
			drawCluster.draw();
		}
		if(paintGrid){
			gridGraph.draw();		
		}
		
		if(paintIntersections){
			int countIntersections = 0;
			this.rectMode(CENTER);
			strokeWeight(3);
			this.stroke(this.color(255,0,0,255));
			this.fill(this.color(255,0,255));
			this.noFill();
			for(FindTraceIntersections.Box c : intersections){
				if(!c.isOverlapping){
					countIntersections++;
					float d = (float) (this.lon(new Point(c.pt.getLon()-(c.rec.width()/2), c.pt.getLat()-(c.rec.height()/2))) - this.lon(new Point(c.pt.getLon(), c.pt.getLat())));
					d = Math.abs(d);
					//d = d * 0.5f;
					this.rect(this.lon(c.pt), this.lat(c.pt), d,d);
				}
			}/*
			strokeWeight(1);
			this.stroke(this.color(0,0,0,255));
			this.fill(this.color(255,0,255));
			for(FindTraceIntersections.Box c : intersections){
				if(c.isOverlapping){
					this.rect(this.lon(c.pt), this.lat(c.pt), 3,3);
				}
			}
			*/
			rectMode(CORNER);
			Debug.syso("Kreuzungen: " + countIntersections);
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
		return gridGraph.getGrid();
	}
	public GridGraph getGridGraph() {
		return gridGraph;
	}
	public void setGrid(Grid grid) {
		this.paintGrid = true;
		this.gridGraph.setGrid(grid);
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
	public DrawTraces getDrawTraces() {
		return drawTraces;
	}
	public void setDrawTraces(DrawTraces drawTraces) {
		this.drawTraces = drawTraces;
	}
	public DrawCluster getDrawCluster() {
		return drawCluster;
	}
	public void setDrawCluster(DrawCluster drawCluster) {
		this.drawCluster = drawCluster;
	}
	
	
}