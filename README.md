# Hazelcast Mesos

This module gives you an ability to deploy Hazelcast in the Mesos cluster.

## Running Hazelcast-Mesos from command line
You can deploy Hazelcast on Mesos via command line by running the Hazelcast Scheduler.

You need to have a running Mesos cluster to deploy Hazelcast to it. To install and run the Apache Mesos please refer to http://mesos.apache.org/gettingstarted/

- Download the Hazelcast-Mesos package from `http://link`
- Extract the archive
- Set Zookeeper address which mesos uses by
```
export MESOS_ZK="zk://<mesos-zookeeper-address>"
```
- Run the scheduler by issuing the command below
```
java -cp hazelcast-mesos-scheduler.jar HazelcastMain
```
The scheduler will pull the Hazelcast binaries from web and distribute it to Mesos slaves and starts the Hazelcast nodes. Hazelcast Scheduler will place one Hazelcast member per Mesos slave.

You can control cluster size of the Hazelcast via the REST API it provides.
For example, to scale Hazelcast cluster to 5 nodes issue the following command.
```
curl -X POST http://localhost:8090/nodes?nodeCount=5
 ```

## Running Hazelcast-Mesos via Marathon
You can deploy Hazelcast on Mesos via Marahton.

To install Marathon, you can refer to https://docs.mesosphere.com/getting-started/datacenter/install/.

After that,
- Save the following JSON as `hazelcast.json`
```
{
  "id": "/hazelcast",
  "instances": 1,
  "cpus": 1,
  "mem": 1024,
  "ports": [
    0
  ],
  "env": {
    "MESOS_ZK": "zk://localhost:2181/mesos",
    "MIN_HEAP": "1g",
    "MAX_HEAP": "1g",
    "NUMBER_OF_NODES": "3"
  },
  "cmd": "java -classpath hazelcast-mesos-scheduler.jar HazelcastMain",
  "uris": [
    "https://s3.amazonaws.com/hazelcast/mesos/hazelcast-mesos.zip"
  ]
}
```
- Initiate POST request to Marathon REST API via issuing the command below
```
curl -X POST http://localhost:8080/v2/apps -d @hazelcast.json -H "Content-type: application/json"
```
- Open Marathon UI at `http://localhost:8080`. You should see the `hazelcast` application with status `running` .
