package graph;

import org.jfree.chart.*;
import org.jfree.data.category.*;
import org.jfree.chart.plot.*;
import java.awt.*;

import javax.swing.JComponent;

public class BarChart<E> {
	private DefaultCategoryDataset dataset = new DefaultCategoryDataset();
	private JFreeChart chart;
	private String lblTitle, lblX, lblY;
	
	public BarChart(String title, String lblX, String lblY){
		lblTitle = title;
		this.lblX = lblX;
		this.lblY = lblY;
		chart = ChartFactory.createBarChart(lblTitle, lblX, lblY, dataset, PlotOrientation.VERTICAL, false,true, false);
		chart.setBackgroundPaint(Color.white);
		chart.getTitle().setPaint(Color.black); 
		
		CategoryPlot p = chart.getCategoryPlot(); 
		p.setRangeGridlinePaint(Color.red);
		
	}
	
	public void bindChart(JComponent com){
		com.add(new ChartPanel(chart));
	}
	
	public <T> void addValue(double val, Comparable<T> rowKey, Comparable<E> colKey){
		dataset.setValue(val, rowKey, colKey);
	}
	
	public void repaint(){
		chart.fireChartChanged();
	}
}
