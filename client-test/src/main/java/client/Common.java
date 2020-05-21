package client;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.scalar.dl.client.config.ClientConfig;
import com.scalar.dl.client.service.ClientModule;
import com.scalar.dl.client.service.ClientService;
import com.scalar.kelpie.config.Config;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.time.Duration;
import java.util.Properties;

public class Common {
  private static String HOST = "localhost";
  private static String PORT = "50051";
  private static ClientConfig config;
  private static final String CERT_HOLDER_ID = "test_holder";
  private static final int MAX_RETRIES = 10;
  private static final Duration WAIT_DURATION = Duration.ofMillis(1000);
  private static final long SLEEP_BASE_MILLIS = 100L;

  public static final int INITIAL_BALANCE = 10000;

  public static ClientConfig getClientConfig(Config config) {
    String host = config.getUserString("client_config", "dl_server", HOST);
    String port = config.getUserString("client_config", "dl_server_port", PORT);
    String certificate = config.getUserString("client_config", "certificate");
    String privateKey = config.getUserString("client_config", "private_key");

    Properties properties = new Properties();
    properties.setProperty(ClientConfig.SERVER_HOST, host);
    properties.setProperty(ClientConfig.SERVER_PORT, port);
    properties.setProperty(ClientConfig.CERT_HOLDER_ID, CERT_HOLDER_ID);
    properties.setProperty(ClientConfig.CERT_PATH, certificate);
    properties.setProperty(ClientConfig.PRIVATE_KEY_PATH, privateKey);

    return new ClientConfig(properties);
  }

  public static ClientService getClientService(Config config) {
    ClientConfig clientConfig = getClientConfig(config);
    Injector injector = Guice.createInjector(new ClientModule(clientConfig));

    return injector.getInstance(ClientService.class);
  }

  public static int getTotalInitialBalance(Config config) {
    int numAccounts = (int) config.getUserLong("test_config", "num_accounts");

    return INITIAL_BALANCE * numAccounts;
  }

  public static Retry getRetryWithFixedWaitDuration(String name) {
    RetryConfig retryConfig =
        RetryConfig.custom().maxAttempts(MAX_RETRIES).waitDuration(WAIT_DURATION).build();

    return Retry.of(name, retryConfig);
  }

  public static Retry getRetryWithExponentialBackoff(String name) {
    IntervalFunction intervalFunc = IntervalFunction.ofExponentialBackoff(SLEEP_BASE_MILLIS, 2.0);

    RetryConfig retryConfig =
        RetryConfig.custom().maxAttempts(MAX_RETRIES).intervalFunction(intervalFunc).build();

    return Retry.of(name, retryConfig);
  }
}
