package tg.edtch.activEducation.shared.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;

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
            limit = 5;
            window = Duration.ofMinutes(15);
        } else if (uri.equals("/api/v1/auth/refresh")) {
            key = "rate:refresh:" + ip;
            limit = 10;
            window = Duration.ofMinutes(5);
        } else {
            key = "rate:api:" + ip;
            limit = 100;
            window = Duration.ofMinutes(1);
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
