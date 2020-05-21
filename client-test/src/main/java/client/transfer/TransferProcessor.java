package client.transfer;

import client.Common;
import com.scalar.dl.client.exception.ClientException;
import com.scalar.dl.client.service.ClientService;
import com.scalar.dl.ledger.service.StatusCode;
import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.modules.TimeBasedProcessor;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class TransferProcessor extends TimeBasedProcessor {
  private final String transferContractName;
  private final ClientService service;
  private final int numAccounts;
  private final boolean isVerification;

  // for verification
  private final Map<String, List<Integer>> unknownTransactions = new ConcurrentHashMap<>();

  public TransferProcessor(Config config) {
    super(config);
    this.service = Common.getClientService(config);

    this.numAccounts = (int) config.getUserLong("test_config", "num_accounts");
    this.transferContractName = config.getUserString("contract", "transfer_contract_name");
    this.isVerification = config.getUserBoolean("test_config", "is_verification", false);
  }

  @Override
  public void executeEach() {
    int fromId = ThreadLocalRandom.current().nextInt(numAccounts);
    int toId = ThreadLocalRandom.current().nextInt(numAccounts);
    int amount = ThreadLocalRandom.current().nextInt(1000) + 1;
    JsonObject arg = makeArgument(fromId, toId, amount);

    String txId = arg.getString("nonce");
    logStart(txId, fromId, toId, amount);

    try {
      service.executeContract(transferContractName, arg);
    } catch (Exception e) {
      logFailure(txId, fromId, toId, amount, e);
      throw e;
    }

    logSuccess(txId, fromId, toId, amount);
  }

  @Override
  public void close() {
    if (isVerification) {
      JsonObjectBuilder builder = Json.createObjectBuilder();
      unknownTransactions.forEach(
          (txId, ids) -> {
            builder.add(txId, Json.createArrayBuilder().add(ids.get(0)).add(ids.get(1)).build());
          });

      setState(Json.createObjectBuilder().add("unknown_transaction", builder.build()).build());
    }

    service.close();
  }

  private JsonObject makeArgument(int fromId, int toId, int amount) {
    JsonArray assetIds =
        Json.createArrayBuilder().add(String.valueOf(fromId)).add(String.valueOf(toId)).build();

    return Json.createObjectBuilder()
        .add("asset_ids", assetIds)
        .add("amount", amount)
        .add("nonce", UUID.randomUUID().toString())
        .build();
  }

  private void logStart(String txId, int fromId, int toId, int amount) {
    if (isVerification) {
      logTxInfo("started", txId, fromId, toId, amount);
    }
  }

  private void logSuccess(String txId, int fromId, int toId, int amount) {
    if (isVerification) {
      logTxInfo("succeeded", txId, fromId, toId, amount);
    }
  }

  private void logFailure(String txId, int fromId, int toId, int amount, Throwable e) {
    if (!isVerification) {
      return;
    }

    if ((e instanceof ClientException)
        && (((ClientException) e).getStatusCode() == StatusCode.UNKNOWN_TRANSACTION_STATUS)) {
      unknownTransactions.put(txId, Arrays.asList(fromId, toId));
      logWarn("the status of the transaction is unknown: " + txId, e);
      logTxInfo("unknown", txId, fromId, toId, amount);
    } else {
      logWarn(txId + " failed", e);
      logTxInfo("failed", txId, fromId, toId, amount);
    }
  }

  private void logTxInfo(String status, String txId, int fromId, int toId, int amount) {
    logInfo(status + " - id: " + txId + " from: " + fromId + " to: " + toId + " amount: " + amount);
  }
}
