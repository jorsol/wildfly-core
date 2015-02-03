/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.wildfly.core.test.standalone.mgmt;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ANY_ADDRESS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.COMPOSITE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.FAILED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.FAILURE_DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.INTERFACE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OUTCOME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PORT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_RESOURCE_DESCRIPTION_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RECURSIVE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RECURSIVE_DEPTH;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOVE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RESULT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.STEPS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUCCESS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;
import static org.jboss.as.controller.parsing.Element.LINK_LOCAL_ADDRESS;
import static org.jboss.as.controller.parsing.Element.LOOPBACK;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.core.testrunner.ManagementClient;
import org.wildfly.core.testrunner.WildflyTestRunner;

/**
 * Basic management operation unit test.
 *
 * @author Emanuel Muckenhuber
 */
@RunWith(WildflyTestRunner.class)
public class BasicOperationsUnitTestCase {

    @Inject
    private static ManagementClient managementClient;

    @Test
    public void testSocketBindingsWildcards() throws IOException {

        final ModelNode address = new ModelNode();
        address.add("socket-binding-group", "*");
        address.add("socket-binding", "*");
        address.protect();

        final ModelNode operation = new ModelNode();
        operation.get(OP).set(READ_RESOURCE_OPERATION);
        operation.get(OP_ADDR).set(address);

        final ModelNode result = managementClient.getControllerClient().execute(operation);
        Assert.assertTrue(result.hasDefined(RESULT));
        Assert.assertEquals(SUCCESS, result.get(OUTCOME).asString());
        final Collection<ModelNode> steps = getSteps(result.get(RESULT));
        Assert.assertFalse(steps.isEmpty());
        for(final ModelNode step : steps) {
            Assert.assertTrue(step.hasDefined(OP_ADDR));
            Assert.assertTrue(step.hasDefined(RESULT));
            Assert.assertEquals(SUCCESS, step.get(OUTCOME).asString());
        }
    }

    @Test
    public void testReadResourceRecursiveDepthRecursiveUndefined() throws IOException {
        // WFCORE-76
        final ModelNode operation = new ModelNode();
        operation.get(OP).set(READ_RESOURCE_OPERATION);
        operation.get(OP_ADDR).setEmptyList();
        operation.get(RECURSIVE_DEPTH).set(1);

        final ModelNode result = managementClient.getControllerClient().execute(operation);
        Assert.assertEquals(SUCCESS, result.get(OUTCOME).asString());
        Assert.assertTrue(result.hasDefined(RESULT));

        final ModelNode logging = result.get(RESULT, SUBSYSTEM, "logging");
        Assert.assertTrue(logging.hasDefined("logger"));
        final ModelNode rootLogger = result.get(RESULT, SUBSYSTEM, "logging", "root-logger");
        Assert.assertFalse(rootLogger.hasDefined("ROOT"));
    }

    @Test
    public void testReadResourceRecursiveDepthRecursiveTrue() throws IOException {
        // WFCORE-76
        final ModelNode operation = new ModelNode();
        operation.get(OP).set(READ_RESOURCE_OPERATION);
        operation.get(OP_ADDR).setEmptyList();
        operation.get(RECURSIVE).set(true);
        operation.get(RECURSIVE_DEPTH).set(1);

        final ModelNode result = managementClient.getControllerClient().execute(operation);
        Assert.assertEquals(SUCCESS, result.get(OUTCOME).asString());
        Assert.assertTrue(result.hasDefined(RESULT));

        final ModelNode logging = result.get(RESULT, SUBSYSTEM, "logging");
        Assert.assertTrue(logging.hasDefined("logger"));
        final ModelNode rootLogger = result.get(RESULT, SUBSYSTEM, "logging", "root-logger");
        Assert.assertFalse(rootLogger.hasDefined("ROOT"));
    }

