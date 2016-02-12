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

import org.jboss.dmr.ModelNode;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Test of KeyStores
 *
 * NOTE: to remove service at runtime, in the end of the removing command has to be:
 * {allow-resource-service-restart=true}
 *
 * @author <a href="mailto:jkalina@redhat.com">Jan Kalina</a>
 */
public class KeyStoreTest extends AbstractTest {

    @Test
    public void testKeyStoreCreateJks() throws Exception {
        cmdIgnore("/subsystem=elytron-testing/keystoretester=KeyStoreCreated/:add");
        cmdIgnore("/subsystem=elytron-testing/keystoretester=KeyStoreCreated/:write-attribute(name=name,value=KeyStoreCreated)");
        cmdIgnore("/subsystem=elytron/keystore=KeyStoreCreated/:remove{allow-resource-service-restart=true}");

        Assert.assertFalse(cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreCreated/:testExists").asBoolean());

        cmdIgnore("/subsystem=elytron/keystore=KeyStoreCreated/:add(type=JKS,password=123456,path=testingCaJks.keystore,relative-to=elytron.testing.resources)");

        Assert.assertTrue(cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreCreated/:testExists").asBoolean());
    }

    @Test
    public void testKeyStoreRemoving() throws Exception {
        cmdIgnore("/subsystem=elytron-testing/keystoretester=KeyStoreRemoving/:add");
        cmdIgnore("/subsystem=elytron-testing/keystoretester=KeyStoreRemoving/:write-attribute(name=name,value=KeyStoreRemoving)");

        cmdIgnore("/subsystem=elytron/keystore=KeyStoreRemoving/:add(type=JKS,password=123456,path=testingCaJks.keystore,relative-to=elytron.testing.resources)");

        Assert.assertTrue(cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreRemoving/:testExists").asBoolean());

        cmdIgnore("/subsystem=elytron/keystore=KeyStoreRemoving/:remove{allow-resource-service-restart=true}");

        Assert.assertFalse(cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreRemoving/:testExists").asBoolean());

        cmdAssert("/subsystem=elytron/keystore=KeyStoreRemoving/:add(type=JKS,password=123456,path=testingCaJks.keystore,relative-to=elytron.testing.resources)");
    }

    @Test
    public void testJksKeyStoreAliases() throws Exception {
        cmdIgnore("/subsystem=elytron/keystore=KeyStore1/:remove{allow-resource-service-restart=true}");
        cmdAssert("/subsystem=elytron/keystore=KeyStore1/:add(type=JKS,password=123456,path=testingCaJks.keystore,relative-to=elytron.testing.resources)");
        cmdAssert("/subsystem=elytron-testing/keystoretester=testKeyStore/:add");
        cmdIgnore("/subsystem=elytron-testing/keystoretester=testKeyStore/:write-attribute(name=name,value=KeyStore1)");

        List<String> aliases = new ArrayList();
        ModelNode result = cmdAssert("/subsystem=elytron-testing/keystoretester=testKeyStore/:testAliases");
        for(ModelNode alias : result.asList()) aliases.add(alias.asString());

        Assert.assertTrue(aliases.contains("testingserver"));
        Assert.assertTrue(aliases.contains("testingca"));
    }

    @Test
    public void testJksKeyStoreContains() throws Exception {
        cmdIgnore("/subsystem=elytron/keystore=KeyStore1/:remove{allow-resource-service-restart=true}");
        cmdAssert("/subsystem=elytron/keystore=KeyStore1/:add(type=JKS,password=123456,path=testingCaJks.keystore,relative-to=elytron.testing.resources)");
        cmdAssert("/subsystem=elytron-testing/keystoretester=testKeyStore/:add");
        cmdIgnore("/subsystem=elytron-testing/keystoretester=testKeyStore/:write-attribute(name=name,value=KeyStore1)");
        cmdIgnore("/subsystem=elytron-testing/keystoretester=testKeyStore/:write-attribute(name=alias,value=testingserver)");

        ModelNode result = cmdAssert("/subsystem=elytron-testing/keystoretester=testKeyStore/:testContains"); // REMOVE
        System.out.println(result.toJSONString(false));

        Assert.assertTrue(cmdAssert("/subsystem=elytron-testing/keystoretester=testKeyStore/:testContains").get("contains").asBoolean());

        cmdIgnore("/subsystem=elytron-testing/keystoretester=testKeyStore/:write-attribute(name=alias,value=nonexisting)");

        Assert.assertFalse(cmdAssert("/subsystem=elytron-testing/keystoretester=testKeyStore/:testContains").get("contains").asBoolean());
    }

    // TODO testGetKey

    @Test
    public void testJksKeyStoreCertificate() throws Exception {
        cmdIgnore("/subsystem=elytron/keystore=KeyStore1/:remove{allow-resource-service-restart=true}");
        cmdAssert("/subsystem=elytron/keystore=KeyStore1/:add(type=JKS,password=123456,path=testingCaJks.keystore,relative-to=elytron.testing.resources)");
        cmdAssert("/subsystem=elytron-testing/keystoretester=testKeyStore/:add");
        cmdIgnore("/subsystem=elytron-testing/keystoretester=testKeyStore/:write-attribute(name=name,value=KeyStore1)");
        cmdIgnore("/subsystem=elytron-testing/keystoretester=testKeyStore/:write-attribute(name=alias,value=testingserver)");
        cmdIgnore("/subsystem=elytron-testing/keystoretester=testKeyStore/:write-attribute(name=password,value=123456)");

        ModelNode result = cmdAssert("/subsystem=elytron-testing/keystoretester=testKeyStore/:testGetCertificate");
        System.out.println(result.toJSONString(false));
        Assert.assertTrue(result.get("contains").asBoolean());
        Assert.assertTrue(result.get("certificate").asString().contains("CN=Testing server"));
        Assert.assertEquals(2, result.get("certificateChain").asList().size());
    }

    // TODO store
    // TODO non-JKS

}
