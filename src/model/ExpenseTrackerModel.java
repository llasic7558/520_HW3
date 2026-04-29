package model;

import java.util.ArrayList;
import java.util.List;

import org.tinylog.Logger;

/**
 * Represents the data model as a list of transactions.
 * 
 * NOTE) Represents the Model in the MVC architecture pattern.
 */
public class ExpenseTrackerModel {

	private List<Transaction> transactions = new ArrayList<>();
	
	public ExpenseTrackerModel() {
		super();
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public void addTransaction(Transaction t) {
		transactions.add(t);
		Logger.info(
			"Model added transaction amount={} category={} timestamp={} transactionCount={} totalCost={}",
			t.getAmount(),
			t.getCategory(),
			t.getTimestamp(),
			transactions.size(),
			computeTransactionsTotalCost()
		);
	}

	public boolean removeTransaction(int transactionID) {
  	  // Perform input validation
  	  if ((transactionID < 0) || (transactionID > this.getTransactions().size() - 1)) {
  		  Logger.warn(
  		  	"Model rejected transaction removal for invalid transactionID={} transactionCount={}",
  		  	transactionID,
  		  	transactions.size()
  		  );
  		  return false;
  	  }
  	  else {
  		  Transaction removedTransaction = transactions.remove(transactionID);
  		  Logger.info(
  		  	"Model removed transaction transactionID={} amount={} category={} timestamp={} transactionCount={} totalCost={}",
  		  	transactionID,
  		  	removedTransaction.getAmount(),
  		  	removedTransaction.getCategory(),
  		  	removedTransaction.getTimestamp(),
  		  	transactions.size(),
  		  	computeTransactionsTotalCost()
  		  );
  		  return true;
  	  }
	}

	public double computeTransactionsTotalCost() {
		double totalCost=0;
		for(Transaction t : transactions) {
			totalCost+=t.getAmount();
		}
		return totalCost;
	}
}
