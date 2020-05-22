package verification.db.transfer;

import com.palantir.giraffe.command.Command;
import com.palantir.giraffe.command.CommandException;
import com.palantir.giraffe.command.Commands;
import com.palantir.giraffe.host.Host;
import com.palantir.giraffe.host.HostControlSystem;
import com.palantir.giraffe.ssh.PublicKeySshCredential;
import com.palantir.giraffe.ssh.SshCredential;
import com.palantir.giraffe.ssh.SshHostAccessor;
import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.exception.InjectionException;
import com.scalar.kelpie.modules.Injector;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CassandraKiller extends Injector {
  private final Random random = new Random(System.currentTimeMillis());
  private final Map<String, SshHostAccessor> accessors;
  private final int maxIntervalSec;
  private final String[] nodes;
  private List<String> targets;

  public CassandraKiller(Config config) {
    super(config);

    maxIntervalSec = (int) config.getUserLong("killer_config", "max_kill_interval_sec", 300L);
    String user = config.getUserString("killer_config", "ssh_user", "centos");
    int port = (int) config.getUserLong("killer_config", "ssh_port", 22L);
    String privateKeyFile = config.getUserString("killer_config", "ssh_private_key");
    nodes =
        config
            .getUserString("killer_config", "contact_points", Common.DEFAULT_CONTACT_POINT)
            .split(",");
    accessors = getAccessors(user, port, privateKeyFile, nodes);
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

  private void kill(String node) {
    logInfo("Killing cassandra on " + node);
    String killCommand = "pkill -9 -F /var/run/cassandra/cassandra.pid";
    execCommand(node, killCommand);
  }

  private void restart(String node) {
    logInfo("Restarting cassandra on " + node);
    String restartCommand = "/etc/init.d/cassandra start";
    execCommand(node, restartCommand);
  }

  private void execCommand(String node, String commandStr) {
    SshHostAccessor accessor = accessors.get(node);

    try (HostControlSystem hcs = accessor.open()) {
      Command.Builder builder = hcs.getExecutionSystem().getCommandBuilder("sudo");
      Arrays.stream(commandStr.split(" ")).forEach(arg -> builder.addArgument(arg));
      Commands.execute(builder.build());
    } catch (CommandException e) {
      logWarn("kill/restart command failed");
    } catch (IOException e) {
      throw new InjectionException("SSH connection failed", e);
    }
  }

  private Map<String, SshHostAccessor> getAccessors(
      String user, int port, String privateKeyFile, String[] nodes) {
    Map<String, SshHostAccessor> accessors = new HashMap<>();
    for (String node : nodes) {
      SshCredential credential;
      Path keyPath = new File(privateKeyFile).toPath();
      try {
        credential = PublicKeySshCredential.fromFile(user, keyPath);
      } catch (IOException e) {
        throw new InjectionException("Reading a private key failed from " + privateKeyFile, e);
      }

      accessors.put(node, SshHostAccessor.forCredential(Host.fromHostname(node), port, credential));
    }
    return accessors;
  }
}
