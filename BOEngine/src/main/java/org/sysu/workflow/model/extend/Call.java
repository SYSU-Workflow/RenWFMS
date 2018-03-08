/*
 * Project Ren @ 2018
 * Rinkako, Ariana, Gordan. SYSU SDCS.
 */
package org.sysu.workflow.model.extend;

import org.sysu.renCommon.enums.LogLevelType;
import org.sysu.renCommon.interactionRouter.LocationContext;
import org.sysu.workflow.*;
import org.sysu.workflow.model.EnterableState;
import org.sysu.workflow.model.ModelException;
import org.sysu.workflow.model.Param;
import org.sysu.workflow.model.ParamsContainer;
import org.sysu.workflow.utility.LogUtil;
import org.sysu.workflow.utility.SerializationUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Rinkako
 * Date  : 2017/3/8
 * Usage : Label context of Call.
 */
public class Call extends ParamsContainer implements Serializable {
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The name of the task or the sub process to call
     */
    private String name;

    /**
     * How many task instances ought to be create
     */
    private int instances = 1;

    /**
     * Get the value of name
     *
     * @return the task name to call
     */
    public String getName() {
        return name;
    }

    /**
     * Set the value of name
     *
     * @param name the task name to call
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the value of instance
     *
     * @return value of instance property
     */
    public int getInstances() {
        return instances;
    }

    /**
     * Set the value of instance
     *
     * @param instances the instance value to set, represent how many task instances ought to be created
     */
    public void setInstances(int instances) {
        this.instances = instances;
    }

    /**
     * Execute Call, send request to resource service.
     *
     * @param exctx The ActionExecutionContext for this execution instance
     * @throws ModelException
     * @throws SCXMLExpressionException
     */
    @Override
    public void execute(ActionExecutionContext exctx) throws ModelException, SCXMLExpressionException {
        if (GlobalContext.IsLocalDebug) {
            LogUtil.Echo(">>> CALL: " + this.name, Call.class.getName(), LogLevelType.INFO);
        }
        SCXMLExecutionContext scxmlExecContext = (SCXMLExecutionContext) exctx.getInternalIOProcessor();
        EnterableState parentState = getParentEnterableState();
        Context ctx = exctx.getContext(parentState);
        ctx.setLocal(getNamespacesKey(), getNamespaces());
        Map<String, Object> payloadDataMap = new LinkedHashMap();
        addParamsToPayload(exctx, payloadDataMap);
        Tasks tasks = scxmlExecContext.getSCXMLExecutor().getStateMachine().getTasks();
        if (tasks != null) {
            List<Task> taskList = tasks.getTaskList();
            List<SubProcess> processList = tasks.getProcessList();
            boolean successFlag = false;
            if (!taskList.isEmpty()) {
                for (Task t : taskList) {
                    if (t.getName().equals(this.name)) {
                        // generate arguments json
                        StringBuilder sb = new StringBuilder();
                        sb.append("{");
                        for (Param p : this.getParams()) {
                            sb.append(p.GenerateDescriptor()).append(",");
                        }
                        String jsonifyParam = sb.toString();
                        if (jsonifyParam.length() > 1) {
                            jsonifyParam = jsonifyParam.substring(0, jsonifyParam.length() - 1);
                        }
                        jsonifyParam += "}";
                        // send to RS
                        HashMap<String, String> args = new HashMap<String, String>();
                        args.put("taskname", this.name);
                        args.put("boname", scxmlExecContext.getSCXMLExecutor().getStateMachine().getName());
                        args.put("nodeId", scxmlExecContext.getSCXMLExecutor().NodeId);
                        args.put("args", jsonifyParam);
                        args.put("rtid", scxmlExecContext.Rtid);
                        if (!GlobalContext.IsLocalDebug) {
                            int timesBorder = this.instances;
                            if (t.getPrinciple().getMethod().equalsIgnoreCase("Offer")) {
                                timesBorder = 1;
                                LogUtil.Log("Method is Offer but instance not 1 is invalid, use default value 1.",
                                        Call.class.getName(), LogLevelType.WARNING,
                                        ((SCXMLExecutionContext) exctx.getInternalIOProcessor()).Rtid);
                            }
                            for (int times = 0; times < timesBorder; times++) {
                                try {
                                    GlobalContext.Interaction.Send(LocationContext.URL_RS_SUBMITTASK, args, scxmlExecContext.Rtid);
                                } catch (Exception e) {
                                    LogUtil.Log("When submit task to Resource Service, exception occurred, " + e.toString(),
                                            Call.class.getName(), LogLevelType.ERROR, scxmlExecContext.Rtid);
                                }
                            }
                        }
                        successFlag = true;
                        break;
                    }
                }
            } else if (!processList.isEmpty()) {
                for (SubProcess subProcess : processList) {
                    if (subProcess.getName().equals(this.name)) {
                        // generate arguments json
                        StringBuilder sb = new StringBuilder();
                        sb.append("{");
                        for (Param p : this.getParams()) {
                            sb.append(p.GenerateDescriptor()).append(",");
                        }
                        String jsonifyParam = sb.toString();
                        if (jsonifyParam.length() > 1) {
                            jsonifyParam = jsonifyParam.substring(0, jsonifyParam.length() - 1);
                        }
                        jsonifyParam += "}";
                        // send to RS
                        HashMap<String, String> args = new HashMap<String, String>();
                        args.put("taskname", this.name);
                        args.put("boname", scxmlExecContext.getSCXMLExecutor().getStateMachine().getName());
                        args.put("nodeId", scxmlExecContext.getSCXMLExecutor().NodeId);
                        //params of the task
                        args.put("args", jsonifyParam);
                        args.put("rtid", scxmlExecContext.Rtid);
                        if (!GlobalContext.IsLocalDebug) {
                            try {
                                GlobalContext.Interaction.Send(LocationContext.URL_RS_SUBMITTASK, args, scxmlExecContext.Rtid);
                            } catch (Exception e) {
                                LogUtil.Log("When submit task to Resource Service, exception occurred, " + e.toString(),
                                        Call.class.getName(), LogLevelType.ERROR, scxmlExecContext.Rtid);
                            }
                        }
                        successFlag = true;
                        break;
                    }
                }
            }
            if (!successFlag) {
                throw new ModelException();
            }
        } else {
            throw new ModelException();
        }
    }
}
