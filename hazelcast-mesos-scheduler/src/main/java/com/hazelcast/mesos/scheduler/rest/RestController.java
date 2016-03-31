package com.hazelcast.mesos.scheduler.rest;

import com.hazelcast.mesos.scheduler.HazelcastScheduler;
import java.io.File;
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
    private final File hazelcastZip;
    private final File hazelcastConfigurationFile;
    private final HazelcastScheduler scheduler;

    public RestController(HazelcastScheduler scheduler, final String hazelcastVersion) {
        this.scheduler = scheduler;
        File f;

        final String hazelcastZipPath = workingDir("/hazelcast-" + hazelcastVersion + ".zip");
        f = new File(hazelcastZipPath);
        verifyFileExistsAndCanRead(f);
        hazelcastZip = f;

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
    @Path("/hazelcast-{version}.zip")
    public Response hazelcastTar(@PathParam("version") final String version) {
        return handleRequest(hazelcastZip, "application/zip", "hazelcast.zip");
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
            throw new IllegalArgumentException("Unable to read specified resource: " + file);
        }
        return file;
    }

}