package trace;


public class TrcOp {
	public static void splitTraceByDistance(Traces traces, double distTol){
		
		double dist=0;//,avgDist=0;
		Trace crtTrace;
		System.out.println("Anzahl der Traces: " + traces.size());
		
		for(Trace t : traces){
			//Wurde der Pfad schon mal zerlegt?
			//Wenn ja dann nutze den zerlegten Pfad
			if(t.getSubTraces().size() > 0){
				splitTraceByDistance(t.getSubTraces(), distTol);
				continue;
			}
			//avgDist = t.getDistance()/t.size();
			//System.out.println("AvgDist: "+avgDist + "");
			crtTrace = new Trace();	 
			
			Point p1 = null;
			for(Point p2 : t){				
				//Anfang der Liste
				if(p1 == null){					
					p1 = p2;					
				}
				dist = PtOpSphere.distance(p1, p2);
				//System.out.println(p1+" "+p2+" Dist:"+PtOp.distance(p1, p2) );
				//Der Punkt fällt aus der Tolaranz lege ein neue Spur an
				if(dist > distTol){
					t.addSubTraces(crtTrace);
					crtTrace = t.addSubTraces();
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
		Trace crtTrace;
		
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
			
			crtTrace = new Trace();	 
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
	
	public static void reduction(Traces traces, double tol){
		for(Trace t: traces){
			if(t.getSubTraces().size() > 0){
				reduction(t.getSubTraces(), tol);
				continue;
			}
			System.out.print("DouglasPeuckerReduction from Points " + t.size() + " ");
			Trace tmp = DouglasPeuckerReduction(t,tol);
			if(tmp != null){
				t.addSubTraces(tmp);
				System.out.println("to " + tmp.size());
			}
			else
				System.out.println("to " + t.size());
			
		}
	}
	public static Trace DouglasPeuckerReduction(Trace Points, double Tolerance){
	    if (Points == null || Points.size() < 3)
	    return null;
	
	    int firstPoint = 0;
	    int lastPoint = Points.size() - 1;
	    Trace pointIndexsToKeep = new Trace();
	
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
	 *  Douglases the peucker reduction.
	 * @param points
	 * @param firstPoint
	 * @param lastPoint
	 * @param tolerance The tolerance.
	 * @param pointIndexsToKeep The point index to keep.
	 */
	private static void DouglasPeuckerReduction(Trace points, int firstPoint, int lastPoint, double tolerance, 
	    Trace pointIndexsToKeep)
	{
	    double maxDistance = 0;
	    int indexFarthest = 0;
	    
	    //Wähle den entferntesten Punkt von der Strecke p1 und p2
	    for (int index = firstPoint; index < lastPoint; index++){
	        double distance = Math.abs(PtOpPlane.alongTrackDist(points.get(firstPoint), points.get(lastPoint), points.get(index)));
	        if (distance > maxDistance) {
	            maxDistance = distance;
	            indexFarthest = index;
	        }
	    }	    
	    //Sollte der entfernteste Punkt noch in der Toleranz liegen füge die dazwischen Liegene Punkte nicht mehr hinzu.
	    if (maxDistance > tolerance && indexFarthest != 0){
	        //Add the largest point that exceeds the tolerance
	    	DouglasPeuckerReduction(points, firstPoint, indexFarthest, tolerance, pointIndexsToKeep);
	    	pointIndexsToKeep.addPoint(points.get(indexFarthest));
	        DouglasPeuckerReduction(points, indexFarthest, lastPoint, tolerance, pointIndexsToKeep);
	    }
	}
	
	/**
	 * 
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
		resizePlainHelper(traces, tol, border);
		
		traces.calcExtrema();
		min = traces.getMin();
		max = traces.getMax();
		System.out.println("Min: " + min + ", Max: " + max);
		
	}
	public static void resizePlainHelper(Traces traces, double tol, double border[]){
		for(Trace t : traces){
			if(t.getSubTraces().size() > 0){
				resizePlainHelper(t.getSubTraces(), tol, border);
			}
			else{
				Trace tmp = new Trace();
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
						tmp = new Trace();
					}
				}
				if(tmp.size() > 0)
					t.addSubTraces(tmp);
			}
		}
	}
}
