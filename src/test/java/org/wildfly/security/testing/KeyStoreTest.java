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
import org.junit.Test;

import java.io.IOException;
import java.net.UnknownHostException;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Test of KeyStores
 *
 * NOTE: to remove service at runtime, in the end of the removing command has to be:
 * {allow-resource-service-restart=true}
 *
 * @author <a href="mailto:jkalina@redhat.com">Jan Kalina</a>
 */
public class KeyStoreTest {

    protected CLI cli;

    protected ModelNode cmdAssert(String command) {
        ModelNode response = cli.cmd(command).getResponse();
        if(!response.get(OUTCOME).asString().equals(SUCCESS)){
            Assert.fail(command + response.toJSONString(false));
        }
        return response.get(RESULT);
    }

    protected ModelNode cmdIgnore(String command) {
        return cli.cmd(command).getResponse().get(RESULT);
    }

    @Before
    public void init() throws UnknownHostException {
        cli = CLI.newInstance();
        cli.connect();
        cmdIgnore("/subsystem=elytron-testing:remove{allow-resource-service-restart=true}");
        cmdIgnore("/extension=org.wildfly.security.elytron-test:remove{allow-resource-service-restart=true}");
        cmdIgnore("/extension=org.wildfly.security.elytron-test:add");
        cmdIgnore("/subsystem=elytron-testing:add");
        System.out.println("connected");
    }

    @After
    public void destroy() throws IOException {
        cli.disconnect();
        System.out.println("disconnected");
    }

    @Test
    public void testKeyStoreCreateExists() throws Exception {
        cmdIgnore("/subsystem=elytron-testing/keystoretester=KeyStoreCreated/:add");
        cmdIgnore("/subsystem=elytron-testing/keystoretester=KeyStoreCreated/:write-attribute(name=name,value=KeyStoreCreated)");
        cmdIgnore("/subsystem=elytron/keystore=KeyStoreCreated/:remove{allow-resource-service-restart=true}");

        Assert.assertFalse(cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreCreated/:testExists").asBoolean());

        cmdIgnore("/subsystem=elytron/keystore=KeyStoreCreated/:add(type=JKS,password=123456,path=clientkeystore,relative-to=jboss.server.config.dir)");

        Assert.assertTrue(cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreCreated/:testExists").asBoolean());
    }

    @Test
    public void testKeyStoreRemoving() throws Exception {
        cmdIgnore("/subsystem=elytron-testing/keystoretester=KeyStoreCreated/:add");
        cmdIgnore("/subsystem=elytron-testing/keystoretester=KeyStoreCreated/:write-attribute(name=name,value=KeyStoreCreated)");

        cmdIgnore("/subsystem=elytron/keystore=KeyStoreRemoving/:add(type=JKS,password=123456,path=clientkeystore,relative-to=jboss.server.config.dir)");

        cmdIgnore("/subsystem=elytron/keystore=KeyStoreRemoving/:remove{allow-resource-service-restart=true}");

        cmdAssert("/subsystem=elytron/keystore=KeyStoreRemoving/:add(type=JKS,password=123456,path=clientkeystore,relative-to=jboss.server.config.dir)");
    }

    @Test
    public void testJksKeyStoreContains() throws Exception {
        cmdIgnore("/subsystem=elytron/keystore=KeyStore1/:remove{allow-resource-service-restart=true}");
        cmdAssert("/subsystem=elytron/keystore=KeyStore1/:add(type=JKS,password=123456,path=clientkeystore,relative-to=jboss.server.config.dir)");
        cmdAssert("/subsystem=elytron-testing/keystoretester=testKeyStore/:add");
        cmdIgnore("/subsystem=elytron-testing/keystoretester=testKeyStore/:write-attribute(name=name,value=KeyStore1)");
        cmdIgnore("/subsystem=elytron-testing/keystoretester=testKeyStore/:write-attribute(name=alias,value=client)");

        ModelNode result = cmdAssert("/subsystem=elytron-testing/keystoretester=testKeyStore/:testContains");
        Assert.assertTrue(result.get("contains").asBoolean());
    }

    // TODO testGetKey

    @Test
    public void testJksKeyStoreCertificate() throws Exception {
        cmdIgnore("/subsystem=elytron/keystore=KeyStore1/:remove{allow-resource-service-restart=true}");
        cmdAssert("/subsystem=elytron/keystore=KeyStore1/:add(type=JKS,password=123456,path=clientkeystore,relative-to=jboss.server.config.dir)");
        cmdAssert("/subsystem=elytron-testing/keystoretester=testKeyStore/:add");
        cmdIgnore("/subsystem=elytron-testing/keystoretester=testKeyStore/:write-attribute(name=name,value=KeyStore1)");
        cmdIgnore("/subsystem=elytron-testing/keystoretester=testKeyStore/:write-attribute(name=alias,value=client)");
        cmdIgnore("/subsystem=elytron-testing/keystoretester=testKeyStore/:write-attribute(name=password,value=123456)");

        ModelNode result = cmdAssert("/subsystem=elytron-testing/keystoretester=testKeyStore/:testGetCertificate");
        System.out.println(result.get("certificate").asString());
    }

}
