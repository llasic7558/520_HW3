

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.swing.table.DefaultTableModel;

import org.junit.Before;
import org.junit.Test;

import controller.ExpenseTrackerController;
import model.CSVExporter;
import model.CSVImporter;
import model.ExpenseTrackerModel;
import model.InputValidation;
import model.Transaction;
import view.AnalysisPanelView;
import view.DataPanelView;
import view.ExpenseTrackerView;

public class ExpenseTrackerTest {
  public static final double[] EXPECTED_AMOUNT = { 50.0, 133.0, 55.0, 22.0 };
  public static final String[] EXPECTED_CATEGORY = {
		  InputValidation.VALID_CATEGORIES[0],
		  InputValidation.VALID_CATEGORIES[1],
		  InputValidation.VALID_CATEGORIES[1],
		  InputValidation.VALID_CATEGORIES[4]
  };
  public static final String TEST_CSV_FILE_NAME = "test/test_expenses.csv";
  public static final String ANOTHER_TEST_CSV_FILE_NAME = "test/another_test_expenses.csv";	
  
  // For unit testing
  private ExpenseTrackerModel model;
  // For end-to-end testing
  private ExpenseTrackerController controller;

  @Before
  public void setup() {
	model = new ExpenseTrackerModel();
	controller = new ExpenseTrackerController();
  }
  
  @Test
  public void testInitialConfiguration() {
    // There aren't any pre-conditions to be checked
    // The setup method called the constructors
    // Check the post-conditions
    assertEquals(0, model.getTransactions().size());
    assertEquals(0, model.computeTransactionsTotalCost(), 0.001);
  }
  
  private void checkTransaction(double expectedAmount, String expectedCategory, Transaction toBeCheckedTransaction) {
	    assertEquals(expectedAmount, toBeCheckedTransaction.getAmount(), 0.001);
	    assertEquals(expectedCategory, toBeCheckedTransaction.getCategory());
  }

