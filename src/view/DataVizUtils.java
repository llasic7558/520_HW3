package view;

import java.util.Collections;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

import org.tinylog.Logger;

import model.ExpenseTrackerModel;
import model.Transaction;

/**
 * The DataVizUtils is a utility class to perform data analysis of
 * a given ExpenseTrackerModel.
 */
public class DataVizUtils 
{
	/**
	 * Computes the opening date for the given time window.
	 * 
	 * @return The opening date for the given time window
	 */
	private static Date computeOpeningDate(int timeWindowOrdinal) {
		DataAnalysisTimeWindow timeWindow = DataAnalysisTimeWindow.values()[timeWindowOrdinal];
		Date openingDate = null;
		if (timeWindow != DataAnalysisTimeWindow.ALL) {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DAY_OF_YEAR, -timeWindow.getDays());
			openingDate = calendar.getTime();
		}
		return openingDate;
	}
	
	/**
	 * Converts the timestamp from a String to a Date.
	 * 
	 * @param timestampString The timestamp as a String
	 * @return The timestamp as a Date
	 */
	private static Date convertTimestamp(String timestampString) {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat(Transaction.TIMESTAMP_DATE_FORMAT);
			Date timestampDate = formatter.parse(timestampString);
			return timestampDate;
		}
		catch (ParseException pe) {
			// The Transaction class generateTimestamp method should never allow this.
			throw new RuntimeException(pe);
		}
	}
	
	/**
	 * Computes a category summary where the model's transactions are
	 * partitioned by category (represented as a map from each category
	 * to its own ExpenseTrackerModel with only the transactions with 
	 * that category).
	 * 
	 * @param model Represents the model's current state
	 * @return A map from each category to its own ExpenseTrackerModel containing
	 *         only the transactions with that category
	 */
	public static Map<String,ExpenseTrackerModel> computeCategorySummary(ExpenseTrackerModel model, int timeWindowOrdinal) {
		Date openingDate = computeOpeningDate(timeWindowOrdinal);
		Map<String,ExpenseTrackerModel> categorySummaries = new LinkedHashMap<String,ExpenseTrackerModel>();		
		for (Transaction currentTransaction : model.getTransactions()) {
			String currentCategory = currentTransaction.getCategory();
			Date currentTimestamp = convertTimestamp(currentTransaction.getTimestamp());
			if ((openingDate == null) || (currentTimestamp.after(openingDate))) {
				ExpenseTrackerModel currentCategorySummary = categorySummaries.get(currentCategory);
				if (currentCategorySummary == null) {
					currentCategorySummary = new ExpenseTrackerModel();
					categorySummaries.put(currentCategory, currentCategorySummary);
				}
				currentCategorySummary.getTransactions().add(currentTransaction);
			}	
		}

		StringBuilder summaryBuilder = new StringBuilder();
		for (Map.Entry<String, ExpenseTrackerModel> entry : categorySummaries.entrySet()) {
			if (summaryBuilder.length() > 0) {
				summaryBuilder.append(", ");
			}
			summaryBuilder.append(entry.getKey());
			summaryBuilder.append("=");
			summaryBuilder.append(entry.getValue().computeTransactionsTotalCost());
		}
		Logger.debug(
			"Computed category summary timeWindow={} categoryCount={} summary={}",
			DataAnalysisTimeWindow.values()[timeWindowOrdinal].getHumanReadableName(),
			categorySummaries.size(),
			summaryBuilder.toString()
		);
		
		return Collections.unmodifiableMap(categorySummaries);
	}
}
