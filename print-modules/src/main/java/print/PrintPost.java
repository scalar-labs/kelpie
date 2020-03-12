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
    long num = config.getUserLong("print_test", "num");

    System.out.println("Checking for " + title);
    System.out.println("Run for " + num + " seconds");

    // always succeed

    // for failure
    //throw new PostProcessException("unexpected result");
  }
}
