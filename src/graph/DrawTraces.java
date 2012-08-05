package graph;


import processing.core.*;
import trace.Point;
import trace.PtOpSphere;
import trace.Trace;
import trace.Traces;

public class DrawTraces {
	private GpxFile gpx;
	
	private MainGraph g;
	
	private int lineColor;
	public DrawTraces(MainGraph g, GpxFile gpx){
		this.gpx = gpx;
		this.g = g;
		lineColor = g.color(0,0,0);
	}
	public DrawTraces(MainGraph g){
		this.g = g;
		lineColor = g.color(0,0,0);
	}
	private void drawArrow(Point p1, Point p2){
		g.stroke(lineColor);
		g.line(g.lon(p1), g.lat(p1), g.lon(p2), g.lat(p2));
		//line(x(p1), y(p1), x(p2), y(p2));
		/*
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
		*/
		g.strokeWeight(1);

	}
	public void setColor(int c){
		lineColor = c;
	}
	public void draw(){
		Traces traces = gpx.getTraces();
		draw(traces);
	}
	public void draw(Traces traces){
		//int cntPoints = traces.countPoints();
		//int avgPointsOnTrace = cntPoints / traces.size();
		
		//System.out.println("avgPointsOnTrace: " + avgPointsOnTrace + ", Max Point" + maxPt + ", Min Point" + minPt);
	  
	  	
		for(Trace t : traces){		  
			if(t.getSubTraces().size()>0){
				this.draw(t.getSubTraces());
				continue;
			}
			if(t.size() <= 2)
				continue;
			//System.out.println("Trace (Number of Pt: " + t.size() + ", distance: " + t.getDistance() + ")");
		  
			Point p1 = t.get(0);
			Point pn = t.get(t.size()-1);
			g.fill(g.color(255,0,0));
			g.rect(g.lon(p1), g.lat(p1), 10,10);
			g.fill(g.color(0,255,0));
			g.rect(g.lon(pn), g.lat(pn), 10,10);
			
			g.stroke(g.color(0,0,0));
			int i=0;
			for(Point pt : t){
			  //System.out.println("Pt:" + (pt.getLat()-minPt.getLat()) * latFactor + ", " +(pt.getLon()-minPt.getLon()) * lonFactor + "");
			  //point(lon(pt), lat(pt));
				  double r = PtOpSphere.cardinalDirection(p1,pt);
				  //if(i%30==0)
					  //text(String.valueOf(Math.round(r)),lon(pt), lat(pt));
				  drawArrow(p1,pt);
				  p1 = pt;
				  i++;
			  }
			  
		  }
	}
}
