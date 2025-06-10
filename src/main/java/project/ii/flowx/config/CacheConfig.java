package project.ii.flowx.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

    @Value("${cache.default.ttl:300}")
    private int defaultTtl;

//    private final List<String> caffeineCacheName = Arrays.asList(
//            "userLocalRoles",
//            "task" //,... add more cache names as needed
//    );
//
//    @Bean
//    public CaffeineCacheManager caffeineCacheManager() {
//        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
//        cacheManager.setCaffeine(caffeineCacheBuilder());
//        cacheManager.setCacheNames(caffeineCacheName);
//        log.info("Configured Caffeine cache manager with TTL: {} seconds", defaultTtl);
//        return cacheManager;
//    }
//
//    private Caffeine<Object, Object> caffeineCacheBuilder() {
//        return Caffeine.newBuilder()
//                .initialCapacity(100)
//                .maximumSize(1000)
//                .expireAfterWrite(defaultTtl, TimeUnit.SECONDS)
//                .recordStats();
//
//    JVM passed out :v, so I use Redis as the only cache manager

    @Bean
    @Primary
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(defaultTtl))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("files", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("tasks", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("userLocalRoles", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("contents", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        cacheConfigurations.put("userRoles", defaultConfig.entryTtl(Duration.ofMinutes(10)));


        log.info("Configured Redis cache manager with default TTL: {} seconds", defaultTtl);
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}