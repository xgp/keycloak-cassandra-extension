package de.arbeitsagentur.opdt.keycloak.cassandra.loginFailure;

import de.arbeitsagentur.opdt.keycloak.cassandra.loginFailure.persistence.LoginFailureRepository;
import de.arbeitsagentur.opdt.keycloak.cassandra.loginFailure.persistence.entities.LoginFailure;
import lombok.RequiredArgsConstructor;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserLoginFailureModel;
import org.keycloak.models.UserLoginFailureProvider;
import org.keycloak.models.utils.KeycloakModelUtils;

import java.util.function.Function;

import static org.keycloak.common.util.StackUtil.getShortStackTrace;

@JBossLog
@RequiredArgsConstructor
public class CassandraLoginFailureProvider implements UserLoginFailureProvider {
  private final KeycloakSession session;
  private final LoginFailureRepository loginFailureRepository;

  private Function<LoginFailure, UserLoginFailureModel> entityToAdapterFunc(RealmModel realm) {
    // Clone entity before returning back, to avoid giving away a reference to the live object to the caller
    return origEntity -> new CassandraLoginFailureAdapter(realm, origEntity, loginFailureRepository);
  }

  @Override
  public UserLoginFailureModel getUserLoginFailure(RealmModel realm, String userId) {
    return loginFailureRepository.findLoginFailuresByUserId(userId).stream()
        .filter(f -> f.getRealmId().equals(realm.getId()))
        .map(entityToAdapterFunc(realm))
        .findFirst()
        .orElse(null);
  }

  @Override
  public UserLoginFailureModel addUserLoginFailure(RealmModel realm, String userId) {
    log.tracef("addUserLoginFailure(%s, %s)%s", realm, userId, getShortStackTrace());

    LoginFailure userLoginFailureEntity = loginFailureRepository.findLoginFailuresByUserId(userId).stream()
        .filter(f -> f.getRealmId().equals(realm.getId()))
        .findFirst()
        .orElse(null);

    if (userLoginFailureEntity == null) {
      userLoginFailureEntity = LoginFailure.builder()
          .userId(userId)
          .realmId(realm.getId())
          .id(KeycloakModelUtils.generateId())
          .build();

      loginFailureRepository.insertOrUpdate(userLoginFailureEntity);
    }

    return entityToAdapterFunc(realm).apply(userLoginFailureEntity);
  }

  @Override
  public void removeUserLoginFailure(RealmModel realm, String userId) {
    log.tracef("removeUserLoginFailure(%s, %s)%s", realm, userId, getShortStackTrace());

    loginFailureRepository.deleteLoginFailureByUserId(userId);
  }

  @Override
  public void removeAllUserLoginFailures(RealmModel realm) {
    loginFailureRepository.findAllLoginFailures().forEach(loginFailureRepository::deleteLoginFailure);
  }

  @Override
  public void close() {

  }
}
