package project.ii.flowx.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache configuration class for setting up Redis caching with custom serialization.
 * This configuration uses Jackson for JSON serialization and deserialization,
 * allowing for polymorphic type handling.
 */
@Configuration
@EnableCaching  // Re-enable caching with String-based approach
@Slf4j
public class CacheConfig {
    @Value("${cache.default.ttl:300}")
    private int defaultTtl;

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Configure ObjectMapper for JSON serialization and deserialization with polymorphic type handling
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        objectMapper.registerModule(new JavaTimeModule());

        // Configure ObjectMapper to handle polymorphic types
        Jackson2JsonRedisSerializer<Object> jacksonSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        // Configure the default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(defaultTtl))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jacksonSerializer))
                .disableCachingNullValues();

        // Define specific cache configurations with different TTLs
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("files", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("tasks", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("roles", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("contents", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }

    /**
     * StringRedisTemplate for session management and simple key-value operations
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serializers for both keys and values
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }


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
//    JVM passed out :v, so I use Redis as the only cache manager

}
