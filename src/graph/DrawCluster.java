package graph;

import core.Debug;
import trace.Trace;
import trace.Traces;
import cluster.Cluster;
import cluster.Clusters;

public class DrawCluster {
	private Clusters clusters;
	private int paintIteration = 1;
	private DrawTraces dT;
	private MainGraph g;
	
	private ColorSet c = new ColorSet();
	
	public DrawCluster(MainGraph g){
		this.g = g;
		dT = new DrawTraces(g);
	}
	public DrawCluster(MainGraph g, Clusters clusters){
		this.clusters = clusters;
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
				dT.setLineWeight(10);
				dT.draw(getCluster().getCentroid(i));
				//Farbe der Linie
				dT.setColor(c.getColor(i));
				dT.setLineWeight(2);
				dT.draw(getCluster().getCentroid(i));
			}
		}
		dT.setLineWeight(1);
		
	}
	
	public Cluster getCluster(int i) {
		return clusters.list.get(i);
	}
	
	public Cluster getCluster() {
		return clusters.list.get(paintIteration);
	}
	public Clusters getClusters() {
		return clusters;
	}
	public void setClusters(Clusters clusters) {
		this.clusters = clusters;
	}
	public int getPaintIteration() {
		return paintIteration;
	}
	public void setPaintIteration(int paintIteration) {
		if(paintIteration >= clusters.size())
			this.paintIteration = clusters.size()-1;
		else
			this.paintIteration = paintIteration;
	}
	public void saveToFile(String dir){
		//Setze alle Spuren auf nicht Anzeigen
		Debug.syso("Set all Traces to none displayed.");
		for(Cluster c : clusters.list){
			for(int cId=0; cId < c.getSize(); cId++){
				c.getCentroid(cId).setDisplay(false);
			}
		}
		Debug.syso("Create for each iteration and each cluster own picture.");
		g.setPaintMode(MainGraph.paintModeOption.Cluster);
		int iterId=0;
		for(Cluster c : clusters.list){
			setPaintIteration(iterId);
			Debug.sysoWithoutLn("Iteration " + (iterId+1) + " Paint Cluster: ");
			for(int cId=0; cId < c.getSize(); cId++){				
				Debug.sysoWithoutLn(""+cId+",");
				c.getCentroid(cId).setDisplay(true);
				g.draw();
				g.save(dir + formatFilename(cId,iterId));
				c.getCentroid(cId).setDisplay(false);
			}
			Debug.syso("");
			iterId++;
		}
		
	}
	private String formatFilename(int clusterId, int iterationId){
		return String.format( "%03d_%03d",  clusterId, iterationId)+".png";
	}
}
