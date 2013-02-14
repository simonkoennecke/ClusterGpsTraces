package core;


import graph.*;
import javax.swing.JFrame;


public class Main {
	public static void main(String[] args) {
		//PApplet.main(new String[] { "--present", "graph.MyProcessingSketch" });
		// threadsafe way to create a Swing GUI
	    
	    
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
	      public void run() {
    	    GpxFile gpx = new GpxFile();
    	    
	    	// create new JFrame
	  		JFrame jf = new JFrame("Clusteranalyse von GPS-Spuren");
	  	 
	  	    // this allows program to exit
	  	    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	  	 
	  	    // You add things to the contentPane in a JFrame
	  	    jf.getContentPane().add(new ControlPanel(jf, gpx));
	  	  
	  	    // keep window from being resized
	  	    jf.setResizable(true);
	  	 
	  	    // size frame
	  	    jf.pack();
	  	    
	  	    jf.setSize(1024, 800);
	  	 
	  	    // make frame visible
	  	    jf.setVisible(true);
	      }
	    }
	    );
		
	}
}
