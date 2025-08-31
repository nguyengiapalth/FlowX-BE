package project.ii.flowx.aspect.redis.annotation;


import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HashCacheable {
    String hash(); // Redis hash key, ví dụ: "content_map"
    String key();  // SpEL expression to extract id, ví dụ: "#id"
}