    @Test
    public void testReadResourceRecursiveDepthGt1RecursiveTrue() throws IOException {
        // WFCORE-76
        final ModelNode operation = new ModelNode();
        operation.get(OP).set(READ_RESOURCE_OPERATION);
        operation.get(OP_ADDR).setEmptyList();
        operation.get(RECURSIVE).set(true);
        operation.get(RECURSIVE_DEPTH).set(2);

        final ModelNode result = managementClient.getControllerClient().execute(operation);
        Assert.assertEquals(SUCCESS, result.get(OUTCOME).asString());
        Assert.assertTrue(result.hasDefined(RESULT));

        final ModelNode logging = result.get(RESULT, SUBSYSTEM, "logging");
        Assert.assertTrue(logging.hasDefined("logger"));
        final ModelNode rootLogger = result.get(RESULT, SUBSYSTEM, "logging", "root-logger");
        Assert.assertTrue(rootLogger.hasDefined("ROOT"));
    }

    @Test
    public void testReadResourceRecursiveDepthRecursiveFalse() throws IOException {
        // WFCORE-76
        final ModelNode operation = new ModelNode();
        operation.get(OP).set(READ_RESOURCE_OPERATION);
        operation.get(OP_ADDR).setEmptyList();
        operation.get(RECURSIVE).set(false);
        operation.get(RECURSIVE_DEPTH).set(1);

        final ModelNode result = managementClient.getControllerClient().execute(operation);
        Assert.assertEquals(SUCCESS, result.get(OUTCOME).asString());
        Assert.assertTrue(result.hasDefined(RESULT));

        final ModelNode logging = result.get(RESULT, SUBSYSTEM, "logging");
        Assert.assertFalse(logging.hasDefined("logger"));
    }

    @Test
    public void testReadResourceNoRecursiveDepthRecursiveTrue() throws IOException {
        // WFCORE-76
        final ModelNode operation = new ModelNode();
        operation.get(OP).set(READ_RESOURCE_OPERATION);
        operation.get(OP_ADDR).setEmptyList();
        operation.get(RECURSIVE).set(true);
        operation.get(RECURSIVE_DEPTH).set(0);

        final ModelNode result = managementClient.getControllerClient().execute(operation);
        Assert.assertEquals(SUCCESS, result.get(OUTCOME).asString());
        Assert.assertTrue(result.hasDefined(RESULT));

        final ModelNode logging = result.get(RESULT, SUBSYSTEM, "logging");
        Assert.assertFalse(logging.hasDefined("logger"));
    }

    @Test
    public void testReadAttributeWildcards() throws IOException {

        final ModelNode address = new ModelNode();
        address.add("socket-binding-group", "*");
        address.add("socket-binding", "*");
        address.protect();

        final ModelNode operation = new ModelNode();
        operation.get(OP).set(READ_ATTRIBUTE_OPERATION);
        operation.get(OP_ADDR).set(address);
        operation.get(NAME).set(PORT);

        final ModelNode result = managementClient.getControllerClient().execute(operation);
        Assert.assertTrue(result.hasDefined(RESULT));
        Assert.assertEquals(SUCCESS, result.get(OUTCOME).asString());
        final Collection<ModelNode> steps = getSteps(result.get(RESULT));
        Assert.assertFalse(steps.isEmpty());
        for(final ModelNode step : steps) {
            Assert.assertTrue(step.hasDefined(OP_ADDR));
            Assert.assertTrue(step.hasDefined(RESULT));
            final ModelNode stepResult = step.get(RESULT);
            Assert.assertTrue(stepResult.getType() == ModelType.EXPRESSION || stepResult.asInt() >= 0);
        }
    }

