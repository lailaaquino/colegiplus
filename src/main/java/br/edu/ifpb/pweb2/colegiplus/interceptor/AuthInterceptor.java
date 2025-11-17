package br.edu.ifpb.pweb2.colegiplus.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        String uri = request.getRequestURI();
        HttpSession session = request.getSession(false);

        if (uri.startsWith("/auth")
                || uri.startsWith("/css")
                || uri.startsWith("/js")
                || uri.startsWith("/imagens")){
            return true;
        }

        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect("/auth");
            return false;
        }
        return true;
    }
}