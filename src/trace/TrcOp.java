package trace;

import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import cluster.AtomicStack;

import core.Debug;
import graph.GpxFile;


public class TrcOp {
	/**
	 * Schneide die Spur nach distTol ab.
	 * @param traces
	 * @param distTol
	 */
	public static void splitTraceAfterDistance(Traces traces, double distTol){
		double dist=0;//,avgDist=0;
		Trace crtTrace = null;
		Integer _vId;
		//System.out.println("Anzahl der Traces: " + traces.size());
		
		for(Trace t : traces){
			//Wurde der Pfad schon mal zerlegt?
			//Wenn ja dann nutze den zerlegten Pfad
			if(t.getSubTraces().size() > 0){
				splitTraceAfterDistance(t.getSubTraces(), distTol);
				continue;
			}
			//avgDist = t.getDistance()/t.size();
			//System.out.println("AvgDist: "+avgDist + "");
			if(crtTrace == null){
				//Erste eine neue Versionsnummer
				crtTrace = new Trace(t.getName(),null);
			}
			else{
				//Verwende die erstellte Versionsnummer
				crtTrace = new Trace(t.getName(),crtTrace.getVersionId());
			}
				
			
			Point p1 = null;
			for(Point p2 : t){				
				//Anfang der Liste
				if(p1 == null){
					p1 = p2;
					crtTrace.addPoint(p2);
					continue;
				}
				dist += PtOpSphere.distance(p1, p2);
				//System.out.println(p1+" "+p2+" Dist:"+PtOp.distance(p1, p2) );
				//Die Max. L�nge eines Traces erreicht?
				if(dist > distTol){
					dist=0;
					t.addSubTraces(crtTrace);
					crtTrace = t.addSubTraces();
					crtTrace.addPoint(p1);
				}
				crtTrace.addPoint(p2);
				p1 = p2;
			}
		}
	}
	/**
	 * Zerlege die Spur, wenn zwei Punkte die distTol �berschreitet.
	 * @param traces
	 * @param distTol �berschreiten zwei Punkte diese Distanz wird die GPS-Spur Zerlegt.
	 */
	public static void splitTraceByDistance(Traces traces, double distTol){		
		splitTraceByDistance(traces, distTol, Trace.getIncrementVersionId());
	}
	/**
	 * 
	 * @param traces
	 * @param distTol �berschreiten zwei Punkte diese Distanz wird die GPS-Spur Zerlegt.
	 * @param vId Der Trace soll diese Vorgangsnummer erhalten.
	 */
	public static void splitTraceByDistance(Traces traces, double distTol, Integer vId){
		double dist=0;//,avgDist=0;
		Trace crtTrace = null;
		System.out.println("Anzahl der Traces: " + traces.size());
		
		for(Trace t : traces){
			//Wurde der Pfad schon mal zerlegt?
			//Wenn ja dann nutze den zerlegten Pfad
			if(t.getSubTraces().size() > 0){
				splitTraceByDistance(t.getSubTraces(), distTol, vId);
				continue;
			}
			//avgDist = t.getDistance()/t.size();
			//System.out.println("AvgDist: "+avgDist + "");
			if(crtTrace == null){
				//Erste eine neue Versionsnummer
				crtTrace = new Trace(t.getName(),vId);
			}
			else{
				//Verwende die erstellte Versionsnummer
				crtTrace = new Trace(t.getName(),vId);
			}
			
			Point p1 = null;
			for(Point p2 : t){				
				//Anfang der Liste
				if(p1 == null){					
					p1 = p2;					
				}
				dist = PtOpSphere.distance(p1, p2);
				//System.out.println(p1+" "+p2+" Dist:"+PtOp.distance(p1, p2) );
				//Der Punkt f�llt aus der Tolaranz lege ein neue Spur an
				if(dist > distTol){
					t.addSubTraces(crtTrace);
					crtTrace = t.addSubTraces(vId);
					//crtTrace.addPoint(p1);
				}
				crtTrace.addPoint(p2);
				p1 = p2;
			}
		}
	}
	/**
	 * 
	 * @param traces
	 * @param angelTol
	 */
	public static void splitTraceByAngle(Traces traces, double angelTol){
		double angle=0, avgAngle=0;
		boolean startAvg = true;
		Trace crtTrace=null;
		
		for(Trace t : traces){
			//Wurde der Pfad schon mal zerlegt?
			//Wenn ja dann nutze den zerlegten Pfad
			if(t.getSubTraces().size() > 0){
				splitTraceByAngle(t.getSubTraces(),angelTol);
				continue;
			}
			//Sollte der Pfad weniger als ein Punkt haben
			//wird der �bergangen
			if(t.size() < 2){
				continue;
			}
			
			if(crtTrace == null){
				//Erste eine neue Versionsnummer
				crtTrace = new Trace(t.getName(),null);
			}
			else{
				//Verwende die erstellte Versionsnummer
				crtTrace = new Trace(t.getName(),crtTrace.getVersionId());
			}
			
			Point p1 = null;
			for(Point p2 : t){				
				//Anfang der Liste
				if(p1 == null){					
					p1 = p2;
					continue;
				}
				angle = PtOpSphere.cardinalDirection(p1, p2);
				if(startAvg){
					avgAngle = angle;
					p1 = p2;
					startAvg = false;
					continue;
				}
				
				//System.out.println(p1+" "+p2+" angle: " + angle + ", avgAngle:"+ avgAngle );
				//Der Punkt f�llt aus der Tolaranz lege ein neue Spur an
				if(avgAngle-angelTol > angle && angle < avgAngle+angelTol){
					startAvg = true;
					crtTrace = t.addSubTraces(crtTrace);
					crtTrace = t.addSubTraces();
					crtTrace.addPoint(p1);
				}
				//Gleitene Durschnitt vom Kurswinkel
				avgAngle += angle;
				avgAngle /= 2;
				crtTrace.addPoint(p2);
				p1=p2;
			}			
		}
	}
	/**
	 * Diese Funktion verringert die Anzahl der Punkt die auf einem Trace liegen.
	 * Dabei wird gepr�ft, ob der mittlere Punkt zwischen den ersten und letzten Punkt nicht au�erhalb der Toleranz liegt.
	 * Sollte es au�erhalb der Toleranz liegen wird der mittlere Punkt mit aufgenommen und das gleiche verfahren wird zwischen
	 * ersten und mittleren wie mittleren und letzten angewendet.
	 * @param trace
	 * @param tol
	 */
	public static void reduction(Traces trace, double tol){
		Integer vId = Trace.getIncrementVersionId(); 
		reduction(trace, tol, vId);
	}
	public static void reduction(Traces traces, double tol, Integer vId){		
		for(Trace t: traces){
			if(t.getSubTraces().size() > 0){
				reduction(t.getSubTraces(), tol, vId);
				continue;
			}
			System.out.print("DouglasPeuckerReduction from Points " + t.size() + " ");
			Trace tmp = DouglasPeuckerReduction(t,tol, vId);
			if(tmp != null){
				t.addSubTraces(tmp);
				System.out.println("to " + tmp.size());
			}
			else
				System.out.println("to " + t.size());
			
		}
	}
	/**
	 * Diese Funktion wird direkt von K-Means verwendet.
	 * @param Points
	 * @param Tolerance
	 * @param vId
	 * @return
	 */
	public static Trace DouglasPeuckerReduction(Trace Points, double Tolerance, Integer vId){
	    if (Points == null || Points.size() < 3)
	    return null;
	
	    int firstPoint = 0;
	    int lastPoint = Points.size() - 1;
	    Trace pointIndexsToKeep = new Trace("", vId);
	
	    //Add the first and last index to the keepers
	    pointIndexsToKeep.addPoint(Points.get(firstPoint));	    
	
	    //The first and the last point cannot be the same
	    while (Points.get(firstPoint).equals(Points.get(lastPoint))){
	        lastPoint--;
	    }
	
	    DouglasPeuckerReduction(Points, firstPoint, lastPoint, Tolerance, pointIndexsToKeep);
	    
	    pointIndexsToKeep.addPoint(Points.get(lastPoint));
	    
	    return pointIndexsToKeep;
	}
	/**
	 * Douglases the peucker reduction.
	 * @param points
	 * @param firstPoint
	 * @param lastPoint
	 * @param tolerance The tolerance.
	 * @param pointIndexsToKeep The point index to keep.
	 */
	private static void DouglasPeuckerReduction(Trace points, int firstPoint, int lastPoint, double tolerance, 
	    Trace pointIndexsToKeep)
	{
	    double maxDistance = 0, distance = Double.MIN_VALUE;
	    int indexFarthest = 0;
	    
	    //W�hle den entferntesten Punkt von der Strecke p1 und p2
	    for (int index = firstPoint; index < lastPoint; index++){
	        distance = Math.abs(PtOpPlane.alongTrackDist(points.get(firstPoint), points.get(lastPoint), points.get(index)));
	        if (distance > maxDistance) {
	            maxDistance = distance;
	            indexFarthest = index;
	        }
	    }	    
	    //Sollte der entfernteste Punkt noch in der Toleranz liegen f�ge die dazwischen liegende Punkte nicht mehr hinzu.
	    if (maxDistance > tolerance && indexFarthest != 0){
	        //Add the largest point that exceeds the tolerance
	    	DouglasPeuckerReduction(points, firstPoint, indexFarthest, tolerance, pointIndexsToKeep);
	    	pointIndexsToKeep.addPoint(points.get(indexFarthest));
	        DouglasPeuckerReduction(points, indexFarthest, lastPoint, tolerance, pointIndexsToKeep);
	    }
	}

