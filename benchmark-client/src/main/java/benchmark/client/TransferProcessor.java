package benchmark.client;

import com.scalar.dl.client.service.ClientService;
import com.scalar.dl.ledger.exception.ContractContextException;
import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.modules.Processor;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

public class TransferProcessor extends Processor {
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
      Supplier<Boolean> op = () -> transfer(service, random);

      ramp(op);

      run(op);
    }
  }

  @Override
  public void close() {}

  private boolean transfer(ClientService service, Random random) {
    try {
      long s = System.currentTimeMillis();

      int fromId = random.nextInt(numAccounts);
      int toId = random.nextInt(numAccounts);
      JsonObject arg = makeArgument(fromId, toId);
      service.executeContract(transferContractName, arg);

      return true;
    } catch (ContractContextException e) {
      logWarn("skip due to the same ID");
      return false;
    } catch (Exception e) {
      logWarn("contract execution failed", e);
      return false;
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
