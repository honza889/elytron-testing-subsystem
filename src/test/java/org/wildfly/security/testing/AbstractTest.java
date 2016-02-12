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

package org.wildfly.security.testing;

import org.jboss.as.cli.scriptsupport.CLI;
import org.jboss.dmr.ModelNode;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.io.IOException;
import java.net.UnknownHostException;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Abstract test using elytron testing subsystem
 *
 * NOTE: to remove service at runtime, in the end of the removing command has to be:
 * {allow-resource-service-restart=true}
 *
 * @author <a href="mailto:jkalina@redhat.com">Jan Kalina</a>
 */
public class AbstractTest {

    protected CLI cli;

    @Before
    public void init() throws UnknownHostException {
        cli = CLI.newInstance();
        cli.connect();
        System.out.println("connected");
        cmdIgnore("/subsystem=elytron-testing:remove{allow-resource-service-restart=true}");
        cmdIgnore("/extension=org.wildfly.security.elytron-test:remove{allow-resource-service-restart=true}");
        cmdAssert("/extension=org.wildfly.security.elytron-test:add");
        cmdAssert("/subsystem=elytron-testing:add");
        cmdIgnore("/path=elytron.testing.resources/:add(path=\"" + getClass().getResource("/").getFile() + "\")");
        cmdIgnore("/path=elytron.testing.resources/:write-attribute(name=path,value=\"" + getClass().getResource("/").getFile() + "\")");
    }

    @After
    public void destroy() throws IOException {
        cli.disconnect();
        System.out.println("disconnected");
    }

    protected ModelNode cmdAssert(String command) {
        ModelNode response = cli.cmd(command).getResponse();
        if (!response.get(OUTCOME).asString().equals(SUCCESS)) {
            Assert.fail(command + response.toJSONString(false));
        }
        return response.get(RESULT);
    }

    protected ModelNode cmdIgnore(String command) {
        ModelNode response = cli.cmd(command).getResponse();
        if (!response.get(OUTCOME).asString().equals(SUCCESS)) {
            System.out.println(command + response.toJSONString(false));
        }
        return response.get(RESULT);
    }

}
