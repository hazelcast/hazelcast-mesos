package com.hazelcast.mesos.scheduler;

import com.hazelcast.mesos.HazelcastNode;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.mesos.Protos;
import org.apache.mesos.Scheduler;
import org.apache.mesos.SchedulerDriver;

import static com.hazelcast.mesos.util.Util.command;
import static com.hazelcast.mesos.util.Util.resource;
import static com.hazelcast.mesos.util.Util.uris;


public class HazelcastScheduler implements Scheduler {

    private URI httpServerURI;
    private volatile int targetNumberOfNodes;

    private Set<HazelcastNode> activeNodes = new HashSet<HazelcastNode>();

    public HazelcastScheduler(URI httpServerURI, int targetNumberOfNodes) {
        this.httpServerURI = httpServerURI;
        this.targetNumberOfNodes = targetNumberOfNodes;
    }

    @Override
    public void registered(SchedulerDriver schedulerDriver, Protos.FrameworkID frameworkID, Protos.MasterInfo masterInfo) {
        System.out.println("HazelcastScheduler.registered");
        System.out.println("schedulerDriver = [" + schedulerDriver + "], frameworkID = [" + frameworkID + "], masterInfo = [" + masterInfo + "]");
    }

    @Override
    public void reregistered(SchedulerDriver schedulerDriver, Protos.MasterInfo masterInfo) {
        System.out.println("HazelcastScheduler.reregistered");
        System.out.println("schedulerDriver = [" + schedulerDriver + "], masterInfo = [" + masterInfo + "]");

    }

    @Override
    public void resourceOffers(SchedulerDriver schedulerDriver, List<Protos.Offer> list) {
        System.out.println("HazelcastScheduler.resourceOffers");
        Collection<Protos.OfferID> accepted = new ArrayList<Protos.OfferID>();
        Collection<Protos.TaskInfo> tasks = new ArrayList<Protos.TaskInfo>();
        System.out.println("targetNumberOfNodes = " + targetNumberOfNodes);
        System.out.println("currentNumberOfNodes = " + activeNodes.size());
        for (Protos.Offer offer : list) {
            Protos.SlaveID slaveId = offer.getSlaveId();
            System.out.println("slaveId = " + slaveId);
            System.out.println("----");
            System.out.println(activeNodes);
            if (needMoreNodes() && !slaveContainsHazelcastNode(slaveId)) {
                System.out.println("launching executor on slaveId = " + slaveId);
                accepted.add(offer.getId());
                Protos.TaskID.Builder taskId = Protos.TaskID.newBuilder().setValue("node-" + slaveId.getValue());
                Protos.TaskInfo.Builder builder = Protos.TaskInfo.newBuilder()
                        .setSlaveId(slaveId)
                        .setName("hazelcast node")
                        .addResources(resource("cpus", 0.1))
                        .addResources(resource("mem", 512.0))
                        .setTaskId(taskId);

                Protos.ExecutorInfo executor = buildExecutor();
                Protos.TaskInfo taskInfo = builder.setExecutor(executor).build();
                tasks.add(taskInfo);
                schedulerDriver.launchTasks(accepted, tasks);
                HazelcastNode hazelcastNode = new HazelcastNode(offer.getHostname(), slaveId.getValue(), taskId.getValue());
                activeNodes.add(hazelcastNode);
            } else if (needLessNodes() && slaveContainsHazelcastNode(slaveId)) {
                Protos.TaskID taskID = Protos.TaskID.newBuilder().setValue("node-" + slaveId.getValue()).build();
                schedulerDriver.killTask(taskID);
                removeNodeFromActiveNodes(taskID);
            } else {
                schedulerDriver.declineOffer(offer.getId());
            }
        }
    }

    private boolean needLessNodes() {
        return activeNodes.size() > targetNumberOfNodes;
    }

    private boolean needMoreNodes() {
        return activeNodes.size() < targetNumberOfNodes;
    }

