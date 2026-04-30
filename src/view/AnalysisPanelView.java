package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.tinylog.Logger;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.CategorySeries;
import org.knowm.xchart.XChartPanel;

import model.ExpenseTrackerModel;
import model.InputValidation;

/**
 * The AnalsisPanelView class supports performing data analysis on the model
 * and displaying the data analysis results to the user.
 */
public class AnalysisPanelView extends JPanel 
{
	public static final String NO_TRANSACTIONS_ERROR_MESSAGE = "There is no transaction data in the specified time window to be analyzed.";
	private JLabel messageLabel;
	public static final String CHART_X_AXIS_TITLE = "Category";
	public static final String CHART_Y_AXIS_TITLE = "Total Cost";
	public static final String CHART_TITLE = CHART_Y_AXIS_TITLE + " per " + CHART_X_AXIS_TITLE;
	private JPanel dataVizPanel;
	private JComboBox<DataAnalysisTimeWindow> timeWindowChooser;
	private JButton analyzeButton;
	private XChartPanel<CategoryChart> chartPanel;
	
	public AnalysisPanelView() {
		super();
		
		this.dataVizPanel = new JPanel();
		this.dataVizPanel.setLayout(new BoxLayout(this.dataVizPanel, BoxLayout.Y_AXIS));
		JPanel inputPanel = new JPanel();
		this.timeWindowChooser = new JComboBox<DataAnalysisTimeWindow>();
		DataAnalysisTimeWindow[] timeWindows = DataAnalysisTimeWindow.values();
		for (int i = 0; i < timeWindows.length; i++) {
			this.timeWindowChooser.addItem(timeWindows[i]);
		}
		this.timeWindowChooser.addActionListener(e ->
			Logger.debug("Analysis time window changed selection={}", this.timeWindowChooser.getSelectedItem()));
		inputPanel.add(this.timeWindowChooser);
		this.analyzeButton = new JButton("Analyze");
		inputPanel.add(this.analyzeButton);
		// a11y: name/describe the controls so screen readers announce them
		this.timeWindowChooser.getAccessibleContext().setAccessibleName("Analysis time window");
		this.timeWindowChooser.getAccessibleContext().setAccessibleDescription(
				"Choose All, Last week, or Last year before running analysis.");
		this.analyzeButton.getAccessibleContext().setAccessibleName("Analyze");
		this.analyzeButton.getAccessibleContext().setAccessibleDescription(
				"Builds or refreshes the bar chart of totals per category for the selected window.");
		this.dataVizPanel.getAccessibleContext().setAccessibleName("Analysis layout");
		this.dataVizPanel.add(inputPanel);
		
		JPanel messagePanel = new JPanel();
		messagePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		this.messageLabel = new JLabel("");
		this.messageLabel.setForeground(Color.red);
		this.messageLabel.setVisible(false);
		// a11y: when shown the message tells the user why the chart is missing
		this.messageLabel.getAccessibleContext().setAccessibleName("Analysis message");
		this.messageLabel.getAccessibleContext().setAccessibleDescription(
				"When visible, announces why the chart cannot be drawn for the chosen window.");
		messagePanel.add(this.messageLabel);
		
		setLayout(new BorderLayout());
		add(this.dataVizPanel, BorderLayout.NORTH);
		add(messagePanel, BorderLayout.SOUTH);	
	}
	
	public JButton getAnalyzeButton() {
		return this.analyzeButton;
	}
	
	public String getMessageLabelText() {
		// For testing purposes
		return this.messageLabel.getText();
	}
	
	public boolean hasChartPanel() {
		// For testing purposes
		return (this.chartPanel != null);
	}
	
	/**
	 * Returns a map from category to the total cost of all transactions with that category.
	 * 
	 * @return A map from category to the total cost of all transactions with that category.
	 */
	public Map<String,Double> getChartDataModel() {
		// For testing purposes
		if (this.chartPanel == null) {
			return null;
		}
		CategoryChart chart = this.chartPanel.getChart();
		Map<String,Double> chartDataModel = new LinkedHashMap<String,Double>();		
		CategorySeries categorySeries = chart.getSeriesMap().get(CHART_TITLE);
		Collection xData = categorySeries.getXData();
		Iterator xDataItr = xData.iterator();
		Collection yData = categorySeries.getYData();
		Iterator yDataItr = yData.iterator();
		for (; xDataItr.hasNext(); ) {
			String currentCategory = (String)xDataItr.next();
			Double currentCategoryTotalCost = (Double)yDataItr.next();
			chartDataModel.put(currentCategory, currentCategoryTotalCost);
		}

		return chartDataModel;
	}
	
