package cluster;

import java.util.HashMap;
import java.util.Map;

import trace.Trace;
import trace.Traces;

public class Cluster implements Iterable<Traces>{
	private Map<Integer, Traces> cluster;
	
	public Cluster(){
		cluster = new HashMap<Integer, Traces>();
	}
	public void put(Integer clusterId, Trace t){
		if(cluster.containsKey(clusterId)){
			cluster.get(clusterId).addTrace(t);
		}
		else{
			Traces tmp = new Traces();
			tmp.addTrace(t);
			cluster.put(clusterId, tmp);
		}
	}
	
	public Traces get(Integer clusterId){
		return cluster.get(clusterId);
	}
	
	
}
