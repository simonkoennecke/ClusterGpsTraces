package trace;

import java.util.Iterator;

public class Iter<E extends IterInterface<T>, T> implements Iterator<T>{
	private E _list;
	private int crtPt = 0;
	
	Iter(E traces){
		_list = traces;
	}
	
	@Override
	public boolean hasNext() {		
		return crtPt < _list.size();
	}

	@Override
	public T next() {
		return _list.get(crtPt++);
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}
	
}
