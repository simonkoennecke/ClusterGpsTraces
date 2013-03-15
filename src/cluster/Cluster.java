package cluster;

import java.util.ArrayList;
import java.util.Collection;
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
	private Traces centroidTraces;
	/**
	 * Anzahl der Cluster
	 */
	private int size = -1;
	
	public Cluster(int k){
		size = k;
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
		if(centroidTraces == null || centroidTraces.size() != centroid.size()){
			centroidTraces = new Traces();
			for(Trace t : centroid){
				centroidTraces.addTrace(t);
			}
		}
		return centroidTraces;
	}
	@Override
	public Iterator<Traces> iterator() {
		Collection<Traces> c = cluster.values();
	    Iterator<Traces> itr = c.iterator();
		return itr;
	}
	
	public ArrayList<Trace> iteratorCentroid(){
		return centroid;
	}

	
	public int getSize() {
		if(size == -1)
			setSize(centroid.size());
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
}
