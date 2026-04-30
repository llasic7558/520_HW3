package view;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import org.tinylog.Logger;

import model.ExpenseTrackerModel;
import model.Transaction;

public class DataPanelView extends JPanel {
	private JTable transactionsTable;
	private JButton addTransactionBtn;
	private JTextField amountField;
	private JTextField categoryField;
	private DefaultTableModel transactionsModel;

	public DataPanelView() {
		super();
		
	    this.transactionsModel = new DefaultTableModel();
	    this.transactionsModel.addColumn("Serial");
	    this.transactionsModel.addColumn("Amount");
	    this.transactionsModel.addColumn("Category");
	    this.transactionsModel.addColumn("Date");
	    
	    addTransactionBtn = new JButton("Add Transaction");

	    // ui components
	    JLabel amountLabel = new JLabel("Amount:");
	    amountField = new JTextField(10);
	    // link label to field for screen readers
	    amountLabel.setLabelFor(amountField);

	    JLabel categoryLabel = new JLabel("Category:");
	    categoryField = new JTextField(10);
	    categoryLabel.setLabelFor(categoryField);

	    // short descriptions help screen readers tell what each field is for
	    amountField.getAccessibleContext().setAccessibleDescription(
	    		"Enter the transaction amount using digits and decimal point.");
	    categoryField.getAccessibleContext().setAccessibleDescription(
	    		"Enter a valid expense category text, for example food or travel.");

	    transactionsTable = new JTable(transactionsModel);
	    transactionsTable.setDefaultEditor(Object.class, null);
	    transactionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    // table + button accessible names so readers announce them clearly
	    transactionsTable.getAccessibleContext().setAccessibleName("Transactions");
	    transactionsTable.getAccessibleContext().setAccessibleDescription(
	    		"Table listing each serial, amount, category, and date. Last row is the total.");
	    addTransactionBtn.getAccessibleContext().setAccessibleName("Add transaction");
	    addTransactionBtn.getAccessibleContext().setAccessibleDescription(
	    		"Saves an expense using the Amount and Category fields and refreshes the table.");

	    setLayout(new BorderLayout());
	    JPanel addTransactionPanel = new JPanel();
	    JPanel inputPanel = new JPanel();
	    inputPanel.add(amountLabel);
	    inputPanel.add(amountField);
	    inputPanel.add(categoryLabel); 
	    inputPanel.add(categoryField);
	    inputPanel.add(addTransactionBtn);
	    addTransactionPanel.add(inputPanel);
	    
	    JPanel buttonPanel = new JPanel();
	    buttonPanel.add(addTransactionBtn);
	    addTransactionPanel.add(buttonPanel);
	  
	    // Add panels to frame
	    add(addTransactionPanel, BorderLayout.NORTH);
	    // wrap table in scroll pane and label it for assistive tech
	    JScrollPane tableScrollPane = new JScrollPane(transactionsTable);
	    tableScrollPane.getAccessibleContext().setAccessibleName("Transactions table");
	    tableScrollPane.getAccessibleContext().setAccessibleDescription(
	    		"Scrollable list of saved expense rows including the total.");
	    add(tableScrollPane, BorderLayout.CENTER);
	}
 	
	public JTable getTransactionsTable() {
		return transactionsTable;
	}

	public double getAmount() {
		if(amountField.getText().isEmpty()) {
			return 0;
		}else {
			double amount = Double.parseDouble(amountField.getText());
			return amount;
		}
	}
	
	public void setAmount(String amount) {
		// For testing purposes
		amountField.setText(amount);
	}

	public String getCategory() {
		return categoryField.getText();
	}
	
	public void setCategory(String category) {
		// For testing purposes
		categoryField.setText(category);
	}

	public JButton getAddTransactionBtn() {
		return addTransactionBtn;
	}
	
	public DefaultTableModel getTableModel() {
		return transactionsModel;
	}
	
	public int getSelectedTransactionID() {
		return this.transactionsTable.getSelectedRow();
	}
	
	public int getTransactionsTableRowCount() {
		// For testing purposes
		return this.transactionsTable.getRowCount();
	}
	  
	public Object getTransactionsTableValueAt(int row, int col) {
		// For testing purposes
		return this.transactionsModel.getValueAt(row, col);
	}
	
	public void refreshTable(ExpenseTrackerModel model) {
		List<Transaction> transactions = model.getTransactions();
		
		// model.setRowCount(0);
		transactionsModel.setRowCount(0);
		int rowNum = transactionsModel.getRowCount();

		// Add rows from transactions list
		for(Transaction t : transactions) {
			transactionsModel.addRow(new Object[]{rowNum+=1,t.getAmount(), t.getCategory(), t.getTimestamp()});

		}
		Object[] totalRow = {"Total", null, null, model.computeTransactionsTotalCost()};
		transactionsModel.addRow(totalRow);

		// Fire table update
		transactionsTable.updateUI();
		Logger.debug(
			"Data panel refreshed displayedTransactionRows={} displayedTableRows={} totalCost={}",
			transactions.size(),
			transactionsModel.getRowCount(),
			model.computeTransactionsTotalCost()
		);
	}  
}
