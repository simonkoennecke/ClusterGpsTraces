package cluster;

import java.util.EmptyStackException;
import java.util.HashMap;
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
			try{
				int[] ids = toCalc.pop();//0 = centroidId, 1 = traceId
				double e = tCmp.compareTo(centroid.get(ids[0]), t.get(ids[1]));
				//System.out.println("Thread "+threadId +": Centroid " + ids[0] + " und Trace " + ids[1] + " hat ein Epsilon von " + e);
				tbl.put(ids[0], ids[1], e);
			}
			catch(EmptyStackException e){
				//zwischen empty und pop hat ein anderer Prozess schon ein pop aufgerufen.
				break;
			}
		}
	}
}
