package print;

import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.modules.PreProcessor;
import javax.json.Json;

public class PrintPre extends PreProcessor {

  public PrintPre(Config config) {
    super(config);
  }

  @Override
  public void execute() {
    String title = config.getUserString("print_test", "title");

    logInfo("Prepare for " + title);
  }

  @Override
  public void close() {
    String title = config.getUserString("print_test", "title");
    setState(Json.createObjectBuilder().add("title", title).build());
  }
}
