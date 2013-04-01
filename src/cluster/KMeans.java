package cluster;

import java.awt.geom.Point2D;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicIntegerArray;

import core.Debug;

import merg.Grid;
import merg.TracesMerge;

import trace.Point;
import trace.Trace;
import trace.Traces;
import trace.TrcOp;

/**
 * Diese Klasse fasst die Traces zu k Cluster zusammen. 
 * @author Simon
 *
 */
public class KMeans implements ClusterTraces {
	/**
	 * Hier werden die Cluster und deren Traces gespeichert
	 */
	private Cluster cluster, cluster2;
	private Clusters clusters = new Clusters();
	/**
	 * t sind alle Traces
	 */
	private Traces t;
	/**
	 * k ist die Anzahl der Cluster
	 */
	private Integer k;
	/**
	 * Type of choose Start points
	 */
	private static enum centroidType {RANDOM_CHOOSE_TRACE, TABLE};
	private centroidType type; 
	/**
	 * Number of Table column and row
	 */
	private int row, column;
	/**
	 * Trace Version id für das Clustern
	 */
	Integer vId = Trace.getIncrementVersionId(), noOfClusterIteration, noOfClusterIterationTotal;
	/**
	 * Clusteranalyse mit k-Means-Algorithmus 
	 * @param k Die Anzahl der Cluster
	 * @param t Die Traces auf den die Cluster generiert werden sollen
	 */
	public KMeans(Integer k, Traces t, int noOfClusterIteration){
		type = centroidType.RANDOM_CHOOSE_TRACE;
		this.k = k;
		this.t = t;
		cluster = new Cluster(k);
		clusters.list.add(cluster);
		this.noOfClusterIteration = noOfClusterIteration;
		this.noOfClusterIterationTotal = noOfClusterIteration;
	}
	public KMeans(int row, int col, Traces t, int noOfClusterIteration){
		type = centroidType.TABLE;
		this.row = row;
		this.column = col;
		this.k = row * col;
		this.t = t;
		cluster = new Cluster(k);
		clusters.list.add(cluster);
		this.noOfClusterIteration = noOfClusterIteration;
		this.noOfClusterIterationTotal = noOfClusterIteration;
	}
	/**
	 * Erstellt die Clusterisierung von den Traces und
	 * speichert die unter Cluster ab.
	 * Die Ergebnisse kann man mit get von der Klasse holen.
	 */
	public void run(){
		//0. Versionsnummer für die Clusterung
		
		Debug.syso("//1. Schritt: Wähle die Centroid in dem zufällig Traces gewählt werden.");
		int[] rnd = {-1};
		if(type == centroidType.RANDOM_CHOOSE_TRACE)
			rnd = selectCentroid();
		else if(type == centroidType.TABLE)
			generateCentorid();
		Debug.syso("//2. Schritt: Berechne die Fréchet-Distance von allen Traces zu den Centroiden");
		EpsilonTable tbl = new EpsilonTable();
		AtomicStack stack = new AtomicStack();
		Integer centroidId = 0;
		boolean isCentroid=false;
		//erstelle die paare von den die Frechet-Distanz berechnet werden müssen
		for(Trace cp : cluster.iteratorCentroid()){
			for(int i = 0; i < t.size(); i++){
				//Datenformat für die Fréchet Distanz erstellen
				Point2D[] tmpPoints = t.get(i).getPoints();
				//Is die aktuelle Spure ein Centroid?
				if(type == centroidType.RANDOM_CHOOSE_TRACE)
					isCentroid = checkIsCentroid(rnd, i);
					
				//Die Spur ist kein Centroid
				if(!isCentroid){
					stack.push(centroidId, i);
				}
			}
			centroidId++;
		}
		Debug.syso("//2.1. Schritt: Berechne die Epsilonwerte");
		calcCuncurrentDist(tbl, stack, cluster);
		Debug.syso("//2.2. Schritt: Teile anhand der gewonnen Epsilonwerte die Traces zu den Clustern zu");
		isCentroid=false;
		for(int i = 0; i < t.size(); i++){
			//Is die aktuelle Spure ein Centroid?
			if(type == type.RANDOM_CHOOSE_TRACE)
				isCentroid = checkIsCentroid(rnd, i);
			//Die Spur ist kein Centroid
			if(!isCentroid){
				cluster.putTraces(getBestFitClusterForTrace(tbl, i), t.get(i));
			}			
		}
		//Print Cluster
		for(int i=0; i < k; i++){
			Traces tmp = cluster.getTraces(i);
			if(tmp != null){
				Debug.syso((i+1)+". Cluster mit der Anzahl von Traces " + tmp.size() + " von " + t.size() + "");				
			}
		}
		Debug.syso("//3. Schritt: Die Centroid berechnen anhand der gegeben Clusterverteilung.");
		
		while(--noOfClusterIteration>0){
			Debug.syso("//3. Schritt: Iteration " + ( noOfClusterIterationTotal - noOfClusterIteration) + "/" + noOfClusterIterationTotal);
			cluster = clusters.list.get(clusters.list.size()-1);
			cluster2 = new Cluster(k);
			clusters.list.add(cluster2);
			TracesMerge[] g = new TracesMerge[cluster.getCentroid().size()];
			int s = 0;
			for(Trace t : cluster.getCentroid()){
				g[s] = new TracesMerge();
				g[s].set(cluster.getTraces(s));
				g[s].run();
				Trace tmp = TrcOp.DouglasPeuckerReduction(g[s].get(), 1000.0, vId);
				try{
					Point2D[] tmpPoints = tmp.getPoints();
				}
				catch (NullPointerException e) {
					// TODO: Warum kommt hier solch eine Exception				
					tmp = g[s].get();
					Debug.syso("//3. Error: NullPointer Trace hat " + tmp.size() + " Punkte");
				}
				cluster2.putCentroid(s, tmp);
				s++;
			}
			
			/*
			//3. Schritt: Die Centroid berechnen anhand der gegeben Clusterverteilung.
			Grid[] g = new Grid[cluster.getCentroid().size()];
			int s = 0;
			for(Trace t : cluster.getCentroid()){
				g[s] = new Grid(cluster.getTraces(s), 10);			
				Trace tmp = TrcOp.DouglasPeuckerReduction(g[s].calcMeanTrace(), 200, vId);
				cluster2.putCentroid(s, tmp);
				s++;
			}
			*/
			
			//4. Erneut Epsilonwerte zu den neuen Centroiden berechnen
			Debug.syso("//4. Schritt: Erneut Epsilonwerte zu den neuen Centroiden berechnen");
			//erstelle die paare von den die Frechet-Distanz berechnet werden müssen
			EpsilonTable tbl2 = new EpsilonTable();
			clusters.eplisonTable.add(tbl2);
			centroidId=0;
			while(!stack.empty()){
				stack.pop();
			}
			for(int k=0; k < cluster2.iteratorCentroid().size(); k++){
				for(int i = 0; i < t.size(); i++){
					stack.push(centroidId, i);				
				}
				centroidId++;
			}
			//4.1. Schritt: Berechne die Epsilonwerte
			Debug.syso("//4.1. Schritt: Berechne die Epsilonwerte");
			calcCuncurrentDist(tbl2, stack, cluster2);
			//4.2. Schritt: Teile anhand der gewonnen Epsilonwerte die Traces zu den Clustern zu
			Debug.syso("//4.2. Schritt: Teile anhand der gewonnen Epsilonwerte die Traces zu den Clustern zu");
			for(int i = 0; i < t.size(); i++){
				cluster2.putTraces(getBestFitClusterForTrace(tbl2, i), t.get(i));						
			}
			
			//Print Cluster
			for(int i=0; i < k; i++){
				Traces tmp = cluster2.getTraces(i);
				if(tmp != null){
					System.out.println((i+1)+". Cluster mit der Anzahl von Traces " + tmp.size() + " von " + t.size() + "");				
				}
			}
		}
			
	}
	/**
	 * Starte die berechnung der Epsilonwerte von der Frechet-Distanz
	 * in meheren Threads
	 * @param tbl Die Tabelle in dem die Epsilonwerte gespeichert werden
	 * @param stack Die Centroid und Trace Paare von den die Frechet-Distanz berechnert werden soll
	 */
	private void calcCuncurrentDist(EpsilonTable tbl, AtomicStack stack, Cluster c){
		//int threadCnt = 2;
		int threadCnt = Runtime.getRuntime().availableProcessors();
		Debug.syso("Verwende " + threadCnt + " Prozessoren.");
		Thread[] thrds = new Thread[threadCnt];
		for(int i=0; i < threadCnt; i++){
			thrds[i] = new Thread(new calcEpsilonTable(i, t, c.getCentroid(), tbl, stack));
			thrds[i].start();
		}
		int newLine = 90;
		boolean allDead = false;
		while(!allDead){
			while(!stack.empty()){
				try{
					Thread.sleep(500);					
					
					//Ausgabe zur Anzeige, dass das Programm noch läuft.
					if(newLine == 0){
						Debug.syso(".");
						newLine = 90;						
					}
					else{
						Debug.sysoWithoutLn(".");
						newLine--;
					}
					
				}
				catch ( InterruptedException e) {
					//do nothing
				}
			}
			Debug.syso("");
			allDead = true;
			for(int i=0; i < threadCnt; i++)
				allDead &= !(thrds[1].isAlive());
		}
		
		
	}
	/**
	 * Prüft, ob der Trace ein Centroid ist.
	 * @param exclude Die außer acht zu lassenen Traces
	 * @param crt Die TraceId die geprüft werden soll
	 * @return true ist in der Liste drin, somit ein Centroid. False ist kein Centroid.
	 */
	private boolean checkIsCentroid(int[] exclude, int crt){
		for(int s : exclude){
			if(s == crt){
				return true;
			}
		}
		return false;
	}
	/**
	 * Die Funktion ermittelt, das am besten passende Cluster.
	 * @param tbl Die Tabelle mit allen Epsilonwerten
	 * @param traceId Der Trace der gerade geprüft werden soll
	 * @return Die ClusterId in dem der Trace am besten rein passt.
	 */
	private Integer getBestFitClusterForTrace(EpsilonTable tbl, Integer traceId){
		int cId=-1;
		Double e = new Double(Double.MAX_VALUE);
		for(int i=0; i < k; i++){
			Double tmp  = tbl.get(i, traceId);
			if(tmp < e){
				e = tmp;
				cId = i;
			}
		}
		return cId;
	}
	/**
	 * Wähle Centroide für die Cluster aus den vorhanden Spuren
	 * Dies entspricht nicht ganz den k-Means-Algorithmus,
	 * es müsste eigentlich eine Zufällig Traces erzeugt werden.
	 * @return liefert die Indices der Gewählten Traces zurück
	 */
	private int[] selectCentroid(){
		int[] rnd = randInt(k, t.size());
		for(int i = 0; i < rnd.length; i++){
			cluster.putCentroid(i, t.get(i));
		}
		return rnd;
	}
	private int[] randInt(int cnt, int max){
		return randInt(cnt, max, null);
	}
	/**
	 * Diese Funktion erzeugt ein Gitter von Punkten
	 */
	private void generateCentorid(){
		t.calcExtrema();
		Point m = t.getMin();
		double dLat = t.getMax().getLat() - m.getLat();//x
		double dLon = t.getMax().getLon() - m.getLon();//y
		double rasterLat = dLat/column;
		double rasterLon = dLon/row;
		m.setLat(m.getLat()+rasterLat/2);
		m.setLon(m.getLon()+rasterLon/2);
		for(int r=0; r < row; r++){
			for(int c=0; c < column; c++){
				Trace tmp = new Trace("", vId);
				tmp.addPoint(m.getLon() + rasterLon * r, m.getLat() + rasterLat * c);
				tmp.addPoint(m.getLon() + rasterLon * r, m.getLat() + rasterLat * c + 0.00002);
				cluster.putCentroid(((r+1)*(c+1))-1, tmp);
			}
		}
		//Sonst kommt es zu Problemen beim
		//nebenläufigen abrufen
		for(int i=0;i<(row+column);i++){
			cluster.getCentroid(i).getPoints();
		}
		
	}
	/**
	 * Erzeugt ein Array von nicht doppelten Zahlen
	 * 
	 * @param cnt Anzahl der zu generierenden Nummern
	 * @param max Die höchste Zahl minus eins, die gewürfelt werden kann
	 * @param exclude Die bereits schon gewürfelt wurde
	 * @return Eine Liste von Zahlen, die nicht doppelt sind.
	 */
	private int[] randInt(int cnt, int max, int[] exclude){
		Random generator = new Random();
		int r= generator.nextInt(max);
		if(exclude != null){
			for(int i=0; i < exclude.length; i++){
				if(exclude[i] == r){
					return randInt(cnt, max, exclude);
				}
			}
		}
		int tmp[];
		if(exclude == null){
			 tmp = new int[]{r};
		}
		else{
			tmp = new int[(exclude.length + 1)];
			int s=0;
			for(s=0; s<exclude.length; s++)
				tmp[s] = exclude[s];
			tmp[s] = r;
		}
		exclude = tmp;
		if(exclude.length < cnt){
			return randInt(cnt, max, exclude);
		}
		else{
			return exclude;
		}
	}
	
	@Override
	public Cluster getCluster() {
		return cluster2;
	}
	public Cluster getClusterFirstIteration(){
		return cluster;
	}
	public Clusters getClusters() {
		return clusters;
	}
}