    @Test
    public void testSocketBindingDescriptions() throws IOException {

        final ModelNode address = new ModelNode();
        address.add("socket-binding-group", "*");
        address.add("socket-binding", "*");
        address.protect();

        final ModelNode operation = new ModelNode();
        operation.get(OP).set(READ_RESOURCE_DESCRIPTION_OPERATION);
        operation.get(OP_ADDR).set(address);

        final ModelNode result = managementClient.getControllerClient().execute(operation);
        Assert.assertTrue(result.hasDefined(RESULT));
        Assert.assertEquals(SUCCESS, result.get(OUTCOME).asString());
        final Collection<ModelNode> steps = result.get(RESULT).asList();
        Assert.assertFalse(steps.isEmpty());
        Assert.assertEquals("should only contain a single type", 1, steps.size());
        for(final ModelNode step : steps) {
            Assert.assertTrue(step.hasDefined(OP_ADDR));
            Assert.assertTrue(step.hasDefined(RESULT));
            Assert.assertEquals(SUCCESS, step.get(OUTCOME).asString());
            final ModelNode stepResult = step.get(RESULT);
            Assert.assertTrue(stepResult.hasDefined(DESCRIPTION));
            Assert.assertTrue(stepResult.hasDefined(ATTRIBUTES));
            Assert.assertTrue(stepResult.get(ModelDescriptionConstants.ATTRIBUTES).hasDefined(ModelDescriptionConstants.NAME));
            Assert.assertTrue(stepResult.get(ModelDescriptionConstants.ATTRIBUTES).hasDefined(ModelDescriptionConstants.INTERFACE));
            Assert.assertTrue(stepResult.get(ModelDescriptionConstants.ATTRIBUTES).hasDefined(ModelDescriptionConstants.PORT));
        }
    }

    @Test
    public void testRecursiveReadIncludingRuntime() throws IOException {
        final ModelNode operation = new ModelNode();
        operation.get(OP).set(READ_RESOURCE_OPERATION);
        operation.get(OP_ADDR).setEmptyList();
        operation.get(RECURSIVE).set(true);
        operation.get(INCLUDE_RUNTIME).set(true);

        final ModelNode result = managementClient.getControllerClient().execute(operation);
        Assert.assertEquals(result.get(FAILURE_DESCRIPTION).isDefined() ? result.get(FAILURE_DESCRIPTION).asString() : "", SUCCESS, result.get(OUTCOME).asString());
        Assert.assertTrue(result.hasDefined(RESULT));
    }

    @Test
    public void testHttpSocketBinding() throws IOException {
        final ModelNode address = new ModelNode();
        address.add("socket-binding-group", "*");
        address.add("socket-binding", "management-http");
        address.protect();

        final ModelNode operation = new ModelNode();
        operation.get(OP).set(READ_RESOURCE_OPERATION);
        operation.get(OP_ADDR).set(address);

        final ModelNode result = managementClient.getControllerClient().execute(operation);
        Assert.assertTrue(result.hasDefined(RESULT));
        Assert.assertEquals(SUCCESS, result.get(OUTCOME).asString());
        final List<ModelNode> steps = getSteps(result.get(RESULT));
        Assert.assertEquals(1, steps.size());
        final ModelNode httpBinding = steps.get(0);
        Assert.assertEquals(9990, httpBinding.get(RESULT, "port").resolve().asInt());

    }

    @Test
    public void testSimpleReadAttribute() throws IOException {
        final ModelNode address = new ModelNode();
        address.add("subsystem", "logging");
        address.add("console-handler", "CONSOLE");

        final ModelNode operation = createReadAttributeOperation(address, "level");
        final ModelNode result = managementClient.getControllerClient().execute(operation);
        assertSuccessful(result);

        Assert.assertEquals("INFO", result.get(RESULT).asString());
    }

    @Test
    @Ignore //TODO UNDERTOW reneable when we expose metrics from undertow
    public void testMetricReadAttribute() throws IOException {
        final ModelNode address = new ModelNode();
        address.add("subsystem", "undertow");
        address.add("connector", "http");

        final ModelNode operation = createReadAttributeOperation(address, "bytesReceived");
        final ModelNode result = managementClient.getControllerClient().execute(operation);
        assertSuccessful(result);
        Assert.assertTrue(result.asInt() >= 0);
    }

