package print;

import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.modules.PreProcessor;
import javax.json.Json;
import javax.json.JsonObject;

public class PrintPre extends PreProcessor {

  public PrintPre(Config config) {
    super(config);
  }

  @Override
  public void execute() {
    String title = config.getUserString("print_test", "title");

    System.out.println("Prepare for " + title);
  }

  @Override
  public JsonObject getState() {
    String title = config.getUserString("print_test", "title");
    return Json.createObjectBuilder().add("title", title).build();
  }
}
