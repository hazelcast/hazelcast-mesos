package com.hazelcast.mesos.scheduler.rest;

import com.hazelcast.mesos.scheduler.HazelcastScheduler;
import com.hazelcast.mesos.util.Util;
import java.io.File;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;


@Path("/")
public final class RestController {
    private final File hazelcastExecutorFile;
    private final File hazelcastZookeeperJar;
    private final File jreTarFile;
    private final File hazelcastTarFile;
    private final File hazelcastConfigurationFile;
    private final HazelcastScheduler scheduler;

    public RestController(HazelcastScheduler scheduler, final String hazelcastVersion) {
        this.scheduler = scheduler;
        File f;

        final String javaVersion = "7u76";
        final String osName = Util.osFromSystemProperty();
        final String providedJreTar = Util.option("JRE_FILE_PATH").or(Util.workingDir("/jre-" + javaVersion + '-' + osName + "-x64.tar.gz"));
        f = new File(providedJreTar);
//        verifyFileExistsAndCanRead(f);
        jreTarFile = f;

        final String providedHazelcastZip = Util.option("HAZELCAST_FILE_PATH").or(Util.workingDir("/hazelcast-" + hazelcastVersion + ".zip"));
        f = new File(providedHazelcastZip);
        verifyFileExistsAndCanRead(f);
        hazelcastTarFile = f;


        final String executorFilePath = Util.option("EXECUTOR_FILE_PATH").or(Util.workingDir("/hazelcast-mesos-executor.jar"));
        f = new File(executorFilePath);
        verifyFileExistsAndCanRead(f);
        hazelcastExecutorFile = f;

        final String zookeeperFilePath = Util.workingDir("/hazelcast-zookeeper.jar");
        f = new File(zookeeperFilePath);
        verifyFileExistsAndCanRead(f);
        hazelcastZookeeperJar = f;

        final String hazelcastConfigurationFilePath = Util.workingDir("/hazelcast.xml");
        f = new File(hazelcastConfigurationFilePath);
        verifyFileExistsAndCanRead(f);
        hazelcastConfigurationFile = f;
    }

    @GET
    @Path("/hazelcast-mesos-executor.jar")
    public Response hazelcastExecutorJar() {
        return handleRequest(hazelcastExecutorFile, "application/java-archive", "hazelcast-mesos-executor.jar");
    }

    @GET
    @Path("/hazelcast-zookeeper.jar")
    public Response hazelcastZookeeperJar() {
        return handleRequest(hazelcastZookeeperJar, "application/java-archive", "hazelcast-zookeeper.jar");
    }

    @GET
    @Path("/jre-{version}-{osname}.tar.gz")
    public Response jreTar(@PathParam("version") final String version, @PathParam("osname") final String osname) {
        return handleRequest(jreTarFile, "application/x-gzip", "jre.tar.gz");
    }

    @GET
    @Path("/hazelcast-{version}.zip")
    public Response hazelcastTar(@PathParam("version") final String version) {
        return handleRequest(hazelcastTarFile, "application/zip", "hazelcast.zip");
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