package org.wildfly.security.testing.extension;

import org.jboss.as.controller.*;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.operations.validation.ParameterValidator;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;

import java.util.List;

/**
 * @author <a href="mailto:jkalina@redhat.com">Jan Kalina</a>
 */
public class HackDefinition extends SimpleResourceDefinition {

    public static final HackDefinition INSTANCE = new HackDefinition();

    private static final Logger log = Logger.getLogger(HackDefinition.class);

    protected static final SimpleAttributeDefinition OBTAINED =
            new SimpleAttributeDefinitionBuilder("obtained", ModelType.STRING, true).build();

    private HackDefinition() {
        super(PathElement.pathElement("hack"), SubsystemExtension.getResourceDescriptionResolver("hack"), HackAdd.INSTANCE, HackRemove.INSTANCE);
    }

    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerReadWriteAttribute(OBTAINED, HackReadAttribute.INSTANCE, HackWriteAttribute.INSTANCE);
    }

    static class HackAdd extends AbstractAddStepHandler {

        public static final HackAdd INSTANCE = new HackAdd();
        private HackAdd() {}

        protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
            HackDefinition.OBTAINED.validateAndSet(operation, model);
        }

        protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
                                      ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
                throws OperationFailedException {
        }
    }

    static class HackRemove extends AbstractRemoveStepHandler {

        public static final HackRemove INSTANCE = new HackRemove();

        private HackRemove() {}

        protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model)
                throws OperationFailedException {
        }
    }

    static class HackReadAttribute implements OperationStepHandler {

        public static final HackReadAttribute INSTANCE = new HackReadAttribute();

        @Override
        public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
            final String opName = operation.require(ModelDescriptionConstants.OP).asString();

            context.getResult().set("holahej");

        }
    }

    static class HackWriteAttribute extends AbstractWriteAttributeHandler<Void> {

        public static final HackWriteAttribute INSTANCE = new HackWriteAttribute();

        private HackWriteAttribute() {
            super(HackDefinition.OBTAINED);
        }

        protected boolean applyUpdateToRuntime(OperationContext context, ModelNode operation, String attributeName,
                                               ModelNode resolvedValue, ModelNode currentValue, HandbackHolder<Void> handbackHolder)
                throws OperationFailedException {
            return false; // restart not required
        }

        protected void revertUpdateToRuntime(OperationContext context, ModelNode operation, String attributeName,
                                             ModelNode valueToRestore, ModelNode valueToRevert, Void handback) {
        }
    }
}
