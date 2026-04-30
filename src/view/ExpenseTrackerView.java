package view;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import org.tinylog.Logger;

import model.ExpenseTrackerModel;
import model.Transaction;

import java.awt.*;
import java.util.List; 

/**
 * Provides a visualization of a list of transactions along with
 * the ability to add/remove transactions.
 * 
 * NOTE: Represents the View in the MVC architecture pattern.
 */
public class ExpenseTrackerView extends JFrame {

  private ExpenseTrackerModel model;
  
  private JTabbedPane tabbedPanel;
  private DataPanelView dataPanelView;
  private AnalysisPanelView analysisPanelView;
  
  private JMenuItem fileOpenFileMenuItem;
  private JMenuItem fileSaveAsMenuItem;
  private JMenuItem editDeleteMenuItem;

  public JTabbedPane getTabbedPanel() {
	  return this.tabbedPanel;
  }
  
  public DataPanelView getDataPanelView() {
	  return this.dataPanelView;
  }
  
  public AnalysisPanelView getAnalysisPanelView() {
	  return this.analysisPanelView;
  }
  
  public JMenuItem getOpenFileMenuItem() {
	  return fileOpenFileMenuItem;
  }
  
  public JMenuItem getSaveAsMenuItem() {
	  return fileSaveAsMenuItem;
  }
  
  public JMenuItem getDeleteMenuItem() {
	  return editDeleteMenuItem;
  }

  public ExpenseTrackerView(ExpenseTrackerModel model) {
    setTitle("Expense Tracker"); // Set title
 
    this.model = model;
    
    // Create the top menu bar
    JMenuBar topMenuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    topMenuBar.add(fileMenu);
    this.fileOpenFileMenuItem = new JMenuItem("Open File...");
    fileMenu.add(this.fileOpenFileMenuItem);
    this.fileSaveAsMenuItem = new JMenuItem("Save As...");
    fileMenu.add(this.fileSaveAsMenuItem);
    JMenu editMenu = new JMenu("Edit");
    topMenuBar.add(editMenu);
    this.editDeleteMenuItem = new JMenuItem("Delete");
    editMenu.add(editDeleteMenuItem);
    setJMenuBar(topMenuBar);
  
    // Layout components
    tabbedPanel = new JTabbedPane();
    dataPanelView = new DataPanelView();
    analysisPanelView = new AnalysisPanelView();
    tabbedPanel.add("Data", dataPanelView);
    tabbedPanel.add("Analysis", analysisPanelView);
    // a11y: name and describe the tab strip
    tabbedPanel.getAccessibleContext().setAccessibleName("Expense Tracker sections");
    tabbedPanel.getAccessibleContext().setAccessibleDescription(
        "Switch between entering transactions and reviewing category charts.");
    tabbedPanel.addChangeListener(e ->
        Logger.debug("Tab changed selectedIndex={}", tabbedPanel.getSelectedIndex()));
    add(tabbedPanel);
  
    // Set frame properties
    setSize(800, 600);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    refresh();
    setVisible(true);
    Logger.info("Expense tracker view initialized");
  
  }
  
  public String showFileChooser(boolean isOpenFile) {
	JFileChooser chooser = new JFileChooser();
	int result = -1;
	Logger.debug("Showing {} file chooser", isOpenFile ? "open" : "save");
	if (isOpenFile) {
		result = chooser.showOpenDialog(this);
	}
	else {
		result = chooser.showSaveDialog(this);
	}
	if (result == JFileChooser.APPROVE_OPTION) {
	  String path = chooser.getSelectedFile().getAbsolutePath();
	  if (!path.toLowerCase().endsWith(".csv")) {
	    path = path + ".csv";
	  }
	  Logger.info("File chooser selected path={}", path);
	  return path;
	}
	Logger.debug("File chooser canceled");
	return null;
  }
  
  public void displayErrorMessage(String message) {
    Logger.warn("Displaying error dialog message={}", message);
    JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
  } 

  public void refresh() {  
    // Pass to views
    dataPanelView.refreshTable(model);
    analysisPanelView.setVisible(model);
    Logger.debug(
        "View refreshed transactionCount={} totalCost={}",
        model.getTransactions().size(),
        model.computeTransactionsTotalCost());
  }

  // Other view methods
}
