package verification_db.transfer;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.modules.Injector;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CassandraKiller extends Injector {
  private final Random random = new Random(System.currentTimeMillis());
  private final JSch jsch = new JSch();
  private final int maxIntervalSec;
  private final String user;
  private final int port;
  private final String[] nodes;
  private List<String> targets;

  public CassandraKiller(Config config) {
    super(config);

    maxIntervalSec = (int) config.getUserLong("killer_config", "max_kill_interval_sec", 300L);
    user = config.getUserString("killer_config", "cassandra_user", "centos");
    port = (int) config.getUserLong("killer_config", "ssh_port", 22L);
    nodes =
        config
            .getUserString("test_config", "contact_points", Common.DEFAULT_CONTACT_POINT)
            .split(",");
  }

  @Override
  public void inject() {
    try {
      int waitTime = random.nextInt(maxIntervalSec * 1000);
      Thread.sleep(waitTime);
    } catch (InterruptedException e) {
      // ignore
    }

    List<String> tmp = Arrays.asList(nodes);
    Collections.shuffle(tmp);
    targets = tmp.subList(0, random.nextInt(nodes.length));

    targets.forEach(
        node -> {
          kill(node);
        });
  }

  @Override
  public void eject() {
    try {
      int waitTime = random.nextInt(maxIntervalSec * 1000);
      Thread.sleep(waitTime);
    } catch (InterruptedException e) {
      // ignore
    }

    targets.forEach(
        node -> {
          restart(node);
        });
  }

  @Override
  public void close() {}

  private void kill(String address) {
    System.out.println("[killer] Killing cassandra on " + address);
    String killCommand = "sudo pkill -9 -F /var/run/cassandra/cassandra.pid";
    execCommand(address, killCommand);
  }

  private void restart(String address) {
    System.out.println("[killer] Restarting cassandra on " + address);
    String restartCommand = "sudo /etc/init.d/cassandra start";
    execCommand(address, restartCommand);
  }

  private void execCommand(String address, String command) {
    Session session = null;
    ChannelExec channel = null;

    try {
      session = jsch.getSession(user, address, port);
      session.setConfig("StrictHostKeyChecking", "no");
      session.connect();

      channel = (ChannelExec) session.openChannel("exec");
      channel.setCommand(command);
      channel.connect();

      // TODO: error message
    } catch (Exception e) {
      // TODO: error handling
    } finally {
      if (channel != null) {
        channel.disconnect();
      }
      if (session != null) {
        session.disconnect();
      }
    }
  }
}
