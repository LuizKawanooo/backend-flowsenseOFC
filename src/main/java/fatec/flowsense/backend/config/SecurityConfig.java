package fatec.flowsense.backend.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(authorize -> authorize
            		
            	.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()


        		.requestMatchers(HttpMethod.POST, "/push-token").authenticated()
        		.requestMatchers("/alertas/vazamento/**").authenticated()
        		.requestMatchers(HttpMethod.POST, "/push-teste").authenticated()
            	
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                .requestMatchers(HttpMethod.POST, "/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/users").permitAll()

                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/api/password/**").permitAll()
                .requestMatchers("/error").permitAll()

                // ESP32 envia fluxo sem login, mas com X-Device-Token
                .requestMatchers(HttpMethod.POST, "/sensores/fluxo").permitAll()

                // ESP32 consulta reset remoto sem login, mas com X-Device-Token
                .requestMatchers(HttpMethod.GET, "/devices/comando").permitAll()
                .requestMatchers(HttpMethod.POST, "/devices/reset-confirmado").permitAll()

                // App precisa estar logado
                .requestMatchers(HttpMethod.GET, "/sensores/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/devices/registrar").authenticated()
                .requestMatchers(HttpMethod.GET, "/devices/meus").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/devices/**").authenticated()
                .requestMatchers("/consumo/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/devices/registrar").authenticated()
                

                .anyRequest().authenticated()
            )

            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(Customizer.withDefaults())
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(Arrays.asList("*"));

        configuration.setAllowedMethods(
            Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")
        );

        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "ngrok-skip-browser-warning",
            "X-Device-Token"
        ));

        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type"
        ));

        configuration.setAllowCredentials(false);

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}