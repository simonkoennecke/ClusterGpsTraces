package graph;

import merg.Grid;
import processing.core.PApplet;

/**
 * 
 * @author Simon
 *
 */
public class GridGraph extends PApplet {
	private static final long serialVersionUID = -3631156700571199647L;
	private int maxWindowWidth = 1024, maxWindowHeight = 800;
	private static int windowBorder = 40;
	private Grid grid;
	
	public GridGraph(Grid grid, int w, int h) {
		this.grid = grid;
		setSize(w, h);
	}
	public void setGpxFile(GpxFile _gpx){
		setSize(maxWindowHeight + windowBorder, maxWindowWidth + windowBorder);;
	    setup(); 
	    redraw();
		
	}
	public void setup(){
		size(maxWindowWidth,maxWindowHeight);

		frameRate(1);
		smooth();
		noLoop();
	}
	
	public void setSize(int w, int h){
		maxWindowHeight = h - windowBorder;
		maxWindowWidth = w - windowBorder;
		resize(w, h);
	}
	
	public void draw(){
		drawGrid(new float[]{windowBorder,windowBorder}, new float[]{maxWindowWidth-windowBorder,maxWindowHeight-windowBorder});
	}
	
	public void drawGrid(float[] s, float[] e){
		float dX = max(s[0],e[0])-min(s[0],e[0])/grid.getColumnNo();
		float dY = max(s[1],e[1])-min(s[1],e[1])/grid.getColumnNo();
		float d = 0;
		for(int i=0; i < grid.getRowNo(); i++){
			d = dY * i;
			line(s[0],s[1]*d,e[0]*d,e[1]);
		}
		for(int j=0; j < grid.getColumnNo(); j++){
			d = dX * j;
			line(s[0]*d,s[1],e[0]*d,e[1]);
		}
		float midX = dX/2, midY = dY/2;
		for(int i=0; i < grid.getRowNo(); i++){
			for(int j=0; j < grid.getColumnNo(); j++){
				text(String.valueOf(grid.getRow(j).get(i)), s[0]*i+midX,s[1]*j+midY);
			}
		}
				
	}
}
