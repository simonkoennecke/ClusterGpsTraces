package graph;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.plaf.SliderUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import merg.Grid;

import cluster.ClusterTraces;
import cluster.Export;
import cluster.KMeans;

import com.lowagie.text.ListItem;

import core.Debug;

import trace.Trace;
import trace.Traces;
import trace.TrcOp;

public class ControlPanel extends JPanel implements ActionListener, WindowStateListener{
	private static final long serialVersionUID = 7468863540376385070L;
	
	private JFrame jf;
	
	private JTabbedPane tabbedPanel;
	
	private MainGraph graph;
	
	private JTree tree;
	
	private static int noOfFields = 13;
	private JTextField txtFields[] = new JTextField[noOfFields];
	private JLabel lblList[] = new JLabel[noOfFields];
	
	private JCheckBoxMenuItem chkList[] = new JCheckBoxMenuItem[100];
	
	//private ConfigMouseListener listener;
	
	private GpxFile gpx;
	
	private JMenuBar menuBar;
	private JMenu menuCluster;
	private FormClusterAnalyse formClusterAnalyse = new FormClusterAnalyse();
	private FormTracesAnalyse formTracesAnalyse;
	
	  // constructor
	public ControlPanel(JFrame _jf, GpxFile _gpx) {
		gpx = _gpx;
		jf = _jf;
		
		setBackground(new Color(180, 180, 220));
	    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    
        createMenuBar();
        jf.add(menuBar, BorderLayout.NORTH);
        
	    
	    tabbedPanel();
	    
	}
	private JComponent tabPanel[] = new JComponent[3];
	private void tabbedPanel(){
		tabbedPanel = new JTabbedPane();
		
		//Graphen erstellen
	    graph = new MainGraph(gpx, 1000,700);
	    graph.init();
	    graph.setup(); 
	    graph.redraw();
	    
	    ImageIcon icon = null;
		
        tabbedPanel.addTab("Map", graph);
        tabbedPanel.setMnemonicAt(0, KeyEvent.VK_1);
        
        
        tabPanel[0] = makeTextPanel("Panel #2");
        formTracesAnalyse = new FormTracesAnalyse(gpx);
        tabbedPanel.addTab("Traces", new JScrollPane(formTracesAnalyse));
        tabbedPanel.setMnemonicAt(1, KeyEvent.VK_2);
        
        tabbedPanel.addTab("Cluster", formClusterAnalyse);
        tabbedPanel.setMnemonicAt(2, KeyEvent.VK_3);
         
        tabPanel[2] = makeTextPanel("Panel #4 (has a preferred size of 410 x 50).");
        tabPanel[2].setPreferredSize(new Dimension(410, 50));
        tabbedPanel.addTab("Tab 4", icon, tabPanel[2], "Does nothing at all");
        tabbedPanel.setMnemonicAt(3, KeyEvent.VK_4);
        
        //Add the tabbed pane to this panel.
        add(tabbedPanel);
	}
	protected JComponent makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        return panel;
    }
	private void createMenuBar() {
		menuBar = new JMenuBar();
		JMenu menu;
		JMenuItem item;
		/**
		 * Menu Punkt: Datei
		 */
		menu = new JMenu("Datei");
	    
	    item = new JMenuItem("GPX-Datei öffnen");
        item.addActionListener(this);
        item.setActionCommand("loadFile");
        menu.add(item);
        
        item = new JMenuItem("Rückgängig");
        item.setActionCommand("redo");
        item.addActionListener(this);
        menu.add(item);
        
        menuBar.add(menu);
        
        /**
		 * Menu Punkt: Graph
		 */
        menu = new JMenu("Graph");
	    
	    item = new JMenuItem("Graph speichern");
        item.addActionListener(this);
        item.setActionCommand("saveGraphToFile");
        menu.add(item);
        item = new JMenuItem("Redraw Graph");
        item.addActionListener(this);
        item.setActionCommand("redraw");
        menu.add(item);
        
        item = new JMenuItem("Traces anzeigen/verbergen");
        item.addActionListener(this);
        item.setActionCommand("togglePaintTraces");
        menu.add(item);
        
        item = new JMenuItem("Zeige Traces oder Cluster an");
        item.addActionListener(this);
        item.setActionCommand("togglePaintModeGraph");
        menu.add(item);
        
        

        menuBar.add(menu);
        
        /**
		 * Menu Punkt: Spuren Anzeigen
		 */
		menu = new JMenu("Spuren");
	    
		tree = new JTree(createNodes());
        JScrollPane treeView = new JScrollPane(tree);
        treeView.setPreferredSize(new Dimension(400, 600));
        menu.add(treeView);
        
        menuBar.add(menu);
		
        
        /**
		 * Menu Punkt: Spuren
		 */
        menu = new JMenu("Spur-Operation");
	    
        item = new JMenuItem("Fläche verkleinern");
        item.setActionCommand("resizePlain");
        item.addActionListener(this);
        menu.add(item);
        createTxtField(menu, 0, "Fläche verkleinern [Grad]:", "0.0004");
        
        item = new JMenuItem("Durchtrenne Kante");
        item.setActionCommand("cutTraceByDistance");
        item.addActionListener(this);
        menu.add(item);
        createTxtField(menu, 1, "bei einer Kantenlänge von [Meter]", "500");
        
        item = new JMenuItem("Durchtrenne Spure");
        item.setActionCommand("splitTraceAfterDistance");
        item.addActionListener(this);
        menu.add(item);
        createTxtField(menu, 2, "nach einer Länge von [Meter]", "500");
        
        item = new JMenuItem("Vereinfache die Spuren");
        item.setActionCommand("simplifyTraces");
        item.addActionListener(this);
        menu.add(item);
        createTxtField(menu, 3, "Vereinfachnungstoleranz [Meter]", "200");
        
        item = new JMenuItem("Teile die Spure");
        item.setActionCommand("splitTraceByDouglasPeucker");
        item.addActionListener(this);
        menu.add(item);
        createTxtField(menu, 4, "bei einem XTA [Meter]", "200");
        
        item = new JMenuItem("Spure löschen");
        item.setActionCommand("deleteTraceWithADistancLess");
        item.addActionListener(this);
        menu.add(item);
        createTxtField(menu, 12, "kleienr als [Meter]", "5");
        
        menuBar.add(menu);
        
        /**
		 * Menu Punkt: Kreuzungen
		 */
        menu = new JMenu("Kreuzungen");
	     
        item = new JMenuItem("Kreuzungen trennen");
        item.setActionCommand("splitByIntersection");
        item.addActionListener(this);
        menu.add(item);
        createTxtField(menu, 7, "Anzahl der Iteration:", "0");
        
        //item = new JCheckBoxMenuItem("Kreuzungen anzeigen");
        item = new JMenuItem("Kreuzungen anzeigen/verbergen");
        item.setActionCommand("toggleIntersectionDisplay");
        item.addActionListener(this);
        menu.add(item);
        
        
        
        menuBar.add(menu);
        
        /**
		 * Menu Punkt: Dichtegitter
		 */
        menu = new JMenu("Gitter");
        
        item = new JMenuItem("Dichtegitter");
        item.setActionCommand("tracesTogrid");
        item.addActionListener(this);
        menu.add(item);
        createTxtField(menu, 5, "Größe der Zelle [Meter]:", "10");
        
        item = new JMenuItem("Dichtegitter anzeigen/verbergen");
        item.setActionCommand("toggleDensityGridDisplay");
        item.addActionListener(this);
        menu.add(item);
        
        item = new JMenuItem("Gitter anzeigen/verbergen");
        item.setActionCommand("toggleGridDisplay");
        item.addActionListener(this);
        menu.add(item);
        
        item = new JMenuItem("Dichtefarbe anzeigen/verbergen");
        item.setActionCommand("toggleDensityColorDisplay");
        item.addActionListener(this);
        menu.add(item);
        
        item = new JMenuItem("Dichtanzahl anzeigen/verbergen");
        item.setActionCommand("toggleDensityNumberDisplay");
        item.addActionListener(this);
        menu.add(item);
        
        menuBar.add(menu);
        
        
        /**
		 * Menu Punkt: Cluster
		 */
        createMenuCluster();
        menuBar.add(menuCluster);
        
		
	}
	
	private void createMenuCluster(){
		menuCluster  = new JMenu("Cluster");
		JMenuItem item;
		createTxtField(menuCluster, 10, "Anzahl der Iteration", "20");
		item = new JMenuItem("Cluster berechnen");
        item.setActionCommand("clusterTraces");
        item.addActionListener(this);
        menuCluster.add(item);
        createTxtField(menuCluster, 6, "Anzahl Cluster", "5");
        
        item = new JMenuItem("Cluster berechnen");
        item.setActionCommand("clusterTracesGrid");
        item.addActionListener(this);
        menuCluster.add(item);
        createTxtField(menuCluster, 8, "Spalte", "6");
        createTxtField(menuCluster, 9, "Zeile", "5");
        
        
        item = new JMenuItem("Cluster anzeigen/verbergen");
        item.setActionCommand("togglePaintModeGraph");
        item.addActionListener(this);
        menuCluster.add(item);
        
        menuCluster.add(new JSeparator());
        
        item = new JMenuItem("Speicher Cluster als .png");
        item.setActionCommand("saveClusterToFiles");
        item.addActionListener(this);
        menuCluster.add(item);
        
        menuCluster.add(new JSeparator());
        
        item = new JMenuItem("Zeige Iteration");
        item.setActionCommand("toggleClusterFirstOrSecond");
        item.addActionListener(this);
        menuCluster.add(item);
        createTxtField(menuCluster, 11, "Nummer:", "4");
        
        item = new JMenuItem("Clusters export");
        item.setActionCommand("clusterExport");
        item.addActionListener(this);
        menuCluster.add(item);
        
    	for(int i=0; i < chkList.length; i++){
    		createCheckbox(menuCluster, i, (i+1)+". Cluster");
    	}
	        
        
	}
	private void createCheckbox(JMenu menu, int index, String label){
		final Dimension dim = new Dimension(150,15);
		chkList[index] = new JCheckBoxMenuItem(label, false);
		chkList[index].setPreferredSize(dim);
		chkList[index].setActionCommand("toggleCluster_"+index);
		chkList[index].addActionListener(this);
		chkList[index].setVisible(false);
		chkList[index].setSelected(true);
        menu.add(chkList[index]);
                
	}
	private void setClusterCheckbox(){
		try{
	        Traces c = graph.getDrawCluster().getCluster().getCentroid();
	        if(graph.getDrawCluster().getCluster().getCentroid().size() > 0){
	        	for(int i=0; i < c.size(); i++){
	        		c.get(i).setDisplay(chkList[i].isSelected());
	        		chkList[i].setVisible(true);
	        	}
	        	for(int i=c.size(); i < chkList.length; i++){
	        		chkList[i].setVisible(false);
	        	}
	        }
        }
		catch (Exception e) {
			// TODO: handle exception
		}
	}
	private void createTxtField(JMenu menu, int index, String label, String defaultValue){
		JPanel p = new JPanel();
		
		final Dimension dim = new Dimension(150,15);
		lblList[index] = new JLabel(label);
		lblList[index].setPreferredSize(dim);
		lblList[index].setMaximumSize(dim);
		p.add(lblList[index]);
        txtFields[index] = new JTextField(defaultValue, 10);
        txtFields[index].setPreferredSize(dim);
        txtFields[index].setMaximumSize(dim);
        lblList[index].setLabelFor(txtFields[index]);        
        p.add(txtFields[index]);
        p.setAlignmentX(10);        
        menu.add(p);        
	}
	
	private DefaultMutableTreeNode createNodes(){
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Alle Traces");
		createNodes(top, gpx.getTraces());
		return top;
	}
	private void createNodes(DefaultMutableTreeNode top, Traces traces){
		if(traces == null)
			return;
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
	public void repaint(){
		try{
			reloadTree();
			graph.redraw();
			//formTracesAnalyse.repaint();
		}
		catch (Exception e) {
			// TODO: handle exception
		}
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		String actionCommand = arg0.getActionCommand();
		long cntDispTraces = gpx.getTraces().countDisplayedTraces();
		long cntDispPoints = gpx.getTraces().countPoints();
		
		if(actionCommand == "loadFile"){
			Debug.syso("Load new File");
			FileDialog dlg=null;

			dlg=new FileDialog(jf,"Select File to Load",FileDialog.LOAD);
			dlg.setFile("*.gpx");
			dlg.setVisible(true);
			String filename = dlg.getDirectory() + dlg.getFile();
			
			if (filename != null) {
				Debug.syso("Lade GPX-Datei: " + filename + ".");
				try{
					gpx = new GpxFile(filename);
					reloadTree();
					graph.setGpxFile(gpx);
					Debug.syso(filename);
				}
				catch (Exception e) {
					Debug.syso("Fehler beim Laden der neuen GPX-Datei.");
				}
			}		        
		}
		else if(actionCommand == "saveGraphToFile"){
			Debug.syso("Load new File");
			FileDialog dlg=null;

			dlg=new FileDialog(jf,"Save Graph to File",FileDialog.SAVE);
			dlg.setFile("*.png");
			dlg.setVisible(true);
			String filename = dlg.getDirectory() + dlg.getFile();
			
			if (filename != null) {
				Debug.syso("Save Graph to: " + filename + ".");
				try{
					graph.save(filename);					
				}
				catch (Exception e) {
					Debug.syso("Fehler beim Speichern vom Graphen.");
				}
			}		        
		}
		else if(actionCommand == "saveClusterToFiles"){
			Debug.syso("Load new File");
			FileDialog dlg=null;

			dlg=new FileDialog(jf,"Choose Directoy for the Files",FileDialog.SAVE);
			dlg.setFile("test.png");
			dlg.setVisible(true);
			String dir = dlg.getDirectory();
			graph.getDrawCluster().saveToFile(dir);
			        
		}
		else if(actionCommand == "clusterExport"){
			Debug.syso("Export Cluster to GPX File");
			FileDialog dlg=null;
			dlg=new FileDialog(jf,"Choose Directoy for the File",FileDialog.SAVE);
			dlg.setFile("cluster.gpx");
			dlg.setVisible(true);
			String dir = dlg.getDirectory() + dlg.getFile();
			Export.ClustersToGpx(graph.getDrawCluster().getCluster(), dir);
			        
		}		
		else if(actionCommand == "simplifyTraces"){
			double tol = Double.valueOf(txtFields[3].getText());
			TrcOp.reduction(gpx.getTraces(), tol);
			Debug.syso("Reduce Points on Traces");
			repaint();
		}
		else if(actionCommand == "splitTraceByDouglasPeucker"){
			double tol = Double.valueOf(txtFields[4].getText());
			TrcOp.splitTraceByDouglasPeucker(gpx.getTraces(), tol);
			Debug.syso("Splite Trace with Douglas Peucker");
			repaint();
		}			
		else if(actionCommand == "resizePlain"){
			double tol = Double.valueOf(txtFields[0].getText());
			TrcOp.resizePlain(gpx.getTraces(), tol);
			Debug.syso("Resize Plain");
			repaint();
		}
		else if(actionCommand == "cutTraceByDistance"){
			double tol = Double.valueOf(txtFields[1].getText());
			TrcOp.splitTraceByDistance(gpx.getTraces(), tol);
			Debug.syso("Cut Trace by Distance");
			repaint();
		}
		else if(actionCommand == "splitTraceAfterDistance"){
			double tol = Double.valueOf(txtFields[2].getText());
			TrcOp.splitTraceAfterDistance(gpx.getTraces(), tol);
			Debug.syso("Split Trace after Distance");
			repaint();
		}			
		else if(actionCommand == "splitByIntersection"){
			//double tol = Double.valueOf(txtFields[5].getText());
			int no = Integer.valueOf(txtFields[7].getText());
			graph.setIntersections(TrcOp.getIntersections(gpx.getTraces(), 0.0015, no));
			repaint();
	        Debug.syso("Repaint");
		}else if(actionCommand == "deleteTraceWithADistancLess"){
			
			double tol = Double.valueOf(txtFields[12].getText());
			Debug.syso("Delete Trace with a distance less than " + tol);
			TrcOp.deleteTraceWithADistancLess(gpx.getTraces(), tol);
			repaint();
		}
		else if(actionCommand == "clusterTraces"){
			Debug.syso("Cluster");
			Traces tmp = TrcOp.getTraces(gpx.getTraces());
			int k = Integer.valueOf(txtFields[6].getText());
			int no = Integer.valueOf(txtFields[10].getText());
			ClusterTraces cltr = new KMeans(k, tmp, no);
			cltr.run();
			graph.getDrawCluster().setClusters(cltr.getClusters());
			formClusterAnalyse.setClusters(cltr.getClusters());
			
			graph.setPaintMode(MainGraph.paintModeOption.Cluster);
			//Config JMenuBar
			setClusterCheckbox();
			repaint();
		}
		else if(actionCommand == "clusterTracesGrid"){
			Debug.syso("Cluster");
			Traces tmp = TrcOp.getTraces(gpx.getTraces());
			int r = Integer.valueOf(txtFields[8].getText());
			int c = Integer.valueOf(txtFields[9].getText());
			int no = Integer.valueOf(txtFields[10].getText());
			ClusterTraces cltr = new KMeans(r, c, tmp, no);
			cltr.run();
			graph.getDrawCluster().setClusters(cltr.getClusters());
			formClusterAnalyse.setClusters(cltr.getClusters());
			graph.getDrawCluster().setPaintIteration((no)-1);
			txtFields[11].setText(String.valueOf(no));
			graph.setPaintMode(MainGraph.paintModeOption.Cluster);
			//Config JMenuBar
			setClusterCheckbox();
			repaint();
		}
		else if(actionCommand == "tracesTogrid"){
			Debug.syso("Trace to Grid");
			int k = Integer.valueOf(txtFields[5].getText());
			graph.setGrid(new Grid(gpx.getTraces(), k));				
			repaint();
		}
		else if(actionCommand == "redo"){
			Debug.syso("Redo");				
			TrcOp.redo(gpx);
			repaint();
		}
		else if(actionCommand == "toggleIntersectionDisplay"){
			Debug.syso("Toggle Intersection Display");
			graph.setPaintIntersections(!graph.isPaintIntersections());
			graph.redraw();
		}
		else if(actionCommand == "toggleDensityGridDisplay"){
			Debug.syso("Toggle Density Grid Display");
			graph.setPaintGrid(!graph.isPaintGrid());
			graph.redraw();
		}
		else if(actionCommand == "toggleDensityColorDisplay"){
			Debug.syso("Toggle Density Color Display");
			graph.getGridGraph().setPaintDensity(!graph.getGridGraph().isPaintDensity());
			graph.redraw();
		}
		else if(actionCommand == "toggleDensityNumberDisplay"){
			Debug.syso("Toggle Density NumberDisplay");
			graph.getGridGraph().setPaintNumbers(!graph.getGridGraph().isPaintNumbers());
			graph.redraw();
		}
		else if(actionCommand == "toggleGridDisplay"){
			Debug.syso("Toggle Density Grid Grid Display");
			graph.getGridGraph().setPaintGrid(!graph.getGridGraph().isPaintGrid());
			graph.redraw();
		}
		else if(actionCommand == "togglePaintTraces"){
			Debug.syso("Toggle Density Grid Grid Display");
			graph.getDrawTraces().setPaintTraces(!graph.getDrawTraces().isPaintTraces());
			graph.redraw();
		}
		else if(actionCommand == "toggleClusterFirstOrSecond"){
			Debug.syso("Toggle between Iteration from Clustering");
			int k = Integer.valueOf(txtFields[11].getText());
			k -= 1;//Der Index wird mit Null begonnen und nicht mit eins...
			if(k < 0){
				txtFields[11].setText("0");
				k=0;
			}
			else if(k >= graph.getDrawCluster().getClusters().size()){
				txtFields[11].setText(String.valueOf(graph.getDrawCluster().getClusters().size()));
				k = graph.getDrawCluster().getClusters().size()-1;
			}
			graph.getDrawCluster().setPaintIteration(k);
			setClusterCheckbox();
			graph.redraw();
			
		}
		else if(actionCommand == "redraw"){
			graph.redraw();
		}
		else if(actionCommand == "togglePaintModeGraph"){
			if(graph.getPaintMode() == MainGraph.paintModeOption.Cluster)
				graph.setPaintMode(MainGraph.paintModeOption.Traces);
			else
				graph.setPaintMode(MainGraph.paintModeOption.Cluster);
			
			graph.redraw();
		}
		else{
			if(actionCommand.startsWith("toggleCluster_")){
				int clusterId = Integer.valueOf(actionCommand.replace("toggleCluster_", ""));
				System.out.println("toggleCluster: " + clusterId);
				Trace c = graph.getDrawCluster().getCluster().getCentroid(clusterId);
				c.setDisplay(!c.isDisplay());
				graph.redraw();
			}
			else{
				System.out.println("Event: " + actionCommand);
			}
		}
		Debug.syso("Displayed Traces before " + cntDispTraces + " with " + cntDispPoints + " Points");
		Debug.syso("Displayed Traces after " + gpx.getTraces().countDisplayedTraces() + " with " + gpx.getTraces().countPoints() + " Points");
	}
	@Override
	public void windowStateChanged(WindowEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("windowStateChanged: " + arg0.paramString());
	}
}
