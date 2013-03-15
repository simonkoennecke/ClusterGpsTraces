package graph;

import trace.Traces;
import cluster.Cluster;

public class DrawCluster {
	private Cluster clusterSecondIteration;
	private Cluster clusterFirstIteration;
	private boolean paintFirstIteration = false;
	private DrawTraces dT;
	private MainGraph g;
	
	private ColorSet c = new ColorSet();
	
	public DrawCluster(MainGraph g){
		this.g = g;
		dT = new DrawTraces(g);
	}
	public DrawCluster(MainGraph g, Cluster cluster){
		this.clusterSecondIteration = cluster;
		this.g = g;
		dT = new DrawTraces(g);
	}
	
	public void draw(){
		//Alle Traces die im Hintergrund liegen als erstes Zeichnen
		for(int i=0; i < getCluster().getSize(); i++){
			if(!getCluster().getCentroid(i).isDisplay()){
				dT.setColor(g.color(199,199,199));
				dT.draw(getCluster().getTraces(i));
			}
			
		}
		//Alle Traces mit passender Farbe zeichnen.
		for(int i=0; i < getCluster().getSize(); i++){
			if(getCluster().getCentroid(i).isDisplay()){
				dT.setColor(c.getColor(i));
				dT.draw(getCluster().getTraces(i));
			}
		}
		
		for(int i=0; i < getCluster().getSize(); i++){
			if(getCluster().getCentroid(i).isDisplay()){
				//Rahmenfarbe der Linie
				dT.setColor(g.color(0));
				dT.setLineWeight(5);
				dT.draw(getCluster().getCentroid(i));
				//Farbe der Linie
				dT.setColor(c.getColor(i));
				dT.setLineWeight(2);
				dT.draw(getCluster().getCentroid(i));
			}
		}
		dT.setLineWeight(1);
		
	}

	public Cluster getCluster() {
		if(paintFirstIteration)
			return clusterFirstIteration;
		else
			return clusterSecondIteration;
	}

	public void setClusterSecondIteration(Cluster cluster) {
		this.clusterSecondIteration = cluster;
	}
	public Cluster getClusterFirstIteration() {
		return clusterFirstIteration;
	}
	public void setClusterFirstIteration(Cluster cluster) {
		this.clusterFirstIteration = cluster;
	}
	public boolean isPaintFirstIteration() {
		return paintFirstIteration;
	}
	public void setPaintFirstIteration(boolean paintFirstIteration) {
		this.paintFirstIteration = paintFirstIteration;
	}
	
	
	
}
