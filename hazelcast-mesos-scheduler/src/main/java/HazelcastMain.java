import com.hazelcast.mesos.scheduler.HazelcastScheduler;
import com.hazelcast.mesos.scheduler.rest.RestController;
import java.io.IOException;
import java.net.URI;
import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.FrameworkInfo;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import static com.hazelcast.mesos.util.HazelcastProperties.getHOST;
import static com.hazelcast.mesos.util.HazelcastProperties.getMesosZk;
import static com.hazelcast.mesos.util.HazelcastProperties.getPORT;

public class HazelcastMain {

    private static final String FRAMEWORK_NAME = "Hazelcast Framework";

    public static void main(String[] args) throws IOException {

        URI httpServerURI = URI.create("http://" + getHOST() + ":" + getPORT() + "/");

        HazelcastScheduler scheduler = new HazelcastScheduler(httpServerURI);

        initializeHttpServer(scheduler, httpServerURI);

        MesosSchedulerDriver driver = new MesosSchedulerDriver(scheduler, getFrameworkInfo(), getMesosZk());

        int status = driver.run() == Protos.Status.DRIVER_STOPPED ? 0 : 1;
        driver.stop();

        System.exit(status);
    }

    private static void initializeHttpServer(HazelcastScheduler scheduler, URI httpServerURI) throws IOException {
        final ResourceConfig resourceConfig = new ResourceConfig()
                .registerInstances(
                        new RestController(scheduler)
                );

        GrizzlyHttpServerFactory.createHttpServer(httpServerURI, resourceConfig);
    }

    public static FrameworkInfo getFrameworkInfo() {
        FrameworkInfo.Builder builder = FrameworkInfo.newBuilder();
        builder.setFailoverTimeout(5);
        builder.setUser("");
        builder.setName(FRAMEWORK_NAME);
        return builder.build();
    }
}
