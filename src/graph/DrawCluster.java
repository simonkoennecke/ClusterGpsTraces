package graph;

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
			dT.setColor(c.getNext());
			dT.draw(t);
		}
	}
	
}
