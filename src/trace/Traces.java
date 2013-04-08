package trace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Traces extends IterInterface<Trace> implements Iterable<Trace>  {
	private final ArrayList<Trace> traces = new ArrayList<Trace>();
	
	private Point maxPt;
	private Point minPt;
	
	public Traces(){
		
	}
	
	public Trace get(int index){
		return traces.get(index);
	}
	
	public int size(){
		if(traces == null)
			return 0;
		else
			return traces.size();
	}
	public Trace addTrace(){
		return addTrace("Sub Trace", null);
	}	
	public Trace addTrace(String _name, Integer _vId){
		Trace trk = new Trace(_name, _vId);
		traces.add(trk);
		return trk;
	}	
	public Trace addTrace(Trace trk){
		traces.add(trk);
		return trk;
	}
	public Trace remove(int index){
		return traces.remove(index);
	}	
	public void calcExtrema(){
		maxPt = new Point(0,0);
		maxPt.setX(Integer.MIN_VALUE);
		maxPt.setY(Integer.MIN_VALUE);
		minPt = new Point(180,180);
		minPt.setX(Integer.MAX_VALUE);
		minPt.setY(Integer.MAX_VALUE);
		calcExtremaHelper(this);
	}
	private void calcExtremaHelper(Traces traces){
		for(Trace t : traces){
			if(t.getSubTraces().size() > 0){
				calcExtremaHelper(t.getSubTraces());
			}
			else{
				PtOpSphere.checkExtrema(t.getMinPt(),maxPt,minPt);
				PtOpSphere.checkExtrema(t.getMaxPt(),maxPt,minPt);
				PtOpPlane.checkExtrema(t.getMinPt(),maxPt,minPt);
				PtOpPlane.checkExtrema(t.getMaxPt(),maxPt,minPt);
			}
		}
	}
	public Point getMax(){
		return maxPt;
	}
	
	public Point getMin(){
		return minPt;
	}
	
	public long countPoints(){
		return countPoints(this, Trace.getCurrentVersionId());
	}
	public long countPoints(int vId){
		return countPoints(this, vId);
	}
	public long countPoints(Traces traces, int vId){
		int cnt=0;
		for(Trace t : traces){
			if(t.getSubTraces().size() > 0 && t.getVersionId() < vId){
				cnt += countPoints(t.getSubTraces(), vId);
				continue;
			}
			cnt += t.size();
		}
		return cnt;
	}
	public long countDisplayedTraces(){
		return countDisplayedTraces(this, Trace.getCurrentVersionId());
	}
	public long countDisplayedTraces(int vId){
		return countDisplayedTraces(this, vId);
	}
	private long countDisplayedTraces(Traces traces, int vId){
		long cnt = 0;
		for(Trace t : traces){
			if(t.getSubTraces().size() == 0 && t.size() >= 2 && t.getVersionId() <= vId){
				cnt += 1;
			}
			else{
				cnt += countDisplayedTraces(t.getSubTraces(), vId);
			}
		}
		return cnt;
	}
	
	public List<Double> calcDistanceBetweenPoints(int vId){
		List<Double> list = new LinkedList<Double>();
		calcDistanceBetweenPoints(this, list,vId);
		return list;
	}
	public void  calcDistanceBetweenPoints(Traces traces, List<Double>  l, int vId){
		for(Trace t : traces){
			if(t.getSubTraces().size() > 0 && t.getVersionId() < vId){
				calcDistanceBetweenPoints(t.getSubTraces(), l, vId);
				continue;
			}
			for(int i = 1; i < t.size(); i++){
				final double tmp = PtOpSphere.distance(t.get(i-1), t.get(i));
				if(tmp > 0)
					l.add(tmp);
			}
		}
	}
	
	public List<Double> calcTraceLength(int vId){
		List<Double> list = new LinkedList<Double>();
		calcTraceLength(this, list, vId);
		return list;
	}
	public void  calcTraceLength(Traces traces, List<Double> l, int vId){
		for(Trace t : traces){
			if(t.getSubTraces().size() > 0 && t.getVersionId() < vId){
				calcTraceLength(t.getSubTraces(), l, vId);
				continue;
			}
			if(t.size() > 1){
				final double tmp = t.getDistance();
				if(tmp > 0)
					l.add(tmp);
			}
		}
	}
	
	public String toString(){
		return "Traces (" + traces.size() + ")";
	}
	@Override
	public Iterator<Trace> iterator() {
		Iter<Traces, Trace> iter = new Iter<Traces, Trace>(this);
		return iter;
	}
	
	
}
