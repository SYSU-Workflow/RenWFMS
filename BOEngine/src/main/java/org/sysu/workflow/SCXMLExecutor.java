/*
 * Project Ren @ 2018
 * Rinkako, Ariana, Gordan. SYSU SDCS.
 */
package org.sysu.workflow;

import org.sysu.workflow.instanceTree.RInstanceTree;
import org.sysu.workflow.invoke.Invoker;
import org.sysu.workflow.model.*;
import org.sysu.workflow.semantics.SCXMLSemanticsImpl;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.sysu.workflow.instanceTree.InstanceManager;
import org.sysu.workflow.instanceTree.RTreeNode;


import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * <p>The SCXML &quot;engine&quot; that executes SCXML documents. The
 * particular semantics used by this engine for executing the SCXML are
 * encapsulated in the SCXMLSemantics implementation that it uses.</p>
 *
 * <p>The default implementation is
 * <code>org.apache.commons.scxml.semantics.SCXMLSemanticsImpl</code></p>
 *
 * Modified by Rinkako, for extending Instance Tree support.
 *
 * @see SCXMLSemantics
 */
public class SCXMLExecutor implements SCXMLIOProcessor {

    /**
     * The Logger for the SCXMLExecutor.
     */
    private Log log = LogFactory.getLog(SCXMLExecutor.class);

    /**
     * Parent SCXMLExecutor
     */
    private SCXMLExecutor parentSCXMLExecutor;

    /**
     * Interpretation semantics.
     */
    private SCXMLSemantics semantics;

    /**
     * The state machine execution context.
     */
    private SCXMLExecutionContext exctx;

    /**
     * The index of this executor in application.
     */
    private String executorIndex;

    /**
     * Tree Node (Current Executor) global id.
     */
    public String NodeId = String.format("BONode_%s", UUID.randomUUID().toString());

    /**
     * Root executor global id.
     */
    public String RootNodeId = "";

    /**
     * Process for this executor runtime record id.
     */
    public String Rtid = "";

    /**
     * Process global id.
     */
    public String Pid = "";

    /**
     * The external event queue.
     */
    private final Queue<TriggerEvent> externalEventQueue = new ConcurrentLinkedQueue<TriggerEvent>();

    /**
     * Convenience constructor.
     */
    public SCXMLExecutor() {
        this(null, null, null, null);
    }

    /**
     * Constructor.
     *
     * @param expEvaluator The expression evaluator
     * @param evtDisp      The event dispatcher
     * @param errRep       The error reporter
     */
    public SCXMLExecutor(final Evaluator expEvaluator,
                         final EventDispatcher evtDisp, final ErrorReporter errRep) {
        this(expEvaluator, evtDisp, errRep, null);
    }

    /**
     * Constructor.
     *
     * @param expEvaluator The expression evaluator
     * @param evtDisp      The event dispatcher
     * @param errRep       The error reporter
     * @param semantics    The SCXML semantics
     */
    public SCXMLExecutor(final Evaluator expEvaluator,
                         final EventDispatcher evtDisp, final ErrorReporter errRep,
                         final SCXMLSemantics semantics) {
        this.semantics = semantics != null ? semantics : new SCXMLSemanticsImpl();
        this.exctx = new SCXMLExecutionContext(this, expEvaluator, evtDisp, errRep);
        this.exctx.Tid = this.NodeId;
        this.exctx.RootTid = this.RootNodeId;
    }

    /**
     * Constructor.
     *
     * @param expEvaluator The expression evaluator
     * @param evtDisp      The event dispatcher
     * @param errRep       The error reporter
     * @param semantics    The SCXML semantics
     * @param rootNodeId      Root state machine id
     */
    public SCXMLExecutor(final Evaluator expEvaluator,
                         final EventDispatcher evtDisp, final ErrorReporter errRep,
                         final SCXMLSemantics semantics,
                         final String rootNodeId) {
        this.semantics = semantics != null ? semantics : new SCXMLSemanticsImpl();
        this.exctx = new SCXMLExecutionContext(this, expEvaluator, evtDisp, errRep);
        this.RootNodeId = rootNodeId;
        this.exctx.Tid = this.NodeId;
        this.exctx.RootTid = this.RootNodeId;
    }

    /**
     * Constructor using a parent SCXMLExecutor
     *
     * @param parentSCXMLExecutor the parent SCXMLExecutor
     */
    public SCXMLExecutor(final SCXMLExecutor parentSCXMLExecutor) {
        this.parentSCXMLExecutor = parentSCXMLExecutor;
        this.semantics = parentSCXMLExecutor.semantics;
        this.exctx = new SCXMLExecutionContext(this, parentSCXMLExecutor.getEvaluator(),
                parentSCXMLExecutor.getEventdispatcher(), parentSCXMLExecutor.getErrorReporter());
        this.exctx.Tid = this.NodeId;
        this.exctx.RootTid = this.RootNodeId;
    }

