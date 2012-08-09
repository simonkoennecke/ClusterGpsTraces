package cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import trace.Trace;
import trace.Traces;

public class Cluster implements Iterable<Traces>{
	private Map<Integer, Traces> cluster;
	/**
	 * centroid sind die Zentren der Cluster
	 */	
	private ArrayList<Trace> centroid;
	
	public Cluster(){
		cluster = new HashMap<Integer, Traces>();
		centroid = new ArrayList<Trace>();
	}
	public void putTraces(Integer clusterId, Trace t){
		if(cluster.containsKey(clusterId)){
			cluster.get(clusterId).addTrace(t);
		}
		else{
			Traces tmp = new Traces();
			tmp.addTrace(t);
			cluster.put(clusterId, tmp);
		}
	}
	
	public Traces getTraces(Integer clusterId){
		return cluster.get(clusterId);
	}
	
	public void putCentroid(Integer clusterId, Trace t){
		centroid.add(clusterId, t);
	}
	public Trace getCentroid(Integer clusterId){
		return centroid.get(clusterId);
	}
	public Traces getCentroid(){
		Traces tmp = new Traces();
		for(Trace t : centroid){
			tmp.addTrace(t);
		}
		return tmp;
	}
	
	@Override
	public Iterator<Traces> iterator() {
		Collection c = cluster.values();
	    Iterator itr = c.iterator();
		return itr;
	}
	
	public ArrayList<Trace> iteratorCentroid(){
		return centroid;
	}
}
