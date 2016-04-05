package com.hazelcast.mesos.scheduler.rest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static com.hazelcast.mesos.util.HazelcastProperties.getHazelcastVersion;

public class HazelcastDownloader {

    private static final String DOWNLOAD_URL = "http://download.hazelcast.com/download.jsp?version=hazelcast-" + getHazelcastVersion() + "&p=Mesos";

    public HazelcastDownloader() {
    }

    public void download(String path) throws IOException {
        URL website = new URL(DOWNLOAD_URL);
        try (InputStream in = website.openStream()) {
            Files.copy(in, Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
        }
    }

}
