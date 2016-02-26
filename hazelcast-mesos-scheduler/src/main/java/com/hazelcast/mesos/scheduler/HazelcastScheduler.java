package com.hazelcast.mesos.scheduler;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.mesos.Protos;
import org.apache.mesos.Scheduler;
import org.apache.mesos.SchedulerDriver;

import static com.hazelcast.mesos.util.Util.command;
import static com.hazelcast.mesos.util.Util.osFromSystemProperty;
import static com.hazelcast.mesos.util.Util.resource;
import static com.hazelcast.mesos.util.Util.uris;


public class HazelcastScheduler implements Scheduler {

    boolean hasNodeCreated;
    private URI httpServerURI;

    public HazelcastScheduler(URI httpServerURI) {
        this.httpServerURI = httpServerURI;
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
//        System.out.println("schedulerDriver = [" + schedulerDriver + "], list = [" + list + "]");

        Collection<Protos.OfferID> accepted = new ArrayList<Protos.OfferID>();
        Collection<Protos.TaskInfo> tasks = new ArrayList<Protos.TaskInfo>();
        int id = 0;
        for (Protos.Offer offer : list) {
//            if (!hasNodeCreated) {
            accepted.add(offer.getId());
            Protos.SlaveID slaveId = offer.getSlaveId();
            Protos.TaskInfo.Builder builder = Protos.TaskInfo.newBuilder()
//                        .setCommand(command("./hazelcast-3.6/demo/console.sh"))
                    .setSlaveId(slaveId)
                    .setName("hazelcast node")
                    .addResources(resource("cpus", 0.1))
                    .addResources(resource("mem", 512.0))
                    .setTaskId(Protos.TaskID.newBuilder().setValue("node" + id++));

            Protos.ExecutorInfo executor = buildExecutor();
            Protos.TaskInfo taskInfo = builder.setExecutor(executor).build();
            tasks.add(taskInfo);
            schedulerDriver.launchTasks(accepted, tasks);
            hasNodeCreated = true;
//            } else {
//                schedulerDriver.declineOffer(offer.getId());
//            }

        }
    }

    private Protos.ExecutorInfo buildExecutor() {
        String osName = osFromSystemProperty();


        return Protos.ExecutorInfo.newBuilder()
                .setName("Hazelcast Executor")
                .setExecutorId(Protos.ExecutorID.newBuilder().setValue("first executor"))
                .setCommand(command("java -cp hazelcast-mesos-executor.jar com.hazelcast.mesos.executor.HazelcastExecutor",
                        uris(
                                getFileURI("hazelcast-3.6.zip"),
                                getFileURI("hazelcast-mesos-executor.jar")
//                                        getFileURI("/jre-7-" + osName + ".tar.gz"),
//                                        getFileURI("hazelcast-executor.jar")

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
}
