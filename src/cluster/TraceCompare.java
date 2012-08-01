package cluster;

import trace.Trace;

public interface TraceCompare {
	
	/**
	 * Die Methode soll zwei Traces vergleich und die Änhlichkeit
	 * in einer Zahl ausdrücken.
	 * @return Hier soll ein Ähnlichkeitsmaß zurückgegebn werden
	 */
	public Double compareTo(Trace t1, Trace t2);
}