	/**
	 * Creates a bar chart to display the total cost 
	 * per category.
	 * 
	 * @param model Represents the model's current state
	 * @return A bar chart displaying the total cost per category
	 */
	protected CategoryChart createCategoryChart(ExpenseTrackerModel model) {
		Map<String,ExpenseTrackerModel> categorySummary = DataVizUtils.computeCategorySummary(model, this.timeWindowChooser.getSelectedIndex());
		// Perform input validation to check that there were 
		// transactions in the specified time window.
		if (categorySummary.isEmpty()) {
			return null;
		}
		
		// Create Chart
		CategoryChart chart =
				new CategoryChartBuilder()
				.width(600)
				.height(400)
				.title(getClass().getSimpleName())
				.xAxisTitle(CHART_X_AXIS_TITLE)
				.yAxisTitle(CHART_Y_AXIS_TITLE)
				.build();

		// Customize Chart
		chart.getStyler().setLegendVisible(false);
		chart.getStyler().setLabelsVisible(false);
		chart.getStyler().setPlotGridLinesVisible(false);

		// Series
		List<Double> xAxisSeries = new ArrayList<Double>();
		for (int i = 0; i < InputValidation.VALID_CATEGORIES.length; i++) {
			ExpenseTrackerModel currentCategoryModel = categorySummary.get(InputValidation.VALID_CATEGORIES[i]);
			Double currentCategoryTotalCost = 0.0;
			if (currentCategoryModel != null) {
				currentCategoryTotalCost = currentCategoryModel.computeTransactionsTotalCost();
			}
			xAxisSeries.add(currentCategoryTotalCost);
		}
		chart.addSeries(CHART_TITLE, Arrays.asList(InputValidation.VALID_CATEGORIES), xAxisSeries);
		chart.setTitle(CHART_TITLE);
		
		return chart;
	}

	/**
	 * Performs data analysis on the given model and 
	 * visualizes the data analysis results.
	 * 
	 * @param model Represents the model's current state
	 */
	public void performDataAnalysis(ExpenseTrackerModel model) {
		Logger.debug(
			"Analyze requested timeWindow={} transactionCount={}",
			this.timeWindowChooser.getSelectedItem(),
			model.getTransactions().size()
		);
		if (model.getTransactions().isEmpty()) {
			this.messageLabel.setText(NO_TRANSACTIONS_ERROR_MESSAGE);
			this.messageLabel.setVisible(true);
			Logger.warn("Analyze failed because the model had no transactions");
		}
		else {
			this.messageLabel.setText("");
			this.messageLabel.setVisible(false);
			if (this.chartPanel != null) {
				this.dataVizPanel.remove(this.chartPanel);
				this.chartPanel = null;
			}
			CategoryChart categoryChart = this.createCategoryChart(model);
			if (categoryChart == null) {
				this.messageLabel.setText(NO_TRANSACTIONS_ERROR_MESSAGE);
				this.messageLabel.setVisible(true);
				Logger.warn(
					"Analyze found no transactions in timeWindow={}",
					this.timeWindowChooser.getSelectedItem()
				);
			}
			else {
				this.chartPanel = new XChartPanel<>(categoryChart);
				// a11y: name + describe the chart so screen readers know what it is
				this.chartPanel.getAccessibleContext().setAccessibleName(CHART_TITLE);
				this.chartPanel.getAccessibleContext().setAccessibleDescription(
						"Bar chart showing summed costs for food, travel, bills, entertainment, and other categories.");
				this.dataVizPanel.add(this.chartPanel, BorderLayout.CENTER);
				this.dataVizPanel.revalidate();
				this.dataVizPanel.repaint();
				CategorySeries categorySeries = categoryChart.getSeriesMap().get(CHART_TITLE);
				int displayedCategoryCount = categorySeries.getXData().size();
				Logger.info(
					"Analyze completed timeWindow={} displayedCategoryCount={}",
					this.timeWindowChooser.getSelectedItem(),
					displayedCategoryCount
				);
			}
		}
	}
	
	/**
	 * Sets the visibility of this panel's UI components 
	 * appropriately based on the model's current state.
	 * 
	 * @param model Represents the model's current state
	 */
	public void setVisible(ExpenseTrackerModel model) {
		this.messageLabel.setText("");
		this.messageLabel.setVisible(false);
		if (this.chartPanel != null) {
			this.dataVizPanel.remove(this.chartPanel);
		}
		this.dataVizPanel.revalidate();
		this.dataVizPanel.repaint();
		this.dataVizPanel.setVisible(true);
	}
}
