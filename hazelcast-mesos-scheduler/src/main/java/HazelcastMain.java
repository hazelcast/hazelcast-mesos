import com.hazelcast.mesos.scheduler.HazelcastScheduler;
import com.hazelcast.mesos.scheduler.rest.RestController;
import com.hazelcast.mesos.util.HazelcastProperties;
import java.net.URI;
import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.FrameworkInfo;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class HazelcastMain {

    private static final String FRAMEWORK_NAME = "Hazelcast Framework";

    public static void main(String[] args) {

        String host = "localhost";
        String port = "8090";
        URI httpServerURI = URI.create("http://" + host + ":" + port + "/");

        HazelcastScheduler scheduler = new HazelcastScheduler(httpServerURI);

        initializeHttpServer(scheduler, HazelcastProperties.getHazelcastVersion(), httpServerURI);

        MesosSchedulerDriver driver = new MesosSchedulerDriver(scheduler, getFrameworkInfo(), HazelcastProperties.getMesosZk());

        int status = driver.run() == Protos.Status.DRIVER_STOPPED ? 0 : 1;
        driver.stop();

        System.exit(status);
    }

    private static void initializeHttpServer(HazelcastScheduler scheduler, String hazelcastVersion, URI httpServerURI) {
        final ResourceConfig resourceConfig = new ResourceConfig()
                .registerInstances(
                        new RestController(scheduler, hazelcastVersion)
                );

        GrizzlyHttpServerFactory.createHttpServer(httpServerURI, resourceConfig);
    }

    public static FrameworkInfo getFrameworkInfo() {
        FrameworkInfo.Builder builder = FrameworkInfo.newBuilder();
        builder.setFailoverTimeout(120000);
        builder.setUser("");
        builder.setName(FRAMEWORK_NAME);
        return builder.build();
    }
}
