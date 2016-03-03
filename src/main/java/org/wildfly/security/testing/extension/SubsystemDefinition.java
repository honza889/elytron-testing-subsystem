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
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="mailto:jkalina@redhat.com">Jan Kalina</a>
 */
public class SubsystemDefinition extends SimpleResourceDefinition {

    public static final SubsystemDefinition INSTANCE = new SubsystemDefinition();

    private SubsystemDefinition() {
        super(SubsystemExtension.SUBSYSTEM_PATH,
                SubsystemExtension.getResourceDescriptionResolver(null),
                SubsystemAdd.INSTANCE,
                SubsystemRemove.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerSubModel(KeyStoreTesterDefinition.INSTANCE);
        resourceRegistration.registerSubModel(DomainTesterDefinition.INSTANCE);
        resourceRegistration.registerSubModel(RealmTesterDefinition.INSTANCE);
    }

    static class SubsystemAdd extends AbstractBoottimeAddStepHandler {

        static final SubsystemAdd INSTANCE = new SubsystemAdd();

        private SubsystemAdd() {
        }

        @Override
        protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
            //model.get("type").setEmptyObject(); //Initialize the 'type' child node
            //OUTPUT.validateAndSet(operation, model);
        }
    }

    static class SubsystemRemove extends AbstractRemoveStepHandler {

        static final SubsystemRemove INSTANCE = new SubsystemRemove();

        private SubsystemRemove() {
        }

        @Override
        protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {

        }
    }
}
