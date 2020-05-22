package client.transfer;

import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.modules.PostProcessor;

public class TransferReporter extends PostProcessor {

  public TransferReporter(Config config) {
    super(config);
  }

  @Override
  public void execute() {
    getSummary();
  }

  @Override
  public void close() {}
}
