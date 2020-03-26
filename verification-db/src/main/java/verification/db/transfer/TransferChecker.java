package verification.db.transfer;

import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.DistributedTransactionManager;
import com.scalar.db.api.Get;
import com.scalar.db.api.Result;
import com.scalar.db.api.TransactionState;
import com.scalar.db.exception.transaction.CoordinatorException;
import com.scalar.db.exception.transaction.CrudException;
import com.scalar.db.transaction.consensuscommit.Coordinator;
import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.exception.PostProcessException;
import com.scalar.kelpie.modules.PostProcessor;
import io.github.resilience4j.retry.Retry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.json.JsonArray;
import javax.json.JsonObject;

public class TransferChecker extends PostProcessor {
  private final DistributedTransactionManager manager;
  private final Coordinator coordinator;

  public TransferChecker(Config config) {
    super(config);
    this.manager = Common.getTransactionManager(config);
    this.coordinator = new Coordinator(Common.getStorage(config));
  }

  @Override
  public void execute() {
    List<Result> results = readRecordsWithRetry();

    int committed = getNumOfCommittedFromCoordinator(config);

    if (!isConsistent(results, committed)) {
      throw new PostProcessException("Inconsistency happened!");
    }
  }

  public List<Result> readRecordsWithRetry() {
    logInfo("reading latest records ...");

    Retry retry = Common.getRetry("readRecords");
    Supplier<List<Result>> decorated = Retry.decorateSupplier(retry, this::readRecords);

    try {
      return decorated.get();
    } catch (Exception e) {
      throw new PostProcessException("Reading records failed repeatedly", e);
    }
  }

  @Override
  public void close() {}

  private List<Result> readRecords() {
    int numAccounts = (int) config.getUserLong("test_config", "num_accounts");
    List<Result> results = new ArrayList<>();

    boolean isFailed = false;
    DistributedTransaction transaction = manager.start();
    for (int i = 0; i < numAccounts; i++) {
      for (int j = 0; j < Common.NUM_TYPES; j++) {
        Get get = Common.prepareGet(i, j);
        try {
          transaction.get(get).ifPresent(r -> results.add(r));
        } catch (CrudException e) {
          // continue to read other records
          isFailed = true;
        }
      }
    }

    if (isFailed) {
      // for Retry
      throw new RuntimeException("at least 1 record couldn't be read");
    }

    return results;
  }

  private int getNumOfCommittedFromCoordinator(Config config) {
    Retry retry = Common.getRetry("checkCoordinator");
    Function<String, Optional<Coordinator.State>> decorated =
        Retry.decorateFunction(retry, id -> getState(id));

    JsonObject unknownTransactions = getPreviousState().getJsonObject("unknown_transaction");
    int committed = 0;
    for (String txId : unknownTransactions.keySet()) {
      Optional<Coordinator.State> state;
      try {
        state = decorated.apply(txId);
      } catch (Exception e) {
        throw new PostProcessException("Reading the status failed repeatedly", e);
      }
      if (state.isPresent() && state.get().getState().equals(TransactionState.COMMITTED)) {
        JsonArray ids = unknownTransactions.getJsonArray(txId);
        logInfo(
            "id: "
                + txId
                + " from: "
                + ids.getInt(0)
                + " to: "
                + ids.getInt(1)
                + " succeeded, not failed");
        committed++;
      }
    }

    return committed;
  }

  private Optional<Coordinator.State> getState(String txId) {
    try {
      logInfo("reading the status of " + txId);

      return coordinator.getState(txId);
    } catch (CoordinatorException e) {
      // convert the exception for Retry
      throw new RuntimeException("Failed to read the state from the coordinator", e);
    }
  }

  private boolean isConsistent(List<Result> results, int committed) {
    int totalVersion = Common.getActualTotalVersion(results);
    int totalBalance = Common.getActualTotalBalance(results);
    int expectedTotalVersion = (getPreviousState().getInt("committed") + committed) * 2;
    int expectedTotalBalance = Common.getTotalInitialBalance(config);

    logInfo("total version: " + totalVersion);
    logInfo("expected total version: " + expectedTotalVersion);
    logInfo("total balance: " + totalBalance);
    logInfo("expected total balance: " + expectedTotalBalance);

    if (totalVersion != expectedTotalVersion) {
      logError("version mismatch !");
      return false;
    }
    if (totalBalance != expectedTotalBalance) {
      logError("balance mismatch !");
      return false;
    }
    return true;
  }
}