    /**
     * Get parent SCXMLExecutor
     */
    protected SCXMLExecutor getParentSCXMLExecutor() {
        return parentSCXMLExecutor;
    }

    /**
     * Get current status.
     *
     * @return The current Status
     */
    public synchronized Status getStatus() {
        return exctx.getScInstance().getCurrentStatus();
    }

    /**
     * Initializes the state machine with a specific active configuration
     * <p>
     * This will first (re)initialize the current state machine: clearing all variable contexts, histories and current
     * status, and clones the SCXML root datamodel into the root context.
     * </p>
     *
     * @param atomicStateIds The set of atomic state ids for the state machine
     * @throws ModelException when the state machine hasn't been properly configured yet, when an unknown or illegal
     *                        stateId is specified, or when the specified active configuration does not represent a legal configuration.
     * @see SCInstance#initialize()
     * @see SCXMLSemantics#isLegalConfiguration(Set, ErrorReporter)
     */
    public synchronized void setConfiguration(Set<String> atomicStateIds) throws ModelException {
        exctx.initialize();
        Set<EnterableState> states = new HashSet<EnterableState>();
        for (String stateId : atomicStateIds) {
            TransitionTarget tt = getStateMachine().getTargets().get(stateId);
            if (tt instanceof EnterableState && ((EnterableState) tt).isAtomicState()) {
                EnterableState es = (EnterableState) tt;
                while (es != null && !states.add(es)) {
                    es = es.getParent();
                }
            } else {
                throw new ModelException("Illegal atomic stateId " + stateId + ": state unknown or not an atomic state");
            }
        }
        if (semantics.isLegalConfiguration(states, getErrorReporter())) {
            for (EnterableState es : states) {
                exctx.getScInstance().getStateConfiguration().enterState(es);
            }
            logState();
        } else {
            throw new ModelException("Illegal state machine configuration!");
        }
    }

    /**
     * Set or replace the expression evaluator
     * <p>
     * If the state machine instance has been initialized before, it will be initialized again, destroying all existing
     * state!
     * </p>
     * <p>
     * Also the external event queue will be cleared.
     * </p>
     *
     * @param evaluator The evaluator to set
     * @throws ModelException if attempting to set a null value or the state machine instance failed to re-initialize
     */
    public void setEvaluator(final Evaluator evaluator) throws ModelException {
        exctx.setEvaluator(evaluator);
    }

    /**
     * Get current binding evaluator.
     *
     * @return Evaluator The evaluator in use.
     */
    public Evaluator getEvaluator() {
        return exctx.getEvaluator();
    }

    /**
     * Get the root context for the state machine execution.
     * <p>
     * The root context can be used for providing external data to the state machine
     * </p>
     *
     * @return Context The root context.
     */
    public Context getRootContext() {
        return exctx.getScInstance().getRootContext();
    }

    /**
     * Get the global context for the state machine execution.
     * <p>
     * The global context is the top level context within the state machine itself and should be regarded and treated
     * "read-only" from external usage.
     * </p>
     *
     * @return Context The global context.
     */
    public Context getGlobalContext() {
        return exctx.getScInstance().getGlobalContext();
    }

    /**
     * Set the root context for the state machine execution.
     * <b>NOTE:</b> Should only be used before the executor is set in motion.
     *
     * @param rootContext The Context that ties to the host environment.
     */
    public void setRootContext(final Context rootContext) {
        exctx.getScInstance().setRootContext(rootContext);
    }

    /**
     * Set if share context.
     *
     * @param singleContext is not share context
     * @throws ModelException
     */
    public void setSingleContext(boolean singleContext) throws ModelException {
        getSCInstance().setSingleContext(singleContext);
    }

    /**
     * Check if shared context.
     *
     * @return
     */
    public boolean isSingleContext() {
        return getSCInstance().isSingleContext();
    }

    /**
     * Get the state machine that is being executed.
     * <b>NOTE:</b> This is the state machine definition or model used by this
     * executor instance. It may be shared across multiple executor instances
     * and should not be altered once in use. Also note that
     * manipulation of instance data for the executor should happen through
     * its root context or state contexts only, never through the direct
     * manipulation of any {@link Datamodel}s associated with this state
     * machine definition.
     * <p/>
     *
     * @return Returns the stateMachine.
     */
    public SCXML getStateMachine() {
        return exctx.getStateMachine();
    }

