package graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.*;
import org.jfree.ui.ApplicationFrame;
import org.jfree.chart.*;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import cluster.Clusters;

public class FormClusterAnalyse extends JPanel implements ActionListener {
	private static final long serialVersionUID = -1549718750919505376L;
	
	private Clusters clusters;
	
	private JTextField txtClusterid = new JTextField("     0");
	
	private int currentClusterId = 0;
	
	public FormClusterAnalyse(){
		super(new BorderLayout());
		setAutoscrolls(true);
		init();
	}
	
	public void init(){
		
		this.removeAll();
		
		JPanel toolbar = new JPanel();
		JButton item = new JButton("Erstelle Graphen");
		item.setActionCommand("drawGraph");
		item.addActionListener(this);
		toolbar.add(item);
		toolbar.add(new JLabel("Cluster Id:"));
		toolbar.add(txtClusterid);
		add(toolbar, BorderLayout.NORTH);
		
		if(clusters != null){
			JPanel charts = new JPanel();
			GridLayout gbc = new GridLayout(0, 1);
			charts.setLayout(gbc);
			
			//charts.add(createXYPlot(createDatasetClusterSize()), gbc);
			charts.add(BarChart("Entwicklung der Anzahl der Traces in einem Cluster",createDatasetBarClusterSize()), gbc);
			charts.add(BoxAndWhisker("Verteilung der Frechet Distanzen", createDatesetDistributionCluster()), gbc);
			charts.add(BoxAndWhisker("Verteilung der Frechet Distanzen", createDatesetDistribution()), gbc);
			
			add(charts);
		}
	}
	
	public ChartPanel BoxAndWhisker(String title, BoxAndWhiskerCategoryDataset dataset) {

		JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(title, "", "Länge [Meter]", dataset, true);
		//chart.set
		ChartPanel chartPanel = new ChartPanel(chart, true);
		// chartPanel.setPreferredSize(new java.awt.Dimension(450, 270));
		return chartPanel;

	}
	/**
	 * Creates a dataset with all edge length or trace length.
	 * 
	 * @return dataset.
	 */
	private BoxAndWhiskerCategoryDataset createDatesetDistributionCluster() {

		final DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
		//Iteration Nummer = iNo
		for (int iNo = 0; iNo < clusters.list.size(); iNo++){
			for(int cId=0; cId < clusters.list.get(0).getSize(); cId++){
				List<Double> list = clusters.getAllFrechetDistanceByClusterId(iNo, cId);
				dataset.add(list, "Iteration "+(iNo+1), "Cluster "+(cId+1));
			}
		}
		
		return dataset;
	}
	
	private BoxAndWhiskerCategoryDataset createDatesetDistribution() {

		final DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
		//Iteration Nummer = iNo
		for (int iNo = 0; iNo < clusters.list.size(); iNo++){
			List<Double> list = new LinkedList<Double>();
			for(int cId=0; cId < clusters.list.get(0).getSize(); cId++){
				list.addAll(clusters.getAllFrechetDistanceByClusterId(iNo, cId));
			}
			dataset.add(list, "Iteration "+(iNo+1), "Clusters");
		}
		
		return dataset;
	}
	
	public XYSeriesCollection createDatasetClusterSize(){
		
		XYSeries series[] = new XYSeries[clusters.list.get(0).getSize()];
		int cntCluster = clusters.list.get(0).getSize();
		for(int i=0; i < cntCluster; i++){
			series[i] = new XYSeries("Cluster "+(i+1)+"");
			for(int j=0; j < clusters.size(); j++){				
				series[i].add(j, clusters.list.get(j).getTraces(i).size());
			}
		}
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		for(int i=0; i < series.length; i++){			
			dataset.addSeries(series[i]);
		}
		return dataset;
	}
	public DefaultCategoryDataset createDatasetBarClusterSize(){
		
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		int cntCluster = clusters.list.get(0).getSize();
		for(int i=0; i < cntCluster; i++){
			for(int j=0; j < clusters.size(); j++){				
				dataset.addValue((double) clusters.list.get(j).getTraces(i).size(), "Iteration "+(j+1), "C. "+(i+1));
			}
		}
		
		return dataset;
	}
	public ChartPanel BarChart(String title, CategoryDataset dataset) {
		 // create the chart...
       final JFreeChart chart = ChartFactory.createBarChart(
           title,         // chart title
           "",               // domain axis label
           "Anzahl",                  // range axis label
           dataset,                  // data
           PlotOrientation.VERTICAL, // orientation
           true,                     // include legend
           true,                     // tooltips?
           false                     // URLs?
       );
       ChartPanel chartPanel = new ChartPanel(chart);
       return chartPanel;

   }
	public ChartPanel createXYPlot(XYSeriesCollection dataset){
		
		JFreeChart chart2 = ChartFactory.createXYLineChart(
				"Line Chart Demo",
				"Iterationsschritt",
				"Anzahl Spuren",
				dataset,
				PlotOrientation.VERTICAL,
				true,
				true,
				false);
		
		ChartPanel chartPanel2 = new ChartPanel(chart2);
		
		return chartPanel2;
	}
	
	public Clusters getClusters() {
		return clusters;
	}

	public void setClusters(Clusters clusters) {
		this.clusters = clusters;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		String actionCommand = arg0.getActionCommand();
		if(actionCommand == "drawGraph"){
			currentClusterId = Integer.valueOf(txtClusterid.getText().trim());
			init();
		}
	}
	
}
