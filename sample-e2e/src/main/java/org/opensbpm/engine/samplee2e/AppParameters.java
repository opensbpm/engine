package org.opensbpm.engine.samplee2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotBlank;
import org.opensbpm.engine.rest.client.AuthenticationTokenProvider;
import org.opensbpm.engine.rest.client.Credentials;
import org.opensbpm.engine.rest.client.EngineServiceClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;
import java.util.Objects;

@Component
@ConfigurationProperties(prefix = "opensbpm")
public class AppParameters {

    @NotBlank
    private String url;

    private String authUrl;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private boolean starter;

    private Statistics statistics;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean hasAuthUrl() {
        return authUrl != null && !authUrl.isEmpty();
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isStarter() {
        return starter;
    }

    public void setStarter(boolean starter) {
        this.starter = starter;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    public EngineServiceClient createEngineServiceClient() {
        return createEngineServiceClient(Credentials.of(username, password.toCharArray()));
    }

    public EngineServiceClient createEngineServiceClient(Credentials credentials) {
        final EngineServiceClient engineServiceClient;
        if (hasAuthUrl()) {
            engineServiceClient = EngineServiceClient.create(getAuthUrl(), getUrl(), credentials);
        } else {
            engineServiceClient = EngineServiceClient.create(getUrl(), new AuthenticationTokenProvider() {
                private final Object lock = new Object();
                private LoginResponse loginResponse;

                @Override
                public String getAuthenticationToken() {
                    synchronized (lock) {
                        if (loginResponse == null) {
                            loginResponse = requestToken();
                        }
                    }
                    return loginResponse.token();
                }

                private LoginResponse requestToken() {
                    try {
                        BodyPublisher bodyPublishers = HttpRequest.BodyPublishers.ofString(
                                String.format("{\"username\": \"%s\",\"password\": \"%s\"}", credentials.getUserName(), String.valueOf(credentials.getPassword()))
                        );
                        HttpRequest authRequest = HttpRequest.newBuilder(URI.create(String.format("%s/login", getUrl())))
                                .header("content-type", "application/json")
                                .POST(bodyPublishers)
                                .build();
                        HttpClient httpClient = HttpClient.newHttpClient();
                        try {
                            HttpResponse<String> authResponse = httpClient.send(authRequest, HttpResponse.BodyHandlers.ofString());
                            if (200 == authResponse.statusCode()) {
                                return new ObjectMapper().readValue(authResponse.body(), LoginResponse.class);
                            } else {
                                throw new IOException(authResponse.body());
                            }
                        } finally {
                            //since 21: httpClient.close();
                        }
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(ex);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
        }
        return engineServiceClient;
    }

    record LoginResponse(String token) {
        LoginResponse(String token) {
            this.token = Objects.requireNonNull(token);
        }
    }

    public static class Statistics {
        private String url;
        private String username;
        private String password;

        private String config;
        private Integer interval;
        private Integer processes;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getConfig() {
            return config;
        }

        public void setConfig(String config) {
            this.config = config;
        }

        public Integer getInterval() {
            return interval;
        }

        public void setInterval(Integer interval) {
            this.interval = interval;
        }

        public Integer getProcesses() {
            return processes;
        }

        public void setProcesses(Integer processes) {
            this.processes = processes;
        }
    }

}
