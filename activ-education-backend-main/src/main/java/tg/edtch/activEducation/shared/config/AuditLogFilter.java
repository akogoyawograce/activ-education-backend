package tg.edtch.activEducation.shared.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import tg.edtch.activEducation.shared.util.AuditLogService;

import java.io.IOException;

@Component
@Order(5)
@RequiredArgsConstructor
public class AuditLogFilter implements Filter {

    private final AuditLogService auditLogService;

    private static final java.util.Set<String> MUTATING_METHODS = java.util.Set.of("POST", "PUT", "PATCH", "DELETE");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        chain.doFilter(request, response);

        if (request instanceof HttpServletRequest httpReq && MUTATING_METHODS.contains(httpReq.getMethod())) {
            String uri = httpReq.getRequestURI();
            if (!uri.startsWith("/api/v1/")) return;

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth != null && auth.isAuthenticated()
                    ? auth.getName() : "anonymous";

            String action = switch (httpReq.getMethod()) {
                case "POST" -> "CREATION";
                case "PUT", "PATCH" -> "MODIFICATION";
                case "DELETE" -> "SUPPRESSION";
                default -> "AUTRE";
            };

            try {
                auditLogService.log(email, "", action, uri, "",
                        httpReq.getRemoteAddr(), httpReq.getHeader("User-Agent"));
            } catch (Exception ignored) {
            }
        }
    }
}
