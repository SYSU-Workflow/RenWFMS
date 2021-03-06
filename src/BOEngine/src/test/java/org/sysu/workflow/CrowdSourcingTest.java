/*
 * Project Ren @ 2018
 * Rinkako, Ariana, Gordan. SYSU SDCS.
 */
package org.sysu.workflow;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.sysu.workflow.core.*;
import org.sysu.workflow.core.env.MultiStateMachineDispatcher;
import org.sysu.workflow.core.env.SimpleErrorReporter;
import org.sysu.workflow.core.env.jexl.JexlEvaluator;
import org.sysu.workflow.core.instanceTree.InstanceManager;
import org.sysu.workflow.core.instanceTree.RInstanceTree;
import org.sysu.workflow.core.io.BOXMLReader;
import org.sysu.workflow.core.model.SCXML;
import org.sysu.workflow.core.model.extend.MessageMode;

import java.net.URL;
import java.util.HashMap;

/**
 * Author: Rinkako
 * Date  : 2018/3/6
 * Usage :
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class CrowdSourcingTest {

    @Before
    public void Prepare() {
        GlobalContext.IsLocalDebug = true;
    }

    @Test
    public void TestCS() throws Exception {
        URL url = SCXMLTestHelper.getResource("Request.xml");
        SCXML scxml = new BOXMLReader().read(url);
        Evaluator evaluator = new JexlEvaluator();
        EventDispatcher dispatcher = new MultiStateMachineDispatcher();
        BOXMLExecutor executor = new BOXMLExecutor(evaluator, dispatcher, new SimpleErrorReporter());
        executor.setStateMachine(scxml);
        executor.setRtid("testRTID");
        executor.go();

        RInstanceTree tree = InstanceManager.GetInstanceTree("testRTID");
        BOXMLExecutionContext ctx = executor.getExctx();

        HashMap<String, Object> submitPayload = new HashMap<>();
        submitPayload.put("taskName", "What?! BO?!");
        submitPayload.put("taskDescription", "Write an article about BO");
        submitPayload.put("judgeCount", 3);
        submitPayload.put("decomposeCount", 3);
        submitPayload.put("decomposeVoteCount", 3);
        submitPayload.put("solveCount", 3);
        submitPayload.put("solveVoteCount", 3);

        dispatcher.send("testRTID", ctx.NodeId, "", MessageMode.TO_NOTIFIABLE_ID, "Request",
                "", BOXMLIOProcessor.DEFAULT_EVENT_PROCESSOR, "submit", submitPayload, "", 0);

        Assert.assertTrue(executor.getExctx().getScInstance().getCurrentStatus().isInState("Waiting"));
    }
}
