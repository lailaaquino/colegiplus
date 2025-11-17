package br.edu.ifpb.pweb2.colegiplus.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

import br.edu.ifpb.pweb2.colegiplus.interceptor.AuthInterceptor;

@Configuration
public class InterceptorConfiguration implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry
            .addInterceptor(authInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns(
                "/auth/**",
                "/css/**",
                "/js/**",
                "/imagens/**",
                "/home"
            );
    }
}
