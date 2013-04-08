package cluster;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import trace.Trace;

public class Clusters {
	public ArrayList<Cluster> list = new ArrayList<Cluster>();
	public ArrayList<EpsilonTable> eplisonTable = new ArrayList<EpsilonTable>();
	public int size(){
		return list.size();
	}
	/**
	 * Liefert ein Liste aller Frechét Distanz vom Cluster cId von der Iteratation iNo
	 * @param iNo Iterationsnummer
	 * @param cId Clusternummer
	 * @return List aller Frechét Distanz vom Cluster ID in der Iteration iNo
	 */
	public List<Double> getAllFrechetDistanceByClusterId(int iNo, int cId){
		if(iNo < eplisonTable.size())
			return eplisonTable.get(iNo).get(cId);
		else{
			return new LinkedList<Double>();
		}
	}
	
	public List<Trace> getAllTraceIdsByClusterId(int iNo, int cId){
		if(iNo < eplisonTable.size())
			return list.get(iNo).getAllTraces(cId);
		else{
			return new LinkedList<Trace>();
		}
	}
	
}
