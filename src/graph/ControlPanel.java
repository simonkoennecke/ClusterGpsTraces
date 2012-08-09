package graph;


import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.plaf.SliderUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import cluster.ClusterTraces;
import cluster.KMeans;

import com.lowagie.text.ListItem;

import core.Debug;

import trace.Trace;
import trace.Traces;
import trace.TrcOp;

public class ControlPanel extends JPanel{
	private static final long serialVersionUID = 7468863540376385070L;
	private int w = 1024, h = 800;
	private Dimension dimLfPanel;
	private Dimension dimRtPanel;
	private Dimension dimComponent;
	
	private static int sizeRtPanel = 270;
	
	private JPanel rtPanel = new JPanel();
	
	private JSplitPane splitPane;
	
	private MainGraph graph;
	
	private JTree tree;
	
	/**
	 * 0. 
	 * 1. 
	 * 2.
	 */
	private JTextField txtFields[] = new JTextField[5];
	
	private JButton btnList[] = new JButton[5];
	
	private JLabel lblList[] = new JLabel[5];
	
	private ConfigMouseListener listener;
	
	private GpxFile gpx;
	
	  // constructor
	public ControlPanel(GpxFile _gpx) {
		gpx = _gpx;
		
		dimLfPanel = new Dimension(w-sizeRtPanel,h);
		dimRtPanel = new Dimension(sizeRtPanel,h);
		dimComponent = new Dimension(sizeRtPanel-40, 20);
		
		setBackground(new Color(180, 180, 220));
	    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	    
	    //Graphen erstellen
	    graph = new MainGraph(gpx, dimLfPanel.width,dimLfPanel.height);
	    graph.init(); 
	    graph.setup(); 
	    graph.redraw();
        
	    //Prozedur zur Erstellung des rechten Panels
        listener = new ConfigMouseListener();
	    rtPanel();
	    
	    //Zwei Spalten Layout anlegen
	    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	    splitPane.setTopComponent(rtPanel);
	    splitPane.setBottomComponent(graph);

	    //Die Spaltenaufteilung festlegen
	    graph.setMinimumSize(dimLfPanel);
	    rtPanel.setMinimumSize(dimRtPanel);
	    rtPanel.setMaximumSize(dimRtPanel);
	    
	    splitPane.setDividerLocation(sizeRtPanel);
	    
	    splitPane.setName("Panel");
	    PropertyChangeListener resizeWindows = new PropertyChangeListener() {
	    	@Override
			public void propertyChange(PropertyChangeEvent arg0) {
	    		System.out.println("Window Size changed.");
	    		calcPanelSize();
	    		graph.redraw();
			}
	    };
	    splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,resizeWindows);
	    addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,resizeWindows);
	    //Das erstelle Layout ans Formular binden
	    add(splitPane);	    
	}
	private void calcPanelSize(){
		Dimension s = splitPane.getSize();
		dimLfPanel = new Dimension(s.width-sizeRtPanel,s.height);
		dimRtPanel = new Dimension(sizeRtPanel,s.height);
		dimComponent = new Dimension(sizeRtPanel-40, 20);
		splitPane.setDividerLocation(sizeRtPanel);
		graph.setSize(dimLfPanel.width, dimLfPanel.height);
		graph.setPreferredSize(dimLfPanel);
		rtPanel.setPreferredSize(dimRtPanel);		
		
	}
	/**
	 * Prozedur zur Erstellung des rechten Panels für die Parameter.
	 * 
	 */
	private void rtPanel(){
		rtPanel.removeAll();
		
		rtPanel.setOpaque(false);
	    //rtPanel.setLayout(new BoxLayout(rtPanel, BoxLayout.));
	    
	    JLabel jl = new JLabel("Parameter setzen");
	    rtPanel.add(jl);
	    
	    tree = new JTree(createNodes());
        JScrollPane treeView = new JScrollPane(tree);
        treeView.setPreferredSize(new Dimension(dimComponent.width, 300));
        rtPanel.add(treeView);
        int index = 0;
        createTxtField(index, "Fläche verkleinern [Grad]:", "0.0004");
        createBtn(index, "Resize Plain", "resizePlain");
        index++;
        //index = 1
        createTxtField(index, "Abschneiden bei einer Distanz von länger als:", "500");
        createBtn(index, "Cut by Distance", "cutTraceByDistance");
        index++;
        //index = 2
        createTxtField(index,"Toleranz für Polylinen-Vereinfachnung  [Meter]:", "2000");
        createBtn(index, "Simplify Traces", "simplifyTraces");
        index++;
        //index = 3
        createTxtField(index,"Anzahl der Cluster:", "5");
        createBtn(index, "Cluster Traces", "clusterTraces");
        index++;
        //index = 4
        createBtn(index, "Redraw Traces", "redrawTraces");
        
        
        
	}
	
	private void createTxtField(int index, String label, String defaultValue){
		lblList[index] = new JLabel(label);
        txtFields[index] = new JTextField(defaultValue, 10);
        lblList[index].setLabelFor(txtFields[index]);
        rtPanel.add(lblList[index]);
        rtPanel.add(txtFields[index]);
	}
	private void createBtn(int index, String name, String label){		
		btnList[index] = new JButton(name);
        btnList[index].setName(label);
        btnList[index].setPreferredSize(dimComponent);
        btnList[index].addMouseListener(listener);
        rtPanel.add(btnList[index]);
	}
	
	private DefaultMutableTreeNode createNodes(){
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Alle Traces");
		createNodes(top, gpx.getTraces());
		return top;
	}
	private void createNodes(DefaultMutableTreeNode top, Traces traces){
	     for(Trace t : traces){
	    	if(t.getSubTraces().size() > 0){
	    		DefaultMutableTreeNode cat = new DefaultMutableTreeNode(t);
	    		top.add(cat);
	    		createNodes(cat, t.getSubTraces());
	    	}
	    	else{
	    		top.add(new DefaultMutableTreeNode(t));
	    	}
	    }
	}
	private void reloadTree(){
		tree.setModel(new DefaultTreeModel(createNodes()));
		((DefaultTreeModel) tree.getModel()).reload();
		tree.revalidate();
		tree.repaint();
		tree.updateUI();
		tree.setVisible(false);
		tree.setVisible(true);
		//tree.setViewportView(taskDataTree);
	}
	/**
	 * Ermittelt alle zu clusterene Traces.
	 * @param t Die Traces nach dem vereinfachen.
	 * @return liefert in einem "Traces" alle zu clusteren Traces
	 */
	private Traces getTraces(Traces t){
		Traces tmp = new Traces();
		getTraces(t, tmp);
		return tmp;
	}
	/**
	 * Helfer Funktion von getTraces(Traces t)
	 * @param _t siehe Hauptfunktion
	 * @param store hier werden alle Traces in einer Ebene gespeichert, die geclustert werden sollen.
	 */
	private void getTraces(Traces _t, Traces store){
		for(Trace t : _t){		  
			if(t.getSubTraces().size()>0){
				getTraces(t.getSubTraces(),store);
				continue;
			}
			if(t.size() <= 2)
				continue;
			store.addTrace(t);
		}
	}
	
	private class ConfigMouseListener implements MouseListener{
		@Override
		public void mouseClicked(MouseEvent arg0) {
			if(arg0.getComponent().getName() == "simplifyTraces"){
				double tol = Double.valueOf(txtFields[2].getText());
				TrcOp.reduction(gpx.getTraces(), tol);
				Debug.syso("Reduce Points on Traces");
				reloadTree();
				graph.redraw();
			}
			else if(arg0.getComponent().getName() == "resizePlain"){
				double tol = Double.valueOf(txtFields[0].getText());
				TrcOp.resizePlain(gpx.getTraces(), tol);
				Debug.syso("Resize Plain");
				reloadTree();
				graph.redraw();
			}
			else if(arg0.getComponent().getName() == "cutTraceByDistance"){
				double tol = Double.valueOf(txtFields[1].getText());
				TrcOp.splitTraceByDistance(gpx.getTraces(), tol);
				Debug.syso("Cut Trace by Distance");
				reloadTree();
				graph.redraw();
			}
			else if(arg0.getComponent().getName() == "redrawTraces"){
				graph.redraw();			
		        Debug.syso("Repaint");
			}
			else if(arg0.getComponent().getName() == "clusterTraces"){
				Debug.syso("Cluster");
				Traces tmp = getTraces(gpx.getTraces());
				int k = Integer.valueOf(txtFields[3].getText());
				ClusterTraces cltr = new KMeans(k, tmp);
				cltr.run();
				graph.setCluster(cltr.getCluster());
				graph.redraw();
			}
			else{
				System.out.println("Event: " + arg0.getComponent().getName());
			}
		}
		@Override
		public void mouseEntered(MouseEvent e) {
			
		}
		@Override
		public void mouseExited(MouseEvent e) {
				
		}
		@Override
		public void mousePressed(MouseEvent e) {
			
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			
		} 
	}
}
