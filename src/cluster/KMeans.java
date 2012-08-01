package cluster;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import trace.Trace;
import trace.Traces;


public class KMeans implements ClusterTraces {
	private Cluster cluster;
	
	private Traces centroid;
	
	private Integer k;
	
	private Traces t;
	
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
	
	public void run(){
		//1. Schritt: Wähle die Centroid in dem zufällig Traces gewählt werden.
		int[] rnd = selectCentroid();
		//2. Schritt: Berechne die Fréchet-Distance von allen Traces zu den Centroiden
		EpsilonTable tbl = new EpsilonTable();
		Integer centroidId = 0;
		boolean isCentroid;
		for(Trace cp : centroid){
			for(int i = 0; i < t.size(); i++){
				//Is die aktuelle Spure ein Centroid?
				isCentroid = checkIsCentroid(rnd, i);
				//Die Spur ist kein Centroid
				if(!isCentroid){
					TraceCompare tCmp = new FrechetDistance();
					tbl.put(centroidId, i, tCmp.compareTo(cp, t.get(i)));
				}
			}
			centroidId++;
		}
		//2.1 Schritt: Teile anhand der gewonnen Epsilonwerte die Traces zu den Clustern zu
		for(int i = 0; i < t.size(); i++){
			isCentroid = checkIsCentroid(rnd, i);
			//Is die aktuelle Spure ein Centroid?
			isCentroid = checkIsCentroid(rnd, i);
			//Die Spur ist kein Centroid
			if(!isCentroid){
				cluster.put(getBestFitClusterForTrace(tbl, i), t.get(i));
			}
		}
		//3. Schritt: Die Centroid berechnen anhand der gegeben Clusterverteilung.
		
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
			if(e < tmp){
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
	 * @return liefert die Indices die gewählt wurden,
	 */
	public int[] selectCentroid(){
		int[] rnd = randInt(k, t.size(), null);
		for(int i = 0; i < rnd.length; i++){
			centroid.addTrace(t.get(i));
		}
		return rnd;
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
		for(int i=0; i < exclude.length; i++){
			if(exclude[i] == r){
				return randInt(cnt, max, exclude);
			}
		}
		if(exclude.length == cnt){
			int tmp[] = new int[exclude.length + 1];
			tmp = exclude.clone();
			tmp[exclude.length + 1] = r;
		}
		return exclude;
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
		
		public EpsilonTable(){
			table = new HashMap<Integer, EpsilonTableRow>();
		}
		
		public void put(Integer clusterId, Integer traceId, Double epsilon){
			if(table.containsKey(clusterId)){
				table.get(clusterId).put(traceId, epsilon);
			}
			else{
				table.put(clusterId, new EpsilonTableRow(traceId, epsilon));				
			}
		}
		
		public double get(Integer clusterId, Integer traceId){
			return table.get(clusterId).get(traceId);
		}
	}
	
}
