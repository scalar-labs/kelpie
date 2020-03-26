package print;

import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.exception.PostProcessException;
import com.scalar.kelpie.modules.PostProcessor;

public class PrintPost extends PostProcessor {

  public PrintPost(Config config) {
    super(config);
  }

  @Override
  public void execute() {
    String title = config.getUserString("print_test", "title");
    int concurrency = (int) config.getConcurrency();
    long num = config.getUserLong("print_test", "num");

    logInfo("Checking for " + title);
    logInfo("Run for " + num + " seconds");

    int expectedTotal = (int) (num * config.getConcurrency());
    int actualTotal = getPreviousState().getInt("total");
    if (expectedTotal != actualTotal) {
      throw new PostProcessException("unexpected result");
    }
  }

  @Override
  public void close() {}
}
