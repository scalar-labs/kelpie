package print;

import com.moandjiezana.toml.Toml;
import com.scalar.kelpie.modules.PostProcessor;

public class PrintPost extends PostProcessor {

  @Override
  public boolean postProcess() {
    Toml toml = config.getToml().getTable("print_test");
    String title = toml.getString("title");
    Long num = 1L;
    if (toml.getLong("num") != null) {
      num = toml.getLong("num");
    }

    System.out.println("Checking for " + title);
    System.out.println("Run for " + num + " seconds");

    // always succeed
    return true;
  }
}
