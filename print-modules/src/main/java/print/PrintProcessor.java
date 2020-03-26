package print;

import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.modules.Processor;
import java.util.concurrent.atomic.AtomicInteger;
import javax.json.Json;

public class PrintProcessor extends Processor {
  private final AtomicInteger total = new AtomicInteger(0);

  public PrintProcessor(Config config) {
    super(config);
  }

  @Override
  public void execute() {
    String preparationTitle = getPreviousState().getString("title");
    String title = config.getUserString("print_test", "title");
    if (!preparationTitle.equals(title)) {
      throw new RuntimeException("inconsistent state");
    }

    long num = config.getUserLong("print_test", "num");

    for (long i = 0; i < num; i++) {
      try {
        long id = Thread.currentThread().getId();
        logInfo("Running... " + i);
        Thread.sleep(1000);
        total.incrementAndGet();
      } catch (InterruptedException e) {
        // ignore
      }
    }
  }

  @Override
  public void close() {
    setState(Json.createObjectBuilder().add("total", total.get()).build());
  }
}
