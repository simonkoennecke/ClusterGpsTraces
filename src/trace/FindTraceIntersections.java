package trace;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Properties;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.jmx.Agent;


import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;
import com.infomatiq.jsi.rtree.SortedList;

import core.Debug;

/**
 * Diese Klasse dient nur als Auslagerung der Funktion zur Kreuzungserkennung
 * @author Simon
 *
 */
public class FindTraceIntersections {
	
	public static class EdgeToTrace{
		public Integer pointId;
		public Integer traceId;
		public EdgeToTrace(Integer traceId, Integer pointId){
			this.pointId = pointId; this.traceId = traceId;
		}
		public String toString(){
			return "(" + traceId + ", " + pointId + ")";
		}
	}
	
	
	public static List<Box> getIntersections(Traces _t){		
		Integer vId = Trace.getIncrementVersionId();
		Traces t = TrcOp.getTraces(_t);
		
		Debug.syso("Start " + t.countDisplayedTraces() + " with " + t.countPoints() + " Points");
		
		Debug.syso("Init. RTree.");
		final Logger rootLogger = Logger.getRootLogger();
		rootLogger.setLevel(Level.INFO);
		rootLogger.addAppender(new ConsoleAppender(new PatternLayout("%-5p [%t]: %m%n")));
		SpatialIndex siEdges = new RTree();
		
		Properties p = new Properties();
		p.setProperty("MinNodeEntries", "10");
		p.setProperty("MaxNodeEntries", "50");
		siEdges.init(p);
		Map<Integer, EdgeToTrace> edgeToTrace = new HashMap<Integer, EdgeToTrace>();
		
		Debug.syso("Alle Kanten in den Index einfuegen.");
		int edgeId=0, i=0, j=0;//i = traceId, j = pointId
		Point p1;
		for(Trace trace : t){
			j=0; p1 = null;
			for(Point p2 : trace){
				if(p1 == null){
					p1 = p2;
				}
				else{
					Rectangle rec = new Rectangle();
					rec.set((float) p1.getLon(), (float) p1.getLat(), (float) p2.getLon(), (float) p2.getLat());			
					siEdges.add(rec, edgeId);				
					edgeToTrace.put(edgeId, new EdgeToTrace(i, j));
					j++;edgeId++;
					p1 = p2;
					
				}				
			}
			i++;
		}
		
		Debug.syso("Ermittel alle Schnittpunkte");
		List<Box> intersections = new ArrayList<Box>();		 
		Map<Integer, List<clsIntersection>> traceToIntersection = new HashMap<Integer, List<clsIntersection>>();
		i=0;//Trace Id
		for(Trace t1 : t){
			j=0;//Point Index
			p1 = null;
			for(Point p2 : t1){
				if(p1 == null){
					p1 = p2;
				}
				else{
					PTIntProcedure proc = new PTIntProcedure(p1);			
					com.infomatiq.jsi.Point point = new com.infomatiq.jsi.Point((float) p1.getLon(), (float) p1.getLat());
					siEdges.nearest(point, proc, (float) (PtOpPlane.dist(p1, p2)* 1.2) );
					//Prüfe ob die nahe liegende Kanten schneiden
					for(Integer crtEdgeId : proc.getNearestNeighbours()){
						//Hole die Information über die Kante (beim RTree kann man nur ids hinterlegen und keine Referenzen...)
						EdgeToTrace eTT = edgeToTrace.get(crtEdgeId);
						//Die gleiche Spur muss nicht mit sich selbst verglichen werden
						if(i != eTT.traceId){
							//Schneiden sich die beiden Kanten?
							if(isEdgeIntersectEdge(intersections, t1, t.get(eTT.traceId), j, eTT.pointId)){
								//Ja und füge zur Map "traceToIntersection" hinzu, um eine einfach Trennung an den Punkt vor zu nehmen.
								List<clsIntersection> intersectionList = traceToIntersection.get(i);
								
								//Gib den Index der
								Integer intersectionPointIndex = intersections.size()-1;
								if(intersectionList != null)
									intersectionList.add(new clsIntersection(
														intersections.get(intersectionPointIndex), 
														intersectionPointIndex, i, j, p1)
													);
								else{
									intersectionList = new ArrayList<clsIntersection>();
									intersectionList.add(new clsIntersection(
															intersections.get(intersectionPointIndex), 
															intersectionPointIndex, i, j, p1)
														);
									traceToIntersection.put(i, intersectionList);
								}								
							}
						}
					}
					p1 = p2;
					j++;
				}
				
			}
			i++;
		}
		
		//Zerlege die Spuren
		Debug.syso("Spuren teilen an " + intersections.size());
		Integer cnt = 0;
		for(Map.Entry<Integer, List<clsIntersection>> c : traceToIntersection.entrySet()){			
			cnt += splitAtIndexAndAddPoint(t.get(c.getKey()), c.getValue(), vId);
		}
		Debug.syso("Die Spuren wurden geteit an " + cnt + " Stellen geteilt.");
		
		
		
		return intersections;
		
	}
	
