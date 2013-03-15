package graph;


import javax.xml.stream.XMLStreamException;

import core.Debug;
import trace.*;

/**
 * Diese Klasse hilft kapselen vom laden einer Datei 
 * @author Simon
 *
 */
public class GpxFile {
	private Traces traces;
	
	public GpxFile(String file){		
		  try {
			  GpxLoader l = new GpxLoader(file);
			  traces = l.getTraces();
		  } catch (Exception e) {
			  Debug.syso("Die GPX Datei konnte nicht eingelesen werden.");
		  }		  
		  Debug.syso("Number of Traces: " + traces.size() + "");		  
	}
	public GpxFile(String file, boolean loadLocal){		
		  try {
			  GpxLoader l = new GpxLoader(file, loadLocal);
			  traces = l.getTraces();
		  } catch (XMLStreamException e) {
			  Debug.syso("Die GPX Datei konnte nicht eingelesen werden.");
		  }		  
		  Debug.syso("Number of Traces: " + traces.size() + "");		  
	}
	public GpxFile(){
		//this("C:\\Users\\Simon\\workspace\\ClusterGpsTraces\\GpxFiles\\test04.gpx");
		//this("C:\\Users\\Simon\\workspace\\ClusterGpsTraces\\GpxFiles\\berlin.gpx");
		this("/berlin.gpx", true);
	}
	
	public Traces getTraces(){
		return traces;
	}
}
