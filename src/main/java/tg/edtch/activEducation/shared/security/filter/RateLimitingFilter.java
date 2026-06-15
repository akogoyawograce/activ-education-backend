package tg.edtch.activEducation.shared.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;
    private final long loginMax;
    private final long loginWindowMinutes;
    private final long refreshMax;
    private final long refreshWindowMinutes;
    private final long apiMax;
    private final long apiWindowMinutes;

    public RateLimitingFilter(StringRedisTemplate redisTemplate,
                              @Value("${rate.limit.login.max}") long loginMax,
                              @Value("${rate.limit.login.window-minutes}") long loginWindowMinutes,
                              @Value("${rate.limit.refresh.max}") long refreshMax,
                              @Value("${rate.limit.refresh.window-minutes}") long refreshWindowMinutes,
                              @Value("${rate.limit.api.max}") long apiMax,
                              @Value("${rate.limit.api.window-minutes}") long apiWindowMinutes) {
        this.redisTemplate = redisTemplate;
        this.loginMax = loginMax;
        this.loginWindowMinutes = loginWindowMinutes;
        this.refreshMax = refreshMax;
        this.refreshWindowMinutes = refreshWindowMinutes;
        this.apiMax = apiMax;
        this.apiWindowMinutes = apiWindowMinutes;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String ip = request.getRemoteAddr();
        String uri = request.getRequestURI();

        String key;
        long limit;
        Duration window;

        if (uri.equals("/api/v1/auth/login")) {
            key = "rate:login:" + ip;
            limit = loginMax;
            window = Duration.ofMinutes(loginWindowMinutes);
        } else if (uri.equals("/api/v1/auth/refresh")) {
            key = "rate:refresh:" + ip;
            limit = refreshMax;
            window = Duration.ofMinutes(refreshWindowMinutes);
        } else {
            key = "rate:api:" + ip;
            limit = apiMax;
            window = Duration.ofMinutes(apiWindowMinutes);
        }

        Long count;
        try {
            count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                redisTemplate.expire(key, window);
            }
        } catch (Exception e) {
            // Si Redis plante, on ne bloque pas les requêtes légitimes (fail-open)
            filterChain.doFilter(request, response);
            return;
        }

        if (count != null && count > limit) {
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(window.getSeconds()));
            response.getWriter().write("Too Many Requests - Try again later");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
