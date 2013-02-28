package merg;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import merg.GridRow.PointList;

import com.vividsolutions.jts.algorithm.LineIntersector;

import core.Debug;

import trace.*;

public class Grid {
	/**
	 * Die Stammdaten für das Grid
	 */
	private Traces t;
	/**
	 * Die Datenstruktur für das Grid
	 */
	private HashMap<Integer, GridRow> grid = new HashMap<Integer, GridRow>();
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
	private Double lonRaster , latRaster;
	
	/**
	 *  
	 */
	private Trace meanTrace, upperDeviation, lowerDeviation;
	/**
	 * Das Gitter erzeugen ohne Angabe welche Punkte eingefügt werden.
	 * @param r Anzahl der Zeilen
	 * @param c Anzahl der Spalten
	 */
	public Grid(Integer r, Integer c){
		this.r = r;
		this.c = c;
	}
	/**
	 * 
	 * @param t
	 * @param minCellSize
	 */
	public Grid(Traces t, double minCellSize){
		this.t = t;
		t.calcExtrema();
		maxPt = t.getMax();
		minPt = t.getMin();
		r = (int) Math.floor(PtOpSphere.distance(minPt, new Point(maxPt.getLon(),minPt.getLat())) / minCellSize);
		c = (int) Math.floor(PtOpSphere.distance(minPt, new Point(minPt.getLon(),maxPt.getLat())) / minCellSize);
		setup();
		addTraces(t);
	}
	/**
	 * Sollte das Gitter auf Traces aufbauen, wir automatisch alles erzeugt.
	 * @param t Traces die in diesem Grid zu finden sind
	 * @param r Anzahl der Zeilen
	 * @param c Anzahl der Spalten
	 */
	public Grid(Traces t, Integer r, Integer c){
		this.t = t;
		this.r = r;
		this.c = c;
		
		t.calcExtrema();
		maxPt = t.getMax();
		minPt = t.getMin();
		setup();
		addTraces(t);
	}
	public void setMaxPt(Point maxPt){
		this.maxPt = maxPt;
	}
	public void setMinPt(Point minPt){
		this.minPt = minPt;
	}
	public Point getMaxPt() {
		return maxPt;
	}
	public Point getMinPt() {
		return minPt;
	}
	public void setup(){
		lonRaster = (maxPt.getLon() - minPt.getLon()) / r;
		latRaster = (maxPt.getLat() - minPt.getLat()) / c;
	}
	public int getRowNo(){
		return r;
	}
	public int getColumnNo(){
		return c;
	}
	public void addTraces(Traces traceList){
		//int i=0;
		//Debug.syso("Add Traces to Grid:");
		for(Trace t: traceList){
			if(t.getSubTraces().size()>0){
				addTraces(t.getSubTraces());
				continue;
			}
			addTrace(t);
			//if(i%30 == 0)
			//	Debug.syso(i + " von " + traceList.size());
			//i++;
		}
	}
	
	public void addTrace(Trace t){
		Point p2;
		Point p1 = t.get(0);
		for(int i = 1; i < t.size(); i++){
			p2 = t.get(i);
			addEdge(t, p1, p2);
			p1 = p2;
		}
	}
	
