package com.hazelcast.mesos.scheduler.rest;

import com.hazelcast.mesos.scheduler.HazelcastScheduler;
import com.hazelcast.mesos.util.HazelcastProperties;
import java.io.File;
import java.io.IOException;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import static com.hazelcast.mesos.util.Util.workingDir;


@Path("/")
public final class RestController {
    private final File hazelcastExecutorJar;
    private final File hazelcastZookeeperJar;
    private final File hazelcastJar;
    private final File hazelcastConfigurationFile;
    private final HazelcastScheduler scheduler;
    private final HazelcastDownloader downloader;

    public RestController(HazelcastScheduler scheduler) throws IOException {
        this.scheduler = scheduler;
        this.downloader = new HazelcastDownloader();
        File f;

        final String hazelcastJarPath = workingDir("/hazelcast-" + HazelcastProperties.getHazelcastVersion() + ".jar");
        f = new File(hazelcastJarPath);
        if (!isFileExistsAndCanRead(f)) {
            System.out.println("Hazelcast distribution not found at the " + hazelcastJarPath + " , downloading from web.");
            downloader.download(hazelcastJarPath);
            f = new File(hazelcastJarPath);
            verifyFileExistsAndCanRead(f);
        }
        hazelcastJar = f;

        final String executorJarPath = workingDir("/hazelcast-mesos-executor.jar");
        f = new File(executorJarPath);
        verifyFileExistsAndCanRead(f);
        hazelcastExecutorJar = f;

        final String zookeeperJarPath = workingDir("/hazelcast-zookeeper.jar");
        f = new File(zookeeperJarPath);
        verifyFileExistsAndCanRead(f);
        hazelcastZookeeperJar = f;

        final String hazelcastConfigurationFilePath = workingDir("/hazelcast.xml");
        f = new File(hazelcastConfigurationFilePath);
        verifyFileExistsAndCanRead(f);
        hazelcastConfigurationFile = f;
    }

    @GET
    @Path("/hazelcast-mesos-executor.jar")
    public Response hazelcastExecutorJar() {
        return handleRequest(hazelcastExecutorJar, "application/java-archive", "hazelcast-mesos-executor.jar");
    }

    @GET
    @Path("/hazelcast-zookeeper.jar")
    public Response hazelcastZookeeperJar() {
        return handleRequest(hazelcastZookeeperJar, "application/java-archive", "hazelcast-zookeeper.jar");
    }

    @GET
    @Path("/hazelcast-{version}.jar")
    public Response hazelcastJar(@PathParam("version") final String version) {
        return handleRequest(hazelcastJar, "application/java-archive", "hazelcast-" + version + ".jar");
    }

    @GET
    @Path("/hazelcast.xml")
    public Response hazelcastXML() {
        return handleRequest(hazelcastConfigurationFile, "application/xml", "hazelcast.xml");
    }

    @NotNull
    private static Response handleRequest(@NotNull final File resource, @NotNull final String type, @NotNull final String attachmentName) {
        final Response.ResponseBuilder builder = Response.ok(resource, type);
        builder.header("Content-Disposition", String.format("attachment; filename=\"%s\"", attachmentName));
        return builder.build();
    }

    @POST
    @Path("/nodes")
    public Response updateNodeCount(@QueryParam("nodeCount") final int nodeCount) {
        scheduler.setTargetNumberOfNodes(nodeCount);
        return Response.ok().build();
    }

    private static File verifyFileExistsAndCanRead(final File file) {
        if (!file.isFile() || !file.canRead()) {
            throw new IllegalArgumentException("Unable to find specified resource: " + file);
        }
        return file;
    }

    private static boolean isFileExistsAndCanRead(final File file) {
        return file.isFile() && file.canRead();
    }

}