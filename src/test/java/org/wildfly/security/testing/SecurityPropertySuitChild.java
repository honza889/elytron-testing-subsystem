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
 * Test of security properties
 *
 * @author <a href="mailto:jkalina@redhat.com">Jan Kalina</a>
 */
public class SecurityPropertySuitChild extends AbstractTest {

    @Test
    public void testPropertyAddRemove() throws Exception {
        cmdIgnore("/subsystem=elytron/security-property=TestingProperty/:remove{allow-resource-service-restart=true}");

        cmdAssert("/subsystem=elytron/security-property=TestingProperty/:add(value=testingValue)");
        Assert.assertEquals("testingValue", cmdAssert("/subsystem=elytron/security-property=TestingProperty/:read-attribute(name=value)").asString());

        cmdAssert("/subsystem=elytron/security-property=TestingProperty/:write-attribute(name=value,value=secondValue)");
        Assert.assertEquals("secondValue", cmdAssert("/subsystem=elytron/security-property=TestingProperty/:read-attribute(name=value)").asString());

        cmdAssert("/subsystem=elytron/security-property=TestingProperty/:remove{allow-resource-service-restart=true}");
    }

}
