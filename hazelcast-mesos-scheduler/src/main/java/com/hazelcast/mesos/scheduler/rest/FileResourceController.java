package com.hazelcast.mesos.scheduler.rest;

import com.hazelcast.mesos.util.Util;
import java.io.File;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;


@Path("/")
public final class FileResourceController {
    @NotNull
    private final File hazelcastExecutorFile;
    @NotNull
    private final File jreTarFile;
    @NotNull
    private final File hazelcastTarFile;

    public FileResourceController(final String hazelcastVersion) {
        File f;

        final String javaVersion = "7u76";
        final String osName = Util.osFromSystemProperty();
        final String providedJreTar = Util.option("JRE_FILE_PATH").or(Util.workingDir("/jre-" + javaVersion + '-' + osName + "-x64.tar.gz"));
        f = new File(providedJreTar);
//        verifyFileExistsAndCanRead(f);
        jreTarFile = f;

        final String providedHazelcastZip = Util.option("HAZELCAST_FILE_PATH").or(Util.workingDir("/hazelcast-" + hazelcastVersion + ".zip"));
        f = new File(providedHazelcastZip);
//        verifyFileExistsAndCanRead(f);
        hazelcastTarFile = f;


        final String executorFilePath = Util.option("EXECUTOR_FILE_PATH").or(Util.workingDir("/hazelcast-mesos-executor.jar"));
        f = new File(executorFilePath);
//        verifyFileExistsAndCanRead(f);
        hazelcastExecutorFile = f;
    }

    @GET
    @Path("/hazelcast-mesos-executor.jar")
    public Response hazelcastExecutorJar() {
        return handleRequest(hazelcastExecutorFile, "application/java-archive", "hazelcast-mesos-executor.jar");
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

    @NotNull
    private static Response handleRequest(@NotNull final File resource, @NotNull final String type, @NotNull final String attachmentName) {
        final Response.ResponseBuilder builder = Response.ok(resource, type);
        builder.header("Content-Disposition", String.format("attachment; filename=\"%s\"", attachmentName));
        return builder.build();
    }

    private static File verifyFileExistsAndCanRead(final File file) {
        if (!file.isFile() || !file.canRead()) {
            throw new IllegalArgumentException("Unable to read specified resource: " + file);
        }
        return file;
    }
}