package merg;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;

import trace.Point;
import trace.Trace;

public class GridRow {
	public class PointList implements Iterable<Point>{
		/**
		 * Es ist nicht unbedingt ein Trace
		 * aber man kann durch iterieren.
		 */
		private Trace list;
		
		public PointList(){
		}
		/**
		 * 
		 * @param t TODO momentan noch nicht berücksichtigt, da es fürs erste interessant ist.
		 * @param pt Der Punkt vom Trace der gespeichert werden soll.
		 */
		public PointList(Trace t, Point pt){
			list = new Trace("Für das Grid",-1);
			add(t, pt);
		}
		public void add(Trace t, Point pt){
			list.addPoint(pt);
		}
		@Override
		public Iterator<Point> iterator() {
			return list.iterator();
		}
		public int size(){
			if(list == null)
				return 0;
			else
				return list.size();
		}
		
	}
	private Map<Integer, PointList> listOfPoints;
	
	public GridRow(Integer iR, Trace t, Point pt){
		listOfPoints = new HashMap<Integer, PointList>();
		add(iR, t, pt);
	}
	
	public void add(Integer iR, Trace t, Point pt){
		if(listOfPoints.containsKey(iR)){
			listOfPoints.get(iR).add(t, pt);
		}
		else{
			listOfPoints.put(iR, new PointList(t, pt));
		}
	}
	public PointList get(Integer iR){
		if(listOfPoints.get(iR) == null){
			PointList l = new PointList();
			listOfPoints.put(iR, l);
			return l;
		}
		else
			return listOfPoints.get(iR);
	}
	public int size(){
		int tmp = 0;
		for(PointList l : listOfPoints.values()){
			tmp += l.size();
		}
		return tmp;
	}
	public int size(Integer iR){
		if(listOfPoints.containsKey(iR)){
			return listOfPoints.get(iR).size();
		}
		else{
			return 0;
		}
	}
	/**
	 * Gewichteter Mittelwert
	 */
	public int weightedMean(int maxRow){
		int numerator = 0;//Zähler
		int denominator =  0;//Nenner
		for(int i=0; i < maxRow; i++){
			int w = size(i);
			numerator += i * w;
			denominator += w;
		}
		return numerator/denominator;
	}
}
