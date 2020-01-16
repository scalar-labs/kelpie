package print;

import com.moandjiezana.toml.Toml;
import com.scalar.kelpie.modules.Processor;

public class PrintProcessor extends Processor {

  @Override
  public void process() {
    Toml toml = config.getToml().getTable("print_test");
    Long num = 1L;
    if (toml.getLong("num") != null) {
      num = toml.getLong("num");
    }

    for (int i = 0; i < num; i++) {
      try {
        System.out.println("Runnning... " + i);
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        // ignore
      }
    }
  }
}