    private void removeNodeFromActiveNodes(Protos.TaskID taskID) {
        Iterator<HazelcastNode> iterator = activeNodes.iterator();
        while (iterator.hasNext()) {
            HazelcastNode node = iterator.next();
            if (node.getTaskId().equals(taskID.getValue())) {
                iterator.remove();
            }
        }
    }

    private boolean slaveContainsHazelcastNode(Protos.SlaveID slaveId) {
        for (HazelcastNode node : activeNodes) {
            if (node.getSlaveId().equals(slaveId.getValue())) {
                return true;
            }
        }
        return false;
    }

    private Protos.ExecutorInfo buildExecutor() {
        return Protos.ExecutorInfo.newBuilder()
                .setName("Hazelcast Executor")
                .setExecutorId(Protos.ExecutorID.newBuilder().setValue("HazelcastExecutor - " + System.currentTimeMillis()))
                .setCommand(command("java -cp hazelcast-mesos-executor.jar com.hazelcast.mesos.executor.HazelcastExecutor",
                        uris(
                                getFileURI("hazelcast-3.6.zip"),
                                getFileURI("hazelcast-mesos-executor.jar"),
                                getFileURI("hazelcast-zookeeper.jar"),
                                getFileURI("hazelcast.xml")
                        )))
                .setSource("java")
                .build();
    }

    private String getFileURI(String file) {
        return httpServerURI.toString() + file;
    }

    @Override
    public void offerRescinded(SchedulerDriver schedulerDriver, Protos.OfferID offerID) {
        System.out.println("HazelcastScheduler.offerRescinded");
        System.out.println("schedulerDriver = [" + schedulerDriver + "], offerID = [" + offerID + "]");

    }

    @Override
    public void statusUpdate(SchedulerDriver schedulerDriver, Protos.TaskStatus taskStatus) {
        System.out.println("HazelcastScheduler.statusUpdate");
        System.out.println("schedulerDriver = [" + schedulerDriver + "], taskStatus = [" + taskStatus + "]");

        Protos.TaskState state = taskStatus.getState();
        Protos.TaskStatus.Reason reason = taskStatus.getReason();
        if (state == Protos.TaskState.TASK_LOST && reason == Protos.TaskStatus.Reason.REASON_INVALID_OFFERS) {
            removeNodeFromActiveNodes(taskStatus.getTaskId());
        } else if ( state == Protos.TaskState.TASK_FAILED) {
            removeNodeFromActiveNodes(taskStatus.getTaskId());
        }

    }

    @Override
    public void frameworkMessage(SchedulerDriver schedulerDriver, Protos.ExecutorID executorID, Protos.SlaveID slaveID, byte[] bytes) {
        System.out.println("HazelcastScheduler.frameworkMessage");
        System.out.println("schedulerDriver = [" + schedulerDriver + "], executorID = [" + executorID + "], slaveID = [" + slaveID + "], bytes = [" + bytes + "]");

    }

    @Override
    public void disconnected(SchedulerDriver schedulerDriver) {
        System.out.println("HazelcastScheduler.disconnected");
        System.out.println("schedulerDriver = [" + schedulerDriver + "]");

    }

    @Override
    public void slaveLost(SchedulerDriver schedulerDriver, Protos.SlaveID slaveID) {
        System.out.println("HazelcastScheduler.slaveLost");
        System.out.println("schedulerDriver = [" + schedulerDriver + "], slaveID = [" + slaveID + "]");
    }

    @Override
    public void executorLost(SchedulerDriver schedulerDriver, Protos.ExecutorID executorID, Protos.SlaveID slaveID, int i) {
        System.out.println("HazelcastScheduler.executorLost");
        System.out.println("schedulerDriver = [" + schedulerDriver + "], executorID = [" + executorID + "], slaveID = [" + slaveID + "], i = [" + i + "]");

    }

    @Override
    public void error(SchedulerDriver schedulerDriver, String s) {
        System.out.println("HazelcastScheduler.error");
        System.out.println("schedulerDriver = [" + schedulerDriver + "], s = [" + s + "]");
    }

    public void setTargetNumberOfNodes(int targetNumberOfNodes) {
        this.targetNumberOfNodes = targetNumberOfNodes;
    }
}
