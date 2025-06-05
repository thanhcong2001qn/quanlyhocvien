package com.dacs.quanlyhocvien.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private RedirectAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private RoleBasedAuthenticationSuccessHandler successHandler;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(authenticationEntryPoint)  // Sử dụng custom entry point
                )
                .authorizeHttpRequests(authorize -> authorize
                        // URLs công khai
                        .requestMatchers(
                                "/", "/home", "/register", "/login", "/css/**", "/js/**", "/images/**",
                                "/api/login", "/api/register", "/api/verify-token", "/forgot-password", "/verify-account", "/api/resend-verification-email"
                        ).permitAll()
                        // URLs chỉ dành cho ADMIN
                        .requestMatchers("/api/enrollments/**").hasAuthority("ROLE_STUDENT")
                        // Đường dẫn dashboard cần xác thực
                        .requestMatchers("/dashboard").authenticated()
                        // Mọi yêu cầu khác đều cần xác thực
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .loginProcessingUrl("/perform-login") // URL để xử lý thông tin đăng nhập
                        .successHandler(successHandler)
                        .failureHandler((request, response, exception) -> {
                            // Lưu loại lỗi để hiển thị thông báo phù hợp
                            String errorMessage;
                            if (exception instanceof BadCredentialsException) {
                                errorMessage = "Sai tên đăng nhập hoặc mật khẩu";
                            } else if (exception instanceof LockedException) {
                                errorMessage = "Tài khoản đã bị khóa";
                            } else if (exception instanceof DisabledException) {
                                errorMessage = "Tài khoản chưa được kích hoạt";
                            } else if (exception instanceof AccountExpiredException) {
                                errorMessage = "Tài khoản đã hết hạn";
                            } else {
                                errorMessage = "Đăng nhập thất bại: " + exception.getMessage();
                            }

                            // Lưu thông báo lỗi vào session
                            HttpSession session = request.getSession();
                            session.setAttribute("SPRING_SECURITY_LAST_EXCEPTION_MESSAGE", errorMessage);

                            // Chuyển hướng đến trang đăng nhập
                            response.sendRedirect(request.getContextPath() + "/login?error");
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .permitAll()
                );

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));  // Cho phép tất cả origin, thay đổi nếu cần
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
