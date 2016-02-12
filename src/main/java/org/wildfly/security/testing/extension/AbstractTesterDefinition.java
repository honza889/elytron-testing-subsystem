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
import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;

import java.security.KeyStoreException;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

/**
 * Simple base for individual testers - models allowing test models of Elytron subsystem
 *
 * @author <a href="mailto:jkalina@redhat.com">Jan Kalina</a>
 */
abstract class AbstractTesterDefinition extends SimpleResourceDefinition {

    protected final String TYPE;

    protected final StandardResourceDescriptionResolver RESOURCE_RESOLVER;

    protected AbstractTesterDefinition(String type) {
        super(PathElement.pathElement(type), SubsystemExtension.getResourceDescriptionResolver(type),
                BlankAddStepHandler.INSTANCE, BlankRemoveStepHandler.INSTANCE);
        RESOURCE_RESOLVER = SubsystemExtension.getResourceDescriptionResolver(type);
        TYPE = type;
    }

    protected void registerTest(ManagementResourceRegistration resourceRegistration, String name, AbstractRuntimeOnlyHandler handler) {
        SimpleOperationDefinition definition = new SimpleOperationDefinitionBuilder(name, RESOURCE_RESOLVER).build();
        resourceRegistration.registerOperationHandler(definition, handler);
    }

    protected static class BlankAddStepHandler extends AbstractAddStepHandler {

        public static final BlankAddStepHandler INSTANCE = new BlankAddStepHandler();

        private BlankAddStepHandler() {
        }

        protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        }
    }

    protected static class BlankRemoveStepHandler extends AbstractRemoveStepHandler {

        public static final BlankRemoveStepHandler INSTANCE = new BlankRemoveStepHandler();

        private BlankRemoveStepHandler() {
        }

        protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
        }
    }

    protected static class BlankWriteAttributeHandler extends AbstractWriteAttributeHandler<Void> {

        public static final BlankWriteAttributeHandler INSTANCE = new BlankWriteAttributeHandler();

        @Override
        protected boolean applyUpdateToRuntime(OperationContext context, ModelNode operation, String attributeName, ModelNode resolvedValue, ModelNode currentValue, HandbackHolder<Void> handbackHolder) throws OperationFailedException {
            return false;
        }

        @Override
        protected void revertUpdateToRuntime(OperationContext context, ModelNode operation, String attributeName, ModelNode valueToRestore, ModelNode valueToRevert, Void handback) throws OperationFailedException {
        }
    }

    protected abstract class TestingOperationHandler extends AbstractRuntimeOnlyHandler {

        protected void executeRuntimeStep(OperationContext context, ModelNode operation) throws OperationFailedException {
            String nodeName = operation.get(OP_ADDR).asList().get(operation.get(OP_ADDR).asList().size()-1).get(TYPE).asString();
            ModelNode attributes = context.readResource(PathAddress.EMPTY_ADDRESS).getModel();
            try {
                test(nodeName, attributes, context, operation);
            } catch (OperationFailedException e) {
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
                throw new OperationFailedException("Testing operation failed: "+e.getMessage(), e);
            }
        }

        protected abstract void test(String nodeName, ModelNode nodeAttributes, OperationContext context, ModelNode operation) throws Exception;

    }
}
