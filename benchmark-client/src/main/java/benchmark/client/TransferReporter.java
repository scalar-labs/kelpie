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
    summary();
  }

  @Override
  public void close() {}
}