    /**
     * Set or replace the state machine to be executed
     * <p>
     * If the state machine instance has been initialized before, it will be initialized again, destroying all existing
     * state!
     * </p>
     * <p>
     * Also the external event queue will be cleared.
     * </p>
     *
     * @param stateMachine The state machine to set
     * @throws ModelException if attempting to set a null value or the state machine instance failed to re-initialize
     */
    public void setStateMachine(final SCXML stateMachine) throws ModelException {
        exctx.setStateMachine(semantics.normalizeStateMachine(stateMachine, exctx.getErrorReporter()));
        externalEventQueue.clear();
    }

    /**
     * Get the environment specific error reporter.
     *
     * @return Returns the errorReporter.
     */
    public ErrorReporter getErrorReporter() {
        return exctx.getErrorReporter();
    }

    /**
     * Set or replace the error reporter.
     *
     * @param errorReporter The error reporter to set, if null a SimpleErrorReporter instance will be used instead
     */
    public void setErrorReporter(final ErrorReporter errorReporter) {
        exctx.setErrorReporter(errorReporter);
    }

    /**
     * Get the event dispatcher.
     *
     * @return Returns the eventdispatcher.
     */
    public EventDispatcher getEventdispatcher() {
        return exctx.getEventDispatcher();
    }

    /**
     * Set or replace the event dispatch
     *
     * @param eventdispatcher The event dispatcher to set, if null a SimpleDispatcher instance will be used instead
     */
    public void setEventdispatcher(final EventDispatcher eventdispatcher) {
        exctx.setEventdispatcher(eventdispatcher);
    }

    /**
     * Set if the SCXML configuration should be checked before execution (default = true)
     *
     * @param checkLegalConfiguration flag to set
     */
    public void setCheckLegalConfiguration(boolean checkLegalConfiguration) {
        this.exctx.setCheckLegalConfiguration(checkLegalConfiguration);
    }

    /**
     * @return if the SCXML configuration will be checked before execution
     */
    public boolean isCheckLegalConfiguration() {
        return exctx.isCheckLegalConfiguration();
    }

    /**
     * Get the notification registry.
     *
     * @return The notification registry.
     */
    public NotificationRegistry getNotificationRegistry() {
        return exctx.getNotificationRegistry();
    }

    /**
     * Add a listener to the {@link org.sysu.workflow.model.Observable}.
     * <p/>
     *
     * @param observable The {@link org.sysu.workflow.model.Observable} to attach the listener to.
     * @param listener   The SCXMLListener.
     */
    public void addListener(final org.sysu.workflow.model.Observable observable, final SCXMLListener listener) {
        exctx.getNotificationRegistry().addListener(observable, listener);
    }

    /**
     * Remove this listener from the {@link org.sysu.workflow.model.Observable}.
     *
     * @param observable The {@link org.sysu.workflow.model.Observable}.
     * @param listener   The SCXMLListener to be removed.
     */
    public void removeListener(final org.sysu.workflow.model.Observable observable,
                               final SCXMLListener listener) {
        exctx.getNotificationRegistry().removeListener(observable, listener);
    }

    /**
     * Register an Invoker for this target type.
     *
     * @param type         The target type (specified by "type" attribute of the invoke element).
     * @param invokerClass The Invoker class.
     */
    public void registerInvokerClass(final String type, final Class<? extends Invoker> invokerClass) {
        exctx.registerInvokerClass(type, invokerClass);
    }

    /**
     * Remove the Invoker registered for this target type (if there is one registered).
     * <p/>
     *
     * @param type The target type (specified by "type" attribute of the invoke element).
     */
    public void unregisterInvokerClass(final String type) {
        exctx.unregisterInvokerClass(type);
    }

    /**
     * Detach the current SCInstance to allow external serialization.
     * <p>
     * {@link #attachInstance(SCInstance)} can be used to re-attach a previously detached instance
     * </p>
     * <p>
     * Note: until an instance is re-attached, no operations are allowed (and probably throw exceptions) except
     * for {@link #addEvent(TriggerEvent)} which might still be used (concurrently) by running Invokers, or
     * {@link #hasPendingEvents()} to check for possible pending events.
     * </p>
     *
     * @return the detached instance
     */
    public SCInstance detachInstance() {
        return exctx.detachInstance();
    }

    /**
     * Re-attach a previously detached SCInstance.
     * <p>
     * Note: an already attached instance will get overwritten (and thus lost).
     * </p>
     *
     * @param instance An previously detached SCInstance
     */
    public void attachInstance(SCInstance instance) {
        exctx.attachInstance(instance);
    }

