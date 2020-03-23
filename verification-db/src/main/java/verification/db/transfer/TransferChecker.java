package verification.db.transfer;

import com.scalar.db.api.DistributedStorage;
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
import java.util.stream.IntStream;
import javax.json.JsonArray;
import javax.json.JsonObject;

public class TransferChecker extends PostProcessor {
  private final DistributedTransactionManager manager;

  public TransferChecker(Config config) {
    super(config);
    this.manager = Common.getTransactionManager(config);
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
    System.out.println("reading latest records ...");

    Retry retry = Common.getRetry("readRecords");
    Supplier<List<Result>> decorated = Retry.decorateSupplier(retry, this::readRecords);

    try {
      return decorated.get();
    } catch (Exception e) {
      throw new RuntimeException("Reading records failed repeatedly", e);
    }
  }

  @Override
  public void close() {}

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

  private int getNumOfCommittedFromCoordinator(Config config) {
    DistributedStorage storage = Common.getStorage(config);
    Coordinator coordinator = new Coordinator(storage);

    System.out.println("reading coordinator status...");

    Retry retry = Common.getRetry("checkCoordinator");
    Function<String, Optional<Coordinator.State>> getState =
        id -> {
          try {
            return coordinator.getState(id);
          } catch (CoordinatorException e) {
            // convert the exception for Retry
            throw new RuntimeException("Failed to read the state from the coordinator", e);
          }
        };
    Function<String, Optional<Coordinator.State>> decorated =
        Retry.decorateFunction(retry, getState);

    JsonObject unknownTransactions = getPreviousState().getJsonObject("unknown_transaction");
    int committed = 0;
    for (String txId : unknownTransactions.keySet()) {
      Optional<Coordinator.State> state = decorated.apply(txId);
      if (state.isPresent() && state.get().getState().equals(TransactionState.COMMITTED)) {
        JsonArray ids = unknownTransactions.getJsonArray(txId);
        System.out.println(
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

  private boolean isConsistent(List<Result> results, int committed) {
    int totalVersion = Common.getActualTotalVersion(results);
    int totalBalance = Common.getActualTotalBalance(results);
    int expectedTotalVersion = (getPreviousState().getInt("committed") + committed) * 2;
    int expectedTotalBalance = Common.getTotalInitialBalance(config);

    System.out.println("total version: " + totalVersion);
    System.out.println("expected total version: " + expectedTotalVersion);
    System.out.println("total balance: " + totalBalance);
    System.out.println("expected total balance: " + expectedTotalBalance);

    if (totalVersion != expectedTotalVersion) {
      System.out.println("version mismatch !");
      return false;
    }
    if (totalBalance != expectedTotalBalance) {
      System.out.println("balance mismatch !");
      return false;
    }
    return true;
  }
}
