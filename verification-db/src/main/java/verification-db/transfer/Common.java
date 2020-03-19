package verification_db.transfer;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.scalar.db.api.Consistency;
import com.scalar.db.api.DistributedStorage;
import com.scalar.db.api.DistributedTransactionManager;
import com.scalar.db.api.Get;
import com.scalar.db.api.Put;
import com.scalar.db.api.Result;
import com.scalar.db.config.DatabaseConfig;
import com.scalar.db.io.IntValue;
import com.scalar.db.io.Key;
import com.scalar.db.service.StorageModule;
import com.scalar.db.service.StorageService;
import com.scalar.db.service.TransactionModule;
import com.scalar.db.service.TransactionService;
import com.scalar.db.transaction.consensuscommit.TransactionResult;
import com.scalar.kelpie.config.Config;
import java.util.List;
import java.util.Properties;

class Common {
  private static final String KEYSPACE = "transfer";
  private static final String TABLE = "tx_transfer";
  private static final String ACCOUNT_ID = "account_id";
  private static final String ACCOUNT_TYPE = "account_type";
  private static final String BALANCE = "balance";
  private static final long SLEEP_BASE_MILLIS = 100;

  static final String DEFAULT_CONTACT_POINT = "localhost";
  static final int INITIAL_BALANCE = 10000;
  static final long NUM_TYPES = 2;

  static DistributedStorage getStorage(Config config) {
    DatabaseConfig dbConfig = getDatabaseConfig(config);
    Injector injector = Guice.createInjector(new StorageModule(dbConfig));

    return injector.getInstance(StorageService.class);
  }

  static DistributedTransactionManager getTransactionManager(Config config) {
    DatabaseConfig dbConfig = getDatabaseConfig(config);
    Injector injector = Guice.createInjector(new TransactionModule(dbConfig));
    DistributedTransactionManager manager = injector.getInstance(TransactionService.class);
    manager.with(KEYSPACE, TABLE);

    return manager;
  }

  private static DatabaseConfig getDatabaseConfig(Config config) {
    Properties props = new Properties();
    String contactPoints =
        config.getUserString("test_config", "contact_points", DEFAULT_CONTACT_POINT);
    props.setProperty("scalar.db.contact_points", contactPoints);
    props.setProperty("scalar.db.username", "cassandra");
    props.setProperty("scalar.db.password", "cassandra");

    return new DatabaseConfig(props);
  }

  static Get prepareGet(int id, int type) {
    Key partitionKey = new Key(new IntValue(ACCOUNT_ID, id));
    Key clusteringKey = new Key(new IntValue(ACCOUNT_TYPE, type));

    return new Get(partitionKey, clusteringKey).withConsistency(Consistency.LINEARIZABLE);
  }

  static Put preparePut(int id, int type, int amount) {
    Key partitionKey = new Key(new IntValue(ACCOUNT_ID, id));
    Key clusteringKey = new Key(new IntValue(ACCOUNT_TYPE, type));
    return new Put(partitionKey, clusteringKey)
        .withConsistency(Consistency.LINEARIZABLE)
        .withValue(new IntValue(BALANCE, amount));
  }

  static int getBalanceFromResult(Result result) {
    return ((IntValue) result.getValue(BALANCE).get()).get();
  }

  static int getTotalInitialBalance(Config config) {
    int numAccounts = (int) config.getUserLong("test_config", "num_accounts");
    int numTypes = (int) config.getUserLong("test_config", "num_account_types", NUM_TYPES);
    return INITIAL_BALANCE * numTypes * numAccounts;
  }

  static int getActualTotalVersion(List<Result> results) {
    return results.stream().mapToInt(r -> ((TransactionResult) r).getVersion() - 1).sum();
  }

  static int getActualTotalBalance(List<Result> results) {
    return results.stream().mapToInt(r -> ((IntValue) r.getValue(BALANCE).get()).get()).sum();
  }

  static void exponentialBackoff(int counter) {
    try {
      Thread.sleep((long) Math.pow(2, counter) * SLEEP_BASE_MILLIS);
    } catch (InterruptedException e) {
      // ignore
    }
  }
}
