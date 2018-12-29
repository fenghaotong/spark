package com.htfeng.sparkproject.domain;

import java.io.Serializable;

/**
 * 任务
 * @author htfeng
 *
 */
public class Task implements Serializable{

    private static final long serialVersionUID = 8090412787105099135L;
    
    private long taskid;
    private String taskName;
    private String createTime;
    private String startTime;
    private String finishTime;
    private String taskType;
    private String taskStatus;
    private String taskParam;
    public long getTaskid() {
        return taskid;
    }
    public String getTaskName() {
        return taskName;
    }
    public String getCreateTime() {
        return createTime;
    }
    public String getStartTime() {
        return startTime;
    }
    public String getFinishTime() {
        return finishTime;
    }
    public String getTaskType() {
        return taskType;
    }
    public String getTaskStatus() {
        return taskStatus;
    }
    public String getTaskParam() {
        return taskParam;
    }
    public void setTaskid(long taskid) {
        this.taskid = taskid;
    }
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    public void setFinishTime(String finishTime) {
        this.finishTime = finishTime;
    }
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }
    public void setTaskParam(String taskParam) {
        this.taskParam = taskParam;
    }
    
    
}
