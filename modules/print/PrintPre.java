package print;

import com.moandjiezana.toml.Toml;
import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.modules.PreProcessor;

public class PrintPre extends PreProcessor {
  private String title;
  private Long num = 1L;

  public PrintPre(Config config) {
    super(config);
  }

  @Override
  public void execute() {
    Toml toml = config.getToml().getTable("print_test");
    title = toml.getString("title");
    if (toml.getLong("num") != null) {
      num = toml.getLong("num");
    }

    System.out.println("Prepare for " + title);
  }
}
