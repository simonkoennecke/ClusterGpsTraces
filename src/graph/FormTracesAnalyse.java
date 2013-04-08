package graph;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import trace.Trace;
import trace.Traces;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.LogarithmicAxis;
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

import core.Debug;

public class FormTracesAnalyse extends JPanel implements ActionListener {
	private static final long serialVersionUID = -4564657332141923817L;
	private GpxFile gpx;
	private boolean paint = true;
	private int vIdStart=0, vIdEnd=0;
	private JTextField start = new JTextField("   0"), end = new JTextField("   0"); 
	public FormTracesAnalyse(GpxFile g) {
		super(new BorderLayout());
		gpx = g;
		setAutoscrolls(true);
		init();
	}

	private void init() {
		removeAll();
		
		JPanel toolbar = new JPanel();
		JButton item = new JButton("Erstelle Graphen");
		item.setActionCommand("drawGraph");
		item.addActionListener(this);
		toolbar.add(item);
		toolbar.add(new JLabel("Opertion Startnummer:"));
		toolbar.add(start);
		toolbar.add(new JLabel("Opertion Endenummer:"));
		toolbar.add(end);
		add(toolbar, BorderLayout.NORTH);

		if (gpx == null)
			return;
		JPanel charts = new JPanel();
		GridLayout gbc = new GridLayout(0, 1);
		charts.setLayout(gbc);
				
		ChartPanel cpanel = histogramm("Histrogramm Kantenlänge","Kantenlänge", createDatasetHistogramm("Kantenlänge"));
		charts.add(cpanel, gbc);
		
		cpanel = histogramm("Histrogramm Spurenlänge", "Spurenlänge", createDatasetHistogramm("Spurenlänge"));
		charts.add(cpanel, gbc);

		charts.add(BoxAndWhiskerDemo("Spurenlänge", boxplot("Spurenlänge")),gbc);
		charts.add(BoxAndWhiskerDemo("Kantenlänge", boxplot("Kantenlänge")),gbc);

		charts.add(BarChart("Entwicklung der Anzahl von Punkten", createDataset("Punkte")),gbc);
		charts.add(BarChart("Entwicklung der Anzahl von Spuren", createDataset("Spuren")),gbc);
		
		add(charts, BorderLayout.CENTER);
	}
	/**
	 * Das Histogramm nimmt nur ein primitives double Array und kein Objekt Double array ...
	 * @param list<double> 
	 * @return double[]
	 */
	private double[] convertDoubleListToDoubleArrayPrimitiv(List<Double> list){
		double[] v = new double[list.size()];
		int i = 0;
		for(double dist : list){
			v[i] = dist;
			i++;
		}
		return v;
	}

	private ChartPanel histogramm(String title, String xAchse, HistogramDataset dataset) {
		//HistogramDataset dataset = new HistogramDataset();
		
		//int bin = 100;
		//dataset.addSeries(gpx.filename, v, bin);
		
		JFreeChart chart = ChartFactory.createHistogram(title, xAchse,
				"Häufigkeit", dataset, PlotOrientation.VERTICAL, true, true,
				false);

		chart.setBackgroundPaint(new Color(230, 230, 230));
		XYPlot xyplot = (XYPlot) chart.getPlot();
		xyplot.setForegroundAlpha(0.7F);
		xyplot.setBackgroundPaint(Color.WHITE);
		xyplot.setDomainGridlinePaint(new Color(150, 150, 150));
		xyplot.setRangeGridlinePaint(new Color(150, 150, 150));
		xyplot.getDomainAxis().setAutoRange(true);
		//LogarithmicAxis logarithmicAxis = new LogarithmicAxis("Häufigkeit");
		//xyplot.setRangeAxis(logarithmicAxis);
		
		XYBarRenderer xybarrenderer = (XYBarRenderer) xyplot.getRenderer();
		xybarrenderer.setShadowVisible(false);
		xybarrenderer.setBarPainter(new StandardXYBarPainter());
		// xybarrenderer.setDrawBarOutline(false);
		ChartPanel chartPanel = new ChartPanel(chart, true);
		// chartPanel.setPreferredSize(new java.awt.Dimension(450, 270));
		return chartPanel;
	}

	public ChartPanel BoxAndWhiskerDemo(String title,
			BoxAndWhiskerCategoryDataset dataset) {

		JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(title, "", "Länge [Meter]", dataset, true);
		ChartPanel chartPanel = new ChartPanel(chart, true);
		// chartPanel.setPreferredSize(new java.awt.Dimension(450, 270));
		return chartPanel;

	}

	/**
	 * Creates a dataset with all edge length or trace length.
	 * 
	 * @return dataset.
	 */
	private BoxAndWhiskerCategoryDataset boxplot(String type) {

		final DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();

		for (int vId = this.vIdStart; vId < this.vIdEnd; vId++) {
			if (type == "Kantenlänge") {
				List<Double> list = gpx.getTraces().calcDistanceBetweenPoints(vId);
				dataset.add(list, "Operation " + (vId+1), "Kantenlänge");
			} else if (type == "Spurenlänge") {
				List<Double> list = gpx.getTraces().calcTraceLength(vId);
				dataset.add(list, "Operation " + (vId+1), "Spurenlänge");
			}
		}
		
		return dataset;
	}
	private HistogramDataset createDatasetHistogramm(String type) {
		final HistogramDataset dataset = new HistogramDataset();
		int bin = 250;
		for (int vId = this.vIdStart; vId < this.vIdEnd; vId++) {
			if (type == "Kantenlänge") {
				List<Double> list = gpx.getTraces().calcDistanceBetweenPoints(vId);
				dataset.addSeries("Operation " + (vId+1), convertDoubleListToDoubleArrayPrimitiv(list), bin);
			} else if (type == "Spurenlänge") {
				List<Double> list = gpx.getTraces().calcTraceLength(vId);
				dataset.addSeries("Operation " + (vId+1),  convertDoubleListToDoubleArrayPrimitiv(list), bin);
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
	
	private CategoryDataset createDataset(String type) {
        // create the dataset...
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int vId = this.vIdStart; vId < this.vIdEnd; vId++) {
			if (type == "Spuren") {
				double cnt = gpx.getTraces().countDisplayedTraces(vId);
				dataset.addValue(cnt, type, "Operation " + (vId+1));				
			} else if (type == "Punkte") {
				double cnt = gpx.getTraces().countPoints(vId);
				dataset.addValue(cnt, type, "Operation " + (vId+1));
			}
		}
        
        
        return dataset;
        
    }
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		String actionCommand = arg0.getActionCommand();
		if (actionCommand == "drawGraph") {
			paint = true;
			vIdStart = Integer.valueOf(start.getText().trim());
			if(vIdStart < 0)
				vIdStart = 0;
			vIdEnd = Integer.valueOf(end.getText().trim());
			if(vIdEnd > Trace.getCurrentVersionId())
				vIdEnd = Trace.getCurrentVersionId();
			if(vIdStart > vIdEnd)
				vIdStart = vIdEnd;
			vIdEnd += 1;
			init();
		}
	}
}
