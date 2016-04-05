package com.hazelcast.mesos.util;

import com.google.common.base.Optional;
import java.util.ArrayList;
import org.apache.mesos.Protos;

import static org.apache.mesos.Protos.Value.Type.SCALAR;

public class Util {

    public static Protos.Resource resource(String name, Double value) {
        Protos.Value.Scalar scalar = Protos.Value.Scalar.newBuilder().
                setValue(value)
                .build();
        return Protos.Resource.newBuilder()
                .setName(name)
                .setType(SCALAR)
                .setScalar(scalar)
                .build();
    }

    public static Protos.CommandInfo command(String command) {
        return Protos.CommandInfo.newBuilder()
                .setValue(command)
                .build();
    }

    public static Protos.CommandInfo command(String command, Iterable<Protos.CommandInfo.URI> uris) {
        return Protos.CommandInfo.newBuilder()
                .addAllUris(uris)
                .setValue(command)
                .build();
    }

    public static Iterable<Protos.CommandInfo.URI> uris(String... uris) {
        ArrayList<Protos.CommandInfo.URI> uriList = new ArrayList<Protos.CommandInfo.URI>();
        for (String uri : uris) {
            Protos.CommandInfo.URI build = Protos.CommandInfo.URI.newBuilder().setValue(uri).build();
            uriList.add(build);
        }
        return uriList;
    }


    public static Optional<String> option(final String key) {
        return Optional.fromNullable(System.getenv(key));
    }


    public static String workingDir(final String defaultFileName) {
        return System.getProperty("user.dir") + defaultFileName;
    }

}
