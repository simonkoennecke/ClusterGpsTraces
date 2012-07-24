package trace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

public class GpxLoader {
	private String _path;
	private StreamSource stream;
	public GpxLoader(String path) {
		_path = path;
		stream = new StreamSource( _path );
	}
	public GpxLoader(String path, boolean loadLocal) {
		_path = path;
		stream = new StreamSource(this.getClass().getResourceAsStream(path));
	}
	
	public Traces getTraces() throws XMLStreamException{
		Traces t = new Traces();
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLEventReader  evRd = inputFactory.createXMLEventReader( stream );
		Stack<String>   stck = new Stack<String>();
		Trace tmpTrace = new Trace();
		double lon=0, lat=0;
		
		while( evRd.hasNext() ) {
			XMLEvent ev = evRd.nextEvent();
			if( ev.isStartElement() ) {
				stck.push( ev.asStartElement().getName().getLocalPart() );
				
				if(ev.asStartElement().getName().getLocalPart() == "trk"){
					tmpTrace = t.addTrace();					
				}
				//TODO: Namen mit importieren
				/*if(ev.asStartElement().getName().getLocalPart() == "name"){
					tmpTrace.setName(ev.asCharacters().getData());					
				}*/
				if(ev.asStartElement().getName().getLocalPart() == "trkpt"){
					Iterator<Attribute> iter = ev.asStartElement().getAttributes();
					while( iter.hasNext() ) {
			        	Attribute a = iter.next();
			        	if(a.getName().getLocalPart() == "lat")
		        			lat = Double.valueOf(a.getValue());
			        	else if(a.getName().getLocalPart() == "lon")
			        		lon = Double.valueOf(a.getValue());			        	
			        }
					tmpTrace.addPoint(lon, lat);
				}
			}			
			if(ev.isEndElement()) 
				stck.pop();
		}
		return t;
	}
	
	private static String buildXPathString( Stack<String> stck, String postfix ){
      StringBuffer sb = new StringBuffer();
      for( String s : stck ) sb.append( "/" ).append( s );
      sb.append( postfix );
      return sb.toString();
   }
}
