package print;

import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.modules.Injector;
import java.util.Random;

public class PrintInjector2 extends Injector {
  private Random random;

  public PrintInjector2(Config config) {
    super(config);
    this.random = new Random(System.currentTimeMillis());
  }

  @Override
  public void inject() {
    try {
      int waitTime = random.nextInt(5000);
      logInfo("Waitng for injection2... " + waitTime + " ms");
      Thread.sleep(waitTime);
    } catch (InterruptedException e) {
      // ignore
    }

    logInfo("Dummy injection2");
  }

  @Override
  public void eject() {
    try {
      int waitTime = random.nextInt(5000);
      logInfo("Waitng for ejection2... " + waitTime + " ms");
      Thread.sleep(waitTime);
    } catch (InterruptedException e) {
      // ignore
    }

    logInfo("Dummy ejection2");
  }

  @Override
  public void close() {}
}
