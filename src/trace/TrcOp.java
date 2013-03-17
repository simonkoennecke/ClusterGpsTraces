package trace;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicInteger;

import cluster.AtomicStack;

import com.infomatiq.jsi.*;
import com.infomatiq.jsi.rtree.RTree;
import com.infomatiq.jsi.rtree.SortedList;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import trace.FindTraceIntersections.Box;

import core.Debug;
import gnu.trove.TIntProcedure;
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
				//Die Max. Länge eines Traces erreicht?
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
	 * Zerlege die Spur, wenn zwei Punkte die distTol überschreitet.
	 * @param traces
	 * @param distTol Überschreiten zwei Punkte diese Distanz wird die GPS-Spur Zerlegt.
	 */
	public static void splitTraceByDistance(Traces traces, double distTol){		
		splitTraceByDistance(traces, distTol, Trace.getIncrementVersionId());
	}
	/**
	 * 
	 * @param traces
	 * @param distTol Überschreiten zwei Punkte diese Distanz wird die GPS-Spur Zerlegt.
	 * @param vId Der Trace soll diese Vorgangsnummer erhalten.
	 */
	public static void splitTraceByDistance(Traces traces, double distTol, Integer vId){
		double dist=0;//,avgDist=0;
		Trace crtTrace = null;
		
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
				//System.out.println(p1+" "+p2+" Dist:"+dist );
				//Der Punkt fällt aus der Tolaranz lege ein neue Spur an
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
			//wird der übergangen
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
				//Der Punkt fällt aus der Tolaranz lege ein neue Spur an
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
	 * Zerlegt eine Spur am Punkt Index und fügt pt zum ersten und zweiten Abschnitt.
	 * @param t Trace an den die Operation durchgeführt werden soll
	 * @param index Die Indexnummer an dem die Spur zerlegt werden soll
	 * @param pt Der Schnittpunkt der zwischen die beiden neuen Spuren hinzugefügt wird 
	 * @param vId Die Versionsnummer für die neuen SubTraces
	 */
	public static void splitAtIndexAndAddPoint(Trace t, int index, Point pt, int vId){
		Trace tmpTrace=t.addSubTraces(vId);
		for(int i=0; i < t.size()-1; i++){
			tmpTrace.addPoint(t.get(i));
			if(i==index){
				tmpTrace.addPoint(pt);
				tmpTrace=t.addSubTraces(vId);
				tmpTrace.addPoint(pt);				
			}
		}
	}
	/**
	 * Diese Funktion verringert die Anzahl der Punkt die auf einem Trace liegen.
	 * Dabei wird geprüft, ob der mittlere Punkt zwischen den ersten und letzten Punkt nicht außerhalb der Toleranz liegt.
	 * Sollte es außerhalb der Toleranz liegen wird der mittlere Punkt mit aufgenommen und das gleiche verfahren wird zwischen
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
			//Debug.sysoWithoutLn("DouglasPeuckerReduction from Points " + t.size() + " ");
			Trace tmp = DouglasPeuckerReduction(t,tol, vId);
			if(tmp != null){
				t.addSubTraces(tmp);
				//Debug.syso("to " + tmp.size());
			}
			else{
				//Debug.syso("to " + t.size());
			}
			
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
	    
	    //Wähle den entferntesten Punkt von der Strecke p1 und p2
	    for (int index = firstPoint; index < lastPoint; index++){
	        distance = Math.abs(PtOpPlane.alongTrackDist(points.get(firstPoint), points.get(lastPoint), points.get(index)));
	        if (distance > maxDistance) {
	            maxDistance = distance;
	            indexFarthest = index;
	        }
	    }	    
	    //Sollte der entfernteste Punkt noch in der Toleranz liegen füge die dazwischen liegende Punkte nicht mehr hinzu.
	    if (maxDistance > tolerance && indexFarthest != 0){
	        //Add the largest point that exceeds the tolerance
	    	DouglasPeuckerReduction(points, firstPoint, indexFarthest, tolerance, pointIndexsToKeep);
	    	pointIndexsToKeep.addPoint(points.get(indexFarthest));
	        DouglasPeuckerReduction(points, indexFarthest, lastPoint, tolerance, pointIndexsToKeep);
	    }
	}

	/**
	 * Diese Funktion schneidet GPS Spuren, wenn eine zu große Distanz zwischen anfangs und end Punkt besteht.
	 * Es werden keine Punkte gelöscht. Alle Subspuren fangen mit den Endpunkt der vorherigen Spur an. 
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
	 * @param list Speicher für die Ausreißer Index in geordneter Folge
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
	 * Schneidet alle Punkte ab die außerhalb von der min - tol und max - tol liegt, x wie y Koordinaten.
	 * @param traces Die Spuren die auf der Fläche sind
	 * @param tol Der Faktor zu Verkleinerung von der Fläche
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
					//liegt der Punkt außerhalb vom Rahmen?
					//if((pt.getX() > border[0] && pt.getX() < border[2]) && (pt.getY() > border[1] && pt.getY() < border[3])){
					if((pt.getLon() > border[0] && pt.getLon() < border[2]) && (pt.getLat() > border[1] && pt.getLat() < border[3])){
						//Der Punkt liegt innerhalb des Rahmens
						tmp.addPoint(pt);
					}				
					else{
						//Der Punkt liegt außerhalb vom Rahmen.
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
	
	public static List<Box> getIntersections(Traces _t, double maxDistance, int noOfIteration){
		return FindTraceIntersections.getIntersections(_t, maxDistance, noOfIteration);
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
	 * Löschen den letzten Vorgang.
	 * @param traces Das Bündel von Traces von dem die vorherige Version wiederhergestellt werden soll.
	 */
	public static void redo(GpxFile file){
		redo(file.getTraces(), Trace.getDecrementVersionId()-1);		
	}
	/**
	 * Löscht den Vorgang X
	 * @param traces Das Bündel von dem die Vorgangsnummer gelöscht werden soll
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
