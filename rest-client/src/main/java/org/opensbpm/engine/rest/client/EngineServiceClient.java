package org.opensbpm.engine.rest.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jakarta.rs.cfg.Annotations;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.apache.cxf.jaxrs.client.Client;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.opensbpm.engine.api.instance.TaskResponse;
import org.opensbpm.engine.api.instance.UserToken;
import org.opensbpm.engine.rest.api.EngineResource;
import org.opensbpm.engine.rest.api.ProcessInstanceResource;
import org.opensbpm.engine.rest.api.ProcessModelResource;
import org.opensbpm.engine.rest.api.UserResource;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

public abstract class EngineServiceClient {

    public static EngineServiceClient create(String baseAddress, Credentials credentials) {
        return create(baseAddress, baseAddress, credentials);
    }

    public static EngineServiceClient create(String authAddress, String serviceAddress, Credentials credentials) {
        requireAddress(authAddress);
        requireAddress(serviceAddress);
        requireNonNull(credentials, "Credentials must not be null");

        return create(serviceAddress, new AuthenticationTokenProvider() {
            private final Object lock = new Object();
            private Authentication authentication;

            @Override
            public String getAuthenticationToken() {
                synchronized (lock) {
                    if (authentication == null || authentication.isRefreshExpired()) {
                        authentication = authenticate();
                    } else if (authentication.isExpired()) {
                        authentication = refreshToken(authentication.getRefreshToken());
                    }
                }
                return authentication.getAccessToken();
            }

            private Authentication authenticate() {
                LOGGER.finer("Authenticating user " + credentials.getUserName());
                return requestToken(
                        BodyPublishers.ofString(
                                String.format("client_id=opensbpm-ui&grant_type=password&username=%s&password=%s", credentials.getUserName(), String.valueOf(credentials.getPassword()))
                        )
                );
            }

            private Authentication refreshToken(String refreshToken) {
                LOGGER.finer("Refreshing token for user " + credentials.getUserName());
                return requestToken(
                        BodyPublishers.ofString(
                                String.format("client_id=opensbpm-ui&grant_type=refresh_token&refresh_token=%s", refreshToken)
                        )
                );
            }

            private Authentication requestToken(BodyPublisher bodyPublishers) {
                try {
                    HttpRequest authRequest = HttpRequest.newBuilder(URI.create(String.format("%s/auth/realms/opensbpm/protocol/openid-connect/token", authAddress)))
                            .header("content-type", "application/x-www-form-urlencoded")
                            .POST(bodyPublishers)
                            .build();
                    HttpClient httpClient = HttpClient.newHttpClient();
                    try {
                        HttpResponse<String> authResponse = httpClient.send(authRequest, HttpResponse.BodyHandlers.ofString());
                        if (200 == authResponse.statusCode()) {
                            return new ObjectMapper().readValue(authResponse.body(), Authentication.class);
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

    public static EngineServiceClient create(String serviceAddress, AuthenticationTokenProvider authenticationTokenProvider) {
        requireAddress(serviceAddress);
        return new EngineServiceClient(serviceAddress) {
            @Override
            protected String getAuthenticationToken() {
                return authenticationTokenProvider.getAuthenticationToken();
            }
        };
    }

    private static String requireAddress(String authAddress) {
        requireNonNull(authAddress, "Address must not be null");
        if (authAddress.endsWith("/")) {
            throw new IllegalArgumentException(authAddress + " must not end with /");
        }
        return authAddress;
    }

    private final static Logger LOGGER = Logger.getLogger(EngineServiceClient.class.getName());
    private final ClassLoader jaxbContextClassLoader;
    private final Supplier<UserResource> userResource = of(UserResource.class);
    private final Supplier<EngineResource> engineResource = of(EngineResource.class);
    private final Supplier<ProcessInstanceResource> processInstanceResource = of(ProcessInstanceResource.class);
    private final Supplier<ProcessModelResource> processModelResource = of(ProcessModelResource.class);
    //
    private final String baseAddress;
    //
    private UserToken userToken;


    EngineServiceClient(String baseAddress) {
        this.baseAddress = requireNonNull(baseAddress, "baseAddress must not be null");
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(TaskResponse.class);
            jaxbContextClassLoader = jaxbContext.getClass().getClassLoader();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract String getAuthenticationToken();

    public UserToken getUserToken() {
        if (userToken == null) {
            userToken = onUserResource(userResource -> userResource.info());
        }
        return userToken;
    }

    public Long getUserId() {
        return getUserToken().getId();
    }

    public <T> T onUserResource(Function<UserResource, T> function) {
        return onResource(userResource, function);
    }

    public <T> T onEngineModelResource(Function<EngineResource.ProcessModelResource, T> function) {
        return onEngineResource(resource -> function.apply(resource.getProcessModelResource(getUserId())));
    }

    public <T> T onEngineTaskResource(Function<EngineResource.TaskResource, T> function) {
        return onEngineResource(resource -> function.apply(resource.getTaskResource(getUserId())));
    }

    public <T> T onEngineResource(Function<EngineResource, T> function) {
        return onResource(engineResource, function);
    }

    public <T> T onProcessInstanceResource(Function<ProcessInstanceResource, T> function) {
        return onResource(processInstanceResource, function);
    }

    public <T> T onProcessModelResource(Function<ProcessModelResource, T> function) {
        return onResource(processModelResource, function);
    }

    private <R, T> T onResource(Supplier<R> resourceSupplier, Function<R, T> function) {
        R resource = resourceSupplier.get();
        Client client = WebClient.client(resource);
        client.header("Authorization", "Bearer " + getAuthenticationToken());
        try {
            Thread.currentThread().setContextClassLoader(jaxbContextClassLoader);
            return function.apply(resource);
        } finally {
            client.reset();
            //client.close();
        }
    }


    private <T> Supplier<T> of(Class<T> type) {
        return new Supplier<>() {
            private T resourceClient;

            @Override
            public T get() {
                if (resourceClient == null) {
                    resourceClient = createResourceClient(type);
                }
                return resourceClient;
            }
        };
    }

    private <T> T createResourceClient(Class<T> type) {
        List<Object> providers = asList(
                new JacksonJsonProvider(new ObjectMapper()
                        .registerModule(new JavaTimeModule()),
                        new Annotations[]{Annotations.JAKARTA_XML_BIND})
        );
        JAXRSClientFactoryBean factoryBean = new JAXRSClientFactoryBean();
        factoryBean.setAddress(String.format("%s/services", baseAddress));
        factoryBean.setProviders(providers);
        factoryBean.setInheritHeaders(true);
        factoryBean.setServiceClass(type);

        factoryBean.setThreadSafe(true);

        Map<String, Object> properties = new HashMap<>();
        //see https://cwiki.apache.org/confluence/pages/viewpage.action?pageId=49941
        properties.put("force.urlconnection.http.conduit", true);
        factoryBean.setProperties(properties);

        return factoryBean.create(type);
    }
}
