package merg;

import java.util.HashMap;

import trace.*;

public class Grid {
	/**
	 * Die Stammdaten für das Grid
	 */
	private Traces t;
	/**
	 * Die Datenstruktur für das Grid
	 */
	private HashMap<Integer, GridRow> grid;
	/**
	 * Anzahl der Spalten und Zeilen des Gitters
	 */
	private final Integer r, c;
	/**
	 * Die Linke untere und Rechte obere Punkte
	 */
	private Point maxPt, minPt;
	/**
	 * Welche Kachelung das Grid hat auf basis der Traces
	 */
	private Double xRaster , yRaster;
	
	public Grid(Traces t, Integer r, Integer c){
		this.t = t;
		this.r = r;
		this.c = c;
		grid = new HashMap<Integer, GridRow>();
		
		t.calcExtrema();
		maxPt = t.getMax();
		minPt = t.getMin();
		xRaster = (maxPt.getX() - minPt.getX()) / r;
		yRaster = (maxPt.getY() - minPt.getY()) / c;
		
	}
	
	public void addTraces(Traces traceList){
		for(Trace t: traceList){
			if(t.getSubTraces().size()>0){
				addTraces(t.getSubTraces());
			}
			addTrace(t);
		}
	}
	
	public void addTrace(Trace t){
		Point p2;
		Point p1 = t.get(0);
		for(int i = 1; i < t.size(); i++){
			p2 = t.get(i);
			addEdge(p1, p2);
			p1 = p2;
		}
	}
	
	public void addEdge(Point p1, Point p2){
		Double dX = (p2.getX() - p1.getX()) / w;
		Double dY = (p2.getY() - p1.getY()) / h;
		
		Point tpt = p1;
		
		while(){
			
		}
	}
	
	public void addPoint(Point pt){
		
	}
}
