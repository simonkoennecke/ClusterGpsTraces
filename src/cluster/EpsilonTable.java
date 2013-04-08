package cluster;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import trace.Traces;
/**
 * Die Tabelle für die ermittelten Epsilonwerte
 * @author Simon Könnecke
 */
public class EpsilonTable {
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
		public List<Double> getList(){
			List<Double> rtn = new LinkedList<Double>();
			for(Map.Entry<Integer, Double> entry : row.entrySet()){
				rtn.add(entry.getValue());
			}
			return rtn;
		}
		public List<Integer> getTraceIdsList(){
			List<Integer> rtn = new ArrayList<Integer>();
			for(Map.Entry<Integer, Double> entry : row.entrySet()){
				rtn.add(entry.getKey());
			}
			return rtn;
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
	public List<Double> get(Integer clusterId){
		s.acquireUninterruptibly();
		List<Double> list;
		try{
			list = table.get(clusterId).getList();
		}
		catch(NullPointerException exception){
			list = new LinkedList<Double>();
		}
		s.release();
		return list;
	}
	public List<Integer> getTraceIds(Integer clusterId){
		s.acquireUninterruptibly();
		List<Integer> list;
		try{
			list = table.get(clusterId).getTraceIdsList();
		}
		catch(NullPointerException exception){
			list = new LinkedList<Integer>();
		}
		s.release();
		return list;
	}
}

/**
 * Diese Klasse wird verwendet, um alle Fréchet Distanzen parallel zu berechnen.
 * @author Simon Könnecke
 */
class calcEpsilonTable implements Runnable{
	/**
	 * Die Datenstruktur zum Abspeichern des berechneten Wert
	 */
	private EpsilonTable tbl;
	/**
	 * Alle Spuren die verglichen werden sollen mit den Centroiden
	 */
	private Traces t;
	/**
	 * Alle Centroide in der Datenstruktur Traces
	 */
	private Traces centroid;
	/**
	 * Die alle Paare von Spur und Centroid die Berechnet werden sollen in der Datenstruktur als Stack
	 */
	private AtomicStack toCalc;
	/**
	 * Die Threas ID
	 */
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
		//Arbeite alle Aufträge aufm Stack ab
		while(!toCalc.empty()){
			try{
				//Lade welcher Centroid und Spur berechnet werden soll
				int[] ids = toCalc.pop();//0 = centroidId, 1 = traceId
				//Berechne die Frechet-Distanz
				//Achtung die Spur muss vorher die Punkte in einen Point2D Array geladen worden sein
				double e = tCmp.compareTo(centroid.get(ids[0]), t.get(ids[1]));
				//Speicher den Wert in die Tabelle
				tbl.put(ids[0], ids[1], e);
			}
			catch(EmptyStackException e){
				//zwischen empty und pop hat ein anderer Prozess schon ein pop aufgerufen.
				break;
			}
		}
	}
}
