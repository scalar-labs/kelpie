package benchmark.client;

import com.scalar.dl.client.service.ClientService;
import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.exception.PreProcessException;
import com.scalar.kelpie.modules.PreProcessor;
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
  private final int INITIAL_BALANCE = 10000;

  private String populationContractName;
  private String populationContractPath;
  private String transferContractName;
  private String transferContractPath;

  public TransferPreparer(Config config) {
    super(config);

    populationContractName = config.getUserString("test_config", "population_contract_name");
    populationContractPath = config.getUserString("test_config", "population_contract_path");
    transferContractName = config.getUserString("test_config", "transfer_contract_name");
    transferContractPath = config.getUserString("test_config", "transfer_contract_path");
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
    } catch (Exception e) {
      logError("Preparation failed", e);
      throw e;
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
                  CompletableFuture.runAsync(
                      () -> {
                        new PopulationRunner(i).run();
                      },
                      es);
              futures.add(future);
            });

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join();

    logInfo("all assets have been inserted");
  }

  private class PopulationRunner {
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

      IntStream.range(0, (numPerThread + NUM_ACCOUNTS_PER_TX - 1) / NUM_ACCOUNTS_PER_TX)
          .forEach(
              i -> {
                int startId = start + NUM_ACCOUNTS_PER_TX * i;
                int endId = Math.min(start + NUM_ACCOUNTS_PER_TX * (i + 1), end);
                populateWithTx(startId, endId);
              });

      try {
        service.close();
      } catch (Exception e) {
        throw new PreProcessException("Failed to shutdown a service");
      }
    }

    private void populateWithTx(int startId, int endId) {
      int retries = 0;
      while (true) {
        if (retries++ > 10) {
          logError("population failed repeatedly!");
          try {
            service.close();
          } catch (Exception e) {
            logError("service close failed");
            throw e;
          }
        }
        try {
          JsonObject argument =
              Json.createObjectBuilder()
                  .add("start_id", startId)
                  .add("end_id", endId)
                  .add("amount", INITIAL_BALANCE)
                  .add("nonce", UUID.randomUUID().toString())
                  .build();

          service.executeContract(populationContractName, argument);

          // success
          break;
        } catch (Exception e) {
          logWarn("population failed, retry");
        }
      }
    }
  }
}
