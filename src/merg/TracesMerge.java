package merg;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import trace.*;

public class TracesMerge {
	private Traces setTraces, setFilledTraces = new Traces();
	
	private Trace merg = new Trace();
	
	public void set(Traces t){
		setTraces = t;
	}
	/**
	 * Berechne eine neue Spur f�r die gegebene Spuren.
	 */
	public void run(){
		//Berechne den durchschnittlichen Kurswinkel,
		//maximale Punktanzahl f�r jeden Trace.
		double meanDirection = 0;
		int maxPoints = 0;
		if(setTraces == null || setTraces.size() == 0){
			return;
		}
		for(Trace t : setTraces){
			meanDirection = t.getMeanCardinalDirection();
			maxPoints = Math.max(maxPoints, t.size());					
		}
		meanDirection = meanDirection / setTraces.size();
		
		//1. Schritt: Spuren "Normalisieren". D.h. gleichm��ig Abst�nde zwischen den Punkten bei den Traces.
		fillingTraces(meanDirection, maxPoints);
		
		//2. Schritt: Spuren zusammenfassen
		List<Iterator<Point>> iterList = new LinkedList<Iterator<Point>>();
		for(Trace t : setFilledTraces){
			iterList.add(t.iterator());
		}
		
		/*
		//ungewichteter mittelwert
		for(int steps=0; steps < maxPoints; steps++){
			double lat=0, lon=0;
			for(Iterator<Point> crtTrace : iterList){
				Point pt = crtTrace.next();
				lat += pt.getLat(); lon += pt.getLon();
			}
			lat = lat / setFilledTraces.size();
			lon = lon / setFilledTraces.size();
			merg.addPoint(new Point(lon, lat));
		}
		*/
		for(int steps=0; steps < maxPoints; steps++){
			double lat=0, lon=0;
			long cntWeights=0;
			int i = 0;
			for(Iterator<Point> crtTrace : iterList){
				Point pt = crtTrace.next();
				cntWeights += setTraces.get(i).size();
				lat += setTraces.get(i).size() * pt.getLat(); lon += setTraces.get(i).size() * pt.getLon();
				
			}
			lat = lat / cntWeights;
			lon = lon / cntWeights;
			merg.addPoint(new Point(lon, lat));
		}
	}
	/**
	 * Die Spuren brauchen gleich viele Punkte und eine Richtung in den die Spuren traversiert werden. 
	 * @param meanDirection Der Mittlerekurswinkel
	 * @param maxPoints Die zu erreichende Punktanzahl auf einer Spur
	 */
	private void fillingTraces(double meanDirection, int maxPoints){
		for(Trace t : setTraces){
			double minDistance = 0;
			minDistance = Math.max(minDistance,t.getMinDistance());
			
			 
			//Der Trace hat noch nicht gen�gend Punkte...
			if(t.size() < maxPoints){
				Trace tmp = fillingUpTraceWithPoints(t, maxPoints);
				setFilledTraces.addTrace(tmp);
				setTraceTraversierung(tmp, meanDirection);
				setFilledTraces.addTrace(tmp);
			}
			//F�ge den Trace ohne eine �nderung hinzu.
			else{
				try {
					//Da die Traversierung ge�ndert wird und 
					//die Originaldaten sonst durcheinander kommen muss ein clone erzeugt werden.
					Trace tmp = (Trace) t.clone();
					setTraceTraversierung(tmp, meanDirection);
					setFilledTraces.addTrace(tmp);
				} catch (CloneNotSupportedException e) {
					System.out.println("TracesMerge: Konnte den Trace nicht clonen.");
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
		}
	}
	/**
	 * Diese Funktion f�gt bei den l�ngsten Teilstrecken einen neuen Punkt hinzu.
	 * Sollten nicht gen�gend Punkte hinzugef�gt worden sein, wird es rekursiv wiederholt.
	 * @param t Der Trace der noch zu wenige Punkte hat.
	 * @param maxPoint Die zu erreichende Punktanzahl aufm Trace
	 * @return Der gleiche Trace wie t nur mit der Anzahl von maxPoint von Punkten
	 */
	private Trace fillingUpTraceWithPoints(Trace t, int maxPoint){
		PriorityQueue<TracesMerge.DistPoints> q = new PriorityQueue<TracesMerge.DistPoints>();
		Point p1 = null;
		int i=0;
		for(Point p2 : t){
			if(p1 != null)
				q.add(new DistPoints(PtOpSphere.dist(p1, p2), i-1));
			p1 = p2;
			i++;
		}
		//Tempor�re Trace
		Trace tmp = new Trace();
		//Anzahl der hinzuzuf�gende Punkte
		int toAddPoints = maxPoint - t.size();
		i=0;
		DistPoints el = q.poll();
		while(tmp.size() <= maxPoint){
			tmp.addPoint(t.get(i));
			if(toAddPoints > 0){
				//Es sollen erst die l�ngsten Teilstrecken einen zus�tzlichen Punkt erhalten
				if(el != null && i == el.index){					
					double lonMin = Math.min(t.get(i).getLon(), t.get(i+1).getLon()), lonMax = Math.max(t.get(i).getLon(), t.get(i+1).getLon()); 
					double lon = lonMin + ((lonMax - lonMin)/2);
					double latMin = Math.min(t.get(i).getLat(), t.get(i+1).getLat()), latMax = Math.max(t.get(i).getLat(), t.get(i+1).getLat());
					double lat = latMin + ((latMax - latMin)/2);
					tmp.addPoint(new Point(lon, lat));
					toAddPoints--;					
					el = q.poll();
				}
			}
			
			i++;
			if(i >= t.size())
				break;
		}
		if(toAddPoints > 0)			
			return fillingUpTraceWithPoints(tmp, maxPoint);
		else
			return tmp;

	}
	/**
	 * 
	 * @param t
	 * @param meanDirection
	 */
	private void setTraceTraversierung(Trace t, double meanDirection){
		double traceMeanDirection = t.getMeanCardinalDirection();
		
		if(((meanDirection - 90) % 360) > traceMeanDirection && ((meanDirection + 90) % 360) < traceMeanDirection)
			t.setTraversierung(true);		
		else
			t.setTraversierung(false);
	}
	public Trace get(){
		return merg;
	}
	/**
	 * Die Datenstruktur f�r die Priority Queue
	 * @author Simon
	 */
	class DistPoints implements Comparable<DistPoints>{
		public double dist;
		public int index;
		public DistPoints(double dist, int index){
			this.dist = dist; this.index = index;
		}
		@Override
		public int compareTo(DistPoints o) {
			return Double.compare(this.dist, o.dist);
		}
		
	}
}
