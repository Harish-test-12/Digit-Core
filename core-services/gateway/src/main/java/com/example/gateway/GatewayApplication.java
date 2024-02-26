package com.example.gateway;
import com.example.gateway.config.ApplicationProperties;
import com.example.gateway.ratelimiters.RateLimiterConfiguration;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@SpringBootApplication
@RestController
//@PropertySource({"${spring.routes.filepath}","${spring.limiter.filepath}"})
public class GatewayApplication {

	@Autowired
	private ApplicationProperties applicationProperties;

	private RateLimiterConfiguration rateLimiterConfiguration;

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

	@Value("${egov.user-info-header}")
	private String userInfoHeader;
	private List<String> encryptedUrlSet;
	private List<String> openEndpointsWhitelist;
	private List<String> mixedModeEndpointsWhitelist;

	@Value("${egov.encrypted-endpoints-list}")
	public void setEncrytpedUrlListValues(List<String> EcryptedListFromProperties) {
		this.encryptedUrlSet = Collections.unmodifiableList(EcryptedListFromProperties);
	}

	@Value("${egov.open-endpoints-whitelist}")
	public void setOpenEndpointsWhitelistValues(List<String> openUrlListFromProperties) {
		this.openEndpointsWhitelist = Collections.unmodifiableList(openUrlListFromProperties);
	}

	@Value("${egov.mixed-mode-endpoints-whitelist}")
	public void setMixModeEndpointListVaaues(List<String> mixModeUrlListFromProperties) {
		this.mixedModeEndpointsWhitelist = Collections.unmodifiableList(mixModeUrlListFromProperties);
	}

	@Value("${egov.auth-service-host}")
	private String authServiceHost;

	@Value("${egov.auth-service-uri}")
	private String authServiceUri;

	@Value("${egov.authorize.access.control.host}${egov.authorize.access.control.uri}")
	private String authorizationUrl;

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public MultiStateInstanceUtil centralInstanceUtil() {
		return new MultiStateInstanceUtil();
	}

}