    @Test
    public void testReadAttributeChild() throws IOException {
        final ModelNode address = new ModelNode();
        address.add("subsystem", "deployment-scanner");

        final ModelNode operation = createReadAttributeOperation(address, "scanner");
        final ModelNode result = managementClient.getControllerClient().execute(operation);
        Assert.assertEquals(FAILED, result.get(OUTCOME).asString());
    }

    @Test
    public void testInterfaceAdd() throws IOException {

        final ModelNode base = new ModelNode();
        base.get(OP).set(WRITE_ATTRIBUTE_OPERATION);
        base.get(OP_ADDR).add(INTERFACE, "test");
        base.protect();

        final ModelNode add = base.clone();
        add.get(OP).set(ADD);
        add.get(ANY_ADDRESS).set(true);
        // Add interface
        execute(add);

        final ModelNode any = base.clone();
        any.get(NAME).set(ANY_ADDRESS);
        any.get(VALUE).set(false);

        final ModelNode linkLocalAddress = base.clone();
        linkLocalAddress.get(NAME).set(LINK_LOCAL_ADDRESS.getLocalName()) ;
        linkLocalAddress.get(VALUE).set(false);

        final ModelNode loopBack = base.clone();
        loopBack.get(NAME).set(LOOPBACK.getLocalName());
        loopBack.get(VALUE).set(true);

        final ModelNode composite = new ModelNode();
        composite.get(OP).set(COMPOSITE);
        composite.get(OP_ADDR).setEmptyList();

        composite.get(STEPS).add(any);
        composite.get(STEPS).add(linkLocalAddress);
        composite.get(STEPS).add(loopBack);

        execute(composite);

        // Remove interface
        final ModelNode remove = base.clone();
        remove.get(OP).set(REMOVE);
        execute(remove);
    }

    @Test
    public void testEmptyAddress() throws IOException {
        ModelNode operation = new ModelNode();
        operation.get(OP).set("whoami");
        operation.get(OP_ADDR).set("");
        final ModelNode result = managementClient.getControllerClient().execute(operation);
        Assert.assertEquals(FAILED, result.get(OUTCOME).asString());
        Assert.assertTrue(result.hasDefined(FAILURE_DESCRIPTION));
        Assert.assertTrue(result.get(FAILURE_DESCRIPTION).asString() + "should contain WFLYCTL0378", result.get(FAILURE_DESCRIPTION).asString().contains("WFLYCTL0378"));
    }

    @Test
    public void testEmptyOperation() throws IOException {
        ModelNode operation = new ModelNode();
        operation.get(OP_ADDR).setEmptyList();
        final ModelNode result = managementClient.getControllerClient().execute(operation);
        Assert.assertEquals(FAILED, result.get(OUTCOME).asString());
        Assert.assertTrue(result.hasDefined(FAILURE_DESCRIPTION));
        Assert.assertTrue(result.get(FAILURE_DESCRIPTION).asString() + "should contain WFLYCTL0383", result.get(FAILURE_DESCRIPTION).asString().contains("WFLYCTL0383"));
    }

    protected ModelNode execute(final ModelNode operation) throws IOException {
        final ModelNode result = managementClient.getControllerClient().execute(operation);
        Assert.assertEquals(result.toString(), SUCCESS, result.get(OUTCOME).asString());
        return result;
    }

    static void assertSuccessful(final ModelNode result) {
        Assert.assertEquals(SUCCESS, result.get(OUTCOME).asString());
        Assert.assertTrue(result.hasDefined(RESULT));
    }

    static ModelNode createReadAttributeOperation(final ModelNode address, final String attributeName) {
        final ModelNode operation = new ModelNode();
        operation.get(OP).set(READ_ATTRIBUTE_OPERATION);
        operation.get(OP_ADDR).set(address);
        operation.get(NAME).set(attributeName);
        return operation;
    }

    protected static List<ModelNode> getSteps(final ModelNode result) {
        Assert.assertTrue(result.isDefined());
        return result.asList();
    }
}
