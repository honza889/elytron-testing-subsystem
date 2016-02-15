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
import org.junit.Ignore;
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
    public void testKeyStoreCreate() throws Exception {
        cmdIgnore("/subsystem=elytron-testing/keystoretester=KeyStoreCreate/:add");
        cmdIgnore("/subsystem=elytron/keystore=KeyStoreCreate/:remove{allow-resource-service-restart=true}");

        cmdIgnore("/subsystem=elytron-testing/keystoretester=KeyStoreCreate/:write-attribute(name=name,value=KeyStoreCreate)");
        Assert.assertFalse(cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreCreate/:serviceExists").asBoolean());

        cmdIgnore("/subsystem=elytron/keystore=KeyStoreCreate/:add(type=JKS,password=123456,path=testingCaJks.keystore,relative-to=elytron.testing.resources)");
        Assert.assertTrue(cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreCreate/:serviceExists").asBoolean());
    }

    @Test
    public void testKeyStoreRemove() throws Exception {
        cmdIgnore("/subsystem=elytron-testing/keystoretester=KeyStoreRemove/:add");
        cmdIgnore("/subsystem=elytron-testing/keystoretester=KeyStoreRemove/:write-attribute(name=name,value=KeyStoreRemove)");

        cmdIgnore("/subsystem=elytron/keystore=KeyStoreRemove/:add(type=JKS,password=123456,path=testingCaJks.keystore,relative-to=elytron.testing.resources)");
        Assert.assertTrue(cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreRemove/:serviceExists").asBoolean());

        cmdIgnore("/subsystem=elytron/keystore=KeyStoreRemove/:remove{allow-resource-service-restart=true}");
        Assert.assertFalse(cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreRemove/:serviceExists").asBoolean());

        cmdAssert("/subsystem=elytron/keystore=KeyStoreRemove/:add(type=JKS,password=123456,path=testingCaJks.keystore,relative-to=elytron.testing.resources)");
    }

    @Test
    public void testKeyStoreAliases() throws Exception {
        cmdIgnore("/subsystem=elytron/keystore=KeyStoreAliases/:remove{allow-resource-service-restart=true}");
        cmdAssert("/subsystem=elytron/keystore=KeyStoreAliases/:add(type=JKS,password=123456,path=testingCaJks.keystore,relative-to=elytron.testing.resources)");

        cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreAliases/:add");
        cmdIgnore("/subsystem=elytron-testing/keystoretester=KeyStoreAliases/:write-attribute(name=name,value=KeyStoreAliases)");

        List<String> aliases = new ArrayList();
        ModelNode result = cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreAliases/:getAliases");
        for(ModelNode alias : result.asList()) aliases.add(alias.asString());

        Assert.assertTrue(aliases.contains("testingserver"));
        Assert.assertTrue(aliases.contains("testingca"));
    }

    @Test
    public void testKeyStoreContains() throws Exception {
        cmdIgnore("/subsystem=elytron/keystore=KeyStoreContains/:remove{allow-resource-service-restart=true}");
        cmdAssert("/subsystem=elytron/keystore=KeyStoreContains/:add(type=JKS,password=123456,path=testingCaJks.keystore,relative-to=elytron.testing.resources)");

        cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreContains/:add");
        cmdIgnore("/subsystem=elytron-testing/keystoretester=KeyStoreContains/:write-attribute(name=name,value=KeyStoreContains)");

        cmdIgnore("/subsystem=elytron-testing/keystoretester=KeyStoreContains/:write-attribute(name=alias,value=testingserver)");
        Assert.assertTrue(cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreContains/:containsAlias").get("contains").asBoolean());

        cmdIgnore("/subsystem=elytron-testing/keystoretester=KeyStoreContains/:write-attribute(name=alias,value=nonexisting)");
        Assert.assertFalse(cmdAssert("/subsystem=elytron-testing/keystoretester=KeyStoreContains/:containsAlias").get("contains").asBoolean());
    }

    @Test
    @Ignore("no testing keystore prepared yet")
    public void testPasswordKeyStore() throws Exception {
        cmdIgnore("/subsystem=elytron/keystore=PasswordKeyStore/:remove{allow-resource-service-restart=true}");
        cmdAssert("/subsystem=elytron/keystore=PasswordKeyStore/:add(type=JKS,password=123456,path=testingCaJks.keystore,relative-to=elytron.testing.resources)");

        cmdAssert("/subsystem=elytron-testing/keystoretester=PasswordKeyStore/:add");
        cmdAssert("/subsystem=elytron-testing/keystoretester=PasswordKeyStore/:write-attribute(name=name,value=PasswordKeyStore)");
        cmdAssert("/subsystem=elytron-testing/keystoretester=PasswordKeyStore/:write-attribute(name=password,value=123456)"); // of keystore item
        cmdAssert("/subsystem=elytron-testing/keystoretester=PasswordKeyStore/:write-attribute(name=alias,value=testingserver)");

        ModelNode result = cmdAssert("/subsystem=elytron-testing/keystoretester=PasswordKeyStore/:getKey");
        System.out.println(result.toJSONString(false));
        Assert.assertTrue(result.get("contains").asBoolean());
        Assert.assertTrue(result.get("certificate").asString().contains("CN=Testing server"));
        Assert.assertEquals(2, result.get("certificateChain").asList().size());
    }

    @Test
    public void testCertificateKeyStore() throws Exception {
        cmdIgnore("/subsystem=elytron/keystore=CertificateKeyStore/:remove{allow-resource-service-restart=true}");
        cmdAssert("/subsystem=elytron/keystore=CertificateKeyStore/:add(type=JKS,password=123456,path=testingCaJks.keystore,relative-to=elytron.testing.resources)");

        cmdAssert("/subsystem=elytron-testing/keystoretester=CertificateKeyStore/:add");
        cmdAssert("/subsystem=elytron-testing/keystoretester=CertificateKeyStore/:write-attribute(name=name,value=CertificateKeyStore)");
        cmdAssert("/subsystem=elytron-testing/keystoretester=CertificateKeyStore/:write-attribute(name=password,value=123456)"); // of keystore item
        cmdAssert("/subsystem=elytron-testing/keystoretester=CertificateKeyStore/:write-attribute(name=alias,value=testingserver)");

        ModelNode result = cmdAssert("/subsystem=elytron-testing/keystoretester=CertificateKeyStore/:getCertificate");
        System.out.println(result.toJSONString(false));
        Assert.assertTrue(result.get("contains").asBoolean());
        Assert.assertTrue(result.get("certificate").asString().contains("CN=Testing server"));
        Assert.assertEquals(2, result.get("certificateChain").asList().size());
    }

    // TODO test storing (not implemented in subsystem yet)
}
