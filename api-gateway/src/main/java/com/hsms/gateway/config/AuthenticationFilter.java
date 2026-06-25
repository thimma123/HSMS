package com.hsms.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    private final RouterValidator validator;
    private final JwtUtil jwtUtil;

    public AuthenticationFilter(RouterValidator validator, JwtUtil jwtUtil) {
        super(Config.class);
        this.validator = validator;
        this.jwtUtil = jwtUtil;
    }

    public static class Config {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (!validator.isSecured(request)) {
                return chain.filter(exchange);
            }

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return handleUnauthorized(exchange);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authHeader = authHeader.substring(7);
            }

            try {
                jwtUtil.validateToken(authHeader);
            } catch (Exception ex) {
                log.error("Token validation failed: {}", ex.getMessage());
                return handleUnauthorized(exchange);
            }

            Long userId = jwtUtil.extractUserId(authHeader);
            String email = jwtUtil.extractUsername(authHeader);
            String roleName = jwtUtil.extractRole(authHeader);
            if (roleName == null) {
                return handleForbidden(exchange);
            }
            String role = roleName.toUpperCase();
            if (role.startsWith("ROLE_")) {
                role = role.substring(5);
            }

            String path = request.getURI().getPath();
            String method = request.getMethod().name();

            log.info("Request userId={}, email={}, path={}, method={}, role={}", userId, email, path, method, role);

            if (!isAuthorized(path, method, role)) {
                return handleForbidden(exchange);
            }

            return chain.filter(exchange);
        };
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> handleForbidden(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }

    private boolean isAuthorized(String path, String method, String role) {
        if (path.startsWith("/api/customers")) {
            return checkCustomerAccess(role);
        }
        if (path.startsWith("/api/technicians")) {
            return checkTechnicianAccess(method, role);
        }
        if (path.startsWith("/api/service-requests")) {
            return checkServiceRequestAccess(path, method, role);
        }
        return isAuthorizedAdditional(path, method, role);
    }

    private boolean isAuthorizedAdditional(String path, String method, String role) {
        if (path.startsWith("/api/categories")) {
            return checkCategoryAccess(method, role);
        }
        if (path.startsWith("/api/assignments")) {
            return checkAssignmentAccess(path, method, role);
        }
        if (path.startsWith("/api/records")) {
            return checkExecutionRecordAccess(path, method, role);
        }
        return isAuthorizedRemaining(path, method, role);
    }

    private boolean isAuthorizedRemaining(String path, String method, String role) {
        if (path.startsWith("/api/payments")) {
            return checkPaymentAccess(path, method, role);
        }
        if (path.startsWith("/api/feedback")) {
            return checkFeedbackAccess(path, method, role);
        }
        if (path.startsWith("/api/analytics")) {
            return checkAnalyticsAccess(role);
        }
        return true;
    }

    private boolean checkCustomerAccess(String role) {
        return Roles.CUSTOMER.equals(role)
                || Roles.ADMIN.equals(role)
                || Roles.SERVICE_MANAGER.equals(role);
    }

    private boolean checkTechnicianAccess(String method, String role) {
        if (HttpMethod.POST.name().equalsIgnoreCase(method)) {
            return Roles.ADMIN.equals(role)
                    || Roles.TECHNICIAN.equals(role)
                    || Roles.SERVICE_MANAGER.equals(role);
        }
        if (HttpMethod.GET.name().equalsIgnoreCase(method)) {
            return Roles.ADMIN.equals(role)
                    || Roles.SERVICE_MANAGER.equals(role)
                    || Roles.CUSTOMER.equals(role)
                    || Roles.TECHNICIAN.equals(role);
        }
        if (HttpMethod.PUT.name().equalsIgnoreCase(method)) {
            return Roles.ADMIN.equals(role)
                    || Roles.SERVICE_MANAGER.equals(role);
        }
        if (HttpMethod.DELETE.name().equalsIgnoreCase(method)) {
            return Roles.ADMIN.equals(role);
        }
        return false;
    }

    private boolean checkServiceRequestAccess(String path, String method, String role) {
        if (HttpMethod.POST.name().equalsIgnoreCase(method)) {
            return Roles.CUSTOMER.equals(role);
        }
        if (HttpMethod.GET.name().equalsIgnoreCase(method)) {
            return checkServiceRequestGetAccess(path, role);
        }
        if (HttpMethod.PUT.name().equalsIgnoreCase(method)) {
            return checkServiceRequestPutAccess(path, role);
        }
        return false;
    }

    private boolean checkServiceRequestGetAccess(String path, String role) {
        if (path.equals("/api/service-requests/my-requests")) {
            return Roles.CUSTOMER.equals(role);
        }
        if (path.matches("/api/service-requests/technician/\\d+")) {
            return Roles.TECHNICIAN.equals(role)
                    || Roles.ADMIN.equals(role)
                    || Roles.SERVICE_MANAGER.equals(role);
        }
        if (path.equals("/api/service-requests") || path.equals("/api/service-requests/")) {
            return Roles.ADMIN.equals(role)
                    || Roles.SERVICE_MANAGER.equals(role);
        }
        return Roles.CUSTOMER.equals(role)
                || Roles.TECHNICIAN.equals(role)
                || Roles.ADMIN.equals(role)
                || Roles.SERVICE_MANAGER.equals(role);
    }

    private boolean checkServiceRequestPutAccess(String path, String role) {
        if (path.matches("/api/service-requests/\\d+/status")) {
            return Roles.ADMIN.equals(role);
        }
        if (path.endsWith("/cancel") || path.matches("/api/service-requests/\\d+")) {
            return Roles.CUSTOMER.equals(role);
        }
        return Roles.ADMIN.equals(role)
                || Roles.SERVICE_MANAGER.equals(role)
                || Roles.TECHNICIAN.equals(role);
    }

    private boolean checkCategoryAccess(String method, String role) {
        if (HttpMethod.POST.name().equalsIgnoreCase(method)
                || HttpMethod.PUT.name().equalsIgnoreCase(method)
                || HttpMethod.DELETE.name().equalsIgnoreCase(method)
                || HttpMethod.PATCH.name().equalsIgnoreCase(method)) {
            return Roles.ADMIN.equals(role);
        }
        return true;
    }

    private boolean checkAssignmentAccess(String path, String method, String role) {
        if (HttpMethod.POST.name().equalsIgnoreCase(method)) {
            return Roles.SERVICE_MANAGER.equals(role);
        }
        if (HttpMethod.PUT.name().equalsIgnoreCase(method)) {
            return checkAssignmentPutAccess(path, role);
        }
        if (HttpMethod.DELETE.name().equalsIgnoreCase(method)) {
            return Roles.SERVICE_MANAGER.equals(role);
        }
        if (HttpMethod.GET.name().equalsIgnoreCase(method)) {
            return checkAssignmentGetAccess(path, role);
        }
        return false;
    }

    private boolean checkAssignmentPutAccess(String path, String role) {
        if (path.endsWith("/accept") || path.endsWith("/reject")) {
            return Roles.TECHNICIAN.equals(role);
        }
        if (path.endsWith("/reassign")) {
            return Roles.SERVICE_MANAGER.equals(role);
        }
        return false;
    }

    private boolean checkAssignmentGetAccess(String path, String role) {
        if (path.matches("/api/assignments/technician/\\d+")) {
            return Roles.TECHNICIAN.equals(role)
                    || Roles.ADMIN.equals(role)
                    || Roles.SERVICE_MANAGER.equals(role);
        }
        return Roles.ADMIN.equals(role)
                || Roles.SERVICE_MANAGER.equals(role);
    }

    private boolean checkExecutionRecordAccess(String path, String method, String role) {
        if (HttpMethod.POST.name().equalsIgnoreCase(method) && path.endsWith("/start")) {
            return Roles.TECHNICIAN.equals(role);
        }
        if (HttpMethod.PUT.name().equalsIgnoreCase(method) && path.contains("/complete")) {
            return Roles.TECHNICIAN.equals(role);
        }
        if (HttpMethod.GET.name().equalsIgnoreCase(method)) {
            return Roles.TECHNICIAN.equals(role)
                    || Roles.ADMIN.equals(role)
                    || Roles.SERVICE_MANAGER.equals(role);
        }
        return false;
    }

    private boolean checkPaymentAccess(String path, String method, String role) {
        if (HttpMethod.POST.name().equalsIgnoreCase(method) && path.endsWith("/save")) {
            return Roles.CUSTOMER.equals(role);
        }
        if (HttpMethod.PUT.name().equalsIgnoreCase(method) || HttpMethod.DELETE.name().equalsIgnoreCase(method)) {
            return Roles.ADMIN.equals(role);
        }
        if (HttpMethod.GET.name().equalsIgnoreCase(method)) {
            return checkPaymentGetAccess(path, role);
        }
        return false;
    }

    private boolean checkPaymentGetAccess(String path, String role) {
        if (path.endsWith("/all")) {
            return Roles.ADMIN.equals(role);
        }
        return Roles.CUSTOMER.equals(role)
                || Roles.ADMIN.equals(role)
                || Roles.SERVICE_MANAGER.equals(role);
    }

    private boolean checkFeedbackAccess(String path, String method, String role) {
        if (HttpMethod.POST.name().equalsIgnoreCase(method)) {
            return Roles.CUSTOMER.equals(role);
        }
        if (HttpMethod.DELETE.name().equalsIgnoreCase(method)) {
            return Roles.ADMIN.equals(role);
        }
        if (HttpMethod.GET.name().equalsIgnoreCase(method)
                && (path.equals("/api/feedback") || path.equals("/api/feedback/"))) {
            return Roles.ADMIN.equals(role);
        }
        return true;
    }

    private boolean checkAnalyticsAccess(String role) {
        return Roles.ADMIN.equals(role);
    }
}