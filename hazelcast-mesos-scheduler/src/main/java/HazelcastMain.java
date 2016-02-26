import com.hazelcast.mesos.scheduler.HazelcastScheduler;
import com.hazelcast.mesos.scheduler.rest.FileResourceController;
import java.net.URI;
import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.FrameworkInfo;
import org.apache.mesos.Scheduler;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class HazelcastMain {

    private static final String FRAMEWORK_NAME = "Hazelcast Framework";
    private static String HAZELCAST_ZK;
    private static String MESOS_ZK;

    public static void main(String[] args) {
        getEnvVariables();

        String host = "localhost";
        String port = "8090";
        String hazelcastVersion = "3.6";

        URI httpServerURI = URI.create("http://" + host + ":" + port + "/");


        initializeHttpServer(hazelcastVersion, httpServerURI);

        Scheduler scheduler = new HazelcastScheduler(httpServerURI);
        MesosSchedulerDriver driver = new MesosSchedulerDriver(scheduler, getFrameworkInfo(), MESOS_ZK);

        int status = driver.run() == Protos.Status.DRIVER_STOPPED ? 0 : 1;
        driver.stop();

        System.exit(status);
    }

    private static void initializeHttpServer(String hazelcastVersion, URI httpServerURI) {
        final ResourceConfig resourceConfig = new ResourceConfig()
                .registerInstances(
                        new FileResourceController(hazelcastVersion)
                );

        GrizzlyHttpServerFactory.createHttpServer(httpServerURI, resourceConfig);
    }

    private static void getEnvVariables() {
        HAZELCAST_ZK = System.getenv("HAZELCAST_ZK_URL");
        MESOS_ZK = System.getenv("MESOS_ZK");

    }

    private static void usage() {
        String name = HazelcastMain.class.getName();
        System.err.println("Usage: " + name + " <mesos-master>");
    }


    public static FrameworkInfo getFrameworkInfo() {
        FrameworkInfo.Builder builder = FrameworkInfo.newBuilder();
        builder.setFailoverTimeout(120000);
        builder.setUser("");
        builder.setName(FRAMEWORK_NAME);
        return builder.build();
    }
}
