package graph;


import processing.core.*;
import trace.Point;
import trace.PtOpSphere;
import trace.Trace;
import trace.Traces;

public class DrawTraces {
	private GpxFile gpx;
	
	private MainGraph g;
	
	private int lineColor, lineWeight=1;
	
	private ColorSet c = new ColorSet();
	
	public DrawTraces(MainGraph g, GpxFile gpx){
		this.gpx = gpx;
		this.g = g;
		lineColor = g.color(0,0,0);//g.color(255,255,255);
	}
	public DrawTraces(MainGraph g){
		this.g = g;
		lineColor = g.color(0,0,0);//g.color(255,255,255);
	}
	private void drawArrow(Point p1, Point p2){
		g.stroke(lineColor);
		g.strokeWeight(lineWeight);
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

	}
	public void setColor(int c){
		//System.out.println("Trace Color von " + lineColor + " zu " + c);
		lineColor = c;
	}
	public void setLineWeight(int w){
		//System.out.println("Line Weight of Trace von " + lineWeight + " zu " + w);
		lineWeight = w;
	}
	public void draw(){
		draw(gpx.getTraces());
		drawStartAndEndPointFromTrace(gpx.getTraces());
	}
	public void draw(Traces traces){
		//Alle Spuren ziechen
		for(Trace t : traces){		  
			if(t.getSubTraces().size()>0){
				this.draw(t.getSubTraces());
				continue;
			}
			if(t.size() < 2)
				continue;
			
			Point p1 = null;
			
			for(Point p2 : t){
				if(p1 == null){
					p1 = p2;
					continue;
				}
				drawArrow(p1,p2);
				p1=p2;
			}
		}
	}
	public void drawStartAndEndPointFromTrace(Traces traces){
		for(Trace t : traces){		  
			if(t.getSubTraces().size()>0){
				this.drawStartAndEndPointFromTrace(t.getSubTraces());
				continue;
			}
			if(t.size() < 2)
				continue;
			
			Point p1 = t.get(0);
			Point pn = t.get(t.size()-1);
			int boxSize = 3;
			g.stroke(g.color(0,0,0,255));
			g.fill(g.color(255,0,0));
			g.rect(g.lon(p1), g.lat(p1), boxSize, boxSize);
			g.fill(g.color(0,255,0));
			g.rect(g.lon(pn), g.lat(pn), boxSize, boxSize);
			
		}
	}
}
