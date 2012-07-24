package graph;
import javax.xml.stream.XMLStreamException;

import processing.core.*;
import trace.*;

public class MainGraph extends PApplet {
	private static final long serialVersionUID = -224150035454082876L;
	private GpxFile gpx;
	int maxWindowHeight = 800;
	int maxWindowWidth = 1024;
	
	Point maxPt;
	Point minPt;
	
	float lonFactor;
	float latFactor;
	
	float xFactor;
	float yFactor;
	
	
	public MainGraph(GpxFile _gpx, int w, int h){
		maxWindowHeight = h;
		maxWindowWidth = w;
		gpx = _gpx;
	}
	
	public void setup(){
		frameRate(1);
		smooth();
		noLoop();
		size(maxWindowWidth,maxWindowHeight);
		
		setSize(maxWindowWidth, maxWindowHeight);
	}
	public void setSize(int w, int h){
		maxWindowHeight = h - 40;
		maxWindowWidth = w - 40;
		gpx.getTraces().calcExtrema();
		maxPt = gpx.getTraces().getMax();
		minPt = gpx.getTraces().getMin();
		Double tmp;
		
		  tmp = (maxWindowWidth/ (maxPt.getLon() - minPt.getLon()));
		  lonFactor =  tmp.floatValue();
		  tmp = maxWindowHeight / (maxPt.getLat() - minPt.getLat());
		  latFactor = tmp.floatValue();
		  
		  //Double tmp;
		  tmp = (maxWindowWidth/ (maxPt.getX() - minPt.getX()));
		  xFactor =  tmp.floatValue();
		  tmp = maxWindowHeight / (maxPt.getY() - minPt.getY());
		  yFactor = tmp.floatValue();
		  
		 resize(w, h);
	}
	
	public float lon(Point pt){
		Double x = (pt.getLon()-minPt.getLon()) * lonFactor;
		return x.floatValue();
	}
	public float lat(Point pt){
		Double y = (pt.getLat()-minPt.getLat()) * latFactor;
		return y.floatValue();
	}
	public float x(Point pt){
		Double x = (pt.getX()-minPt.getX()) * xFactor;
		return x.floatValue();
	}
	public float y(Point pt){
		Double y = (pt.getY()-minPt.getY()) * yFactor;
		return y.floatValue();
	}
	void drawArrow(Point p1, Point p2){
		line(lon(p1), lat(p1), lon(p2), lat(p2));
		//line(x(p1), y(p1), x(p2), y(p2));
		pushMatrix();
			  translate(lon(p2), lat(p2));
			  //translate(x(p2), y(p2));
			  Double x = Math.toRadians(PtOpSphere.cardinalDirection(p1, p2));
			  rotate(x.floatValue());
			  strokeWeight(1);
			  noFill();			  
			  beginShape();
			  vertex(-3,3);
			  vertex(0,0);			  
			  vertex(3,3);
			  endShape();
		popMatrix();
		strokeWeight(1);

	}
	public void drawGPSTraces(Traces traces){
		int cntPoints = traces.countPoints();
		int avgPointsOnTrace = cntPoints / traces.size();
		
		//System.out.println("avgPointsOnTrace: " + avgPointsOnTrace + ", Max Point" + maxPt + ", Min Point" + minPt);
	  
	  	
		for(Trace t : traces){		  
			if(t.getSubTraces().size()>0){
				drawGPSTraces(t.getSubTraces());
				continue;
			}
			if(t.size() <= 2)
				continue;
			//System.out.println("Trace (Number of Pt: " + t.size() + ", distance: " + t.getDistance() + ")");
		  
			Point p1 = t.get(0);
			Point pn = t.get(t.size()-1);
			fill(color(255,0,0));
			rect(lon(p1), lat(p1), 10,10);
			fill(color(0,255,0));
			rect(lon(pn), lat(pn), 10,10);
			
			stroke(color(0,0,0));
			int i=0;
			for(Point pt : t){
			  //System.out.println("Pt:" + (pt.getLat()-minPt.getLat()) * latFactor + ", " +(pt.getLon()-minPt.getLon()) * lonFactor + "");
			  //point(lon(pt), lat(pt));
				  double r = PtOpSphere.cardinalDirection(p1,pt);
				  if(i%30==0)
					  text(String.valueOf(Math.round(r)),lon(pt), lat(pt));
				  drawArrow(p1,pt);
				  p1 = pt;
				  i++;
			  }
			  
		  }
	}
	
	public void draw() {
		background(255);
		drawGPSTraces(gpx.getTraces());
		translate(20, 20);
	  stroke(color(255,0,0));
	  if (mousePressed) {
	    line(mouseX,mouseY,pmouseX,pmouseY);
	  }
	}
}