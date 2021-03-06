/*
 * Project Ren @ 2018
 * Rinkako, Ariana, Gordan. SYSU SDCS.
 */
package org.sysu.renCommon.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Author: Rinkako
 * Date  : 2018/3/1
 * Usage :
 */
@Entity
@Table(name = "ren_workitem", schema = "renboengine")
public class RenWorkitemEntity {
    private String wid;
    private String rtid;
    private String resourcingId;
    private String processId;
    private String boId;
    private String taskid;
    private String taskPolymorphismId;
    private String arguments;
    private Timestamp firingTime;
    private Timestamp enablementTime;
    private Timestamp startTime;
    private Timestamp completionTime;
    private String status;
    private String resourceStatus;
    private String startedBy;
    private String completedBy;
    private String timertrigger;
    private String timerexpiry;
    private Timestamp latestStartTime;
    private long executeTime;
    private String callbackNodeId;

    @Id
    @Column(name = "wid", nullable = false, length = 64)
    public String getWid() {
        return wid;
    }

    public void setWid(String wid) {
        this.wid = wid;
    }

    @Basic
    @Column(name = "rtid", nullable = false, length = 64)
    public String getRtid() {
        return rtid;
    }

    public void setRtid(String rtid) {
        this.rtid = rtid;
    }

    @Basic
    @Column(name = "resourcing_id", nullable = false, length = 64)
    public String getResourcingId() {
        return resourcingId;
    }

    public void setResourcingId(String resourcingId) {
        this.resourcingId = resourcingId;
    }

    @Basic
    @Column(name = "process_id", nullable = false, length = 64)
    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    @Basic
    @Column(name = "bo_id", nullable = false, length = 64)
    public String getBoId() {
        return boId;
    }

    public void setBoId(String boId) {
        this.boId = boId;
    }

    @Basic
    @Column(name = "taskid", nullable = false)
    public String getTaskid() {
        return taskid;
    }

    public void setTaskid(String taskid) {
        this.taskid = taskid;
    }

    @Basic
    @Column(name = "task_polymorphism_id", nullable = false)
    public String getTaskPolymorphismId() {
        return taskPolymorphismId;
    }

    public void setTaskPolymorphismId(String taskPolymorphismId) {
        this.taskPolymorphismId = taskPolymorphismId;
    }

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "arguments", columnDefinition = "Text")
    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    @Basic
    @Column(name = "firing_time")
    public Timestamp getFiringTime() {
        return firingTime;
    }

    public void setFiringTime(Timestamp firingTime) {
        this.firingTime = firingTime;
    }

    @Basic
    @Column(name = "enablement_time")
    public Timestamp getEnablementTime() {
        return enablementTime;
    }

    public void setEnablementTime(Timestamp enablementTime) {
        this.enablementTime = enablementTime;
    }

    @Basic
    @Column(name = "start_time")
    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    @Basic
    @Column(name = "completion_time")
    public Timestamp getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(Timestamp completionTime) {
        this.completionTime = completionTime;
    }

    @Basic
    @Column(name = "status", length = 128)
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Basic
    @Column(name = "resource_status", length = 128)
    public String getResourceStatus() {
        return resourceStatus;
    }

    public void setResourceStatus(String resourceStatus) {
        this.resourceStatus = resourceStatus;
    }

    @Basic
    @Column(name = "started_by", length = 64)
    public String getStartedBy() {
        return startedBy;
    }

    public void setStartedBy(String startedBy) {
        this.startedBy = startedBy;
    }

    @Basic
    @Column(name = "completed_by", length = 64)
    public String getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(String completedBy) {
        this.completedBy = completedBy;
    }

    @Basic
    @Column(name = "timertrigger", length = 64)
    public String getTimertrigger() {
        return timertrigger;
    }

    public void setTimertrigger(String timertrigger) {
        this.timertrigger = timertrigger;
    }

    @Basic
    @Column(name = "timerexpiry", length = 64)
    public String getTimerexpiry() {
        return timerexpiry;
    }

    public void setTimerexpiry(String timerexpiry) {
        this.timerexpiry = timerexpiry;
    }

    @Basic
    @Column(name = "latest_start_time")
    public Timestamp getLatestStartTime() {
        return latestStartTime;
    }

    public void setLatestStartTime(Timestamp latestStartTime) {
        this.latestStartTime = latestStartTime;
    }

    @Basic
    @Column(name = "execute_time", nullable = false)
    public long getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(long executeTime) {
        this.executeTime = executeTime;
    }

    @Basic
    @Column(name = "callback_node_id", nullable = false, length = 64)
    public String getCallbackNodeId() {
        return callbackNodeId;
    }

    public void setCallbackNodeId(String callbackNodeId) {
        this.callbackNodeId = callbackNodeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RenWorkitemEntity that = (RenWorkitemEntity) o;
        return executeTime == that.executeTime &&
                Objects.equals(wid, that.wid) &&
                Objects.equals(rtid, that.rtid) &&
                Objects.equals(resourcingId, that.resourcingId) &&
                Objects.equals(processId, that.processId) &&
                Objects.equals(boId, that.boId) &&
                Objects.equals(taskid, that.taskid) &&
                Objects.equals(taskPolymorphismId, that.taskPolymorphismId) &&
                Objects.equals(arguments, that.arguments) &&
                Objects.equals(firingTime, that.firingTime) &&
                Objects.equals(enablementTime, that.enablementTime) &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(completionTime, that.completionTime) &&
                Objects.equals(status, that.status) &&
                Objects.equals(resourceStatus, that.resourceStatus) &&
                Objects.equals(startedBy, that.startedBy) &&
                Objects.equals(completedBy, that.completedBy) &&
                Objects.equals(timertrigger, that.timertrigger) &&
                Objects.equals(timerexpiry, that.timerexpiry) &&
                Objects.equals(latestStartTime, that.latestStartTime) &&
                Objects.equals(callbackNodeId, that.callbackNodeId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(wid, rtid, resourcingId, processId, boId, taskid, taskPolymorphismId, arguments, firingTime, enablementTime, startTime, completionTime, status, resourceStatus, startedBy, completedBy, timertrigger, timerexpiry, latestStartTime, executeTime, callbackNodeId);
    }
}