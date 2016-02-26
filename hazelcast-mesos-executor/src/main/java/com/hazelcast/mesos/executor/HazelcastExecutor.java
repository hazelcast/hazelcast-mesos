package com.hazelcast.mesos.executor;


import java.io.File;
import java.io.IOException;
import org.apache.mesos.Executor;
import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.MesosExecutorDriver;
import org.apache.mesos.Protos;

public class HazelcastExecutor implements Executor {
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
        Protos.CommandInfo command = taskInfo.getCommand();
        final ProcessBuilder processBuilder = new ProcessBuilder(command.getValue())
                .directory(new File(System.getProperty("user.dir")));
        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Protos.TaskStatus taskStatus = Protos.TaskStatus.newBuilder()
                .setExecutorId(taskInfo.getExecutor().getExecutorId())
                .setTaskId(taskInfo.getTaskId())
                .setState(Protos.TaskState.TASK_RUNNING)
                .setSource(Protos.TaskStatus.Source.SOURCE_EXECUTOR)
                .build();
        executorDriver.sendStatusUpdate(taskStatus);
    }

    @Override
    public void killTask(ExecutorDriver executorDriver, Protos.TaskID taskID) {
        System.out.println("HazelcastExecutor.killTask");
        System.out.println("executorDriver = [" + executorDriver + "], taskID = [" + taskID + "]");
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
