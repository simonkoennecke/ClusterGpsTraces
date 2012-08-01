package cluster;

import java.util.HashMap;
import java.util.Map;

import trace.Trace;
import trace.Traces;

public class Cluster {
	private Map<Integer, Traces> cluster;
	
	public Cluster(){
		cluster = new HashMap<Integer, Traces>();
	}
	public void put(Integer i, Trace t){
		if(cluster.containsKey(i)){
			cluster.get(i).addTrace(t);
		}
		else{
			Traces tmp = new Traces();
			tmp.addTrace(t);
			cluster.put(i, tmp);
		}
	}
	
	public Traces get(Integer i){
		return cluster.get(i);
	}
}
