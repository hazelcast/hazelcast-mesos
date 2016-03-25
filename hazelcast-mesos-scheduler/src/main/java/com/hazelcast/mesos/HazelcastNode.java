package com.hazelcast.mesos;

/**
 * date: 3/25/16
 * author: emindemirci
 */
public class HazelcastNode {
    String hostname;
    String slaveId;
    String taskId;

    public HazelcastNode(String hostname, String slaveId, String taskId) {
        this.hostname = hostname;
        this.slaveId = slaveId;
        this.taskId = taskId;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getSlaveId() {
        return slaveId;
    }

    public void setSlaveId(String slaveId) {
        this.slaveId = slaveId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public String toString() {
        return "HazelcastNode{" +
                "taskId='" + taskId + '\'' +
                ", slaveId='" + slaveId + '\'' +
                ", hostname='" + hostname + '\'' +
                '}';
    }
}
