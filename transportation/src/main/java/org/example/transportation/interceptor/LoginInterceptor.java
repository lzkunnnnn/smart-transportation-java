package org.example.transportation.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.transportation.constant.MessageConstant;
import org.example.transportation.service.impl.AdminServiceImpl;
import org.example.transportation.service.impl.UserServiceImpl;
import org.example.transportation.util.JwtClaimsConstant;
import org.example.transportation.util.JwtUtil;
import org.example.transportation.util.ThreadLocalUtil;
import org.example.transportation.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private ObjectMapper objectMapper;

    private static final List<String> ALLOWED_PATHS = Arrays.asList(
            "/api/user/login",
            "/api/user/register",
            "/api/admin/login",
            "/api/sensor/**",
            "/api/event/**",
            "/api/relation/**"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        String path = request.getRequestURI();

        for (String allowedPath : ALLOWED_PATHS) {
            if (allowedPath.endsWith("/**")) {
                String prefix = allowedPath.substring(0, allowedPath.length() - 3);
                if (path.startsWith(prefix)) {
                    return true;
                }
            } else if (path.equals(allowedPath)) {
                return true;
            }
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token == null || token.isEmpty()) {
            sendErrorResponse(response, 401, MessageConstant.NOT_LOGIN);
            return false;
        }

        try {
            Map<String, Object> claims = JwtUtil.parseToken(token);
            String role = (String) claims.get(JwtClaimsConstant.ROLE);

            boolean isValidToken = false;
            if ("ADMIN".equals(role)) {
                isValidToken = AdminServiceImpl.validateToken(token);
            } else if ("USER".equals(role)) {
                isValidToken = UserServiceImpl.validateToken(token);
            }

            if (!isValidToken) {
                sendErrorResponse(response, 401, MessageConstant.SESSION_EXPIRED);
                return false;
            }

            ThreadLocalUtil.set(claims);
            return true;
        } catch (Exception e) {
            sendErrorResponse(response, 401, MessageConstant.SESSION_EXPIRED);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ThreadLocalUtil.remove();
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        ResultVO result = ResultVO.error(status, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
