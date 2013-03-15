package trace;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
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
	
	
	public static List<Circle> getIntersections(Traces _t, double maxDistance, int noOfIteration){		
		Integer vId = Trace.getIncrementVersionId();
		Traces t = TrcOp.getTraces(_t);
		
		Debug.syso("Start " + t.countDisplayedTraces() + " with " + t.countPoints() + " Points");
		
		Debug.syso("Init. RTree.");
		final Logger rootLogger = Logger.getRootLogger();
		rootLogger.setLevel(Level.INFO);
		rootLogger.addAppender(new ConsoleAppender(new PatternLayout("%-5p [%t]: %m%n")));
		SpatialIndex siEdges = new RTree();
		SpatialIndex siIntersections = new RTree();
		
		Properties p = new Properties();
		p.setProperty("MinNodeEntries", "10");
		p.setProperty("MaxNodeEntries", "50");
		siEdges.init(p);
		siIntersections.init(p);
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
		List<Circle> intersections = new ArrayList<Circle>();		 
		Map<Integer, List<clsIntersection>> traceToIntersection = new HashMap<Integer, List<clsIntersection>>();
		i=0;
		for(Trace t1 : t){
			j=0;
			p1 = null;
			for(Point p2 : t1){
				if(p1 == null){
					p1 = p2;
				}
				else{
					PTIntProcedure proc = new PTIntProcedure(p1);			
					com.infomatiq.jsi.Point point = new com.infomatiq.jsi.Point((float) p1.getLon(), (float) p1.getLat());
					siEdges.nearest(point, proc, (float) (PtOpPlane.dist(p1, p2)* 1.2) );
					for(Integer crtEdgeId : proc.getNearestNeighbours()){
						EdgeToTrace eTT = edgeToTrace.get(crtEdgeId);
						if(i != eTT.traceId){//Die gleiche Spur muss nicht mit sich selbst verglichen werden
							if(isEdgeIntersectEdge(intersections, t1, t.get(eTT.traceId), j, eTT.pointId)){
								List<clsIntersection> intersectionList = traceToIntersection.get(i);
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
		
		Debug.syso("Spuren teilen an " + intersections.size());
		Integer cnt = 0;
		for(Map.Entry<Integer, List<clsIntersection>> c : traceToIntersection.entrySet()){			
			cnt += splitAtIndexAndAddPoint(t.get(c.getKey()), c.getValue(), vId);
		}
		Debug.syso("Die Spuren wurden geteit an " + cnt + " Stellen geteilt.");
		
		/*
		//const float boundingBoxSize;
		Debug.syso("Schnittpunkte indecieren");
		i=0;
		for(Circle c : intersections){
			Rectangle rec = new Rectangle();
			rec.set((float) (c.pt.getLon()-c.r), (float) (c.pt.getLat()-c.r), (float) (c.pt.getLon()+c.r), (float) (c.pt.getLat()+c.r));			
			siIntersections.add(rec,i);
			i++;
		}
		
		
		Debug.syso("Schnittbereich berechnen");		
		for(i=1; i <= noOfIteration; i++)
			dedectIntersection(siEdges, siIntersections, intersections, maxDistance*i);
		
		
		Debug.syso("Berechne Nachbarschaften");
		List<PTIntProcedure> procList = new LinkedList<PTIntProcedure>();
		while(!intersections.isEmpty()){
			Point pt = intersections.pop();
			PTIntProcedure proc = new PTIntProcedure(pt);
			procList.add(proc);			
			com.infomatiq.jsi.Point point = new com.infomatiq.jsi.Point((float) pt.getLon(), (float) pt.getLat());
			si.nearest(point, proc, maxDistance);
		}
		Debug.syso("Berechne Nachbarschaften");
		LinkedList<Point> ret = new LinkedList<Point>();
		for(PTIntProcedure proc : procList){
			Debug.syso(proc.toString());
			if(proc.getCountNearestNeighbours() >= minNeighbours){
				for(Integer traceId : proc.getNearestNeighbours()){
					//Traces an der Kreuzung bzw. das Quadrat aussparen 
					Point pt = proc.getIntersectionPoint();
					ret.add(pt);
					final float r = maxDistance/2;
					double[] recX = new double[]{pt.getLon()-r,pt.getLon()+r,pt.getLon()+r,pt.getLon()-r};
					double[] recY = new double[]{pt.getLat()+r,pt.getLat()+r,pt.getLat()-r,pt.getLat()-r};
					//excludeRectangleFromTrace(t.get(traceId), recX, recY,  maxDistance, vId);
					
					//Alle Punkte die im Quadrat liegen aus der Liste von den erkannten Kreuzungen entfernen,
					//da die Kreuzung schon erkannt wurde
					for(int i=intersections.size()-1; i >= 0; i--){
						Point ptI = intersections.get(i);
						if(Geometry.isPointInsidePolygon(recX,recY, ptI.getLat(), ptI.getLon())){
							intersections.remove(i);
						}
					}
				}
			}
			
		}*/
		return intersections;
		
	}
	private static void dedectIntersection(SpatialIndex siEdges, SpatialIndex siIntersections, List<Circle> intersections, double maxDistance){
		int i=0; 
		int cntIntersection = intersections.size()-1;
		while(i < cntIntersection){
			Circle c = intersections.get(i);
			if(!c.isOverlapping){
				PTIntProcedure proc = new PTIntProcedure(c.pt);			
				com.infomatiq.jsi.Point point = new com.infomatiq.jsi.Point((float) c.pt.getLon(), (float) c.pt.getLat());
				siEdges.nearest(point, proc, (float) (c.r * 1.2));
				if(proc.getCountNearestNeighbours() > 2){
					/*
					//METHODE 1:
					List<Circle> tmpList = new ArrayList<Circle>();
					tmpList.add(c);
					for(Integer intersectionId : proc.getNearestNeighbours()){
						//Der Kreis soll nur einmal betrachtet werden
						if(!intersections.get(intersectionId).isOverlapping){
							//Die Kreiszenteren sollen nur 20 Meter von einander entfernt liegen
							double dist = PtOpPlane.dist(c.pt, intersections.get(intersectionId).pt);							
							//Debug.syso("Dist: " + dist + " < " + maxDistance);
							if(dist < maxDistance){
								tmpList.add(intersections.get(intersectionId));
								intersections.get(intersectionId).isOverlapping = true;
								c.isOverlapping = true;
							}
						}
					}
					intersections.add(minimumBoundingCircleForCircles(tmpList));
					//ENDE METHODE 1
					*/
					/**/
					//METHODE 2:
					Rectangle rec = new Rectangle();
					//Loesche den Eintrag Kreis
					rec.set((float) (c.pt.getLon()-c.r), (float) (c.pt.getLat()-c.r), (float) (c.pt.getLon()+c.r), (float) (c.pt.getLat()+c.r));
					siIntersections.delete(rec,i);					
					
					for(Integer intersectionId : proc.getNearestNeighbours()){
						Circle c2 = intersections.get(intersectionId);
						if(i != intersectionId || !c2.isOverlapping || proc.getCountNearestNeighbours() > 0){
							rec.set((float) (c2.pt.getLon()-c2.r), (float) (c2.pt.getLat()-c2.r), 
									(float) (c2.pt.getLon()+c2.r), (float) (c2.pt.getLat()+c2.r));			
							siIntersections.delete(rec,intersectionId);
							c2.isOverlapping = true;
							//c = mergCircle(c, c2);
							//c = minimalEnclosingCircle(c, c2);
							double dist = PtOpPlane.dist(c.pt, intersections.get(intersectionId).pt);							
							//Debug.syso("Dist: " + dist + " < " + maxDistance);
							//if(dist < maxDistance){
								c.isOverlapping = true;
								c = circleFrom2Circles(c, c2);
							//}							
						}					
					}
					
					
					int circleIntersectionId = intersections.size();
					intersections.add(c);						
					rec.set((float) (c.pt.getLon()), (float) (c.pt.getLat()), 
							(float) (c.pt.getLon()), (float) (c.pt.getLat()));
					
					siIntersections.add(rec, circleIntersectionId);
					//ENDE METHODE 2
					/**/ 
					
				}
			}
			i++;
			//cntIntersection = intersections.size()-1;
		}
		for(i=0; i < cntIntersection; i++){
			intersections.get(i).isOverlapping = true;
		}
	}
	static class clsIntersection implements Comparable<clsIntersection>{
		public Circle circle;
		public Integer index;
		public Integer traceId;
		public Integer tracePointIndex;
		public Point tracePoint;
		public clsIntersection(Circle circle, int intersectionPointIndex,int traceId, int tracePointIndex, Point pt) {
			this.circle=circle;
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
				double distTracePtToIter = PtOpSphere.dist(tracePoint, circle.pt);
				double distTracePtToIterFromArg = PtOpSphere.dist(tracePoint, arg0.circle.pt);
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
			return "( " + tracePointIndex + ", " + PtOpSphere.dist(tracePoint, circle.pt) + ")";
		}
		
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
				tmpTrace.addPoint(splitPoints.get(index).circle.pt);
				tmpTrace=t.addSubTraces(vId);
				tmpTrace.addPoint(splitPoints.get(index).circle.pt);
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
	 * Soll einen gewissen Bereich von den Trace ausschneiden
	 */
	private static void excludeRectangleFromTrace(Trace trace, double[] recX, double[] recY, float maxDistance, int vId){
		boolean insertFlag = true;
		boolean checkFlag = true;
		Trace t = trace.addSubTraces(vId);
		
		Point2D[] l = trace.getPoints();
		for(int i=0; i < l.length-1; i++){
			if(insertFlag){
				t.addPoint(trace.get(i));
			}
			if(checkFlag){
				double[] points = Geometry.findLinePolygonIntersections(recX, recY, l[i].getX(), l[i].getY(), l[i+1].getX(), l[i+1].getY());
				if(points == null || points.length == 0){
					continue;
				}
				else if(points.length == 2){
					t.addPoint(points[0], points[1]);
					t = trace.addSubTraces(vId);
					if(insertFlag){
						insertFlag = false;
					}
					else{
						insertFlag = true;
						checkFlag = false;
					}
				}
				else if(points.length == 4){
					checkFlag = false;
					Point p1 = new Point(points[0],points[1]);
					Point p2 = new Point(points[2],points[3]);
					if(PtOpPlane.distance(trace.get(i), p1) < PtOpPlane.distance(trace.get(i), p2)){
						t.addPoint(p1);
						t = trace.addSubTraces(vId);
						t.addPoint(p2);
					}
					else{
						t.addPoint(p2);
						t = trace.addSubTraces(vId);
						t.addPoint(p1);
					}
				}
			}
		}
	}
	
	private static void edgeIntersect(List<Circle> intersections, Trace t1, Trace t2){
		Point2D[] p1 = t1.getPoints();
		Point2D[] p2 = t2.getPoints();
		
		for(int i=0; i < (p1.length-2); i++){
			for(int s=0; s < (p2.length-2); s++){
				isEdgeIntersectEdge(intersections, p1, p2, i, s);
			}			
		}
	}
	
	private static void isEdgeIntersectEdge(List<Circle> intersections, Point2D[] p1, Point2D[] p2, int p1Index, int p2Index){
		double[] coordiants = new double[2];
		//Überschneiden die beiden Kanten sich überhaupt?
		int isIntersect = Geometry.findLineSegmentIntersection(p1[p1Index].getX(), p1[p1Index].getY(), p1[p1Index+1].getX(), p1[p1Index+1].getY(), 
							p2[p2Index].getX(), p2[p2Index].getY(), p2[p2Index+1].getX(), p2[p2Index+1].getY(), coordiants);
		if(isIntersect == 1){
			double rad = Geometry.computeAngle(coordiants, new double[]{p1[p1Index].getX(), p1[p1Index].getY()}, new double[]{p2[p2Index].getX(), p2[p2Index].getY()});
			// der kleinere Winkel zählt
			if(rad > (Math.PI/2))
				rad = Math.PI - rad;
			
			if(rad > (Math.PI/4)){
				//Ja, es liegt eine Überschneidung vor
				intersections.add(new Circle(new Point(coordiants[0], coordiants[1])));
			}
			
		}
	}
	
	private static boolean isEdgeIntersectEdge(List<Circle> intersections, Trace t1, Trace t2, int p1Index, int p2Index){
		double[] coordiants = new double[2];
		//Überschneiden die beiden Kanten sich überhaupt?
		//TODO: Die getX und getY auf getLon und getLat umstellen
		int isIntersect = Geometry.findLineSegmentIntersection(t1.get(p1Index).getLon(), t1.get(p1Index).getLat(), t1.get(p1Index+1).getLon(), t1.get(p1Index+1).getLat(), 
				t2.get(p2Index).getLon(), t2.get(p2Index).getLat(), t2.get(p2Index+1).getLon(), t2.get(p2Index+1).getLat(), coordiants);
		if(isIntersect == 1){
			double rad = Geometry.computeAngle(coordiants, new double[]{t1.get(p1Index).getLon(), t1.get(p1Index).getLat()}, new double[]{t2.get(p2Index).getLon(), t2.get(p2Index).getLat()});
			// der kleinere Winkel zählt
			if(rad > (Math.PI/2))
				rad = Math.PI - rad;
			
			if(rad > (Math.PI/4)){
				//Ja, es liegt eine Überschneidung vor
				intersections.add(new Circle(new Point(coordiants[0], coordiants[1]), t1, t2, p1Index, p2Index));
				return true;
			}
		}
		return false;
	}
	/**
	 * Eine Datenstruktur für Kreuzende Kanten.
	 * Der Begriff Kreis wird hier verwendet, wegen der minimalen Bounding Box.
	 * Die Punkte werden als Kreise verstanden und sollen nach und nach zusammen geführt werden.
	 * @author Simon Koennecke
	 */
	public static class Circle{
		/*
		 * Diese Attribute sollen helfen Kreuzungen zu erkennen.
		 */
		public Point pt;
		public double r = 0.000002;
		public int count=1;
		public boolean isOverlapping=false;
		
		/*
		 * Information über den Schnittpunkt, welche Spuren und welche Kante betroffen ist.
		 * Dies Dient zu einer einfachen Zerlegung der Spur in Teil Spuren.
		 */
		public Trace t1, t2;
		public Integer t1Index, t2Index;
		
		public Circle(){
			pt = new Point(0,0);
		}
		public Circle(Point p){
			pt=p;
		}
		public Circle(Point p, double r){
			pt=p;this.r=r;
		}
		public Circle(Point p, Trace t1, Trace t2, int t1Index, int t2Index){
			pt=p;this.t1=t1;this.t2=t2;
			this.t1Index=t1Index;this.t2Index=t2Index;
		}
		public String toString(){
			return "@Circle(" + pt+ ", "  + r + ", " + count + ")";
		}
	}
	
	private static Circle mergCircle(Circle one, Circle two){
		double dx = one.pt.getLon() - two.pt.getLon();
		double dy = one.pt.getLat() - two.pt.getLat();
		double dxSq = dx * dx;
		double dySq = dy * dy;        
		double circleDistSq = (dxSq + dySq);
		double r2 = (one.r - two.r);
	    Circle c = new Circle();
	    //check if r1 encloses r2
	    if( r2*r2 >= circleDistSq) {
	        if(one.r < two.r) {
	            c.r = two.r;
	            c.pt = two.pt;	            
	        } else {
	        	c.r = one.r;
	            c.pt = one.pt;
	        } 
	    }
	    else {
	        double circleDist = Math.sqrt(circleDistSq);
	        double r = (circleDist + one.r + two.r) / 2.;
	        c.r = r;
	        if (circleDist > 0) {
	            double f = ((r - one.r) / circleDist);
	            c.pt.setLon(one.pt.getLon() - f * dx);
	            c.pt.setLat(one.pt.getLat() - f * dy);	            
	        } else {
	            c.pt = one.pt;
	        }
	    }
	    c.count = one.count + two.count;
	    return c;
	}
	public static Circle minimalEnclosingCircle(Circle A, Circle B) {
        double angle = Math.atan2(B.pt.getLat() - A.pt.getLat(), B.pt.getLon() - A.pt.getLon());
        Point a = new Point((B.pt.getLon() + Math.cos(angle) * B.r), (B.pt.getLat() + Math.sin(angle) * B.r));
        angle += Math.PI;
        Point b = new Point((A.pt.getLon() + Math.cos(angle) * A.r), (A.pt.getLat() + Math.sin(angle) * A.r));
        double rad = Math.sqrt(Math.pow(a.getLon() - b.getLon(), 2) + Math.pow(a.getLat() - b.getLat(), 2)) / 2;
        if (rad < A.r) {
            return A;
        } else if (rad < B.r) {
            return B;
        } else {
            return new Circle( new Point(((a.getLon() + b.getLon()) / 2), ((a.getLat() + b.getLat()) / 2)), rad);
        }
    }
	private static void getAllEdges(Traces edges, Trace t, Integer vId){
		Point p1=null,p2=null;		
		for(Point pt : t){			
			if(p1 == null){
				p1 = pt;
				continue;
			}
			else if(p2 == null){
				p2 = pt;
			}
			else{
				p1 = p2;
				p2 = pt;
			}
			Trace tmp = edges.addTrace("Edge " + vId, vId);
			tmp.addPoint(p1);
			tmp.addPoint(p2);			
		}
	}

	/**
	 * Quelle: http://stackoverflow.com/questions/6976125/bounding-circle-of-set-of-circles
	 * Calculates the minimum bounding circle for a set of circles.
	 * O(n^4)
	 *
	 * @param circles A list of 2+ circles.
	 * @return {cx, cy, radius} of the circle.
	 */
	public static Circle minimumBoundingCircleForCircles(List<Circle> circles) {

	    // try every pair and triple
	    Circle  best = new Circle();
	    best.r = Double.MAX_VALUE;

	    for (int i = 0; i < circles.size(); i++) {
	        for (int j = i + 1; j < circles.size(); j++) {
	            Circle circle = circleFrom2Circles(circles.get(i), circles.get(j));
	            if (areAllCirclesInOrOnCircle(circles, circle) &&
	                circle.r < best.r) {
	                best.pt = circle.pt;
	                best.r = circle.r;
	            }

	            for (int k = j + 1; k < circles.size(); k++) {
	                int[] signs = new int[]{-1, 1, 1, 1};
	                circle = apollonius(circles.get(i), circles.get(j), circles.get(k),
	                                    signs);
	                if (areAllCirclesInOrOnCircle(circles, circle) &&
	                    circle.r < best.r) {
	                    best.pt = circle.pt;
	                    best.r = circle.r;
	                }
	            }
	        }
	    }

	    return best;
	}

    private static boolean areAllCirclesInOrOnCircle(List<Circle> circles, Circle circle) {
        for (int i = 0; i < circles.size(); i++) {
            if (!isCircleInOrOnCircle(circles.get(i), circle))
            	return false;
        }
        return true;
    };
	/**
	 * Calculates a circle from 2 circles.
	 *
	 * @param circle1 The first circle.
	 * @param circle2 The second circle.
	 * @return cx, cy, radius of the circle.
	 */
	public static Circle circleFrom2Circles(Circle circle1, Circle circle2) {

	    double angle = Math.atan2(circle1.pt.getLat() - circle2.pt.getLat(),
	                           circle1.pt.getLon() - circle2.pt.getLon());

	    Point[] lineBetweenExtrema = new Point[]{new Point(circle1.pt.getLon() + circle1.r * Math.cos(angle),
	                               circle1.pt.getLat() + circle1.r * Math.sin(angle)),
	                              new Point(circle2.pt.getLon() - circle2.r * Math.cos(angle),
	                               circle2.pt.getLat() - circle2.r * Math.sin(angle))};

	    Point center = lineMidpoint(lineBetweenExtrema[0], lineBetweenExtrema[1]);
	    
	    return new Circle(center, lineLength(lineBetweenExtrema[0],lineBetweenExtrema[1])/2);
	}
	
	
	
	/**
	 * Solve the Problem of Apollonius: a circle tangent to all 3 circles.
	 * http://mathworld.wolfram.com/ApolloniusProblem.html
	 *
	 * @param circle1 The first circle.
	 * @param circle2 The second circle.
	 * @param circle3 The third circle.
	 * @param signs The array of signs to use. [-1, 1, 1, 1] gives max circle.
	 * @return The tangent circle.
	 */
	public static Circle apollonius(Circle circle1, Circle circle2, Circle circle3, int[] signs) {

	    
	    double a1 = 2 * (circle1.pt.getLon() - circle2.pt.getLon());
	    double a2 = 2 * (circle1.pt.getLon() - circle3.pt.getLon());
	    double b1 = 2 * (circle1.pt.getLat() - circle2.pt.getLat());
	    double b2 = 2 * (circle1.pt.getLat() - circle3.pt.getLat());
	    double c1 = 2 * (signs[0] * circle1.r + signs[1] * circle2.r);
	    double c2 = 2 * (signs[0] * circle1.r + signs[2] * circle3.r);
	    double d1 = (sqr(circle1.pt.getLon()) + sqr(circle1.pt.getLat()) - sqr(circle1.r)) -
	        (sqr(circle2.pt.getLon()) + sqr(circle2.pt.getLat()) - sqr(circle2.r));
	    double d2 = (sqr(circle1.pt.getLon()) + sqr(circle1.pt.getLat()) - sqr(circle1.r)) -
	        (sqr(circle3.pt.getLon()) + sqr(circle3.pt.getLat()) - sqr(circle3.r));

	    // x = (p+q*r)/s; y = (t+u*r)/s

	    double p = b2 * d1 - b1 * d2;
	    double q = (- b2 * c1) + (b1 * c2);
	    double s = a1 * b2 - b1 * a2;
	    double t = - a2 * d1 + a1 * d2;
	    double u = a2 * c1 - a1 * c2;

	    // you are not expected to understand this.
	    // It was generated using Mathematica's Solve function.
	    double det = (2 * (-sqr(q) + sqr(s) - sqr(u)));
	    double r = (1 / det) * 
	        (2 * p * q + 2 * circle1.r * sqr(s) + 2 * t * u -
	         2 * q * s * circle1.pt.getLon() - 2 * s * u * circle1.pt.getLat() + signs[3] *
	         Math.sqrt(sqr(-2 * p * q - 2 * circle1.r * sqr(s) - 2 * t * u +
	                       2 * q * s * circle1.pt.getLon() + 2 * s * u * circle1.pt.getLat()) - 
	                   4 * (-sqr(q) + sqr(s) - sqr(u)) * 
	                   (-sqr(p) + sqr(circle1.r) * sqr(s) - sqr(t) +
	                    2 * p * s * circle1.pt.getLon() - sqr(s) * sqr(circle1.pt.getLon()) + 
	                    2 * s * t * circle1.pt.getLat() - sqr(s) * sqr(circle1.pt.getLat()))));
	    r = Math.abs(r);

	    double x = (p + q * r) / s;

	    double y = (t + u * r) / s;

	    return new Circle(new Point(x,y), r);
	}
	public static double sqr(double x){
		return Math.pow(x,2);
		
	}
	/**
	 * Is the circle inside/on another circle?
	 *
	 * @param innerCircle the inner circle.
	 * @param outerCircle the outer circle.
	 * @return is the circle inside/on the circle?
	 */
	public static boolean isCircleInOrOnCircle(Circle innerCircle, Circle outerCircle) {
		double dist = lineLength(innerCircle.pt, outerCircle.pt);
		return (dist < outerCircle.r);
	}


	/**
	 * Calculates the length of a line.
	 * @param pt1 The first pt.
	 * @param pt2 The second pt.
	 * @return The length of the line.
	 */
	public static double lineLength(Point pt1, Point pt2) {
	    return PtOpPlane.dist(pt1, pt2);
	}

	/**
	 * Calculates the midpoint of a line.
	 * @param pt1 The first pt.
	 * @param pt2 The second pt.
	 * @return The midpoint of the line.
	 */
	public static Point lineMidpoint(Point pt1,Point pt2) {
	    return new Point((pt1.getLon() + pt2.getLon()) / 2, (pt1.getLat() + pt2.getLat()) / 2);
	}
}
