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
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
public class KeyStoreSuitChild extends AbstractTest {

    @BeforeClass
    public static void init() throws Exception {
        Path keyStoreToRead = new File(KeyStoreSuitChild.class.getResource("/testingCaJks.keystore").getFile()).toPath();
        Path keyStoreToModify = new File(KeyStoreSuitChild.class.getResource("/testingCaJksMod.keystore").getFile()).toPath();
        Files.copy(keyStoreToRead, keyStoreToModify, StandardCopyOption.REPLACE_EXISTING);

        cmdIgnoreDuplicate("/subsystem=elytron-testing/keystoretester=KeyStoreAddingRemoving/:add");
        cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreAddingRemoving/:write-attribute(name=name,value=KeyStoreToAddRemove)");
        cmdIgnoreNotFound("/subsystem=elytron/keystore=KeyStoreToAddRemove/:remove{allow-resource-service-restart=true}");

        cmdIgnoreDuplicate("/subsystem=elytron/keystore=TestingKeyStore/:add(type=JKS,password=123456,path=testingCaJks.keystore,relative-to=elytron.testing.resources)");
        cmdIgnoreDuplicate("/subsystem=elytron-testing/keystoretester=KeyStoreTester/:add");
        cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreTester/:write-attribute(name=name,value=TestingKeyStore)");

        cmdIgnoreDuplicate("/subsystem=elytron/keystore=ModifiableKeyStore/:add(type=JKS,password=123456,path=testingCaJksMod.keystore,relative-to=elytron.testing.resources)");
        cmdIgnoreDuplicate("/subsystem=elytron-testing/keystoretester=KeyStoreModTester/:add");
        cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreModTester/:write-attribute(name=name,value=ModifiableKeyStore)");
        cmdAssert("/subsystem=elytron/keystore=ModifiableKeyStore/:load");

        System.out.println("initialized");
    }

    @Test
    public void testKeyStoreAddRemoveService() throws Exception {
        Assert.assertFalse(cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreAddingRemoving/:serviceExists").asBoolean());
        cmdAssert("/subsystem=elytron/keystore=KeyStoreToAddRemove/:add(type=JKS,password=123456,path=testingCaJks.keystore,relative-to=elytron.testing.resources)");
        Assert.assertTrue(cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreAddingRemoving/:serviceExists").asBoolean());
        cmdAssert("/subsystem=elytron/keystore=KeyStoreToAddRemove/:remove{allow-resource-service-restart=true}");
        Assert.assertFalse(cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreAddingRemoving/:serviceExists").asBoolean());
    }

    @Test
    public void testKeyStoreAliases() throws Exception {
        List<String> aliases = new ArrayList();
        ModelNode result = cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreTester/:getAliases");
        for(ModelNode alias : result.asList()) aliases.add(alias.asString());
        Assert.assertTrue(aliases.contains("testingserver"));
        Assert.assertTrue(aliases.contains("testingca"));
    }

    @Test
    public void testKeyStoreContains() throws Exception {
        cmdIgnore("/subsystem=elytron-testing/keystoretester=KeyStoreTester/:write-attribute(name=alias,value=testingserver)");
        Assert.assertTrue(cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreTester/:containsAlias").get("contains").asBoolean());
        cmdIgnore("/subsystem=elytron-testing/keystoretester=KeyStoreTester/:write-attribute(name=alias,value=nonexisting)");
        Assert.assertFalse(cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreTester/:containsAlias").get("contains").asBoolean());
    }

    @Test
    @Ignore("no testing keystore prepared yet")
    public void testPasswordKeyStore() throws Exception {
        cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreTester/:write-attribute(name=alias,value=testingserver)");
        cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreTester/:write-attribute(name=password,value=123456)"); // password of keystore item

        ModelNode result = cmdAssert("/subsystem=elytron-testing/keystoretester=TestingKeyStore/:getKey");
        System.out.println(result.toJSONString(false));
        Assert.assertTrue(result.get("contains").asBoolean());
        Assert.assertTrue(result.get("certificate").asString().contains("CN=Testing server"));
        Assert.assertEquals(2, result.get("certificateChain").asList().size());
    }

    @Test
    public void testCertificateKeyStore() throws Exception {
        cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreTester/:write-attribute(name=alias,value=testingserver)");
        cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreTester/:write-attribute(name=password,value=123456)"); // password of keystore item

        ModelNode result = cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreTester/:getCertificate");
        System.out.println(result.toJSONString(false));
        Assert.assertTrue(result.get("contains").asBoolean());
        Assert.assertTrue(result.get("certificate").asString().contains("CN=Testing server"));
        Assert.assertEquals(2, result.get("certificateChain").asList().size());
    }

    @Test
    public void testRemoveCertificate() throws Exception {
        cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreModTester/:write-attribute(name=alias,value=testingserver)");
        Assert.assertTrue(cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreModTester/:containsAlias").get("contains").asBoolean());

        cmdAssert("/subsystem=elytron/keystore=ModifiableKeyStore/alias=testingserver/:remove");
        cmdAssert("/subsystem=elytron/keystore=ModifiableKeyStore/:store");

        cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreModTester/:write-attribute(name=alias,value=testingserver)");
        Assert.assertFalse(cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreModTester/:containsAlias").get("contains").asBoolean());
    }

    // TODO test inserting key/cert (not implemented in subsystem yet)
}
