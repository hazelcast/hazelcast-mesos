package com.hazelcast.mesos.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static com.hazelcast.mesos.util.Util.option;
import static java.lang.Integer.parseInt;

public class HazelcastProperties {
    static String HAZELCAST_VERSION = option("HAZELCAST_VERSION").or("3.6");
    static String HOST = option("HOST").or("MESOS_HOSTNAME");
    static String PORT = option("PORT").or("8090");
    static String MESOS_ZK = option("MESOS_ZK").or("zk://localhost:2181/mesos");
    static String MIN_HEAP = option("MIN_HEAP").or("1g");
    static String MAX_HEAP = option("MAX_HEAP").or("1g");
    static Double CPU_PER_NODE = Double.parseDouble(option("CPU_PER_NODE").or("1.0"));
    static Double MEMORY_PER_NODE = Double.parseDouble(option("MEMORY_PER_NODE").or("1024.0"));
    static int NUMBER_OF_NODES = parseInt(option("NUMBER_OF_NODES").or("1"));

    public static String getHazelcastVersion() {
        return HAZELCAST_VERSION;
    }

    public static String getMesosZk() {
        return MESOS_ZK;
    }

    public static String getMinHeap() {
        return MIN_HEAP;
    }

    public static String getMaxHeap() {
        return MAX_HEAP;
    }

    public static Double getCpuPerNode() {
        return CPU_PER_NODE;
    }

    public static Double getMemoryPerNode() {
        return MEMORY_PER_NODE;
    }

    public static int getNumberOfNodes() {
        return NUMBER_OF_NODES;
    }

    public static String getHOST() {
        return HOST == null ? getHostname() : HOST;
    }

    private static String getHostname() {
        String host;
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            System.out.println("Could not get hostname.");
            host = "localhost";
        }
        return host;
    }

    public static String getPORT() {
        return PORT;
    }
}