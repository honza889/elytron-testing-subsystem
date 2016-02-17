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
import org.wildfly.security.auth.server.SecurityDomain;

/**
 * @author <a href="mailto:jkalina@redhat.com">Jan Kalina</a>
 */
public class DomainTesterDefinition extends AbstractTesterDefinition {

    public static final DomainTesterDefinition INSTANCE = new DomainTesterDefinition();

    private DomainTesterDefinition() {
        super("domaintester");
    }

    static final RuntimeCapability<Void> SECURITY_DOMAIN_RUNTIME_CAPABILITY = RuntimeCapability
            .Builder.of("org.wildfly.security.security-domain", true, SecurityDomain.class)
            .build();

    protected static final SimpleAttributeDefinition NAME =
            new SimpleAttributeDefinitionBuilder("name", ModelType.STRING, true)
                    .setCapabilityReference("org.wildfly.security.security-domain", SECURITY_DOMAIN_RUNTIME_CAPABILITY)
                    .build();

    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerReadWriteAttribute(NAME, null, BlankWriteAttributeHandler.INSTANCE);
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);

        registerTest(resourceRegistration, "serviceExists", new TestingOperationHandler() {
            protected void test(String nodeName, ModelNode attributes, OperationContext context, ModelNode operation) throws Exception {
                if(!attributes.get(NAME.getName()).isDefined()) {
                    throw new OperationFailedException("Attribute name is not defined");
                }

                ServiceName serviceName = DomainTesterDefinition.SECURITY_DOMAIN_RUNTIME_CAPABILITY.getCapabilityServiceName(attributes.get(NAME.getName()).asString());
                ServiceController serviceController = context.getServiceRegistry(false).getService(serviceName);

                context.getResult().set(serviceController != null);
            }
        });

    }

}
