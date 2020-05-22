package contract;

import com.scalar.dl.ledger.contract.Contract;
import com.scalar.dl.ledger.database.Ledger;
import java.util.Optional;
import java.util.stream.IntStream;
import javax.json.Json;
import javax.json.JsonObject;

public class BatchCreate extends Contract {

  @Override
  public JsonObject invoke(Ledger ledger, JsonObject argument, Optional<JsonObject> properties) {
    // endId isn't included
    int startId = argument.getInt("start_id");
    int endId = argument.getInt("end_id");
    int amount = argument.getInt("amount");

    IntStream.range(startId, endId)
        .forEach(
            id -> {
              JsonObject json = Json.createObjectBuilder().add("balance", amount).build();
              String assetId = String.valueOf(id);
              ledger.put(assetId, json);
            });

    return null;
  }
}
