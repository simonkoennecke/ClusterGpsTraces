package graph;

import processing.core.PApplet;

public class ColorSet extends PApplet{
	private static final long serialVersionUID = -2893807347974555601L;

	private final int[] color;
	
	private int ptr=0;

	public ColorSet(){
		//color = new int[]{color(246,247,146), color(51,55,69), color(119,196,211), color(218,237,226), color(234,46,73)};
		/* Rot #ff1e00, Gelb #e1ff00, Grün #00ff1e, Türkis #00e1ff, Blau #1e00ff, Lila #ff00e1 */
		color = new int[]{color(50, 50, 50), color(255, 30, 0), /*color(225, 255, 0),*/ color(0, 255, 30), /*color( 0, 225, 255),*/ color(30, 0, 255), color( 255, 0, 200)};
	}
	public int getColor(){
		return color[ptr];
	}
	public int getNextColor(){
		int t = color[ptr];
		ptr = (ptr + 1) % color.length;
		return t;
	}
	public void resetPointer(){
		ptr=0;
	}
}
