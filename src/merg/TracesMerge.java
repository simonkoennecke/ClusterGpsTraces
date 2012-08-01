package merg;

import trace.Traces;

public interface TracesMerge {
	public void set(Traces t);
	public boolean run();
	public Traces get();
}
