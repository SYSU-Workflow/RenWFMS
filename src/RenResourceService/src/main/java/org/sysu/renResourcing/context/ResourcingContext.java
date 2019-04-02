package org.sysu.renResourcing.context;

import org.sysu.renCommon.utility.CommonUtil;
import org.sysu.renCommon.utility.SerializationUtil;
import org.sysu.renCommon.utility.TimestampUtil;
import org.sysu.renCommon.enums.RServiceType;
import org.sysu.renCommon.entity.RenRsrecordEntity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Hashtable;

/**
 * Author: Rinkako
 * Date  : 2017/2/5
 * Usage : Task context is an encapsulation of RenRsrecordEntity in a
 * convenient way for resourcing service.
 */
public class ResourcingContext implements Comparable<ResourcingContext>, Serializable, RCacheablesContext {
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Resourcing record global id.
     */
    private String rstid;

    /**
     * Process runtime record id.
     */
    private String rtid;

    /**
     * Priority, bigger schedule faster
     */
    private int priority = 0;

    /**
     * Execution duration time span in MS.
     */
    private long executionTimespan = 0L;

    /**
     * Service type enum.
     */
    private RServiceType service;

    /**
     * Service argument dictionary.
     */
    private Hashtable<String, Object> argsDict;

    /**
     * Timestamp of request received.
     */
    private Timestamp receivedTimestamp;

    /**
     * Timestamp of request scheduled.
     */
    private Timestamp scheduledTimestamp;

    /**
     * Timestamp of request finished.
     */
    private Timestamp finishTimestamp;

    /**
     * Success flag.
     */
    private int isSucceed;

    /**
     * Execution result descriptor, not save to entity.
     */
    private String executionResult;

    /**
     * Get the resourcing request global id.
     *
     * @return resourcing record id string
     */
    public String getRstid() {
        return rstid;
    }

    /**
     * Get process runtime record global id.
     *
     * @return process rtid string
     */
    public String getRtid() {
        return rtid;
    }

    /**
     * Get the priority of resourcing request.
     * Bigger schedule faster.
     *
     * @return priority int, default `0`
     */
    public int getPriority() {
        return this.priority;
    }

    /**
     * Get service enum type of this request.
     *
     * @return service enum type
     */
    public RServiceType getService() {
        return this.service;
    }

    /**
     * Get argument dict of this request.
     *
     * @return argument HashTable
     */
    public Hashtable<String, Object> getArgs() {
        return this.argsDict;
    }

    /**
     * Get the execution time span in MS.
     *
     * @return execution time span ms long integer
     */
    public long getExecutionTimespan() {
        return this.executionTimespan;
    }

    /**
     * Set the execution time span in MS.
     *
     * @param executionTimespan time span ms long integer
     */
    public void setExecutionTimespan(long executionTimespan) {
        this.executionTimespan = executionTimespan;
    }

    /**
     * Get request received timestamp.
     *
     * @return timestamp
     */
    public Timestamp getReceivedTimestamp() {
        return this.receivedTimestamp;
    }

    /**
     * Get request scheduled timestamp.
     *
     * @return timestamp
     */
    public Timestamp getScheduledTimestamp() {
        return this.scheduledTimestamp;
    }

    /**
     * Get request finish timestamp.
     *
     * @return timestamp
     */
    public Timestamp getFinishTimestamp() {
        return this.finishTimestamp;
    }

    /**
     * Set resourcing request is scheduled.
     */
    public void SetScheduled() {
        this.scheduledTimestamp = TimestampUtil.GetCurrentTimestamp();
    }

    /**
     * Set resourcing request is finished.
     */
    public void SetFinish() {
        this.finishTimestamp = TimestampUtil.GetCurrentTimestamp();
        this.executionTimespan = this.finishTimestamp.getTime() - this.scheduledTimestamp.getTime();
    }

    /**
     * Set success flag.
     *
     * @param isSucceed 1 for succeed, 0 for failed
     */
    public void setIsSucceed(int isSucceed) {
        this.isSucceed = isSucceed;
    }

    /**
     * Get the execution result.
     *
     * @return result descriptor string.
     */
    public String getExecutionResult() {
        return this.executionResult;
    }

    /**
     * Set the execution result.
     *
     * @param executionResult result descriptor string.
     */
    public void setExecutionResult(String executionResult) {
        this.executionResult = executionResult;
    }

    /**
     * Get the execution success flag.
     *
     * @return
     */
    public int getIsSucceed() {
        return this.isSucceed;
    }

    /**
     * Create a new resourcing request context.
     * Private constructor for preventing create outside.
     *
     * @param rstid    resourcing request id
     * @param rtid     process rtid
     * @param service  service descriptor
     * @param argsDict service argument dictionary
     */
    private ResourcingContext(String rstid, String rtid, String service, Hashtable<String, Object> argsDict) {
        this.rstid = rstid;
        this.rtid = rtid;
        this.service = RServiceType.valueOf(service);
        this.argsDict = argsDict;
    }

    /**
     * Generate a resourcing context by a entity entity.
     *
     * @param rsrecordEntity RS Record entity
     * @return equivalent resourcing context.
     */
    @SuppressWarnings("unchecked")
    public static ResourcingContext GenerateResourcingContext(RenRsrecordEntity rsrecordEntity) {
        assert rsrecordEntity != null;
        String argdDescriptor = rsrecordEntity.getArgs();
        Hashtable<String, Object> dict;
        if (CommonUtil.IsNullOrEmpty(argdDescriptor)) {
            dict = new Hashtable<>();
        } else {
            dict = SerializationUtil.JsonDeserialization(rsrecordEntity.getArgs(), Hashtable.class);
        }
        ResourcingContext context = new ResourcingContext(rsrecordEntity.getRstid(),
                rsrecordEntity.getRtid(), rsrecordEntity.getService(), dict);
        context.receivedTimestamp = rsrecordEntity.getReceiveTimestamp();
        context.scheduledTimestamp = rsrecordEntity.getScheduledTimestamp();
        context.finishTimestamp = rsrecordEntity.getFinishTimestamp();
        context.isSucceed = rsrecordEntity.getIsSucceed();
        return context;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(ResourcingContext o) {
        return Integer.compare(this.priority, o.priority);
    }
}
