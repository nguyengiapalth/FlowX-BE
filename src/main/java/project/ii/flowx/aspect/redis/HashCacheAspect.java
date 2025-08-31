package project.ii.flowx.aspect.redis;

import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class HashCacheAspect {
//
//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;
//
//    private String getFieldKey(JoinPoint joinPoint, String spelKey) {
//        EvaluationContext context = new StandardEvaluationContext();
//        Object[] args = joinPoint.getArgs();
//        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
//        for (int i = 0; i < args.length; i++) {
//            context.setVariable(paramNames[i], args[i]);
//        }
//        ExpressionParser parser = new SpelExpressionParser();
//        return parser.parseExpression(spelKey).getValue(context, String.class);
//    }
//
//    @Around("@annotation(hashCacheable)")
//    public Object cache(ProceedingJoinPoint joinPoint, HashCacheable hashCacheable) throws Throwable {
//        String hash = hashCacheable.hash();
//        String fieldKey = getFieldKey(joinPoint, hashCacheable.key());
//
//        Object cached = redisTemplate.opsForHash().get(hash, fieldKey);
//        if (cached != null) {
//            return cached;
//        }
//
//        Object result = joinPoint.proceed();
//        if (result != null) {
//            redisTemplate.opsForHash().put(hash, fieldKey, result);
//        }
//
//        return result;
//    }
//
//    @AfterReturning(value = "@annotation(hashCachePut)", returning = "result")
//    public void put(JoinPoint joinPoint, HashCachePut hashCachePut, Object result) {
//        String hash = hashCachePut.hash();
//        String fieldKey = getFieldKey(joinPoint, hashCachePut.key());
//        if (result != null) {
//            redisTemplate.opsForHash().put(hash, fieldKey, result);
//        }
//    }
//
//    @After("@annotation(hashCacheEvict)")
//    public void evict(JoinPoint joinPoint, HashCacheEvict hashCacheEvict) {
//        String hash = hashCacheEvict.hash();
//        String fieldKey = getFieldKey(joinPoint, hashCacheEvict.key());
//        redisTemplate.opsForHash().delete(hash, fieldKey);
//    }
}