  private void testAddTransactionHelper(double amount, String category) {
	    // Check the pre-conditions
	    assertEquals(0, model.getTransactions().size());
	    assertEquals(0, model.computeTransactionsTotalCost(), 0.001);
		
	    // Create a new transaction and add it
	    Transaction transaction = new Transaction(amount, category);
	    model.addTransaction(transaction);

	    // Check the post-conditions: 
	    // Verify that the transaction was added appropriately
	    java.util.List<Transaction> transactions = model.getTransactions();
	    assertEquals(1, transactions.size());
	    checkTransaction(amount, category, transactions.get(0));
	    assertEquals(amount, model.computeTransactionsTotalCost(), 0.001);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testTransactionInvalidAmount() {
	  // Perform setup and check pre-conditions
	  double invalidAmount = -1.0;
	  String validCategory = InputValidation.VALID_CATEGORIES[0];
	  assertFalse(InputValidation.isValidAmount(invalidAmount));
	  assertTrue(InputValidation.isValidCategory(validCategory));
	  // Call the unit under test
	  new Transaction(invalidAmount, validCategory);
	  // Check the post-condition (see the @Test annotation)
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testTransactionInvalidCategory() {
	  // Perform setup and check pre-conditions
	  double validAmount = 1.0;
	  String invalidCategory = "Books";
	  assertTrue(InputValidation.isValidAmount(validAmount));
	  assertFalse(InputValidation.isValidCategory(invalidCategory));
	  // Call the unit under test
	  new Transaction(validAmount, invalidCategory);
	  // Check the post-condition (see the @Test annotation)
  } 
  
  @Test
  public void testAddTransaction() {
	  double amount = 100.0;
	  String category = "Food";
	  this.testAddTransactionHelper(amount, category);
  }
  
  @Test
  public void testRemoveTransactionInvalidIDLow() {
	  // Perform setup and check preconditions
	  int invalidID = -1;
	  assertTrue(invalidID < 0);
	  // Call the unit under test
	  boolean removed = model.removeTransaction(invalidID);
	  // Check the post-conditions
	  assertFalse(removed);
	  assertEquals(0, model.getTransactions().size());
	  assertEquals(0, model.computeTransactionsTotalCost(), 0.001);
  }
  
  @Test
  public void testRemoveTransactionInvalidIDHigh() {
	  // Perform setup and check preconditions
	  int invalidID = model.getTransactions().size() + 1;
	  assertTrue(invalidID > 0);
	  // Call the unit under test
	  boolean removed = model.removeTransaction(invalidID);
	  // Check the post-conditions
	  assertFalse(removed);
	  assertEquals(0, model.getTransactions().size());
	  assertEquals(0, model.computeTransactionsTotalCost(), 0.001);
  }
  
  @Test
  public void testRemoveTransaction() {
	  // Initialize: Add a new transaction
	  double amount = 100.0;
	  String category = "Food";
	  this.testAddTransactionHelper(amount, category);
	  // Remove that transaction
	  model.removeTransaction(0);
	  // Check the post-conditions
	  assertEquals(0, model.getTransactions().size());
	  assertEquals(0, model.computeTransactionsTotalCost(), 0.001);
  }
  
  @Test
  public void testExportImport() {
	  // Also tests the Transaction constructor with package visibility
	  //
	  // Perform setup and check pre-conditions
	  for (int i = 0; i < EXPECTED_AMOUNT.length; i++) {
		  model.addTransaction(new Transaction(EXPECTED_AMOUNT[i], EXPECTED_CATEGORY[i]));
	  }
	  List<Transaction> transactionsList = model.getTransactions();
	  assertEquals(4, transactionsList.size());
	  int expectedTotalCost = 0;
	  for (int j = 0; j < EXPECTED_AMOUNT.length; j++) {
		  Transaction currentTransaction = transactionsList.get(j);
		  expectedTotalCost += currentTransaction.getAmount();
		  checkTransaction(EXPECTED_AMOUNT[j], EXPECTED_CATEGORY[j], currentTransaction);
	  }
	  assertEquals(expectedTotalCost, model.computeTransactionsTotalCost(), 0.01);
	  CSVExporter exporter = new CSVExporter();
	  String exporterResult = exporter.exportTransactions(model.getTransactions(), TEST_CSV_FILE_NAME);
	  assertNull(exporterResult);
	  // Call the unit under test
	  CSVImporter importer = new CSVImporter();
	  List<Transaction> importedTransactions = null;
	  try {
		  importedTransactions = importer.importTransactions(TEST_CSV_FILE_NAME);
	  }
	  catch (IOException ioe) {
		  fail(ioe.getMessage());
	  }
	  // Check the postconditions
	  assertEquals(4, importedTransactions.size());
	  expectedTotalCost = 0;
	  for (int j = 0; j < transactionsList.size(); j++) {
		  Transaction exportedTransaction = transactionsList.get(j);
		  Transaction importedTransaction = importedTransactions.get(j);
		  expectedTotalCost += importedTransaction.getAmount();
		  checkTransaction(exportedTransaction.getAmount(), exportedTransaction.getCategory(), importedTransaction);
	  }
	  assertEquals(expectedTotalCost, model.computeTransactionsTotalCost(), 0.01);
  }
  
  @Test
  public void testAddTransactionE2E() {
	  // Perform initialization and check the preconditions
	  double newAmount = 44.0;
	  String newCategory = "Other";
	  DataPanelView view = controller.getView().getDataPanelView();
	  view.setAmount("" + newAmount);
	  view.setCategory(newCategory);
	  assertEquals(1, view.getTransactionsTableRowCount());
	  // Call the unit under test: Add the new transaction
	  view.getAddTransactionBtn().doClick();
	  // Check the post-conditions
	  assertEquals(2, view.getTransactionsTableRowCount());
	  assertEquals(newAmount, view.getTransactionsTableValueAt(0, 1));
	  assertEquals(newCategory, view.getTransactionsTableValueAt(0, 2));
	  assertEquals(newAmount, view.getTransactionsTableValueAt(1, 3));
  }
  
  @Test
  public void testAnalyzeHasNoTransactionsE2E() {
	  // Perform initialization and check the preconditions
	  DataPanelView dataPanelView = controller.getView().getDataPanelView();
	  assertEquals(1, dataPanelView.getTransactionsTableRowCount());	  
	  // Switch to the analysis tab
	  controller.getView().getTabbedPanel().setSelectedIndex(1);
	  // Call the unit under test: Try to analyze the model
	  AnalysisPanelView analysisPanelView = controller.getView().getAnalysisPanelView();
	  analysisPanelView.getAnalyzeButton().doClick();
	  // Check the postconditions
	  assertEquals(AnalysisPanelView.NO_TRANSACTIONS_ERROR_MESSAGE, analysisPanelView.getMessageLabelText());
	  assertEquals(
			  "Analysis could not be generated. " + AnalysisPanelView.NO_TRANSACTIONS_ERROR_MESSAGE,
			  analysisPanelView.getAnalysisSummaryText());
	  assertFalse(analysisPanelView.hasChartPanel());
  }
  
  private double computeTotalCostPerCategory(ExpenseTrackerModel model, String category) {
	  double categoryTotalCost = 0.0;
	  for (Transaction currentTransaction : model.getTransactions()) {
		  if (currentTransaction.getCategory().equals(category)) {
			  categoryTotalCost += currentTransaction.getAmount();
		  }
	  }
	  return categoryTotalCost;
  }
  
  @Test
  public void testAnalyzeHasTransactionsE2E() {
	  // Perform initialization and check the preconditions
	  DataPanelView dataPanelView = controller.getView().getDataPanelView();
	  CSVImporter csvImporter = new CSVImporter();
	  try {
		  List<Transaction> importedTransactionsList = csvImporter.importTransactions(ANOTHER_TEST_CSV_FILE_NAME);
		  for (Transaction importedTransaction : importedTransactionsList) {				
			  controller.getModel().addTransaction(importedTransaction);
		  }
		  controller.getView().refresh();
		  assertEquals(importedTransactionsList.size() + 1, dataPanelView.getTransactionsTableRowCount());
		  double expectedTotalCost = 0.0;
		  for (int i = 0; i < importedTransactionsList.size(); i++) {
			  Transaction importedTransaction = importedTransactionsList.get(i);
			  assertEquals(importedTransaction.getAmount(), dataPanelView.getTransactionsTableValueAt(i, 1));
			  assertEquals(importedTransaction.getCategory(), dataPanelView.getTransactionsTableValueAt(i, 2));			  
			  expectedTotalCost += importedTransaction.getAmount();
		  }
		  assertEquals(expectedTotalCost, dataPanelView.getTransactionsTableValueAt(importedTransactionsList.size(), 3));
	  }
	  catch (IOException ioe) {
		  fail();
	  }
	  // Switch to the analysis tab
	  controller.getView().getTabbedPanel().setSelectedIndex(1);
	  // Call the unit under test: Try to analyze the model
	  AnalysisPanelView analysisPanelView = controller.getView().getAnalysisPanelView();
	  analysisPanelView.getAnalyzeButton().doClick();
	  // Check the postconditions
	  assertEquals("", analysisPanelView.getMessageLabelText());
	  assertTrue(analysisPanelView.hasChartPanel());
	  String summaryText = analysisPanelView.getAnalysisSummaryText();
	  assertTrue(summaryText.startsWith("Analysis results for"));
	  Map<String,Double> chartDataModel = analysisPanelView.getChartDataModel();
	  for (String currentCategory : chartDataModel.keySet()) {
		  double expectedTotalCost = computeTotalCostPerCategory(controller.getModel(), currentCategory);
		  double actualTotalCost = chartDataModel.get(currentCategory);
		  assertEquals(expectedTotalCost, actualTotalCost, 0.01);
		  assertTrue(summaryText.contains(currentCategory + ": " + actualTotalCost));
	  }
  }
}