	/**
	 * Zerlegt eine Spur an den splitPoints
	 * @param t
	 * @param splitPoints 
	 * @param vId Die Versions für die neuen Subtraces
	 * @return Anzahl der Spurtrenung die durch geführt wurden
	 */
	public static int splitAtIndexAndAddPoint(Trace t, List<clsIntersection> splitPoints, int vId){
		//Die Kreuzende Punkte müssen geordnet werden
		Collections.sort(splitPoints);
		//Neuen Subtrace anlegen (es kann ggf. ein Subtrace erzeugt werden, obwohl keine veränderung vorliegt)
		Trace tmpTrace=t.addSubTraces(vId);
		//c ist der Counter wie oft tatsächlich getrennt wurde
		int c = 0;
		//Index auf die nächste Trennung
		int index = 0;
		int crtIndex = splitPoints.get(index).tracePointIndex;
		//Die Flag zeigt an, ob weitere Trennungen für diese Linie Vorliegen
		boolean flag = true;
		for(int i=0; i < t.size()-1; i++){
			tmpTrace.addPoint(t.get(i));
			//Eine Kannte öfteres geschnietten werden, daher while
			while(i == crtIndex){
				c += 1;
				tmpTrace.addPoint(splitPoints.get(index).box.pt);
				tmpTrace=t.addSubTraces(vId);
				tmpTrace.addPoint(splitPoints.get(index).box.pt);
				//Sind noch Trennungen vorhanden
				if(flag && ++index == splitPoints.size()){
					index=splitPoints.size(); // Es soll ein Fehler produzieren, wenn der index nochmal verwendet wird.
					crtIndex = -1;//Darf nicht mehr auf ein gültigen Index zeigen
					flag = false;
				}
				//nächsten Trennungspunkt
				else{
					crtIndex = splitPoints.get(index).tracePointIndex;
				}
			}
		}
		
		return c;
	}
	
