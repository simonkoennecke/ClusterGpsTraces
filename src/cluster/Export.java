package cluster;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import trace.Point;
import trace.Trace;
import trace.Traces;

public class Export {
	public static void ClustersToGpx(Cluster c, String filename){
		try {
			File file = new File(filename);
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write("<?xml version='1.0' encoding='UTF-8'?>\r\n"+
						"<gpx version=\"1.1\" creator=\"Cluster GPS Traces\" xmlns=\"http://www.topografix.com/GPX/1/1\"\r\n"+
							"\t  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"+ 
							"\t  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\r\n");
			
			for(int i=0; i<c.getSize(); i++){
				bw.write("\t<trk>\r\n");
				bw.write(traceToString(c.getCentroid(i)));
				Traces traces = c.getTraces(i);
				for(Trace t: traces){
					bw.write(traceToString(t));
				}
				bw.write("\t</trk>\r\n");				
			}
			bw.write("</gpx>\r\n");
			bw.close();
 
			System.out.println("Cluster stored to file: " + filename);
 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String traceToString(Trace t){
		String str = ("\t\t<trkseg>\r\n");
		for(Point p: t){
			str += ("\t\t\t<trkpt lat=\"" + p.getLat() + "\" lon=\"" + p.getLon() + "\"></trkpt>\r\n");
		}
		str += ("\t\t</trkseg>\r\n");
		return str;
	}
	
}
