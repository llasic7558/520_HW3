package controller;

import java.io.IOException;
import java.util.List;

import org.tinylog.Logger;

import model.CSVExporter;
import model.CSVImporter;
import model.ExpenseTrackerModel;
import model.Transaction;
import view.ExpenseTrackerView;

/**
 * Provides the application programming layer to support the
 * following interface: addTransaction, delete, import, export.
 * 
 * NOTE) Represents the Controller in the MVC architecture pattern.
 */
public class ExpenseTrackerController {
	private ExpenseTrackerModel model = new ExpenseTrackerModel();    
    private ExpenseTrackerView view = new ExpenseTrackerView(model);
    
    public ExpenseTrackerController() {
    	super();
    	
    	// Hook up the view and controller
    	
        // Handle add transaction button clicks
        view.getDataPanelView().getAddTransactionBtn().addActionListener(e -> {
        	addTransaction();
        });
        
        // Handle "Delete" menu item clicks
        view.getDeleteMenuItem().addActionListener(e -> {
        	delete();
        });
        
        // Handle "Open File..." menu item clicks
        view.getOpenFileMenuItem().addActionListener(e -> {
        	openFile();
        });
        
        // Handle "Save" menu item clicks
        view.getSaveAsMenuItem().addActionListener(e -> {	  
        	saveAs();
        });
        
        // Handle "Analyze" button clicks
        view.getAnalysisPanelView().getAnalyzeButton().addActionListener(e -> {
        	performDataAnalysis();
        });
        
        // Initialize view
        view.setVisible(true);
        Logger.info("Controller initialized and main view displayed");
    }
    
    public ExpenseTrackerModel getModel() {
    	// For testing purposes
    	return this.model;
    }
    
    public ExpenseTrackerView getView() {
    	// For testing purposes
    	return this.view;
    }
    
    public void addTransaction() { 
    	Logger.debug("Add transaction requested");
    	try {
    		// Get transaction data from view
    		double amount = view.getDataPanelView().getAmount(); 
    		String category = view.getDataPanelView().getCategory();

    		// Create transaction object
    		Transaction t = new Transaction(amount, category);

    		// Call controller to add transaction
    		model.addTransaction(t);
    		view.refresh();
    		Logger.info("Add transaction completed");
    	}
    	catch (NumberFormatException nfe) {
    		Logger.warn(
    			"Add transaction rejected because the amount could not be parsed message={}",
    			nfe.getMessage()
    		);
    		view.displayErrorMessage("The amount cannot be parsed as a double number.");
    	}
    	catch (IllegalArgumentException iae) {
    		Logger.warn(
    			"Add transaction rejected because the input was invalid message={}",
    			iae.getMessage()
    		);
    		view.displayErrorMessage(iae.getMessage());
    	}
    }
    
    public void delete() {
        int selectedTransactionID = view.getDataPanelView().getSelectedTransactionID();
        Logger.debug("Delete transaction requested for selection={}", selectedTransactionID);
    	boolean removed = model.removeTransaction(selectedTransactionID);
    	if (! removed) {
    		Logger.warn("Delete transaction failed because no valid transaction was selected");
    		view.displayErrorMessage("A valid transaction was not selected to be removed.");
    	}
    	else {
    		view.refresh();
    		Logger.info("Delete transaction completed for selection={}", selectedTransactionID);
    	}
    }
    
    public void openFile() {
    	Logger.debug("Open file requested");
    	String inputFileName = view.showFileChooser(true);
    	if (inputFileName != null) {  	    
    		Logger.debug("Importing transactions from file={}", inputFileName);
    		int transactionCount = model.getTransactions().size();
    		for (int i = 0; i < transactionCount; i++) {
    			model.removeTransaction(0);
    		}

    		try {
    			CSVImporter csvImporter = new CSVImporter();
    			List<Transaction> importedTransactionsList = csvImporter.importTransactions(inputFileName);
    			for (Transaction importedTransaction : importedTransactionsList) {				
    				model.addTransaction(importedTransaction);
    			}
    			Logger.info("Open file completed with importedTransactionCount={}", importedTransactionsList.size());
    		}
    		catch (IOException ioe) {
    			Logger.error(
    				"Open file failed for file={} exception={} message={}",
    				inputFileName,
    				ioe.getClass().getSimpleName(),
    				ioe.getMessage()
    			);
    			view.displayErrorMessage(ioe.getMessage());
    		}
    		view.refresh();
    	}
    	else {
    		Logger.debug("Open file canceled by user");
    	}
    }
    
    public void saveAs() {
    	Logger.debug("Save file requested");
    	String outputFileName = view.showFileChooser(false);
    	if (outputFileName != null) {
    		CSVExporter csvExporter = new CSVExporter();
    		String errorMessage = csvExporter.exportTransactions(model.getTransactions(), outputFileName);
    		if (errorMessage != null) {
    			Logger.error("Save file failed for file={} with message={}", outputFileName, errorMessage);
    			view.displayErrorMessage(errorMessage);
    		}
    		else {
    			Logger.info("Save file completed for file={}", outputFileName);
    		}
    	}
    	else {
    		Logger.debug("Save file canceled by user");
    	}
    }
    
    public void performDataAnalysis() {
    	Logger.debug("Analyze requested from controller");
    	view.getAnalysisPanelView().performDataAnalysis(model);
    }
}
