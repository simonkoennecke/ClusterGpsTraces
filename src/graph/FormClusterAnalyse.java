package graph;

import javax.swing.JPanel;
import org.jfree.data.xy.*;
import org.jfree.ui.ApplicationFrame;
import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import cluster.Clusters;

public class FormClusterAnalyse extends JPanel {
	private static final long serialVersionUID = -1549718750919505376L;
	
	private Clusters clusters;
	
	public FormClusterAnalyse(){
		
	}
	
	public void init(){
		this.removeAll();
		XYSeries series[] = new XYSeries[clusters.list.get(0).getSize()];
		for(int i=0; i < clusters.list.get(0).getSize(); i++){
			series[i] = new XYSeries("Cluster "+(i+1)+" in der Iteration " + (j+1));
			for(int j=0; j < clusters.size(); j++){				
				series[i].add(i, clusters.list.get(j).getTraces(i).size());
			}
		}
		XYSeriesCollection dataset = new XYSeriesCollection();
		for(int i=0; i < series.length; i++){			
			dataset.addSeries(series[i]);
		}
		XYLineAndShapeRenderer dot = new XYLineAndShapeRenderer();
		
		NumberAxis xax = new NumberAxis("x");
		NumberAxis yax = new NumberAxis("y");
		
		XYPlot plot = new XYPlot(dataset,xax,yax, dot);
		
		ApplicationFrame punkteframe = new ApplicationFrame("Punkte");
		JFreeChart chart2 = new JFreeChart(plot);
		
		ChartPanel chartPanel2 = new ChartPanel(chart2);
		punkteframe.setContentPane(chartPanel2);
		punkteframe.pack();
		punkteframe.setVisible(true);
		
		add(chartPanel2);
		
	}
	
	public Clusters getClusters() {
		return clusters;
	}

	public void setClusters(Clusters clusters) {
		this.clusters = clusters;
		init();
	}
	
}