	/**
	 * 
	 * @param intersections
	 * @param t1
	 * @param t2
	 * @param p1Index
	 * @param p2Index
	 * @return
	 */
	private static boolean isEdgeIntersectEdge(List<Box> intersections, Trace t1, Trace t2, int p1Index, int p2Index){
		double[] coordiants = new double[2];
		//Überschneiden die beiden Kanten sich überhaupt?
		int isIntersect = Geometry.findLineSegmentIntersection(t1.get(p1Index).getLon(), t1.get(p1Index).getLat(), t1.get(p1Index+1).getLon(), t1.get(p1Index+1).getLat(), 
				t2.get(p2Index).getLon(), t2.get(p2Index).getLat(), t2.get(p2Index+1).getLon(), t2.get(p2Index+1).getLat(), coordiants);
		if(isIntersect == 1){
			double rad = Geometry.computeAngle(coordiants, new double[]{t1.get(p1Index).getLon(), t1.get(p1Index).getLat()}, new double[]{t2.get(p2Index).getLon(), t2.get(p2Index).getLat()});
			// der kleinere Winkel zählt
			if(rad > (Math.PI/2))
				rad = Math.PI - rad;
			
			if(rad > (Math.PI/4)){
				//Ja, es liegt eine Überschneidung vor
				intersections.add(new Box(new Point(coordiants[0], coordiants[1])));
				return true;
			}
		}
		return false;
	}
	/**
	 * 
	 * @param intersections
	 * @param noOfIteration
	 * @return
	 */
	public static List<Box> findMinimalBoundindBoxes(List<Box> intersections, int noOfIteration){
		Debug.syso("Init. RTree.");
		final Logger rootLogger = Logger.getRootLogger();
		rootLogger.setLevel(Level.INFO);
		rootLogger.addAppender(new ConsoleAppender(new PatternLayout("%-5p [%t]: %m%n")));
		SpatialIndex siIntersections = new RTree();
		
		Properties p = new Properties();
		p.setProperty("MinNodeEntries", "10");
		p.setProperty("MaxNodeEntries", "50");
		siIntersections.init(p);
		
		Debug.syso("Schnittpunkte indecieren");
		for(int i=0; i < intersections.size(); i++){
			siIntersections.add(intersections.get(i).rec,i);
		}
		
		return mergBoundingBox(siIntersections, intersections, noOfIteration);
	}
	
	/**
	 * 
	 * @param siIntersections
	 * @param intersections
	 * @param noOfIteration
	 */
	public static List<Box> mergBoundingBox(SpatialIndex siIntersections, List<Box> intersections, int noOfIteration){
		if(noOfIteration == 0)
			return intersections;
		
		Debug.syso("Berechne Anzahl der Nachbarschaften.");
		List<PTIntProcedure> procList = new ArrayList<PTIntProcedure>();
		for(int i=0; i < intersections.size(); i++){
			findNeighbours(siIntersections, intersections, procList, intersections.get(i));
		}
		
		Debug.syso("Füge benachbarte Bounding Boxen zusammen.");
		//Fange mit den Punkten an mit den meisten Nachbarn
		Collections.sort(procList, Collections.reverseOrder());
		PTIntProcedure proc;
		Box a, b;
		int count = procList.size(); 
		for(int i=0; i < count; i++){
			proc = procList.get(i);
			a = intersections.get(i);
			if(!a.isOverlapping){
				for(Integer intersectionsId : proc.getNearestNeighbours()){
					b = intersections.get(intersectionsId);
					mergBox(siIntersections, i, intersectionsId, a, b);
				}
			}
		}
		int isO = 0;
		int isNO = 0;
		for(Box box : intersections){
			if(box.isOverlapping)
				isO ++;
			else
				isNO++;
		}
		Debug.syso("Boxen("+isO+","+isNO+")");
		
		return mergBoundingBox(siIntersections, intersections, --noOfIteration);
	}
	/**
	 * 
	 * @param siIntersections
	 * @param intersections
	 * @param procList
	 * @param b
	 */
	public static void findNeighbours(SpatialIndex siIntersections, List<Box> intersections, List<PTIntProcedure> procList, Box b){
		PTIntProcedure proc = new PTIntProcedure(b.pt);
		procList.add(proc);			
		siIntersections.nearest(b.rec.centre(), proc, b.getRedius());
	}
	/**
	 * 
	 * @param siIntersections
	 * @param i
	 * @param intersectionsId
	 * @param a
	 * @param b
	 * @param noRec
	 */
	private static boolean mergBox(SpatialIndex siIntersections, int i, int intersectionsId, Box a, Box b){
		if(!b.isOverlapping && i != intersectionsId){ 
			if(a.rec.intersects(b.rec) || a.rec.containedBy(b.rec) || a.rec.contains(b.rec) ){
				//Lösche die beiden alten Einträge ausm Index
				siIntersections.delete(b.rec, intersectionsId);
				siIntersections.delete(a.rec, i);
				
				//Füge merge a und b zu c und Speicher es in a
				a.merg(b);
				//Füge die neue Box a in den Index hinzu.
				siIntersections.add(a.rec, i);
				
				return true;
			}
		}
		return false;
		
	}
	/**
	 * Diese Klasse wird zum Trennen von den Spuren verwendet.
	 * Dazu werden alle nötige Information hier abgelegt und 
	 * eine Sortierung für eine schnelle Trennung stellt diese Klasse ebenfalls
	 * zur Verfügung.
	 * @author Simon Könnecke
	 */
	static class clsIntersection implements Comparable<clsIntersection>{
		public Box box;
		public Integer index;
		public Integer traceId;
		public Integer tracePointIndex;
		public Point tracePoint;
		public clsIntersection(Box box, int intersectionPointIndex,int traceId, int tracePointIndex, Point pt) {
			this.box=box;
			index=intersectionPointIndex;
			this.tracePointIndex = tracePointIndex;
			this.tracePoint = pt;
		}
		/**
		 * Die Funktion hilft bei der Erstellung von der Schnittreihenfolge
		 * einer Spur.
		 * Als erstes wird der Kanten Index verglichen, sollte der gleich sein
		 * ist die Distanz vom Kantenanfang das nachfolgende Kriterium
		 */
		@Override
		public int compareTo(clsIntersection arg0) {
			if(arg0.tracePointIndex != tracePointIndex)
				return tracePointIndex.compareTo(arg0.tracePointIndex);
			else{
				double distTracePtToIter = PtOpSphere.dist(tracePoint, box.pt);
				double distTracePtToIterFromArg = PtOpSphere.dist(tracePoint, arg0.box.pt);
				double s = distTracePtToIter - distTracePtToIterFromArg;
				if(s == 0)
					return 0;
				else if(s < 0){
					return -1;
				}
				else if(s > 0){
					return 1;
				}
			}
			return 0;
		}
		public String toString(){
			//return "@Intrsec(" + index + "," + circle +")";
			return "( " + tracePointIndex + ", " + PtOpSphere.dist(tracePoint, box.pt) + ")";
		}
		
	}
	
