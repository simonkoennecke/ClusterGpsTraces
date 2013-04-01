package graph;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import trace.Trace;
import trace.Traces;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.util.Log;
import org.jfree.util.LogContext;

public class FormTracesAnalyse extends JPanel{
	private static final long serialVersionUID = -4564657332141923817L;
	private GpxFile gpx;
	
	public FormTracesAnalyse(GpxFile g){
		super();
		gpx = g;		
		repaint();
		setAutoscrolls(true);		
	}
	
	public void repaint(){
		removeAll();
		if(gpx == null)
			return;
		
		double[] v = new double[(int) gpx.getTraces().countPoints()];
		gpx.getTraces().calcDistanceBetweenPoints(v, Trace.getCurrentVersionId());
		
		ChartPanel cpanel = new ChartPanel(histogramm("Histrogramm Kantenlänge","Kantenlänge",v));
		add(cpanel, BorderLayout.CENTER);
		
		double[] val = new double[(int) gpx.getTraces().countDisplayedTraces()];
		gpx.getTraces().calcTraceLength(val, Trace.getCurrentVersionId());
		cpanel = new ChartPanel(histogramm("Histrogramm Spurenlänge","Spurenlänge",val));
		add(cpanel, BorderLayout.CENTER);
		
		BoxAndWhiskerDemo("Spurenlänge", boxplot("Spurenlänge"));
		BoxAndWhiskerDemo("Kantenlänge", boxplot("Kantenlänge"));
	}
	
	private JFreeChart histogramm(String title, String xAchse, double[] v){
		HistogramDataset dataset = new HistogramDataset();	    
	    int bin = 30;
	    dataset.addSeries(gpx.filename, v, bin);
	    
	    JFreeChart chart = ChartFactory.createHistogram(
	              title, 
	              xAchse, 
	              "Häufigkeit", 
	              dataset, 
	              PlotOrientation.VERTICAL, 
	              true, 
	              false, 
	              false
	          );

	    chart.setBackgroundPaint(new Color(230,230,230));
	    XYPlot xyplot = (XYPlot)chart.getPlot();
	    xyplot.setForegroundAlpha(0.7F);
	    xyplot.setBackgroundPaint(Color.WHITE);
	    xyplot.setDomainGridlinePaint(new Color(150,150,150));
	    xyplot.setRangeGridlinePaint(new Color(150,150,150));
	    XYBarRenderer xybarrenderer = (XYBarRenderer)xyplot.getRenderer();
	    xybarrenderer.setShadowVisible(false);
	    xybarrenderer.setBarPainter(new StandardXYBarPainter()); 
//		    xybarrenderer.setDrawBarOutline(false);
	    return chart;
	}
	
	
	public void BoxAndWhiskerDemo(final String title, final BoxAndWhiskerCategoryDataset dataset) {
	 	//final BoxAndWhiskerCategoryDataset dataset = createSampleDataset();

        final CategoryAxis xAxis = new CategoryAxis("Type");
        final NumberAxis yAxis = new NumberAxis("Value");
        yAxis.setAutoRangeIncludesZero(false);
        final BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
        renderer.setFillBox(false);
        final CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);

        final JFreeChart chart = new JFreeChart(
            title,
            plot	            
        );
        final ChartPanel chartPanel = new ChartPanel(chart);
        //chartPanel.setPreferredSize(new java.awt.Dimension(450, 270));
        add(chartPanel);

    }

    /**
     * Creates a sample dataset.
     * 
     * @return A sample dataset.
     */
    private BoxAndWhiskerCategoryDataset boxplot(String type) {
        
        final DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        
        for (int vId = 0; vId < Trace.getCurrentVersionId(); vId++) {
        	if(type == "Kantenlänge"){
	        	double[] v = new double[(int) gpx.getTraces().countPoints()];
	    		gpx.getTraces().calcDistanceBetweenPoints(v, Trace.getCurrentVersionId());
	    		ArrayList<Double> list = new ArrayList<Double>();
	    		for(int i=0; i < v.length; i++){
	    			list.add(v[i]);
	    		}
	            dataset.add(list, "vId " + vId, "Kantenlänge");
        	}
            else if(type == "Spurenlänge"){
            	double[] v = new double[(int) gpx.getTraces().countDisplayedTraces()];
	    		gpx.getTraces().calcTraceLength(v, Trace.getCurrentVersionId());
	    		ArrayList<Double> list = new ArrayList<Double>();
	    		for(int i=0; i < v.length; i++){
	    			list.add(v[i]);
	    		}
	            dataset.add(list, "vId " + vId, "Spurenlänge");
            }
        }

        return dataset;
    }
}
