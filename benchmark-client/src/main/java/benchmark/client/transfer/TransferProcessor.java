package benchmark.client.transfer;

import benchmark.client.Common;
import com.scalar.dl.client.service.ClientService;
import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.modules.TimeBasedProcessor;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

public class TransferProcessor extends TimeBasedProcessor {
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
  public void executeEach() {
    int fromId = ThreadLocalRandom.current().nextInt(numAccounts);
    int toId = ThreadLocalRandom.current().nextInt(numAccounts);
    JsonObject arg = makeArgument(fromId, toId);

    service.executeContract(transferContractName, arg);
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