	public static void killDouble(){
		
	}
	
	/**
	 * Eine Datenstruktur für Kreuzende Kanten.
	 * Der Begriff Box wird hier verwendet, wegen der minimalen Bounding Box.
	 * Die Punkte werden als Kreise verstanden und sollen nach und nach zusammen geführt werden.
	 * @author Simon Koennecke
	 */
	public static class Box{
		/*
		 * Diese Attribute sollen helfen Kreuzungen zu erkennen.
		 */
		public Point pt;
		public int count=1;
		public boolean isOverlapping=false;
		public Rectangle rec = new Rectangle();
		public Box parent;
		
		public Box(Box b){
			pt = b.pt;
			//r = b.r;
			count = b.count;
			rec = b.rec;
		}
		public Box(){
			this(new Point(0,0));
		}
		public Box(Point p){
			this(p, 0.00007);
		}
		public Box(Point p, double r){
			pt=p;//this.r=r;
			rec.set((float) (pt.getLon()-r), (float) (pt.getLat()-r), (float) (pt.getLon()+r), (float) (pt.getLat()+r));
		}
		public void merg(Box b){
			b.parent = getRootParent();
			b.isOverlapping = true;
			rec.add(b.rec);
			count++;
			com.infomatiq.jsi.Point p = rec.centre();
			pt.setLat(p.x);
			pt.setLon(p.y);
		}
		private Box getRootParent(){
			if(parent != null){
				Box p = parent;
				while(p.parent != null){
					p = p.parent;
				}
				return p;
			}
			else{
				return this;
			}
		}
		/**
		 * Radius ist vielleicht nicht der richtige Begriff.
		 * @return
		 */
		public float getRedius(){
			return Math.max(rec.height(), rec.width())/2;
		}
		public String toString(){
			return "@Box(" + pt+ ", "  + getRedius() + ", " + count + ")";
		}
		
		
	}
	
	
}
