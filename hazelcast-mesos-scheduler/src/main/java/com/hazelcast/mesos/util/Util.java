package com.hazelcast.mesos.util;

import com.google.common.base.Optional;
import java.util.ArrayList;
import org.apache.mesos.Protos;

import static org.apache.mesos.Protos.Value.Type.SCALAR;

/**
 * date: 3/4/16
 * author: emindemirci
 */
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

    public static String osFromSystemProperty() {
        final String osName = System.getProperty("os.name").toLowerCase();
        final String os;
        if (osName.contains("mac") || osName.contains("darwin")) {
            os = "macosx";
        } else if (osName.contains("linux")) {
            os = "linux";
        } else {
            throw new IllegalArgumentException("Unknown OS " + osName);
        }
        return os;
    }

}
