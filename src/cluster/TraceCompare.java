package cluster;

import trace.Trace;

public interface TraceCompare {
	
	/**
	 * Die Methode soll zwei Traces vergleich und die �nhlichkeit
	 * in einer Zahl ausdr�cken.
	 * @return Hier soll ein �hnlichkeitsma� zur�ckgegebn werden
	 */
	public Double compareTo(Trace t1, Trace t2);
}
