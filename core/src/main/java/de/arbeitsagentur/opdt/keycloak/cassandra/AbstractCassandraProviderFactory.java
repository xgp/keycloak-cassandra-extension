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

import com.datastax.oss.driver.api.core.CqlSession;
import de.arbeitsagentur.opdt.keycloak.cassandra.authSession.persistence.AuthSessionMapper;
import de.arbeitsagentur.opdt.keycloak.cassandra.authSession.persistence.AuthSessionMapperBuilder;
import de.arbeitsagentur.opdt.keycloak.cassandra.authSession.persistence.CassandraAuthSessionRepository;
import de.arbeitsagentur.opdt.keycloak.cassandra.cache.L1CacheInterceptor;
import de.arbeitsagentur.opdt.keycloak.cassandra.connection.CassandraConnectionProvider;
import de.arbeitsagentur.opdt.keycloak.cassandra.loginFailure.persistence.CassandraLoginFailureRepository;
import de.arbeitsagentur.opdt.keycloak.cassandra.loginFailure.persistence.LoginFailureMapper;
import de.arbeitsagentur.opdt.keycloak.cassandra.loginFailure.persistence.LoginFailureMapperBuilder;
import de.arbeitsagentur.opdt.keycloak.cassandra.singleUseObject.persistence.CassandraSingleUseObjectRepository;
import de.arbeitsagentur.opdt.keycloak.cassandra.singleUseObject.persistence.SingleUseObjectMapper;
import de.arbeitsagentur.opdt.keycloak.cassandra.singleUseObject.persistence.SingleUseObjectMapperBuilder;
import de.arbeitsagentur.opdt.keycloak.cassandra.userSession.persistence.CassandraUserSessionRepository;
import de.arbeitsagentur.opdt.keycloak.cassandra.userSession.persistence.UserSessionMapper;
import de.arbeitsagentur.opdt.keycloak.cassandra.userSession.persistence.UserSessionMapperBuilder;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.KeycloakSession;

import java.lang.reflect.Proxy;

@JBossLog
public abstract class AbstractCassandraProviderFactory {

    CassandraUserSessionRepository userSessionRepository;
    CassandraAuthSessionRepository authSessionRepository;
    CassandraLoginFailureRepository loginFailureRepository;
    CassandraSingleUseObjectRepository singleUseObjectRepository;

    protected CompositeRepository createRepository(KeycloakSession session) {
        CassandraConnectionProvider connectionProvider = session.getProvider(CassandraConnectionProvider.class);
        CqlSession cqlSession = connectionProvider.getCqlSession();

        if (userSessionRepository == null) {
            UserSessionMapper userSessionMapper = new UserSessionMapperBuilder(cqlSession).withSchemaValidationEnabled(false).build();
            userSessionRepository = new CassandraUserSessionRepository(userSessionMapper.userSessionDao());
        }

        if (authSessionRepository == null) {
            AuthSessionMapper authSessionMapper = new AuthSessionMapperBuilder(cqlSession).withSchemaValidationEnabled(false).build();
            authSessionRepository = new CassandraAuthSessionRepository(authSessionMapper.authSessionDao());
        }

        if (loginFailureRepository == null) {
            LoginFailureMapper loginFailureMapper = new LoginFailureMapperBuilder(cqlSession).withSchemaValidationEnabled(false).build();
            loginFailureRepository = new CassandraLoginFailureRepository(loginFailureMapper.loginFailureDao());
        }

        if (singleUseObjectRepository == null) {
            SingleUseObjectMapper singleUseObjectMapper = new SingleUseObjectMapperBuilder(cqlSession).withSchemaValidationEnabled(false).build();
            singleUseObjectRepository = new CassandraSingleUseObjectRepository(singleUseObjectMapper.singleUseObjectDao());
        }

        ManagedCompositeCassandraRepository cassandraRepository = new ManagedCompositeCassandraRepository();
        cassandraRepository.setUserSessionRepository(userSessionRepository);
        cassandraRepository.setAuthSessionRepository(authSessionRepository);
        cassandraRepository.setLoginFailureRepository(loginFailureRepository);
        cassandraRepository.setSingleUseObjectRepository(singleUseObjectRepository);

        L1CacheInterceptor intercepted = new L1CacheInterceptor(session, cassandraRepository);
        return (CompositeRepository) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{CompositeRepository.class}, intercepted);
    }
}
