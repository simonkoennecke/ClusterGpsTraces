package trace;

import java.util.ArrayList;
import java.util.Iterator;

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
	
	public int countPoints(){
		int cnt=0;
		for(Trace t : this){
			cnt += t.size();
		}
		return cnt;
	}
	public double countDisplayedTraces(){
		return countDisplayedTraces(this, 0);
	}
	private double countDisplayedTraces(Traces traces,double cnt){
		for(Trace t : traces){
			if(t.getSubTraces().size() == 0){
				cnt += 1;
			}
			else{
				cnt += countDisplayedTraces(t.getSubTraces(),cnt);
			}
		}
		return cnt;
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
