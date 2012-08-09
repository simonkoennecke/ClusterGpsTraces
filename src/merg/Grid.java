package merg;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

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
	private Double cRaster , rRaster;
	/**
	 * Welche Kachelung das Grid hat auf basis der Traces
	 */
	private Double lonRaster , latRaster;
	/**
	 *  
	 */
	private Trace meanTrace, upperDeviation, lowerDeviation;
	/**
	 * 
	 * @param t
	 * @param r
	 * @param c
	 */
	public Grid(Traces t, Integer r, Integer c){
		this.t = t;
		this.r = r;
		this.c = c;
		grid = new HashMap<Integer, GridRow>();
		
		t.calcExtrema();
		maxPt = t.getMax();
		minPt = t.getMin();
		cRaster = (maxPt.getX() - minPt.getX()) / c;
		rRaster = (maxPt.getY() - minPt.getY()) / r;
		
		lonRaster = (maxPt.getLon() - minPt.getLon()) / c;
		latRaster = (maxPt.getLat() - minPt.getLat()) / r;
		
		addTraces(t);
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
		int[] start = calcPointGridCoordinats(t, p1);
		int[] end	= calcPointGridCoordinats(t, p2);
		
		int[] step = getDirection(start, end);
		
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
		}
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
	
	public int[] calcPointGridCoordinats(Trace t, Point pt){
		int iR,iC; // insert in Row- or Column- Id
		//iR = (int) Math.floor((pt.getY() - minPt.getY()) / rRaster);
		//iC = (int) Math.floor((pt.getX() - minPt.getX()) / cRaster);
		iR = (int) Math.floor((pt.getLat() - minPt.getLat()) / latRaster);
		iC = (int) Math.floor((pt.getLon() - minPt.getLon()) / lonRaster);
		return new int[]{iC,iR};
	}
	
	public void addPoint(Trace t, int iC, int iR, Point pt){
		if(grid.containsKey(iC)){
			grid.get(iC).add(iR, t, pt);
		}
		else{
			grid.put(iC, new GridRow(iR,t, pt));
		}
		System.out.println("(" + iC + "," + iR + ")");
		//return new int[]{iC,iR};
	}
	
	public GridRow getRow(int column){
		if(grid.containsKey(column))
			return grid.get(column);
		else
			return null;
	}
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
