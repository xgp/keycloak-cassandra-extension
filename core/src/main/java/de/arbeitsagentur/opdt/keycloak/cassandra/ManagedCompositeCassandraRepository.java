/*
 * Copyright 2022 IT-Systemhaus der Bundesagentur fuer Arbeit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.arbeitsagentur.opdt.keycloak.cassandra;

import de.arbeitsagentur.opdt.keycloak.cassandra.authSession.persistence.AuthSessionRepository;
import de.arbeitsagentur.opdt.keycloak.cassandra.authSession.persistence.entities.AuthenticationSession;
import de.arbeitsagentur.opdt.keycloak.cassandra.authSession.persistence.entities.RootAuthenticationSession;
import de.arbeitsagentur.opdt.keycloak.cassandra.cache.InvalidateCache;
import de.arbeitsagentur.opdt.keycloak.cassandra.cache.L1Cached;
import de.arbeitsagentur.opdt.keycloak.cassandra.loginFailure.persistence.LoginFailureRepository;
import de.arbeitsagentur.opdt.keycloak.cassandra.loginFailure.persistence.entities.LoginFailure;
import de.arbeitsagentur.opdt.keycloak.cassandra.singleUseObject.persistence.SingleUseObjectRepository;
import de.arbeitsagentur.opdt.keycloak.cassandra.singleUseObject.persistence.entities.SingleUseObject;
import de.arbeitsagentur.opdt.keycloak.cassandra.userSession.persistence.UserSessionRepository;
import de.arbeitsagentur.opdt.keycloak.cassandra.userSession.persistence.entities.AuthenticatedClientSessionValue;
import de.arbeitsagentur.opdt.keycloak.cassandra.userSession.persistence.entities.UserSession;
import de.arbeitsagentur.opdt.keycloak.cassandra.userSession.persistence.entities.UserSessionToAttributeMapping;
import lombok.Setter;
import org.keycloak.common.util.MultivaluedHashMap;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static de.arbeitsagentur.opdt.keycloak.cassandra.cache.KeycloakSessionCache.*;

@Setter
public class ManagedCompositeCassandraRepository implements CompositeRepository {
    private UserSessionRepository userSessionRepository;

    private AuthSessionRepository authSessionRepository;

    private LoginFailureRepository loginFailureRepository;

    private SingleUseObjectRepository singleUseObjectRepository;


    @L1Cached(cacheName = USER_SESSION_CACHE)
    @InvalidateCache
    public void insert(UserSession session) {
        this.userSessionRepository.insert(session);
    }

    @L1Cached(cacheName = USER_SESSION_CACHE)
    @InvalidateCache
    public void insert(UserSession session, String correspondingSessionId) {
        this.userSessionRepository.insert(session, correspondingSessionId);
    }

    @L1Cached(cacheName = USER_SESSION_CACHE)
    @InvalidateCache
    public void update(UserSession session) {
        this.userSessionRepository.update(session);
    }

    @L1Cached(cacheName = USER_SESSION_CACHE)
    @InvalidateCache
    public void update(UserSession session, String correspondingSessionId) {
        this.userSessionRepository.update(session, correspondingSessionId);
    }

    @L1Cached(cacheName = USER_SESSION_CACHE)
    @InvalidateCache
    public void addClientSession(UserSession session, AuthenticatedClientSessionValue clientSession) {
        this.userSessionRepository.addClientSession(session, clientSession);
    }

    @L1Cached(cacheName = USER_SESSION_CACHE)
    public UserSession findUserSessionById(String id) {
        return this.userSessionRepository.findUserSessionById(id);
    }

    @L1Cached(cacheName = USER_SESSION_CACHE)
    public List<UserSession> findAll() {
        return this.userSessionRepository.findAll();
    }

    @L1Cached(cacheName = USER_SESSION_CACHE)
    public List<UserSession> findUserSessionsByBrokerSession(String brokerSessionId) {
        return this.userSessionRepository.findUserSessionsByBrokerSession(brokerSessionId);
    }

    @L1Cached(cacheName = USER_SESSION_CACHE)
    public List<UserSession> findUserSessionsByUserId(String userId) {
        return this.userSessionRepository.findUserSessionsByUserId(userId);
    }

    @L1Cached(cacheName = USER_SESSION_CACHE)
    public List<UserSession> findUserSessionsByClientId(String clientId) {
        return this.userSessionRepository.findUserSessionsByClientId(clientId);
    }

    @L1Cached(cacheName = USER_SESSION_CACHE)
    public List<UserSession> findUserSessionsByBrokerUserId(String brokerUserId) {
        return this.userSessionRepository.findUserSessionsByBrokerUserId(brokerUserId);
    }

    @L1Cached(cacheName = USER_SESSION_CACHE)
    @InvalidateCache
    public void deleteUserSession(UserSession session) {
        this.userSessionRepository.deleteUserSession(session);
    }

    @L1Cached(cacheName = USER_SESSION_CACHE)
    @InvalidateCache
    public void deleteUserSession(String id) {
        this.userSessionRepository.deleteUserSession(id);
    }

    @L1Cached(cacheName = USER_SESSION_CACHE)
    @InvalidateCache
    public void deleteCorrespondingUserSession(UserSession session) {
        this.userSessionRepository.deleteCorrespondingUserSession(session);
    }

    @L1Cached(cacheName = USER_SESSION_CACHE)
    public Set<String> findUserSessionIdsByAttribute(String name, String value, int firstResult, int maxResult) {
        return this.userSessionRepository.findUserSessionIdsByAttribute(name, value, firstResult, maxResult);
    }

    @L1Cached(cacheName = USER_SESSION_CACHE)
    public List<UserSession> findUserSessionsByAttribute(String name, String value) {
        return this.userSessionRepository.findUserSessionsByAttribute(name, value);
    }

    @L1Cached(cacheName = USER_SESSION_CACHE)
    public UserSession findUserSessionByAttribute(String name, String value) {
        return this.userSessionRepository.findUserSessionByAttribute(name, value);
    }

    @L1Cached(cacheName = USER_SESSION_CACHE)
    public MultivaluedHashMap<String, String> findAllUserSessionAttributes(String userSessionId) {
        return this.userSessionRepository.findAllUserSessionAttributes(userSessionId);
    }

    @L1Cached(cacheName = USER_SESSION_CACHE)
    public UserSessionToAttributeMapping findUserSessionAttribute(String userSessionId, String attributeName) {
        return this.userSessionRepository.findUserSessionAttribute(userSessionId, attributeName);
    }

    @L1Cached(cacheName = AUTH_SESSION_CACHE)
    @InvalidateCache
    public void insertOrUpdate(RootAuthenticationSession session) {
        this.authSessionRepository.insertOrUpdate(session);
    }

    @L1Cached(cacheName = AUTH_SESSION_CACHE)
    @InvalidateCache
    public void insertOrUpdate(AuthenticationSession session, RootAuthenticationSession parent) {
        this.authSessionRepository.insertOrUpdate(session, parent);
    }

    @L1Cached(cacheName = AUTH_SESSION_CACHE)
    @InvalidateCache
    public void deleteRootAuthSession(String sessionId) {
        this.authSessionRepository.deleteRootAuthSession(sessionId);
    }

    @L1Cached(cacheName = AUTH_SESSION_CACHE)
    @InvalidateCache
    public void deleteRootAuthSession(RootAuthenticationSession session) {
        this.authSessionRepository.deleteRootAuthSession(session);
    }

    @L1Cached(cacheName = AUTH_SESSION_CACHE)
    @InvalidateCache
    public void deleteAuthSession(AuthenticationSession session) {
        this.authSessionRepository.deleteAuthSession(session);
    }

    @L1Cached(cacheName = AUTH_SESSION_CACHE)
    @InvalidateCache
    public void deleteAuthSessions(String parentSessionId) {
        this.authSessionRepository.deleteAuthSessions(parentSessionId);
    }

    @L1Cached(cacheName = AUTH_SESSION_CACHE)
    public List<AuthenticationSession> findAuthSessionsByParentSessionId(String parentSessionId) {
        return this.authSessionRepository.findAuthSessionsByParentSessionId(parentSessionId);
    }

    @L1Cached(cacheName = AUTH_SESSION_CACHE)
    public RootAuthenticationSession findRootAuthSessionById(String id) {
        return this.authSessionRepository.findRootAuthSessionById(id);
    }

    @L1Cached(cacheName = LOGIN_FAILURE_CACHE)
    @InvalidateCache
    public void insertOrUpdate(LoginFailure loginFailure) {
        this.loginFailureRepository.insertOrUpdate(loginFailure);
    }

    @L1Cached(cacheName = LOGIN_FAILURE_CACHE)
    public List<LoginFailure> findLoginFailuresByUserId(String userId) {
        return this.loginFailureRepository.findLoginFailuresByUserId(userId);
    }

    @L1Cached(cacheName = LOGIN_FAILURE_CACHE)
    @InvalidateCache
    public void deleteLoginFailure(LoginFailure loginFailure) {
        this.loginFailureRepository.deleteLoginFailure(loginFailure);
    }

    @L1Cached(cacheName = LOGIN_FAILURE_CACHE)
    @InvalidateCache
    public void deleteLoginFailureByUserId(String userId) {
        this.loginFailureRepository.deleteLoginFailureByUserId(userId);
    }

    @L1Cached(cacheName = LOGIN_FAILURE_CACHE)
    public List<LoginFailure> findAllLoginFailures() {
        return this.loginFailureRepository.findAllLoginFailures();
    }

    @L1Cached(cacheName = SUO_CACHE)
    public SingleUseObject findSingleUseObjectByKey(String key) {
        return this.singleUseObjectRepository.findSingleUseObjectByKey(key);
    }

    @L1Cached(cacheName = SUO_CACHE)
    @InvalidateCache
    public void insertOrUpdate(SingleUseObject singleUseObject, int ttl) {
        this.singleUseObjectRepository.insertOrUpdate(singleUseObject, ttl);
    }

    @L1Cached(cacheName = SUO_CACHE)
    @InvalidateCache
    public void insertOrUpdate(SingleUseObject singleUseObject) {
        this.singleUseObjectRepository.insertOrUpdate(singleUseObject);
    }

    @L1Cached(cacheName = SUO_CACHE)
    @InvalidateCache
    public boolean deleteSingleUseObjectByKey(String key) {
        return this.singleUseObjectRepository.deleteSingleUseObjectByKey(key);
    }

}
