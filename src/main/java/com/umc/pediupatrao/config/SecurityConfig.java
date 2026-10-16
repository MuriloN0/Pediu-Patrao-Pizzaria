package com.umc.pediupatrao.config;

import com.umc.pediupatrao.service.UsuarioDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UsuarioDetailsService usuarioDetailsService;

    public SecurityConfig(UsuarioDetailsService usuarioDetailsService) {
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(usuarioDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/dist/**", "/plugins/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/usuarios", "/usuarios/**", "/api/usuarios", "/api/usuarios/**", "/admin").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/clientes", "/api/clientes").hasAnyRole("GERENTE", "ATENDENTE")
                        .requestMatchers(HttpMethod.GET, "/clientes/novo", "/clientes/editar/**").hasRole("ATENDENTE")
                        .requestMatchers(HttpMethod.POST,"/clientes/salvar", "/clientes/editar/**").hasRole("ATENDENTE")
                        .requestMatchers(HttpMethod.POST,"/api/clientes").hasRole("ATENDENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/clientes/*").hasRole("ATENDENTE")
                        .requestMatchers("/clientes/**", "/api/clientes/**").denyAll()
                        .requestMatchers(HttpMethod.GET, "/pedidos","/api/pedidos").hasAnyRole("ADMIN", "GERENTE", "ATENDENTE")
                        .requestMatchers(HttpMethod.POST,"/api/pedidos").hasRole("ATENDENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/*/status").hasRole("GERENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/*/desconto", "/api/pedidos/*/cancelamento").hasRole("GERENTE")
                        .requestMatchers( "/pedidos/**", "/api/pedidos/**").denyAll()
                        .requestMatchers(HttpMethod.GET, "/auditoria").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.GET, "/produtos").hasAnyRole("ATENDENTE", "GERENTE")
                        .requestMatchers(HttpMethod.POST, "/produtos/salvar").hasAnyRole("ATENDENTE", "GERENTE")
                        .requestMatchers(HttpMethod.GET, "/api/produtos").hasAnyRole("ADMIN", "GERENTE", "ATENDENTE")
                        .requestMatchers(HttpMethod.POST, "/api/produtos").hasAnyRole("ATENDENTE", "GERENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/produtos/*").hasAnyRole("ATENDENTE", "GERENTE")
                        .requestMatchers(HttpMethod.DELETE, "/api/produtos/*").hasAnyRole("ATENDENTE", "GERENTE")
                        .requestMatchers("/auditoria", "/auditoria/**", "/api/auditoria", "/api/auditoria/**").denyAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .successHandler(authenticationSuccessHandler())
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            response.sendRedirect("/"); // Redireciona para a página inicial após login bem-sucedido
        };
    }
}
