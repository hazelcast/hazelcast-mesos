package com.hazelcast.mesos.executor;


import java.io.File;
import java.io.IOException;
import org.apache.mesos.Executor;
import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.MesosExecutorDriver;
import org.apache.mesos.Protos;

public class HazelcastExecutor implements Executor {
    private Process process;
    private Protos.ExecutorID executorId;

    @Override
    public void registered(ExecutorDriver executorDriver, Protos.ExecutorInfo executorInfo, Protos.FrameworkInfo frameworkInfo, Protos.SlaveInfo slaveInfo) {
        System.out.println("HazelcastExecutor.registered");
        System.out.println("executorDriver = [" + executorDriver + "], executorInfo = [" + executorInfo + "], frameworkInfo = [" + frameworkInfo + "], slaveInfo = [" + slaveInfo + "]");
    }

    @Override
    public void reregistered(ExecutorDriver executorDriver, Protos.SlaveInfo slaveInfo) {
        System.out.println("HazelcastExecutor.reregistered");
        System.out.println("executorDriver = [" + executorDriver + "], slaveInfo = [" + slaveInfo + "]");

    }

    @Override
    public void disconnected(ExecutorDriver executorDriver) {
        System.out.println("HazelcastExecutor.disconnected");
        System.out.println("executorDriver = [" + executorDriver + "]");

    }

    @Override
    public void launchTask(ExecutorDriver executorDriver, Protos.TaskInfo taskInfo) {
        System.out.println("HazelcastExecutor.launchTask");
        System.out.println("executorDriver = [" + executorDriver + "], taskInfo = [" + taskInfo + "]");
        executorId = taskInfo.getExecutor().getExecutorId();
        final ProcessBuilder processBuilder = new ProcessBuilder("java",
                "-server",
                "-Djava.net.preferIPv4Stack=true",
                "-Dhazelcast.config=" + System.getProperty("user.dir") + "/hazelcast.xml",
                "-cp",
                "lib/hazelcast-all-3.6.jar:../hazelcast-zookeeper.jar",
                "com.hazelcast.core.server.StartServer"
        )
                .directory(new File(System.getProperty("user.dir") + "/hazelcast-3.6/"))
                .redirectOutput(new File("hazelcast.log"))
                .redirectError(new File("hazelcast.err.log"));
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            sendStatusUpdate(executorDriver, Protos.TaskState.TASK_FAILED, taskInfo.getTaskId(), executorId);
        }

        try {
            Thread.sleep(5000);
            process.exitValue();
            sendStatusUpdate(executorDriver, Protos.TaskState.TASK_FAILED, taskInfo.getTaskId(), executorId);
        } catch (InterruptedException | IllegalThreadStateException e) {
        }

        sendStatusUpdate(executorDriver, Protos.TaskState.TASK_RUNNING, taskInfo.getTaskId(), executorId);
        System.out.println("Node started successfully.");
    }

    private void sendStatusUpdate(ExecutorDriver executorDriver, Protos.TaskState state, Protos.TaskID taskId, Protos.ExecutorID executorId) {
        Protos.TaskStatus taskStatus = Protos.TaskStatus.newBuilder()
                .setExecutorId(executorId)
                .setTaskId(taskId)
                .setState(state)
                .setSource(Protos.TaskStatus.Source.SOURCE_EXECUTOR)
                .build();
        executorDriver.sendStatusUpdate(taskStatus);
    }

    @Override
    public void killTask(ExecutorDriver executorDriver, Protos.TaskID taskID) {
        System.out.println("HazelcastExecutor.killTask");
        System.out.println("executorDriver = [" + executorDriver + "], taskID = [" + taskID + "]");
        process.destroy();
        System.out.println("Node stop successfully.");
        sendStatusUpdate(executorDriver, Protos.TaskState.TASK_KILLED, taskID, executorId);
    }

    @Override
    public void frameworkMessage(ExecutorDriver executorDriver, byte[] bytes) {
        System.out.println("HazelcastExecutor.frameworkMessage");
        System.out.println("executorDriver = [" + executorDriver + "], bytes = [" + bytes + "]");

    }

    @Override
    public void shutdown(ExecutorDriver executorDriver) {
        System.out.println("HazelcastExecutor.shutdown");
        System.out.println("executorDriver = [" + executorDriver + "]");

    }

    @Override
    public void error(ExecutorDriver executorDriver, String s) {
        System.out.println("HazelcastExecutor.error");
        System.out.println("executorDriver = [" + executorDriver + "], s = [" + s + "]");

    }

    public static void main(final String[] args) {
        final MesosExecutorDriver driver = new MesosExecutorDriver(new HazelcastExecutor());
        final int status;
        switch (driver.run()) {
            case DRIVER_STOPPED:
                status = 0;
                break;
            case DRIVER_ABORTED:
                status = 1;
                break;
            case DRIVER_NOT_STARTED:
                status = 2;
                break;
            default:
                status = 3;
                break;
        }
        driver.stop();

        System.exit(status);
    }

}
