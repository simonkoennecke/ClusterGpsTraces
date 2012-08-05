package cluster;

import java.util.Stack;
import java.util.concurrent.Semaphore;

public class AtomicStack {
	class Entry{
		public Integer centroidId, traceId;
		public Entry(Integer centroidId, Integer traceId){
			this.centroidId = centroidId;
			this.traceId = traceId;
		}
	}
	private Stack<Entry> stack;
	private Semaphore s;
	
	public AtomicStack(){
		stack = new Stack<Entry>();
		s = new Semaphore(1);
	}
	
	public void push(Integer centroidId, Integer traceId){
		s.acquireUninterruptibly();
		stack.add(new Entry(centroidId, traceId));
		s.release();
	}
	public int[] pop(){
		s.acquireUninterruptibly();
		Entry e = stack.pop();
		int[] t = new int[]{e.centroidId, e.traceId};
		s.release();
		return t;
	}
	public boolean empty(){
		s.acquireUninterruptibly();
		boolean t = stack.empty();
		s.release();
		return t;
	}
	
	
}
