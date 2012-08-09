package graph;

import trace.Trace;
import trace.Traces;
import cluster.Cluster;

public class DrawCluster {
	private Cluster cluster;
	
	private MainGraph g;
	
	private ColorSet c;
	
	public DrawCluster(MainGraph g, Cluster cluster){
		this.cluster = cluster;
		this.g = g;
		this.c = new ColorSet();
	}
	
	public void draw(){
		DrawTraces dT = new DrawTraces(g);
		for(Traces t : cluster){
			System.out.println("Color: " + c.getColor());
			dT.setColor(c.getNextColor());			
			dT.draw(t);
		}
		dT.setColor(g.color(0));
		dT.setLineWeight(5);
		dT.draw(cluster.getCentroid());		
		
	}
	
}
