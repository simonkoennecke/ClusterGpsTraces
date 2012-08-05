package graph;

import processing.core.PApplet;

public class ColorSet extends PApplet{
	private static final long serialVersionUID = -2893807347974555601L;

	private static int[] color;
	
	private int ptr=0;

	public ColorSet(){
		color = new int[]{color(246,247,146), color(51,55,69), color(119,196,211), color(218,237,226), color(234,46,73)};
	}
	public int getNext(){
		int t = color[ptr];
		ptr = (ptr + 1) % color.length;
		return t;
	}
	public void resetPointer(){
		ptr=0;
	}
}
