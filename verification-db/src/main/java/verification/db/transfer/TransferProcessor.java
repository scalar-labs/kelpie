package verification.db.transfer;

import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.DistributedTransactionManager;
import com.scalar.db.api.Get;
import com.scalar.db.api.Put;
import com.scalar.db.api.Result;
import com.scalar.db.exception.transaction.CommitException;
import com.scalar.db.exception.transaction.CrudException;
import com.scalar.db.exception.transaction.UnknownTransactionStatusException;
import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.modules.Processor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.json.Json;
import javax.json.JsonObjectBuilder;

public class TransferProcessor extends Processor {
  private final DistributedTransactionManager manager;
  private final AtomicInteger committed = new AtomicInteger(0);
  private final Map<String, List<Integer>> unknownTransactions = new ConcurrentHashMap<>();

  public TransferProcessor(Config config) {
    super(config);
    this.manager = Common.getTransactionManager(config);
  }

  @Override
  public void execute() {
    int numAccounts = (int) config.getUserLong("test_config", "num_accounts");
    List<Integer> ids = new ArrayList<>();
    Random random = new Random(System.currentTimeMillis() + Thread.currentThread().getId());

    long end = System.currentTimeMillis() + (config.getRampForSec() + config.getRunForSec()) * 1000;
    while (System.currentTimeMillis() < end) {
      ids.clear();
      ids.add(random.nextInt(numAccounts)); // fromId
      ids.add(random.nextInt(numAccounts)); // toId
      int amount = random.nextInt(100) + 1;

      DistributedTransaction transaction = manager.start();
      String txId = transaction.getId();
      logStart(txId, ids, amount);

      try {
        transfer(transaction, ids, amount);

        logSuccess(txId, ids, amount);
        continue;
      } catch (UnknownTransactionStatusException e) {
        unknownTransactions.put(txId, ids);
        logWarn("the status of the transaction is unknown: " + txId, e);
      } catch (Exception e) {
        logWarn(txId + " failed", e);
      }
      logFailure(txId, ids, amount);
    }
  }

  @Override
  public void close() {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    unknownTransactions.forEach(
        (txId, ids) -> {
          builder.add(txId, Json.createArrayBuilder().add(ids.get(0)).add(ids.get(1)).build());
        });

    setState(
        Json.createObjectBuilder()
            .add("committed", committed.get())
            .add("unknown_transaction", builder.build())
            .build());
  }

  private void transfer(DistributedTransaction transaction, List<Integer> ids, int amount)
      throws CrudException, CommitException, UnknownTransactionStatusException {
    int fromId = ids.get(0);
    int toId = ids.get(1);
    int fromType = 0;
    int toType = 0;
    if (fromId == toId) {
      toType = 1; // transfer between the same account
    }

    Get fromGet = Common.prepareGet(fromId, fromType);
    Get toGet = Common.prepareGet(toId, toType);

    Optional<Result> fromResult = transaction.get(fromGet);
    Optional<Result> toResult = transaction.get(toGet);
    int fromBalance = Common.getBalanceFromResult(fromResult.get());
    int toBalance = Common.getBalanceFromResult(toResult.get());

    Put fromPut = Common.preparePut(fromId, fromType, fromBalance - amount);
    Put toPut = Common.preparePut(toId, toType, toBalance + amount);
    transaction.put(fromPut);
    transaction.put(toPut);

    transaction.commit();
  }

  private void logStart(String txId, List<Integer> ids, int amount) {
    logTxInfo("started", txId, ids, amount);
  }

  private void logSuccess(String txId, List<Integer> ids, int amount) {
    committed.incrementAndGet();
    logTxInfo("succeeded", txId, ids, amount);
  }

  private void logFailure(String txId, List<Integer> ids, int amount) {
    logTxInfo("falied", txId, ids, amount);
  }

  private void logTxInfo(String status, String txId, List<Integer> ids, int amount) {
    int fromId = ids.get(0);
    int toId = ids.get(1);

    logInfo(
        status
            + " - id: "
            + txId
            + " from: "
            + fromId
            + ",0"
            + " to: "
            + toId
            + ","
            + ((fromId == toId) ? 1 : 0)
            + " amount: "
            + amount);
  }
}
