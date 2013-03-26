package graph;

import java.util.Arrays;
import java.util.Collections;

import core.Debug;
import merg.Grid;
import merg.GridRow.PointList;

/**
 * 
 * @author Simon
 *
 */
public class GridGraph{
	private Grid grid;
	private MainGraph g;
	private float[] s,e;
	private float dX,dY;
	private boolean paintGrid=true,paintNumbers=false,paintDensity=true;
	
	public GridGraph(MainGraph g) {
		this.g = g;
	}	
	public GridGraph(MainGraph g, Grid grid) {
		this.g = g;
		this.grid = grid;
	}	
	public void setGrid(Grid grid){
		this.grid = grid;
	}
	public Grid getGrid(){
		return grid;
	}
	
	public void draw(){
		
		s = new float[]{(float) grid.getMinPt().getLon(),(float) grid.getMinPt().getLat()};
		e = new float[]{(float) grid.getMaxPt().getLon(),(float) grid.getMaxPt().getLat()};
		dX = grid.getLonRaster().floatValue();
		dY = grid.getLatRaster().floatValue();
		
		if(paintGrid)
			drawGrid();
		if(paintDensity)
			paintDensity();
		if(paintNumbers)
			drawNumbers();
		
	}
	
	public void drawGrid(){
		float d = 0;
		//Paint Rows
		g.stroke(g.color(190,190,190, 102));
		for(int i=0; i <= grid.getRowNo(); i++){
			d = dX * i;			
			g.dLine(s[0]+d,s[1],s[0]+d,e[1]);
		}
		//Paint Columns
		for(int j=0; j <= grid.getColumnNo(); j++){
			d = dY * j;			
			g.dLine(s[0],s[1]+d,e[0],s[1]+d);
		}						
	}
	public void drawNumbers(){
		float midX = dX/2, midY = dY/2;
		g.textSize(7);
		g.fill(150);
		for(int i=0; i < grid.getRowNo(); i++){
			for(int j=0; j < grid.getColumnNo(); j++){
				int listSize = grid.getRow(i).get(j).size();
				String listSizeStr = String.valueOf(listSize);
				
				if(listSize>9)
					g.text(listSizeStr, g.lon(s[0]+dX*i+midX)-3,g.lat(s[1]+dY*j+midY)+2);
				else
					g.text(listSizeStr, g.lon(s[0]+dX*i+midX),g.lat(s[1]+dY*j+midY));
			}
		}
	}
	public void paintDensity(){
		int[] l = grid.getAllSizeOfCells();
		long mean = 0;
		for(int i : l)
			mean += i;
		mean = mean/l.length;
		double var = 0.0;
	    for (int i : l)
	    	var += Math.pow(mean - i,2);
	    var /= (l.length);
	    double stdvar =	Math.sqrt(var);
	    int aplha = 120; 
	    int color[] = {g.color(255,0,0,aplha), //rot
	    			   g.color(255,140,0,aplha), //organe
	    			   g.color(255,255,0,aplha), // gelb
	    			   g.color(255,215,0,aplha), // gold
	    			   g.color(255,255,255,1)
	    			   }; 
	    Arrays.sort(l);
		g.rectMode(g.CORNERS);  // Set rectMode to CORNERS
		g.noStroke();
		int c=0;
		for(int i=0; i < grid.getRowNo(); i++){
			for(int j=0; j < grid.getColumnNo(); j++){
				try{
					if(grid.getRow(i).get(j).size() > (stdvar*3)){
						c=0;				
					}
					else if(grid.getRow(i).get(j).size() > (stdvar*2)){
						c=1;					
					}
					else if(grid.getRow(i).get(j).size() > (stdvar*1)){
						c=2;					
					}
					else if(grid.getRow(i).get(j).size() > (stdvar*0.5)){
						c=3;					
					}
					else{
						c=4;					
					}
				}
				catch (Exception e) {
					c=4;
				}
				if(grid.getRow(i).get(j).size() == l[l.length-1])
					g.fill(g.color(255,0,255,255));				
				else
					g.fill(color[c]);
				
				g.rect(g.lon(s[0]+dX*i),g.lat(s[1]+dY*j), g.lon(s[0]+dX*(i+1)),g.lat(s[1]+dY*(j+1)));
			}
		}
		g.rectMode(g.CORNER);
	}
	
	public boolean isPaintGrid() {
		return paintGrid;
	}
	public void setPaintGrid(boolean paintGrid) {
		this.paintGrid = paintGrid;
	}
	public boolean isPaintNumbers() {
		return paintNumbers;
	}
	public void setPaintNumbers(boolean paintNumbers) {
		this.paintNumbers = paintNumbers;
	}
	public boolean isPaintDensity() {
		return paintDensity;
	}
	public void setPaintDensity(boolean paintDensity) {
		this.paintDensity = paintDensity;
	}
	
}
