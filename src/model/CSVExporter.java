package model;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.tinylog.Logger;

/**
 * CSV (Comma Separated Value) implementation of {@link TransactionExporter}.
 */
public class CSVExporter implements TransactionExporter, CSVConstants {
  
  @Override
  public String exportTransactions(List<Transaction> txns, String filename) {
    if (txns == null) {
      Logger.warn("Export rejected because the transaction list was null");
      return TRANSACTION_LIST_ERROR_MESSAGE;
    }
    if (!InputValidation.isValidFilename(filename)) {
      Logger.warn("Export rejected because the filename was invalid filename={}", filename);
      return FILENAME_ERROR_MESSAGE;
    }

    Logger.debug("Export started file={} transactionCount={}", filename, txns.size());

    try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
      bw.write(CSV_HEADERS);
      bw.newLine();
      for (Transaction t : txns) {
        String line = String.format("%s" + COMMA_SEPARATOR + "%s" + COMMA_SEPARATOR + "%s", Double.toString(t.getAmount()), t.getCategory(), t.getTimestamp());
        bw.write(line);
        bw.newLine();
      }
      bw.flush();
      Logger.info("Export completed file={} transactionCount={}", filename, txns.size());
      return null;
    } catch (IOException e) {
      Logger.error(
          "Export failed file={} transactionCount={} exception={} message={}",
          filename,
          txns.size(),
          e.getClass().getSimpleName(),
          e.getMessage());
      return e.getMessage();
    }
  }

}
