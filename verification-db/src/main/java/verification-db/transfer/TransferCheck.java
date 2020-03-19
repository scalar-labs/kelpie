package verification_db.transfer;

import com.scalar.db.api.DistributedStorage;
import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.DistributedTransactionManager;
import com.scalar.db.api.Get;
import com.scalar.db.api.Result;
import com.scalar.db.api.TransactionState;
import com.scalar.db.exception.transaction.CrudException;
import com.scalar.db.transaction.consensuscommit.Coordinator;
import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.exception.PostProcessException;
import com.scalar.kelpie.modules.PostProcessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import javax.json.JsonArray;
import javax.json.JsonObject;

public class TransferCheck extends PostProcessor {
  private final DistributedTransactionManager manager;
  private int committed = 0;

  public TransferCheck(Config config) {
    super(config);
    this.manager = Common.getTransactionManager(config);
  }

  @Override
  public void execute() {
    List<Result> results = readRecordsWithRetry();

    checkCoordinatorWithRetry(config);

    boolean isConsistent = checkConsistency(results);
    if (!isConsistent) {
      throw new PostProcessException("Inconsistency happened!");
    }
  }

  public List<Result> readRecordsWithRetry() {
    System.out.println("reading latest records ...");
    int i = 0;
    while (true) {
      if (i >= 10) {
        throw new RuntimeException("some records can't be recovered");
      }
      try {
        return readRecords();
      } catch (Exception e) {
        System.err.println(e.getMessage());
        ++i;
        Common.exponentialBackoff(i);
      }
    }
  }

  private List<Result> readRecords() {
    int numAccounts = (int) config.getUserLong("test_config", "num_accounts");
    int numTypes = (int) config.getUserLong("test_config", "num_account_types", Common.NUM_TYPES);
    List<Result> results = new ArrayList<>();

    DistributedTransaction transaction = manager.start();
    IntStream.range(0, numAccounts)
        .forEach(
            i -> {
              IntStream.range(0, numTypes)
                  .forEach(
                      j -> {
                        Get get = Common.prepareGet(i, j);
                        try {
                          transaction.get(get).ifPresent(r -> results.add(r));
                        } catch (CrudException e) {
                          throw new RuntimeException(e);
                        }
                      });
            });
    return results;
  }

  public void checkCoordinatorWithRetry(Config config) {
    DistributedStorage storage = Common.getStorage(config);
    Coordinator coordinator = new Coordinator(storage);

    System.out.println("reading coordinator status...");
    JsonObject unknownTransactions = this.state.getJsonObject("unknown_transaction");
    unknownTransactions.forEach(
        (txId, ids) -> {
          int i = 0;
          while (true) {
            if (i >= 10) {
              throw new RuntimeException("some records can't be recovered");
            }
            try {
              Optional<Coordinator.State> state = coordinator.getState(txId);
              if (state.isPresent() && state.get().getState().equals(TransactionState.COMMITTED)) {
                System.out.println(
                    "id: "
                        + txId
                        + " from: "
                        + ((JsonArray) ids).getInt(0)
                        + " to: "
                        + ((JsonArray) ids).getInt(1)
                        + " succeeded, not failed");
                // we can get the detail of the transaction by the ID if needed
                this.committed++;
              }
              break;
            } catch (Exception e) {
              ++i;
              Common.exponentialBackoff(i);
            }
          }
        });
  }

  private boolean checkConsistency(List<Result> results) {
    int totalVersion = Common.getActualTotalVersion(results);
    int totalBalance = Common.getActualTotalBalance(results);
    int expectedTotalVersion = (this.state.getInt("committed") + this.committed) * 2;
    int expectedTotalBalance = Common.getTotalInitialBalance(config);

    System.out.println("total version: " + totalVersion);
    System.out.println("expected total version: " + expectedTotalVersion);
    System.out.println("total balance: " + totalBalance);
    System.out.println("expected total balance: " + expectedTotalBalance);

    boolean isConsistent = true;
    if (totalVersion != expectedTotalVersion) {
      System.out.println("version mismatch !");
      isConsistent = false;
    }
    if (totalBalance != expectedTotalBalance) {
      System.out.println("balance mismatch !");
      isConsistent = false;
    }
    return isConsistent;
  }
}
