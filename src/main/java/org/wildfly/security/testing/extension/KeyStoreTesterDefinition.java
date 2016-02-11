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

import org.jboss.as.controller.*;
import org.jboss.as.controller.capability.RuntimeCapability;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.wildfly.common.Assert;

import java.security.KeyStore;
import java.security.KeyStoreException;

/**
 * @author <a href="mailto:jkalina@redhat.com">Jan Kalina</a>
 */
public class KeyStoreTesterDefinition extends AbstractTesterDefinition {

    public static final KeyStoreTesterDefinition INSTANCE = new KeyStoreTesterDefinition();

    private KeyStoreTesterDefinition() {
        super("keystoretester");
    }

    static final RuntimeCapability<Void> KEY_STORE_RUNTIME_CAPABILITY =  RuntimeCapability
            .Builder.of("org.wildfly.security.keystore", true, KeyStore.class)
            .build();

    protected static final SimpleAttributeDefinition NAME =
            new SimpleAttributeDefinitionBuilder("name", ModelType.STRING, true)
                    .setCapabilityReference("org.wildfly.security.keystore", KEY_STORE_RUNTIME_CAPABILITY)
                    .build();

    protected static final SimpleAttributeDefinition ALIAS =
            new SimpleAttributeDefinitionBuilder("alias", ModelType.STRING, true)
                    .build();

    protected static final SimpleAttributeDefinition PASSWORD =
            new SimpleAttributeDefinitionBuilder("password", ModelType.STRING, true)
                    .build();

    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerReadWriteAttribute(NAME, null, BlankWriteAttributeHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(ALIAS, null, BlankWriteAttributeHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(PASSWORD, null, BlankWriteAttributeHandler.INSTANCE);
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);

        registerTest(resourceRegistration, "testExists", new TestingOperationHandler() {
            protected void test(String nodeName, ModelNode attributes, OperationContext context, ModelNode operation) throws Exception {
                if(!attributes.get(NAME.getName()).isDefined()) {
                    throw new OperationFailedException("Attribute is null!");
                }

                ServiceName serviceName = KeyStoreTesterDefinition.KEY_STORE_RUNTIME_CAPABILITY.getCapabilityServiceName(attributes.get(NAME.getName()).asString());
                ServiceController serviceController = context.getServiceRegistry(false).getService(serviceName);

                context.getResult().set(serviceController != null);
            }
        });

        registerTest(resourceRegistration, "testContains", new TestingOperationHandler() {
            protected void test(String nodeName, ModelNode attributes, OperationContext context, ModelNode operation) throws Exception {
                if(!attributes.get(NAME.getName()).isDefined() || !attributes.get(ALIAS.getName()).isDefined()) {
                    throw new OperationFailedException("Attribute is null!");
                }

                ServiceName serviceName = KeyStoreTesterDefinition.KEY_STORE_RUNTIME_CAPABILITY.getCapabilityServiceName(attributes.get(NAME.getName()).asString());
                KeyStore keyStore = (KeyStore) context.getServiceRegistry(false).getService(serviceName).getValue();

                context.getResult().set("keyStore", keyStore.toString());
                context.getResult().set("contains", keyStore.containsAlias(attributes.get(ALIAS.getName()).asString()));
            }
        });

        registerTest(resourceRegistration, "testGetKey", new TestingOperationHandler() {
            protected void test(String nodeName, ModelNode attributes, OperationContext context, ModelNode operation) throws Exception {
                if(!attributes.get(NAME.getName()).isDefined() || !attributes.get(ALIAS.getName()).isDefined() || !attributes.get(PASSWORD.getName()).isDefined()) {
                    throw new OperationFailedException("Attribute is null!");
                }

                ServiceName serviceName = KeyStoreTesterDefinition.KEY_STORE_RUNTIME_CAPABILITY.getCapabilityServiceName(attributes.get(NAME.getName()).asString());
                KeyStore keyStore = (KeyStore) context.getServiceRegistry(false).getService(serviceName).getValue();

                context.getResult().set("keyStore", keyStore.toString());
                context.getResult().set("contains", keyStore.containsAlias(attributes.get(ALIAS.getName()).asString()));
                context.getResult().set("key", keyStore.getKey(attributes.get(ALIAS.getName()).asString(), attributes.get(PASSWORD.getName()).asString().toCharArray()).toString());
            }
        });

        registerTest(resourceRegistration, "testGetCertificate", new TestingOperationHandler() {
            protected void test(String nodeName, ModelNode attributes, OperationContext context, ModelNode operation) throws Exception {
                if(!attributes.get(NAME.getName()).isDefined() || !attributes.get(ALIAS.getName()).isDefined()) {
                    throw new OperationFailedException("Attribute is null!");
                }

                ServiceName serviceName = KeyStoreTesterDefinition.KEY_STORE_RUNTIME_CAPABILITY.getCapabilityServiceName(attributes.get(NAME.getName()).asString());
                KeyStore keyStore = (KeyStore) context.getServiceRegistry(false).getService(serviceName).getValue();

                context.getResult().set("keyStore", keyStore.toString());
                context.getResult().set("contains", keyStore.containsAlias(attributes.get(ALIAS.getName()).asString()));
                context.getResult().set("certificate", keyStore.getCertificate(attributes.get(ALIAS.getName()).asString()).toString());
            }
        });

    }

}
