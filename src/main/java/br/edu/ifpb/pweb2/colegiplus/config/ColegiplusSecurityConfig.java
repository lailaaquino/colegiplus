package br.edu.ifpb.pweb2.colegiplus.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class ColegiplusSecurityConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/acesso-negado",
                                "/css/**", "/imagens/**", "/js/**")
                        .permitAll()
                        .requestMatchers("/alunos/**").hasRole("ADMIN")
                        .requestMatchers("/professores/**").hasRole("ADMIN")
                        .requestMatchers("/colegiados/**").hasRole("ADMIN")
                        .requestMatchers("/assuntos/**").hasRole("ADMIN")
                        .requestMatchers("/home/**").authenticated()
                        .requestMatchers("/processos/**").hasRole("PROFESSOR")
                        .requestMatchers("/reunioes/**").hasAnyRole("PROFESSOR", "COORDENADOR"))



                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .defaultSuccessUrl("/home", true)
                        .permitAll())
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/acesso-negado"))
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/login?logout")
                        .permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsManager userDetailsmDetailsManager() {
        JdbcUserDetailsManager users = new JdbcUserDetailsManager(dataSource);

        if (!users.userExists("admin")) {
            UserDetails admin = User.withUsername("admin")
                    .password(passwordEncoder().encode("admin"))
                    .roles("ADMIN")
                    .build();

            UserDetails sagan = User.withUsername("sagan")
                    .password(passwordEncoder().encode("cosmos"))
                    .roles("PROFESSOR")
                    .build();

            UserDetails candido = User.withUsername("candido")
                    .password(passwordEncoder().encode("cosmos"))
                    .roles("COORDENADOR")
                    .build();

            UserDetails pedro = User.withUsername("pedro")
                    .password(passwordEncoder().encode("cosmos"))
                    .roles("ALUNO")
                    .build();

            users.createUser(admin);
            users.createUser(sagan);
            users.createUser(candido);
            users.createUser(pedro);
        }
        return users;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsmDetailsManager());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}