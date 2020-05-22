package contract;

import com.scalar.dl.ledger.asset.Asset;
import com.scalar.dl.ledger.contract.Contract;
import com.scalar.dl.ledger.database.Ledger;
import java.util.Optional;
import javax.json.Json;
import javax.json.JsonObject;

public class Balance extends Contract {

  @Override
  public JsonObject invoke(Ledger ledger, JsonObject argument, Optional<JsonObject> properties) {
    String assetId = argument.getString("asset_id");

    Asset asset = ledger.get(assetId).get();

    return Json.createObjectBuilder()
        .add("result", "OK")
        .add("age", asset.age())
        .add("balance", asset.data().getInt("balance"))
        .build();
  }
}
