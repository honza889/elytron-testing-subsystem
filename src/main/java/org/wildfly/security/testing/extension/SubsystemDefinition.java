package org.wildfly.security.testing.extension;

import org.jboss.as.controller.*;
import org.jboss.as.controller.operations.validation.ParameterValidator;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.msc.service.ServiceController;

import java.util.List;

/**
 * @author <a href="mailto:jkalina@redhat.com">Jan Kalina</a>
 */
public class SubsystemDefinition extends SimpleResourceDefinition {

    public static final SubsystemDefinition INSTANCE = new SubsystemDefinition();

    protected static final SimpleAttributeDefinition OBTAINED =
            new SimpleAttributeDefinitionBuilder("obtained", ModelType.STRING)
                    .setAllowExpression(true).setXmlName("obtained").setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(null).setAllowNull(true).setValidator(new ParameterValidator() {
                public void validateParameter(String parameterName, ModelNode value) throws OperationFailedException {

                }
                public void validateResolvedParameter(String parameterName, ModelNode value) throws OperationFailedException {

                }
            }).build();

    private SubsystemDefinition() {
        super(SubsystemExtension.SUBSYSTEM_PATH,
                SubsystemExtension.getResourceDescriptionResolver(null),
                SubsystemAdd.INSTANCE,
                SubsystemRemove.INSTANCE);
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        //you can register aditional operations here
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {

    }

    static class SubsystemAdd extends AbstractBoottimeAddStepHandler {

        static final SubsystemAdd INSTANCE = new SubsystemAdd();

        private SubsystemAdd() {

        }

        @Override
        protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
            //model.get("type").setEmptyObject(); //Initialize the 'type' child node
            OBTAINED.validateAndSet(operation, model);
        }

        @Override
        public void performBoottime(OperationContext context, ModelNode operation, ModelNode model,
                                    ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
                throws OperationFailedException {

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
