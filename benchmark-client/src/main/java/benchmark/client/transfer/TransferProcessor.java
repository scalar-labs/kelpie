package benchmark.client.transfer;

import benchmark.client.Common;
import com.scalar.dl.client.service.ClientService;
import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.modules.TimeConsumingProcessor;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

public class TransferProcessor extends TimeConsumingProcessor {
  private final String transferContractName;
  private final ClientService service;
  private final int numAccounts;

  public TransferProcessor(Config config) {
    super(config);
    this.service = Common.getClientService(config);

    this.numAccounts = (int) config.getUserLong("test_config", "num_accounts");
    this.transferContractName = config.getUserString("test_config", "transfer_contract_name");
  }

  @Override
  public Supplier<Boolean> makeOperation() {
    Random random = new Random(System.currentTimeMillis() + Thread.currentThread().getId());

    Supplier<Boolean> operation =
        () -> {
          try {
            int fromId = random.nextInt(numAccounts);
            int toId = random.nextInt(numAccounts);
            JsonObject arg = makeArgument(fromId, toId);
            service.executeContract(transferContractName, arg);

            return true;
          } catch (Exception e) {
            // contract execution failed
            return false;
          }
        };

    return operation;
  }

  @Override
  public void close() {
    service.close();
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
