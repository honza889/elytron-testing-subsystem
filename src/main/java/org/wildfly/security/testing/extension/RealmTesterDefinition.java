/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
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

package org.wildfly.security.testing.extension;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.capability.RuntimeCapability;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.wildfly.security.auth.server.*;
import org.wildfly.security.evidence.PasswordGuessEvidence;

import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * @author <a href="mailto:jkalina@redhat.com">Jan Kalina</a>
 */
public class RealmTesterDefinition extends AbstractTesterDefinition {

    public static final RealmTesterDefinition INSTANCE = new RealmTesterDefinition();

    private RealmTesterDefinition() {
        super("realmtester");
    }

    static final RuntimeCapability<Void> SECURITY_REALM_RUNTIME_CAPABILITY = RuntimeCapability
            .Builder.of("org.wildfly.security.security-realm", true, SecurityRealm.class)
            .build();

    protected static final SimpleAttributeDefinition NAME =
            new SimpleAttributeDefinitionBuilder("name", ModelType.STRING, true)
                    .setCapabilityReference("org.wildfly.security.security-realm", SECURITY_REALM_RUNTIME_CAPABILITY)
                    .build();

    protected static final SimpleAttributeDefinition IDENTITY =
            new SimpleAttributeDefinitionBuilder("identity", ModelType.STRING, true)
                    .build();

    protected static final SimpleAttributeDefinition PASSWORD =
            new SimpleAttributeDefinitionBuilder("password", ModelType.STRING, true)
                    .build();

    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerReadWriteAttribute(NAME, null, BlankWriteAttributeHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(IDENTITY, null, BlankWriteAttributeHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(PASSWORD, null, BlankWriteAttributeHandler.INSTANCE);
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);

        registerTest(resourceRegistration, "serviceExists", new TestingOperationHandler() {
            protected void test(String nodeName, ModelNode attributes, OperationContext context, ModelNode operation) throws Exception {
                if(!attributes.get(NAME.getName()).isDefined()) {
                    throw new OperationFailedException("Attribute is not defined");
                }

                ServiceName serviceName = RealmTesterDefinition.SECURITY_REALM_RUNTIME_CAPABILITY.getCapabilityServiceName(attributes.get(NAME.getName()).asString());
                ServiceController serviceController = context.getServiceRegistry(false).getService(serviceName);

                context.getResult().set(serviceController != null);
            }
        });

        registerTest(resourceRegistration, "iterateIdentities", new TestingOperationHandler() {
            protected void test(String nodeName, ModelNode attributes, OperationContext context, ModelNode operation) throws Exception {
                if(!attributes.get(NAME.getName()).isDefined()) {
                    throw new OperationFailedException("Attribute is not defined");
                }

                ServiceName serviceName = RealmTesterDefinition.SECURITY_REALM_RUNTIME_CAPABILITY.getCapabilityServiceName(attributes.get(NAME.getName()).asString());
                ModifiableSecurityRealm securityRealm = (ModifiableSecurityRealm) context.getServiceRegistry(false).getService(serviceName).getValue();

                String out = "";
                Iterator<ModifiableRealmIdentity> i = securityRealm.getRealmIdentityIterator();
                while (i.hasNext()) {
                    ModifiableRealmIdentity identity = i.next();
                    out += identity.toString() + ",";
                }
                context.getResult().set(out);
            }
        });

        registerTest(resourceRegistration, "testLogin", new TestingOperationHandler() {
            protected void test(String nodeName, ModelNode attributes, OperationContext context, ModelNode operation) throws Exception {
                if(!attributes.get(NAME.getName()).isDefined()) {
                    throw new OperationFailedException("Attribute is not defined");
                }

                ServiceName serviceName = RealmTesterDefinition.SECURITY_REALM_RUNTIME_CAPABILITY.getCapabilityServiceName(attributes.get(NAME.getName()).asString());
                ModifiableSecurityRealm securityRealm = (ModifiableSecurityRealm) context.getServiceRegistry(false).getService(serviceName).getValue();

                RealmIdentity identity = securityRealm.getRealmIdentity(attributes.get(IDENTITY.getName()).asString(), null, null);
                boolean verified = false;
                if (identity != null) {
                    verified = identity.verifyEvidence(new PasswordGuessEvidence(attributes.get(PASSWORD.getName()).asString().toCharArray()));
                }

                context.getResult().get("identity").set(identity.toString());
                context.getResult().get("verified").set(verified);
            }
        });

    }

}
