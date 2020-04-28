package benchmark.client;

import com.scalar.dl.client.service.ClientService;
import com.scalar.dl.ledger.exception.ContractContextException;
import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.modules.Processor;
import java.util.Random;
import java.util.UUID;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import org.HdrHistogram.Histogram;

public class TransferProcessor extends Processor {
  // 1 ms to 300 seconds with 3 decimal point resolution:
  private final Histogram histogram = new Histogram(300000L, 3);
  private final String transferContractName;
  private int numAccounts;

  public TransferProcessor(Config config) {
    super(config);
    this.numAccounts = (int) config.getUserLong("test_config", "num_accounts");
    transferContractName = config.getUserString("test_config", "transfer_contract_name");
  }

  @Override
  public void execute() {
    Random random = new Random(System.currentTimeMillis() + Thread.currentThread().getId());

    try (ClientService service = Common.getClientService(config)) {
      long end = System.currentTimeMillis() + config.getRampForSec() * 1000;
      do {
        transfer(service, random, false);
      } while (System.currentTimeMillis() < end);

      end = System.currentTimeMillis() + config.getRunForSec() * 1000;
      do {
        transfer(service, random, true);
      } while (System.currentTimeMillis() < end);
    }
  }

  @Override
  public void close() {
    long total = histogram.getTotalCount();
    double mean = histogram.getMean();
    double sd = histogram.getStdDeviation();
    long max = histogram.getMaxValue();
    setState(Json.createObjectBuilder().add("total", (int)total)
        .add("mean", String.format("%.2f", mean))
        .add("sd", String.format("%.2f", sd))
        .add("max", (int)max)
        .build());
  }

  private void transfer(ClientService service, Random random, boolean isRecorded) {
    try {
      long s = System.currentTimeMillis();

      int fromId = random.nextInt(numAccounts);
      int toId = random.nextInt(numAccounts);
      JsonObject arg = makeArgument(fromId, toId);
      service.executeContract(transferContractName, arg);

      if (isRecorded) {
        histogram.recordValue(System.currentTimeMillis() - s);
      }
    } catch (ContractContextException e) {
      logWarn("skip due to the same ID");
    } catch (Exception e) {
      logWarn("contract execution failed", e);
    }
  }

  private JsonObject makeArgument(int fromId, int toId) {
    JsonArray assetIds =
        Json.createArrayBuilder().add(String.valueOf(fromId)).add(String.valueOf(toId)).build();

    return Json.createObjectBuilder()
        .add("asset_ids", assetIds)
        .add("amount", 1)
        .add("nonce", UUID.randomUUID().toString())
        .build();
  }
}
