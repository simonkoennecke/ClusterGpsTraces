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
	public String filename;
	
	/**
	 * Load File from Hard Drive
	 * @param file Path to file
	 */
	public GpxFile(String file){		
		  this(file,false);	  
	}
	/**
	 * Lade GPX Datei vom jar
	 * @param file Path in Jar-File
	 * @param loadLocal 
	 */
	public GpxFile(String file, boolean loadLocal){		
		  try {
			  filename = file;
			  if (filename.contains("\\")) {
			      filename = filename.substring(filename.lastIndexOf("\\"), filename.length());			      
			  }
			  if(filename == "")
				  filename = file;
			  GpxLoader l = new GpxLoader(file, loadLocal);
			  traces = l.getTraces();
		  } catch (XMLStreamException e) {
			  Debug.syso("Die GPX Datei konnte nicht eingelesen werden.");
		  }
		  Debug.syso("File Name=" + filename);
		  Debug.syso("Number of Traces: " + traces.countDisplayedTraces() + "");		  
	}
	public GpxFile(){
		this("/berlin.gpx", true);
	}
	
	public Traces getTraces(){
		return traces;
	}
}
