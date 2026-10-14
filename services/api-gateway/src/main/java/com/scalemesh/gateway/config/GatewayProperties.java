package com.scalemesh.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "services")
public class GatewayProperties {

    private ServiceEndpoint telemetry = new ServiceEndpoint("http://localhost:8082");
    private ServiceEndpoint dispatch = new ServiceEndpoint("http://localhost:8083");
    private ServiceEndpoint routing = new ServiceEndpoint("http://localhost:8084");
    private ServiceEndpoint demand = new ServiceEndpoint("http://localhost:8085");
    private ServiceEndpoint incident = new ServiceEndpoint("http://localhost:8086");

    public static class ServiceEndpoint {
        private String url;

        public ServiceEndpoint() {
        }

        public ServiceEndpoint(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public ServiceEndpoint getTelemetry() {
        return telemetry;
    }

    public void setTelemetry(ServiceEndpoint telemetry) {
        this.telemetry = telemetry;
    }

    public ServiceEndpoint getDispatch() {
        return dispatch;
    }

    public void setDispatch(ServiceEndpoint dispatch) {
        this.dispatch = dispatch;
    }

    public ServiceEndpoint getRouting() {
        return routing;
    }

    public void setRouting(ServiceEndpoint routing) {
        this.routing = routing;
    }

    public ServiceEndpoint getDemand() {
        return demand;
    }

    public void setDemand(ServiceEndpoint demand) {
        this.demand = demand;
    }

    public ServiceEndpoint getIncident() {
        return incident;
    }

    public void setIncident(ServiceEndpoint incident) {
        this.incident = incident;
    }
}
