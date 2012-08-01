package merg;

import trace.Traces;

public class TracesMergeGrid implements TracesMerge{
	private Traces t;
	
	private Grid g;
	
	@Override
	public void set(Traces t) {
		this.t = t;		
	}

	@Override
	public boolean run() {
		
		return true;
	}

	@Override
	public Traces get() {
		return t;
	}
	
}
