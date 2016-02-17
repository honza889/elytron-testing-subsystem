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

import org.junit.Assert;
import org.junit.Test;

/**
 * Test of Domain
 *
 * @author <a href="mailto:jkalina@redhat.com">Jan Kalina</a>
 */
public class DomainTest extends AbstractTest {

    @Test
    public void testDomainAdd() throws Exception {
        cmdIgnore("/subsystem=elytron/security-domain=TestingDomain/:remove{allow-resource-service-restart=true}");
        cmdIgnore("/subsystem=elytron/properties-realm=TestingRealm1/:remove{allow-resource-service-restart=true}");

        cmdIgnore("/subsystem=elytron-testing/domaintester=DomainAdd/:add");
        cmdIgnore("/subsystem=elytron-testing/domaintester=DomainAdd/:write-attribute(name=name,value=TestingDomain)");

        Assert.assertFalse(cmdAssert("/subsystem=elytron-testing/domaintester=DomainAdd/:serviceExists").asBoolean());

        cmdAssert("/subsystem=elytron/properties-realm=TestingRealm1/:add(users-properties={path=testingrealm1-users.properties,relative-to=elytron.testing.resources},groups-attribute=groups,groups-properties={path=testingrealm1-groups.properties,relative-to=elytron.testing.resources})");
        cmdAssert("/subsystem=elytron/security-domain=TestingDomain/:add(default-realm=TestingRealm1,realms=[{realm=TestingRealm1}])");

        Assert.assertTrue(cmdAssert("/subsystem=elytron-testing/domaintester=DomainAdd/:serviceExists").asBoolean());
    }

}
