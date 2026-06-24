package tg.edtch.activEducation.shared.config;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class MaintenanceFilter implements Filter {

    private static boolean maintenanceMode = false;
    private static String maintenanceMessage = "Plateforme en maintenance. Revenez dans quelques instants.";
    private static final String[] ADMIN_SUBNETS = {"127.0.0.1", "0:0:0:0:0:0:0:1", "10.", "172.16.", "192.168."};

    public static void setMaintenanceMode(boolean mode) {
        maintenanceMode = mode;
        log.warn("Mode maintenance : {}", mode ? "ACTIVÉ" : "DÉSACTIVÉ");
    }

    public static boolean isMaintenanceMode() {
        return maintenanceMode;
    }

    public static void setMaintenanceMessage(String message) {
        if (message != null && !message.isBlank()) {
            maintenanceMessage = message;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!maintenanceMode) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();
        String ip = req.getRemoteAddr();

        if (path.equals("/api/v1/admin/maintenance") || estIpPrivee(ip)) {
            chain.doFilter(request, response);
            return;
        }

        res.setStatus(503);
        res.setContentType("application/json");
        res.getWriter().write("{\"message\":\"" + maintenanceMessage + "\"}");
    }

    private boolean estIpPrivee(String ip) {
        for (String subnet : ADMIN_SUBNETS) {
            if (ip.startsWith(subnet)) return true;
        }
        return false;
    }
}
