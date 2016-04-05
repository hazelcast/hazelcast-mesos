package com.hazelcast.mesos.executor;


import com.google.protobuf.InvalidProtocolBufferException;
import com.hazelcast.mesos.HazelcastMessages.HazelcastServerProcessTask;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.mesos.Executor;
import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.MesosExecutorDriver;
import org.apache.mesos.Protos;

import static com.hazelcast.mesos.HazelcastMessages.HazelcastServerProcessTask.parseFrom;

public class HazelcastExecutor implements Executor {
    private Process process;
    private Protos.ExecutorID executorId;

    @Override
    public void registered(ExecutorDriver executorDriver, Protos.ExecutorInfo executorInfo, Protos.FrameworkInfo frameworkInfo, Protos.SlaveInfo slaveInfo) {
    }

    @Override
    public void reregistered(ExecutorDriver executorDriver, Protos.SlaveInfo slaveInfo) {
    }

    @Override
    public void disconnected(ExecutorDriver executorDriver) {
    }

    @Override
    public void launchTask(ExecutorDriver executorDriver, Protos.TaskInfo taskInfo) {
        executorId = taskInfo.getExecutor().getExecutorId();
        HazelcastServerProcessTask processTask = null;
        try {
            processTask = parseFrom(taskInfo.getData());
        } catch (InvalidProtocolBufferException e) {
            System.out.println("Failed to read task details, " + e.getMessage());
            sendStatusUpdate(executorDriver, Protos.TaskState.TASK_FAILED, taskInfo.getTaskId(), executorId);
            return;
        }

        List<String> commandList = new ArrayList<String>(processTask.getCommandList());
        commandList.add(commandList.size() - 3, "-Dhazelcast.config=" + System.getProperty("user.dir") + "/hazelcast.xml");
        for (String s : commandList) {
            System.out.println("s = " + s);
        }
        final ProcessBuilder processBuilder = new ProcessBuilder(
                commandList
        )
                .directory(new File(System.getProperty("user.dir")))
                .redirectOutput(new File("hazelcast.log"))
                .redirectError(new File("hazelcast.err.log"));
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            System.out.println("Failed to start process, " + e.getMessage());
            sendStatusUpdate(executorDriver, Protos.TaskState.TASK_FAILED, taskInfo.getTaskId(), executorId);
            return;
        }

        try {
            Thread.sleep(5000);
            int exitValue = process.exitValue();
            System.out.println("Process exited with value -> " + exitValue);
            sendStatusUpdate(executorDriver, Protos.TaskState.TASK_FAILED, taskInfo.getTaskId(), executorId);
            return;
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
        if (process != null) {
            process.destroy();
        }
        System.out.println("Node with taskId = " + taskID.getValue() + ", stopped successfully.");
        sendStatusUpdate(executorDriver, Protos.TaskState.TASK_KILLED, taskID, executorId);
    }

    @Override
    public void frameworkMessage(ExecutorDriver executorDriver, byte[] bytes) {
    }

    @Override
    public void shutdown(ExecutorDriver executorDriver) {
    }

    @Override
    public void error(ExecutorDriver executorDriver, String s) {
        System.out.println("HazelcastExecutor.error -> " + s);

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
