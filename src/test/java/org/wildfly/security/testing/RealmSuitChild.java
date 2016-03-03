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

import org.junit.Ignore;
import org.junit.Test;

/**
 * Test of security realms.
 *
 * @author <a href="mailto:jkalina@redhat.com">Jan Kalina</a>
 */
@Ignore("not working yet")
public class RealmSuitChild extends AbstractTest {

    @Test
    public void testFileSystemRealm() throws Exception {
        cmdIgnore("/subsystem=elytron/filesystem-realm=TestingFilesystemRealm/:remove{allow-resource-service-restart=true}");
        cmdAssert("/subsystem=elytron/filesystem-realm=TestingFilesystemRealm/:add(path=filesystem-realm,levels=2,relative-to=elytron.testing.resources)");
        //testLogin("/subsystem=elytron/filesystem-realm=TestingFilesystemRealm", "admin", "admin"); // SecurityRealmService not implemented in subsystem yet
        testWrite("/subsystem=elytron/filesystem-realm=TestingFilesystemRealm");
    }

    /* try testLogin with property file from resources */
    @Test
    public void testPropertyRealm() throws Exception {
        cmdIgnore("/subsystem=elytron/filesystem-realm=TestingFilesystemRealm/:remove{allow-resource-service-restart=true}");
        cmdAssert("/subsystem=elytron/filesystem-realm=TestingFilesystemRealm/:add(path=filesystem-realm,levels=2,relative-to=elytron.testing.resources)");
        //testLogin("/subsystem=elytron/filesystem-realm=TestingFilesystemRealm", "admin", "admin"); // SecurityRealmService not implemented in subsystem yet
        testWrite("/subsystem=elytron/filesystem-realm=TestingFilesystemRealm");
    }

    private void testLogin(String realm, String identity, String password) throws Exception {
        cmdAssert("/subsystem=elytron-testing/realmtester=TestLogin/:add");
        cmdAssert("/subsystem=elytron-testing/realmtester=TestLogin/:write-attribute(name=name,value=\""+realm+"\")");
        cmdAssert("/subsystem=elytron-testing/realmtester=TestLogin/:write-attribute(name=identity,value=\""+identity+"\")");
        cmdAssert("/subsystem=elytron-testing/realmtester=TestLogin/:write-attribute(name=password,value=\""+password+"\")");
        System.out.println(cmdAssert("/subsystem=elytron-testing/realmtester=TestLogin/:testLogin"));
    }

    private void testWrite(String realm) throws Exception {
        cmdIgnore(realm + "/identity=secondUser/:remove{allow-resource-service-restart=true}");
        cmdAssert(realm + "/identity=secondUser/:add");

        System.out.println(cmdAssert(realm + "/identity=secondUser/:read-attribute-group-names"));

        System.out.println(cmdAssert(realm + "/identity=secondUser/:read-operation-names"));

        System.out.println(cmdAssert(realm + "/identity=secondUser/:read-identity"));

        System.out.println(cmdAssert(realm + "/identity=secondUser/:set-password(password=secret)"));
    }

}
