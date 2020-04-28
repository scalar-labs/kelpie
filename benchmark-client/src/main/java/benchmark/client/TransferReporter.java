package benchmark.client;

import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.modules.PostProcessor;
import javax.json.JsonObject;

public class TransferReporter extends PostProcessor {

  public TransferReporter(Config config) {
    super(config);
  }

  @Override
  public void execute() {
    JsonObject result = getPreviousState();
    logInfo("TPS: " + (double) result.getInt("total") / config.getRunForSec());
    logInfo("Mean: " + result.getString("mean") + " ms");
    logInfo("SD: " + result.getString("sd") + " ms");
    logInfo("Max latency: " + result.getInt("max") + " ms");
  }

  @Override
  public void close() {}
}
