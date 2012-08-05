package cluster;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicIntegerArray;

import merg.Grid;

import trace.Trace;
import trace.Traces;

/**
 * Diese Klasse fasst die Traces zu k Cluster zusammen. 
 * @author Simon
 *
 */
public class KMeans implements ClusterTraces {
	/**
	 * Hier werden die Cluster und deren Traces gespeichert
	 */
	private Cluster cluster;
	/**
	 * centroid sind die Zentren der Cluster und die sind alle Traces
	 */
	private Traces centroid, t;
	/**
	 * k ist die Anzahl der Cluster
	 */
	private Integer k;
	
	/**
	 * 
	 */
	
	/**
	 * Clusteranalyse mit k-Means-Algorithmus 
	 * @param k Die Anzahl der Cluster
	 * @param t Die Traces auf den die Cluster gerneriert werden sollen
	 */
	public KMeans(Integer k, Traces t){
		this.k = k;
		this.t = t;
		cluster = new Cluster();
		centroid = new Traces();
	}
	/**
	 * Erstellt die Clusterisierung von den Traces und
	 * speichert die unter Cluster ab.
	 * Die Ergebnisse kann man mit get von der Klasse holen.
	 */
	public void run(){
		//1. Schritt: Wähle die Centroid in dem zufällig Traces gewählt werden.
		int[] rnd = selectCentroid();
		//2. Schritt: Berechne die Fréchet-Distance von allen Traces zu den Centroiden
		EpsilonTable tbl = new EpsilonTable();
		AtomicStack stack = new AtomicStack();
		Integer centroidId = 0;
		boolean isCentroid;
		//erstelle die paare von den die Frechet-Distanz berechnet werden müssen
		for(Trace cp : centroid){
			for(int i = 0; i < t.size(); i++){
				//Is die aktuelle Spure ein Centroid?
				isCentroid = checkIsCentroid(rnd, i);
				//Die Spur ist kein Centroid
				if(!isCentroid){
					stack.push(centroidId, i);
				}
			}
			centroidId++;
		}
		//2.1. Schritt: Berechne die Epsilonwerte
		calcCuncurrentDist(tbl, stack);
		//2.2. Schritt: Teile anhand der gewonnen Epsilonwerte die Traces zu den Clustern zu
		for(int i = 0; i < t.size(); i++){
			isCentroid = checkIsCentroid(rnd, i);
			//Is die aktuelle Spure ein Centroid?
			isCentroid = checkIsCentroid(rnd, i);
			//Die Spur ist kein Centroid
			if(!isCentroid){
				cluster.put(getBestFitClusterForTrace(tbl, i), t.get(i));
			}
		}
		//Print Cluster
		for(int i=0; i < k; i++){
			Traces tmp = cluster.get(i);
			System.out.println((i+1)+". Cluster (Anzahl der Traces im Cluster " + tmp.size() + " von " + t.size() + "):");
			if(tmp != null){
				for(Trace t: tmp)
					System.out.print(t + ", ");
				System.out.println("");
			}
		}
		//3. Schritt: Die Centroid berechnen anhand der gegeben Clusterverteilung.
		Grid g = new Grid(cluster.get(0), 100, 100);
		
		//4. Erneut Epsilonwerte zu den neuen Centroiden berechnen
		//TODO: Neue Centroid berechnung und erneut "clustern"
	}
	/**
	 * Starte die berechnung der Epsilonwerte von der Frechet-Distanz
	 * in meheren Threads
	 * @param tbl Die Tabelle in dem die Epsilonwerte gespeichert werden
	 * @param stack Die Centroid und Trace Paare von den die Frechet-Distanz berechnert werden soll
	 */
	private void calcCuncurrentDist(EpsilonTable tbl, AtomicStack stack){
		int threadCnt = 50;
		Thread[] thrds = new Thread[threadCnt];
		for(int i=0; i < threadCnt; i++){
			thrds[i] = new Thread(new calcEpsilonTable(i, t, centroid, tbl, stack));
			thrds[i].start();
		}
		boolean allDead = false;
		while(!allDead){
			while(!stack.empty()){
				try{
					Thread.sleep(100);
				}
				catch ( InterruptedException e) {
					//do nothing
				}
			}
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
	public int[] selectCentroid(){
		int[] rnd = randInt(k, t.size());
		for(int i = 0; i < rnd.length; i++){
			centroid.addTrace(t.get(i));
		}
		return rnd;
	}
	private int[] randInt(int cnt, int max){
		return randInt(cnt, max, null);
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
	
	/**
	 * Die Tabelle für die ermittelten Epsilonwerte
	 * @author Simon
	 */
	class EpsilonTable{
		class EpsilonTableRow{
			private Map<Integer, Double> row;
			
			public EpsilonTableRow(){
				row = new HashMap<Integer, Double>();
			}
			public EpsilonTableRow(Integer key, Double value){
				row = new HashMap<Integer, Double>();
				put(key, value);
			}
			public Double get(Integer key){
				return row.get(key);
			}
			public void put(Integer key, Double value){
				row.put(key, value);
			}
		}
		
		private Map<Integer, EpsilonTableRow> table;
		
		private Semaphore s;
		
		public EpsilonTable(){
			table = new HashMap<Integer, EpsilonTableRow>();
			s = new Semaphore(1);
		}
		
		public void put(Integer clusterId, Integer traceId, Double epsilon){
			s.acquireUninterruptibly();
			if(table.containsKey(clusterId)){
				table.get(clusterId).put(traceId, epsilon);
			}
			else{
				table.put(clusterId, new EpsilonTableRow(traceId, epsilon));				
			}
			s.release();
		}
		
		public double get(Integer clusterId, Integer traceId){
			s.acquireUninterruptibly();
			double e = Double.MAX_VALUE;
			try{
				e = table.get(clusterId).get(traceId);
			}
			catch(NullPointerException exception){
				//Es kann sein, dass beim berechnen ein Fehler eingetreten ist.
			}
			s.release();
			return e;
		}
	}
	
	class calcEpsilonTable implements Runnable{
		private EpsilonTable tbl;
		private Traces t;
		private Traces centroid;
		private AtomicStack toCalc;
		private int threadId;
		
		public calcEpsilonTable(int threadId, Traces t, Traces centroid, EpsilonTable tbl, AtomicStack toCalc){
			this.threadId = threadId;
			this.t = t;
			this.centroid = centroid;
			this.tbl = tbl;
			this.toCalc = toCalc;
		}
		@Override
		public void run() {
			TraceCompare tCmp = new FrechetDistance();
			while(!toCalc.empty()){
				int[] ids = toCalc.pop();//0 = centroidId, 1 = traceId
				double e = tCmp.compareTo(centroid.get(ids[0]), t.get(ids[1]));
				System.out.println("Thread "+threadId +": Centroid " + ids[0] + " und Trace " + ids[1] + " hat ein Epsilon von " + e);
				tbl.put(ids[0], ids[1], e);
			}
		}
		
	}

	@Override
	public Cluster getCluster() {
		return cluster;
	}
}
