package de.arbeitsagentur.opdt.keycloak.cassandra.connection;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.internal.core.type.codec.extras.enums.EnumNameCodec;
import com.datastax.oss.driver.internal.core.type.codec.extras.json.JsonCodec;
import com.google.auto.service.AutoService;
import com.google.common.base.Strings;
import de.arbeitsagentur.opdt.keycloak.cassandra.CassandraJsonSerialization;
import de.arbeitsagentur.opdt.keycloak.cassandra.clientScope.persistence.entities.ClientScopeValue;
import de.arbeitsagentur.opdt.keycloak.cassandra.group.persistence.entities.GroupValue;
import de.arbeitsagentur.opdt.keycloak.cassandra.role.persistence.entities.RoleValue;
import de.arbeitsagentur.opdt.keycloak.cassandra.user.persistence.entities.CredentialValue;
import de.arbeitsagentur.opdt.keycloak.cassandra.userSession.persistence.entities.AuthenticatedClientSessionValue;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
import org.keycloak.models.UserSessionModel;
import org.keycloak.sessions.CommonClientSessionModel;

@JBossLog
@AutoService(CassandraConnectionProviderFactory.class)
public class DatastaxConnectionProviderFactory extends DefaultCassandraConnectionProviderFactory {
  public static final String PROVIDER_ID = "datastax";

  private String getRequiredParam(Config.Scope scope, String key) {
    String value = scope.get(key);
    if (value == null || Strings.isNullOrEmpty(value)) {
      throw new IllegalStateException(String.format("%s config value must be set", key));
    }
    return value;
  }

  @Override
  public void init(Config.Scope scope) {
    CqlSessionBuilder builder = CqlSession.builder();

    String keyspace = getRequiredParam(scope, "keyspace");
    int replicationFactor = Integer.parseInt(getRequiredParam(scope, "replicationFactor"));
    String token = getRequiredParam(scope, "token");
    String configFile = getRequiredParam(scope, "configFile");
    Path configPath = Paths.get(configFile);
    if (Files.exists(configPath)) {
      log.infof("Attempting to configure Cassandra with file at %s", configPath);
      builder = builder.withConfigLoader(DriverConfigLoader.fromPath(configPath));
    } else {
      throw new IllegalStateException(String.format("Config file %s does not exist.", configFile));
    }

    builder = builder.withAuthCredentials("token", token);

    if (scope.getBoolean("createKeyspace", true)) {
      log.info("Create keyspace (if not exists)...");
      createDbIfNotExists(builder, keyspace, replicationFactor);
    } else {
      log.info("Skipping create keyspace, assuming keyspace and tables already exist...");
    }

    cqlSession =
        builder
            .withKeyspace(keyspace)
            .addTypeCodecs(new EnumNameCodec<>(UserSessionModel.State.class))
            .addTypeCodecs(new EnumNameCodec<>(UserSessionModel.SessionPersistenceState.class))
            .addTypeCodecs(new EnumNameCodec<>(CommonClientSessionModel.ExecutionStatus.class))
            .addTypeCodecs(new JsonCodec<>(RoleValue.class, CassandraJsonSerialization.getMapper()))
            .addTypeCodecs(
                new JsonCodec<>(GroupValue.class, CassandraJsonSerialization.getMapper()))
            .addTypeCodecs(
                new JsonCodec<>(CredentialValue.class, CassandraJsonSerialization.getMapper()))
            .addTypeCodecs(
                new JsonCodec<>(
                    AuthenticatedClientSessionValue.class, CassandraJsonSerialization.getMapper()))
            .addTypeCodecs(
                new JsonCodec<>(ClientScopeValue.class, CassandraJsonSerialization.getMapper()))
            .build();

    repository = createRepository(cqlSession);
  }

  protected void createDbIfNotExists(
      CqlSessionBuilder builder, String keyspace, int replicationFactor) {
    try (CqlSession createKeyspaceSession = builder.build()) {
      createKeyspaceIfNotExists(createKeyspaceSession, keyspace, replicationFactor);
    }
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }
}
