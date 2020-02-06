package print;

import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.modules.Processor;

public class PrintProcessor extends Processor {

  public PrintProcessor(Config config) {
    super(config);
  }

  @Override
  public void execute() {
    long num = config.getUserLong("print_test", "num");

    for (long i = 0; i < num; i++) {
      try {
        long id = Thread.currentThread().getId();
        System.out.println("[thread " + id + "] Runnning... " + i);
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        // ignore
      }
    }
  }
}