	/**
	 * Diese Funktion schneidet GPS Spuren, wenn eine zu gro�e Distanz zwischen anfangs und end Punkt besteht.
	 * Es werden keine Punkte gel�scht. Alle Subspuren fangen mit den Endpunkt der vorherigen Spur an. 
	 * @param traces
	 * @param tol
	 * @param vId
	 */
	public static void splitTraceByDouglasPeucker(Traces traces, double tol){
		Integer vId = Trace.getIncrementVersionId();		
		splitTraceByDouglasPeucker(traces,tol,vId);
	}
	public static void splitTraceByDouglasPeucker(Traces traces, double tol, Integer vId){
		for(Trace t: traces){
			if(t.getSubTraces().size() > 0){
				splitTraceByDouglasPeucker(t.getSubTraces(), tol, vId);				
				continue;
			}
			Queue<Integer> list = new LinkedList<Integer>();
			//Ermittel alle Schnittpunkte von der Spur
			splitWithDouglasPeucker(list, t, 0, t.size()-1, tol);
			
			//Gibt es mehr als ein Schnittpunkt 
			if(list.size() > 0){
				//Alle Schnittpunkte sollen in ein separate "SubTrace"
				Trace tmpTrace = t.addSubTraces(vId);
				int i = 0, splitTraceById = list.poll();
				
				for(Point pt : t){
					tmpTrace.addPoint(pt);
					//Der Schnittpunkt ist erreicht erstelle ein neuen "SubTrace"
					if(splitTraceById == i){						
						tmpTrace = t.addSubTraces(vId);
						tmpTrace.addPoint(pt);
						//Wechsel die Schnittstelle, wenn es noch welche gibt.
						if(list.size() > 0){
							splitTraceById = list.poll();							
						}
					}
					i++;
				}
			}
		}
	}	
	/**
	 * Diese Helferfunktion stellt fest welche Punkte aus der Reihe tanzen und speichert die in eine Liste ab.
	 * @param list Speicher f�r die Ausrei�er Index in geordneter Folge
	 * @param points Der Trace der gerade inspiziert wird
	 * @param firstPoint Der Startpunkt ab den untersucht wird
	 * @param lastPoint Der Endpunkt bis wohin untersucht wird
	 * @param tolerance Die Toleranz ab wann eine Spur an den Punkt zerlegt wird
	 */
	public static void splitWithDouglasPeucker(Queue<Integer> list, Trace points, Integer firstPoint, Integer lastPoint, double tolerance){
	    if (points != null && points.size() > 2){
		    double maxDistance = 0, distance = Double.MIN_VALUE;
		    int indexFarthest = 0;
		    
		    for (int index = firstPoint; index < lastPoint; index++){
		        distance = Math.abs(PtOpPlane.alongTrackDist(points.get(firstPoint), points.get(lastPoint), points.get(index)));
		        if (distance > maxDistance) {
		            maxDistance = distance;
		            indexFarthest = index;
		        }
		    }
		    
		    if (maxDistance > tolerance && indexFarthest != 0){
		        //Add the largest point that exceeds the tolerance
		    	splitWithDouglasPeucker(list, points, firstPoint, indexFarthest, tolerance);
		    	list.add(indexFarthest);
		    	splitWithDouglasPeucker(list, points, indexFarthest, lastPoint, tolerance);
		    }
	    }
	}
	/**
	 * Schneidet alle Punkte ab die au�erhalb von der min - tol und max - tol liegt, x wie y Koordinaten.
	 * @param traces Die Spuren die auf der Fl�che sind
	 * @param tol Der Faktor zu Verkleinerung von der Fl�che
	 */
	public static void resizePlain(Traces traces, double tol){
		traces.calcExtrema();
		Point min = traces.getMin();
		Point max = traces.getMax();
		System.out.println("Min: " + min + ", Max: " + max);
		//min x ,y und max x,y;
		//double border[] = {min.getX() + tol, min.getY() + tol, max.getX() - tol, max.getY() - tol};
		
		double border[] = {min.getLon() + tol, min.getLat() + tol, max.getLon() - tol, max.getLat() - tol};
		
		System.out.println("Borders (" + border[0] + ", " + border[1] + ", " + border[2] + ", " + border[3] + ")");
		resizePlainHelper(traces, tol, border,Trace.getIncrementVersionId());
		
		traces.calcExtrema();
		min = traces.getMin();
		max = traces.getMax();
		System.out.println("Min: " + min + ", Max: " + max);
		
	}
	/**
	 * 
	 * @param traces
	 * @param tol
	 * @param border
	 * @param vId
	 */
	private static void resizePlainHelper(Traces traces, double tol, double border[], Integer vId){
		Trace tmp = null;
		for(Trace t : traces){
			if(t.getSubTraces().size() > 0){
				resizePlainHelper(t.getSubTraces(), tol, border, vId);
			}
			else{
				if(tmp == null){
					//Erste eine neue Versionsnummer
					tmp = new Trace(t.getName(),vId);
				}
				else{
					//Verwende die erstellte Versionsnummer
					tmp = new Trace(t.getName(),vId);
				}
				for(Point pt : t){
					//liegt der Punkt au�erhalb vom Rahmen?
					//if((pt.getX() > border[0] && pt.getX() < border[2]) && (pt.getY() > border[1] && pt.getY() < border[3])){
					if((pt.getLon() > border[0] && pt.getLon() < border[2]) && (pt.getLat() > border[1] && pt.getLat() < border[3])){
						//Der Punkt liegt innerhalb des Rahmens
						tmp.addPoint(pt);
					}				
					else{
						//Der Punkt liegt au�erhalb vom Rahmen.
						//Lege ein neuen Trace an und verwerfe den aktuellen Punkt
						if(tmp.size() > 0)
							t.addSubTraces(tmp);
						tmp = new Trace(t.getName(),vId);
					}
				}
				if(tmp.size() > 0)
					t.addSubTraces(tmp);
			}
		}
	}
	
