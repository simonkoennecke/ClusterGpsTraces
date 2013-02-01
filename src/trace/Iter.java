package trace;

import java.util.Iterator;

public class Iter<E extends IterInterface<T>, T> implements Iterator<T>{
	private E _list;
	private int crtPt = 0;
	private boolean traversierung = true;
	
	Iter(E traces){
		_list = traces;
	}
	/**
	 * Gebe die Liste von hinten rum aus, wenn traversierung == false ist.
	 * @param traces
	 * @param traversierung
	 */
	Iter(E traces, boolean traversierung){
		_list = traces;
		this.traversierung = traversierung;
		if(!traversierung)
			crtPt = _list.size()-1;
	}
	
	@Override
	public boolean hasNext() {
		if(traversierung)
			return crtPt < _list.size();
		else
			return crtPt > -1;
	}

	@Override
	public T next() {
		if(traversierung)
			return _list.get(crtPt++);
		else
			return _list.get(crtPt--);
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}
	
}
