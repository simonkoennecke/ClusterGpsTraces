package trace;

import java.util.ArrayList;
import java.util.List;

import gnu.trove.TIntProcedure;

class PTIntProcedure implements TIntProcedure {
	private Integer lastId = null;
	private Integer counter = 0;
	private Point intersection;
	private Integer traceId = null;
	private List<Integer> intersectList = new ArrayList<Integer>();
	
	
	public PTIntProcedure(Point intersection){		
		this.intersection = intersection;
	}
	
	public PTIntProcedure(int traceId){	
		this.traceId = traceId;
	}
	public boolean execute(int i) {
		intersectList.add(i);
		lastId = i;
		counter++;
		return true;
	}

	public Integer getId() {
		return lastId;
	}
	
	public List<Integer> getNearestNeighbours(){
		return intersectList;
	}
	public Integer getCountNearestNeighbours(){
		return counter;
	}
	public Point getIntersectionPoint(){
		return intersection;
	}
	public String toString(){
		if(traceId != null)
			return "(" + traceId + ", " + counter + ")";
		else
			return "(" + intersection + ", " + counter + ")";
		
	}
}