    /**
     * @return Returns true if the state machine is running
     */
    public boolean isRunning() {
        return exctx.isRunning();
    }

    /**
     * Begin execute this state machine.
     *
     * @throws ModelException in case there is a fatal SCXML object
     *                        model problem.
     */
    public void go() throws ModelException {
        // register a new instance tree if this state-machine is the root one
        try {
            if (this.RootNodeId.equals("") || this.RootNodeId.equals(this.NodeId)) {
                RInstanceTree myTree = new RInstanceTree();
                RTreeNode nRoot = new RTreeNode(this.exctx.getStateMachine().getName(), this.NodeId, this.exctx, null);
                myTree.SetRoot(nRoot);
                InstanceManager.RegisterInstanceTree(this.Rtid, myTree);
                this.RootNodeId = this.NodeId;
                this.exctx.RootTid = this.RootNodeId;
            }
        } catch (Exception e) {
            System.out.println("Executor error at go");
            e.printStackTrace();
        }
        // same as reset
        this.reset();
    }

    /**
     * Reset the state machine and clear all events.
     *
     * @throws ModelException if the state machine instance failed to initialize
     */
    public void reset() throws ModelException {
        // clear any pending external events
        externalEventQueue.clear();

        // go
        semantics.firstStep(exctx);
        logState();
    }

    /**
     * Add a new external event, which may be done concurrently, and even when the current SCInstance is detached.
     * <p>
     * No processing of the vent will be done, until the next triggerEvent methods is invoked.
     * </p>
     *
     * @param evt an external event
     */
    public void addEvent(final TriggerEvent evt) {
        if (evt != null) {
            externalEventQueue.add(evt);
        }
    }

    /**
     * Check if any pending event.
     */
    public boolean hasPendingEvents() {
        return !externalEventQueue.isEmpty();
    }

    /**
     * Get pending event number.
     */
    public int getPendingEvents() {
        return externalEventQueue.size();
    }

    /**
     * Trigger a external event.
     *
     * @param evt the external events which triggered during the last
     *            time quantum
     * @throws ModelException in case there is a fatal SCXML object
     *                        model problem.
     */
    public void triggerEvent(final TriggerEvent evt)
            throws ModelException {
        //add a TriggerEvent into the external event queue
        addEvent(evt);
        //trigger all the TriggerEvents in the extrnal event queue
        triggerEvents();
    }

    /**
     * The worker method.
     * Re-evaluates current status whenever any events are triggered.
     *
     * @param evts an array of external events which triggered during the last
     *             time quantum
     * @throws ModelException in case there is a fatal SCXML object
     *                        model problem.
     */
    public void triggerEvents(final TriggerEvent[] evts)
            throws ModelException {
        if (evts != null) {
            for (TriggerEvent evt : evts) {
                addEvent(evt);
            }
        }
        triggerEvents();
    }

    /**
     * Trigger all pending and incoming events, until there are no more pending events.
     *
     * @throws ModelException in case there is a fatal SCXML object model problem.
     */
    public void triggerEvents() throws ModelException {
        TriggerEvent evt;
        while (exctx.isRunning() && (evt = externalEventQueue.poll()) != null) {
            eventStep(evt);
        }
    }

    /**
     * Go to next event step.
     *
     * @param event trigger event
     * @throws ModelException
     */
    protected void eventStep(TriggerEvent event) throws ModelException {
        semantics.nextStep(exctx, event);
        logState();
    }

    /**
     * Get binding SCInstance.
     *
     * @return The SCInstance for this executor.
     */
    protected SCInstance getSCInstance() {
        return exctx.getScInstance();
    }

    /**
     * Log current active state.
     */
    protected void logState() {

        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("Current States: [ ");
            for (EnterableState es : getStatus().getStates()) {
                sb.append(es.getId()).append(", ");
            }
            int length = sb.length();
            sb.delete(length - 2, length).append(" ]");
            log.debug(sb.toString());
        }
    }

    public String getExecutorIndex() {
        return executorIndex;
    }

    public void setExecutorIndex(String executorIndex) {
        this.executorIndex = executorIndex;
    }

    public SCXMLExecutionContext getExctx() {
        return exctx;
    }

    public String getRtid() {
        return Rtid;
    }

    public void setRtid(String rtid) {
        Rtid = rtid;
        this.exctx.Rtid = Rtid;
    }

    public String getPid() {
        return Pid;
    }

    public void setPid(String pid) {
        Pid = pid;
        this.exctx.Pid = Pid;
    }
}