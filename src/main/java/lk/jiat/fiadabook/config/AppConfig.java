package lk.jiat.fiadabook.config;

import org.glassfish.jersey.server.ResourceConfig;

public class AppConfig extends ResourceConfig {
    public AppConfig() {
        packages("lk.jiat.fiadabook.controller");
        packages("lk.jiat.fiadabook.resource");
        packages("lk.jiat.fiadabook.middleware");
        register(org.glassfish.jersey.media.multipart.MultiPartFeature.class);
    }
}
