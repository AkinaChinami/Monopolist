package Monopolist;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import javax.ws.rs.ApplicationPath;

@Component
@ApplicationPath("/monopolist")
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        register(Webservice.class);
        register(CORSResponseFilter.class);
    }
}
