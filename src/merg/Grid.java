package merg;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import com.vividsolutions.jts.algorithm.LineIntersector;

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
		//Füge den Anfangspunkt hinzu.
		int[] start = calcPointGridCoordinats(p1);
		int[] end	= calcPointGridCoordinats(p2);
		
		int[] step = getDirection(start, end);
		
		for(int x=start[0];x<end[0];x += step[0]){
			for(int y=start[1];y<end[1];y += step[1]){
				//Obere linke Ecke
				double top[] = {x*latRaster, y*lonRaster};
				//untere rechte Ecke
				double buttom[] = {(x+1)*latRaster, (y+1)*lonRaster};
				if(Geometry.isLineIntersectingRectangle(p1.getLat(), p1.getLon(), p2.getLat(), p2.getLon(), top[0], top[1], buttom[0], buttom[1])){
					addPoint(t, x, y, new Point(minPt.getLon() + x * lonRaster, minPt.getLon() + y * lonRaster));
				}
			}
		}
		
		/*
		//Liegt start und end Punkt mindestens zwei Zellen entfernt?
		if(dist(start,end) > 1){
			int x = start[0], y=start[1];
			boolean wX=true, wY=true;
			//System.out.println("Schritte (x: " + step[0] + ",y: " + step[1] + ")");
			while(wX && wY){
				if(x == end[0]){
					wX = false;
				}
				else{
					x += step[0];
				}
				if(y == end[1]){
					wY = false;
				}
				else{
					y += step[1];
				}
				
				//System.out.println("X: (" + x + ", " + start[0] + ", " + end[0] + "), Y: (" + y + ", " + start[1] + ", " + end[1] + ").");
				//addPoint(t, new Point(p1.getX() + x * dX, p1.getY() + y * dY, true));
				addPoint(t, x, y, new Point(p1.getLon() + x * lonRaster, p1.getLon() + y * latRaster));
			}
		}
		*/
	}

	/**
	 * Berechnet die Schrittweiten von X und Y.
	 * @param start Die Raster Koordinaten für den Anfangspunkt der Kante
	 * @param end Die Raster Koordinaten für den Endpunkt der Kante
	 * @return int[0] ist die Schrittweite von X und int[1] ist die Schrittweite von Y
	 */
	private int[] getDirection(int[] start, int[] end){
		int[] r = {0,0};
		//ist x größer als y?
		int dX = (end[0]-start[0]), dY = (end[1] - start[1]);
		int algSign[] = {1,1};
		if(dX != 0)
			algSign[0] = dX/Math.abs(dX);
		if(dY != 0)
			algSign[1] = dY/Math.abs(dY);
		
		dX = Math.abs(dX);
		dY = Math.abs(dY);
		if(dX > dY){
			if(dY == 0)
				r[0] = 0;
			else
				r[0] = algSign[0];
			r[1] = algSign[1];
		}
		//ist y ist größer als x
		else if (dX < dY){
			r[0] = 1 * algSign[0];
			if(dX == 0)
				r[1] = 0;
			else
				r[1] = algSign[1];
		}
		else{
			r[0] = algSign[0];
			r[1] = algSign[1]; 
		}
		//Hier werden Zeilen oder Spalten ausgelassen
		/*
		 * |---|---|---|
		 * | 1 |   |   | X\
		 * |---|---|---|   \
		 * |   |   | 1 |    \>
		 * |---|---|---|
		 */
		/*
  		dX = Math.abs(dX);
		dY = Math.abs(dY);
		if(dX > dY){
			if(dY == 0)
				r[0] = 0;
			else
				r[0] = (dX/dY) * algSign[0];
			r[1] = 1 * algSign[1];
		}
		//ist y ist größer als x
		else if (dX < dY){
			r[0] = 1 * algSign[0];
			if(dX == 0)
				r[1] = 0;
			else
				r[1] = (dY/dX) * algSign[1];
		}
		else{
			r[0] = algSign[0];
			r[1] = algSign[1]; 
		}*/
		
		return r;
	}
	/**
	 * Distanzfunktion auf dem Raster
	 */
	private int dist(int[] p1, int[] p2){
		int cDist = Math.abs(p2[0] - p1[0]);
		int rDist = Math.abs(p2[1] - p1[1]);
		return Math.max(cDist, rDist);
		
	}
	
	public int[] calcPointGridCoordinats(Point pt){
		int iR,iC; // insert in Row- or Column- Id
		iC = (int) Math.floor((pt.getLat() - minPt.getLat()) / latRaster);
		iR = (int) Math.floor((pt.getLon() - minPt.getLon()) / lonRaster);
		return new int[]{iC,iR};
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
		//System.out.println("(" + iC + "," + iR + ")");
		//return new int[]{iC,iR};
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
}
