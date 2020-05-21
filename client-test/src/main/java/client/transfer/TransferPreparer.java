package client.transfer;

import client.Common;
import com.scalar.dl.client.service.ClientService;
import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.exception.PreProcessException;
import com.scalar.kelpie.modules.PreProcessor;
import io.github.resilience4j.retry.Retry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import javax.json.Json;
import javax.json.JsonObject;

public class TransferPreparer extends PreProcessor {
  private final long POPULATION_CONCURRENCY = 16L;
  private final int NUM_ACCOUNTS_PER_TX = 100;

  private final String populationContractName;
  private final String populationContractPath;
  private final String transferContractName;
  private final String transferContractPath;
  private final String balanceContractName;
  private final String balanceContractPath;
  private final boolean isVerification;

  public TransferPreparer(Config config) {
    super(config);

    this.populationContractName = config.getUserString("contract", "population_contract_name");
    this.populationContractPath = config.getUserString("contract", "population_contract_path");
    this.transferContractName = config.getUserString("contract", "transfer_contract_name");
    this.transferContractPath = config.getUserString("contract", "transfer_contract_path");

    this.isVerification = config.getUserBoolean("test_config", "is_verification", false);
    this.balanceContractName = config.getUserString("contract", "balance_contract_name", "");
    this.balanceContractPath = config.getUserString("contract", "balance_contract_path", "");
  }

  @Override
  public void execute() {
    registerCertificateAndContracts();

    populateRecords();
  }

  @Override
  public void close() {}

  private void registerCertificateAndContracts() {
    try (ClientService service = Common.getClientService(config)) {
      service.registerCertificate();
      service.registerContract(
          populationContractName, populationContractName, populationContractPath, Optional.empty());
      service.registerContract(
          transferContractName, transferContractName, transferContractPath, Optional.empty());
      if (isVerification) {
        service.registerContract(
            balanceContractName, balanceContractName, balanceContractPath, Optional.empty());
      }
    } catch (Exception e) {
      throw new PreProcessException("Preparation failed a service", e);
    }
  }

  private void populateRecords() {
    logInfo("insert initial values ... ");

    ExecutorService es = Executors.newCachedThreadPool();
    List<CompletableFuture> futures = new ArrayList<>();
    int populationConcurrency =
        (int) config.getUserLong("test_config", "population_concurrency", POPULATION_CONCURRENCY);
    IntStream.range(0, populationConcurrency)
        .forEach(
            i -> {
              CompletableFuture<Void> future =
                  CompletableFuture.runAsync(new PopulationRunner(i), es);
              futures.add(future);
            });

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join();

    logInfo("all assets have been inserted");
  }

  private class PopulationRunner implements Runnable {
    private final ClientService service;
    private final int threadId;

    public PopulationRunner(int threadId) {
      this.service = Common.getClientService(config);
      this.threadId = threadId;
    }

    public void run() {
      int populationConcurrency =
          (int) config.getUserLong("test_config", "population_concurrency", POPULATION_CONCURRENCY);
      int numAccounts = (int) config.getUserLong("test_config", "num_accounts");
      int numPerThread = (numAccounts + populationConcurrency - 1) / populationConcurrency;
      int start = numPerThread * threadId;
      int end = Math.min(numPerThread * (threadId + 1), numAccounts);
      if (start >= end) {
        // this thread doesn't need to run
        return;
      }

      try {
        IntStream.range(0, (numPerThread + NUM_ACCOUNTS_PER_TX - 1) / NUM_ACCOUNTS_PER_TX)
            .forEach(
                i -> {
                  int startId = start + NUM_ACCOUNTS_PER_TX * i;
                  int endId = Math.min(start + NUM_ACCOUNTS_PER_TX * (i + 1), end);
                  populateWithTx(startId, endId);
                });
      } catch (Exception e) {
        throw new PreProcessException("Population failed", e);
      } finally {
        service.close();
      }
    }

    private void populateWithTx(int startId, int endId) {
      JsonObject argument =
          Json.createObjectBuilder()
              .add("start_id", startId)
              .add("end_id", endId)
              .add("amount", Common.INITIAL_BALANCE)
              .add("nonce", UUID.randomUUID().toString())
              .build();
      Runnable populate = () -> service.executeContract(populationContractName, argument);

      Retry retry = Common.getRetryWithFixedWaitDuration("populate");
      Runnable decorated = Retry.decorateRunnable(retry, populate);
      try {
        decorated.run();
      } catch (Exception e) {
        logError("population failed repeatedly!");
        throw e;
      }
    }
  }
}