	public static List<Point> getIntersections(Traces _t){
		//Integer vId = Trace.getIncrementVersionId();
		Traces t = getTraces(_t);
		Debug.syso("Start " + t.countDisplayedTraces() + " with " + t.countPoints() + " Points");
		/*
		//Alle Kanten bekommen einen eigenen Trace:
		Traces edges = new Traces();
		for(Trace trace : t){
			getAllEdges(edges, trace, vId);
		}
		Debug.syso("Edges " + edges.countDisplayedTraces() + " with " + edges.countPoints() + " Points");
		*/
		//Ermittelt alle Schnittpunkte:
		//Alle zu berechnende Paar auflisten
		List<Point> intersections = new LinkedList<Point>();
		//AtomicStack stack = new AtomicStack();
		//AtomicInteger stackCounter = new AtomicInteger();
		for(int i=0; i < t.size(); i++){
			for(int s=i+1; s < t.size(); s++){
				if(i != s)
					edgeIntersect(intersections, t.get(i), t.get(s));
				//stack.push(i, s);
				//stackCounter.incrementAndGet();
				
			}
		}
		/*
		Debug.syso("Es m�ssen " + stackCounter.get() + " intersections berechnet werden.");
		while(stack.empty()){
			int[] calc = stack.pop();
			edgeIntersect(intersections, edges.get(calc[0]), edges.get(calc[1]));
			stackCounter.decrementAndGet();
		}
		*/
		return intersections;
		
	}
	private static void edgeIntersect(List<Point> intersections, Trace t1, Trace t2){
		Point2D[] p1 = t1.getPoints();
		Point2D[] p2 = t2.getPoints();
		
		for(int i=0; i < (p1.length-2); i++){
			for(int s=0; s < (p2.length-2); s++){
				isEdgeIntersectEdge(intersections, p1, p2, i, s);
			}			
		}
	}
	private static void isEdgeIntersectEdge(List<Point> intersections, Point2D[] p1, Point2D[] p2, int p1Index, int p2Index){
		double[] coordiants = new double[2];
		//�berschneiden die beiden Kanten sich �berhaupt?
		int isIntersect = Geometry.findLineSegmentIntersection(p1[p1Index].getX(), p1[p1Index].getY(), p1[p1Index+1].getX(), p1[p1Index+1].getY(), 
							p2[p2Index].getX(), p2[p2Index].getY(), p2[p2Index+1].getX(), p2[p2Index+1].getY(), coordiants);
		if(isIntersect == 1){
			//Ja, es liegt eine �berschneidung vor
			intersections.add(new Point(coordiants[0], coordiants[1]));
		}
	}
	private static boolean samePoints(Point2D[] p1, Point2D[] p2){
		if(p1[1].getX() == p2[0].getX() && p1[1].getY() == p2[0].getY())
			return true;
		else
			return false;
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
	 * Ermittelt alle zu clusterene Traces.
	 * @param t Die Traces nach dem vereinfachen.
	 * @return liefert in einem "Traces" alle zu clusteren Traces
	 */
	public static Traces getTraces(Traces t){
		Traces tmp = new Traces();
		getTraces(t, tmp);
		return tmp;
	}
	/**
	 * Helfer Funktion von getTraces(Traces t)
	 * @param _t siehe Hauptfunktion
	 * @param store hier werden alle Traces in einer Ebene gespeichert, die geclustert werden sollen.
	 */
	private static void getTraces(Traces _t, Traces store){
		for(Trace t : _t){		  
			if(t.getSubTraces().size()>0){
				getTraces(t.getSubTraces(),store);
				continue;
			}
			if(t.size() < 2)
				continue;
			store.addTrace(t);
		}
	}
	/**
	 * L�schen den letzten Vorgang.
	 * @param traces Das B�ndel von Traces von dem die vorherige Version wiederhergestellt werden soll.
	 */
	public static void redo(GpxFile file){
		redo(file.getTraces(), Trace.getDecrementVersionId()-1);		
	}
	/**
	 * L�scht den Vorgang X
	 * @param traces Das B�ndel von dem die Vorgangsnummer gel�scht werden soll
	 * @param vId Die Vorgangsnummer X
	 */
	public static void redo(Traces traces, Integer vId){	
		for(int i=traces.size()-1;i >= 0 ; i--){
			if(traces.get(i).getSubTraces().size() > 0){
				redo(traces.get(i).getSubTraces(), vId);
				continue;
			}
			if(traces.get(i).getVersionId() == vId){
				traces.remove(i);
			}
		}
	}
}