	/**
	 * Fügt die Kante zum Raster hinzu. Dafür werden alle Punkte, die die Kante beschreibt
	 * in dem Raster erzeugt und hinzugefügt
	 * @param t Die Spure von dem das Punktepaar abstammt
	 * @param p1 Der Anfangspunkt von der Kante
	 * @param p2 Der Endpunkt von der Kante
	 */
	public void addEdge(Trace t, Point p1, Point p2){
		//Berechne in Welchen Grid der Anfangspunkt und Endpunkt fällt
		int[] start = calcPointGridCoordinats(p1);
		int[] end	= calcPointGridCoordinats(p2);
		
		int[] step = {end[0]-start[0],end[1]-start[1]};
		step[0] = (step[0]==0)?1:step[0]/Math.abs(step[0]);
		step[1] = (step[1]==0)?1:step[1]/Math.abs(step[1]);
		
		for(int x=start[0];x!=(end[0]+step[0]);x += step[0]){
			for(int y=start[1];y!=(end[1]+step[1]);y += step[1]){
				//Obere linke Ecke
				double top[] = {minPt.getLon()+x*lonRaster, minPt.getLat()+y*latRaster};
				//untere rechte Ecke
				double buttom[] = {top[0]+lonRaster, top[1]+latRaster};
				//Schneidet die Kante das Rechteck?
				if(Geometry.isLineIntersectingRectangle(p1.getLon(), p1.getLat(), p2.getLon(), p2.getLat(), top[0], top[1], buttom[0], buttom[1])){
					//Ja füge den Punkt zur Zelle hinzu.
					addPoint(t, x, y, new Point(minPt.getLon() + x * lonRaster, minPt.getLon() + y * lonRaster));					
				}
			}
		}		
	}
	
	public int[] calcPointGridCoordinats(Point pt){
		int[] r ={(int) Math.floor((pt.getLon() - minPt.getLon()) / lonRaster), (int) Math.floor((pt.getLat() - minPt.getLat()) / latRaster)};
		return r;
	}
	/**
	 * Füge ein Punkt hinzu mit bekannten Indices für Spalte und Zeile
	 * @param t
	 * @param iC
	 * @param iR
	 * @param pt
	 */
	private void addPoint(Trace t, int iC, int iR, Point pt){
		if(grid.containsKey(iC)){
			grid.get(iC).add(iR, t, pt);
		}
		else{
			grid.put(iC, new GridRow(iR,t, pt));
		}		
	}
	/**
	 * Füge einen Punkt ins Grid ein.
	 * @param t Der Trace nur als Referenz
	 * @param pt Der Punkt der Hinzugefügt wird
	 */
	public void addPoint(Trace t, Point pt){
		int[] cell = calcPointGridCoordinats(pt);
		addPoint(t, cell[0], cell[1], pt);
	}
	/**
	 * Liefert die Zeile zurück.
	 * @param column Die Spaltennummer die erfragt wird
	 * @return GridRow mit den Häufigkeiten, wie viele Punkte in der jeweiligen Zeile liegen
	 */
	public GridRow getRow(int colNo){
		if(grid.containsKey(colNo))
			return grid.get(colNo);
		else
			return null;
	}
	/**
	 * Berechnen des Centroid für die Hinzugefügten Traces.
	 * Centroid soll der geometrische Schwerpunkt aller Traces werden,
	 * das soll nicht als Punkt aufgefasst werden, sondern als eine Polyliene.
	 * @return Trace mit den Punkten
	 */
	public Trace calcMeanTrace(){
		int[] col = new int[c];
		boolean[] colAdd = new boolean[c];
		for(int j = 0; j < c; j++){
			//Wo liegt der Mittlere Y-Wert in dieser Spalte?
			if(grid.containsKey(j)){
				col[j] = grid.get(j).weightedMean(r);
				colAdd[j] = true;
			}
			//Spalte existiert nicht
			else {
				col[j] = 0;
				colAdd[j] = false;
			}
		}
		
		meanTrace = new Trace();
		for(int j=0; j < c; j++){
			if(colAdd[j]){
				meanTrace.addPoint(new Point(minPt.getLon() + j * lonRaster,
							minPt.getLat() + col[j] * latRaster));
			}
		}
		return meanTrace;
	}
	public Double getLonRaster() {
		return lonRaster;
	}
	public Double getLatRaster() {
		return latRaster;
	}
	public int[] getAllSizeOfCells(){
		int r[] = new int[getRowNo()*getColumnNo()];
		for(int i=0; i < getRowNo(); i++){
			for(int j=0; j < getColumnNo(); j++){
				PointList list = getRow(i).get(j);
				if(list != null)
					r[i*j] = list.size();
				else
					r[i*j] = 0;
			}
		}
		return r;
	}
}
