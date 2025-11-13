package apartment.example.backend.config;

import apartment.example.backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // Constructor injection via @RequiredArgsConstructor
    private final JwtAuthenticationFilter jwtAuthFilter;
    
    // Read CORS configuration from environment
    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // CORS preflight requests - must be first
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Public endpoints - no authentication required
                        .requestMatchers("/api/auth/**", "/health/**", "/ready", "/api/health").permitAll()
                        // Prometheus metrics endpoint - allow unauthenticated access for Prometheus scraping
                        .requestMatchers("/actuator/prometheus", "/actuator/health", "/actuator/info").permitAll()
                        
                        // Units - Read access for authenticated users, Write access for ADMIN only
                        .requestMatchers(HttpMethod.GET, "/units/**").authenticated()  // Any authenticated user can view
                        .requestMatchers("/units/**").hasAnyRole("ADMIN")  // Only ADMIN can create/update/delete
                        
                        // Rental Requests - User endpoints (MUST be before the catch-all admin rule)
                        .requestMatchers(HttpMethod.GET, "/rental-requests/me/latest").authenticated()  // User can check their own status
                        .requestMatchers(HttpMethod.POST, "/rental-requests/*/acknowledge").authenticated()  // User can acknowledge rejection (single path segment)
                        .requestMatchers(HttpMethod.POST, "/rental-requests/authenticated").authenticated()  // User can submit authenticated booking
                        .requestMatchers(HttpMethod.POST, "/rental-requests", "/rental-requests/unit/**").authenticated()  // Users can submit (legacy)
                        .requestMatchers("/rental-requests/**").hasAnyRole("ADMIN")  // ADMIN can view/approve/reject (catch-all)
                        
                        // Settings - Read access for authenticated, Write for ADMIN only
                        .requestMatchers(HttpMethod.GET, "/settings/**").authenticated()
                        .requestMatchers("/settings/**").hasAnyRole("ADMIN")
                        
                        // Admin-only endpoints
                        .requestMatchers("/admin/**", "/tenants/**", "/leases/**", 
                                       "/payments/**", "/maintenance-requests/**").hasAnyRole("ADMIN")
                        // Villager endpoints (villagers can access their own data)
                        .requestMatchers("/villager/**").hasAnyRole("VILLAGER", "ADMIN")
                        // All other requests must be authenticated
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Parse comma-separated origins from environment variable
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);  // เปลี่ยนจาก setAllowedOriginPatterns
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache preflight for 1 hour
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}