package tg.edtch.activEducation.shared.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tg.edtch.activEducation.shared.security.filter.JwtAuthenticationFilter;
import tg.edtch.activEducation.shared.security.filter.RateLimitingFilter;
import tg.edtch.activEducation.shared.security.userdetails.UtilisateurDetailsService;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Active @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthFilter;
        private final RateLimitingFilter rateLimitingFilter;
        private final UtilisateurDetailsService userDetailsService;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(AbstractHttpConfigurer::disable)
                                .headers(headers -> headers
                                                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                                                .xssProtection(xss -> xss.disable()) // Remplacé par CSP souvent
                                                .contentSecurityPolicy(
                                                                csp -> csp.policyDirectives(
                                                                                "default-src 'self'; frame-ancestors 'none';"))
                                                .httpStrictTransportSecurity(hsts -> hsts
                                                                .includeSubDomains(true)
                                                                .maxAgeInSeconds(31536000)))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                // Endpoints Publics Auth & Registration
                                                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login",
                                                                "/api/v1/auth/refresh", "/api/v1/auth/forgot-password",
                                                                "/api/v1/auth/otp/verify",
                                                                "/api/v1/auth/reset-password",
                                                                "/api/v1/auth/2fa/validate",
                                                                "/api/v1/eleves",
                                                                "/api/v1/parents")
                                                .permitAll()

                                                // Bibliothèque Publique (Lecture)
                                                .requestMatchers(HttpMethod.GET,
                                                                "/api/v1/bibliotheque/etablissements/**",
                                                                "/api/v1/bibliotheque/filieres/**",
                                                                "/api/v1/bibliotheque/metiers/**",
                                                                "/api/v1/bibliotheque/series/**",
                                                                "/api/v1/bibliotheque/recherche-fiche-ia/**",
                                                                "/api/v1/bibliotheque/faq/**")
                                                .permitAll()

                                                // Error dispatch (évite 401 sur les erreurs de validation)
                                                .requestMatchers("/error").permitAll()

                                                // WebSocket Chat (auth via token en query param)
                                                .requestMatchers("/ws/chat").permitAll()

                                                // Simulateur de parcours
                                                .requestMatchers(HttpMethod.POST, "/api/v1/simulateur/**")
                                                .authenticated()

                                                // Assistant Vocal
                                                .requestMatchers(HttpMethod.POST, "/api/v1/vocal/transcrire",
                                                                "/api/v1/vocal/chat",
                                                                "/api/v1/vocal/synthese")
                                                .authenticated()

                                                // Santé Actuator
                                                .requestMatchers("/actuator/health").permitAll()

                                                // Téléchargement fichiers (photos profil, etc.)
                                                .requestMatchers(HttpMethod.GET, "/files/download/IMAGE/**").permitAll()
                                                .requestMatchers("/files/download/**").authenticated()
                                                .requestMatchers("/files/stream/**").authenticated()
                                                .requestMatchers("/files/metadata/**").authenticated()
                                                .requestMatchers("/files/pdf/**").authenticated()
                                                .requestMatchers("/files/presigned-url/**").authenticated()
                                                .requestMatchers("/files/url/**").authenticated()

                                                // Swagger & OpenApi (dev)
                                                .requestMatchers("/api-docs/**", "/v3/api-docs/**",
                                                                "/swagger-ui/**", "/swagger-ui.html")
                                                .permitAll()

                                                // ─── Exceptions : accès utilisateur (avant les règles ADMIN) ───
                                                // Profil : un élève peut modifier son propre profil (vérifié par @PreAuthorize)
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/eleves/*",
                                                                "/api/v1/parents/*",
                                                                "/api/v1/conseillers/*")
                                                .authenticated()
                                                // Notes : un élève ou un conseiller peut ajouter/modifier des notes
                                                .requestMatchers(HttpMethod.POST, "/api/v1/eleves/*/notes")
                                                .authenticated()
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/notes/**")
                                                .authenticated()
                                                // Favoris : tout utilisateur peut ajouter/supprimer ses favoris
                                                .requestMatchers(HttpMethod.POST, "/api/v1/bibliotheque/favoris")
                                                .authenticated()
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/bibliotheque/favoris/**")
                                                .authenticated()

                                                // Parents : auto-lier/délier leurs enfants (vérifié par @PreAuthorize)
                                                .requestMatchers(HttpMethod.POST,
                                                                "/api/v1/parents/*/enfants/*")
                                                .authenticated()
                                                .requestMatchers(HttpMethod.DELETE,
                                                                "/api/v1/parents/*/enfants/*")
                                                .authenticated()

                                                // Documents : un élève peut gérer ses propres documents (vérifié par @PreAuthorize)
                                                .requestMatchers(HttpMethod.GET, "/api/v1/eleves/*/documents",
                                                                "/api/v1/eleves/*/documents/**")
                                                .authenticated()
                                                .requestMatchers(HttpMethod.POST, "/api/v1/eleves/*/documents",
                                                                "/api/v1/eleves/*/documents/**")
                                                .authenticated()
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/eleves/*/documents/**")
                                                .authenticated()

                                                // Relevé de notes : l'élève peut uploader son propre relevé (vérifié par @PreAuthorize)
                                                .requestMatchers(HttpMethod.POST, "/api/v1/eleves/*/releve-notes")
                                                .authenticated()

                                                // Bulletins scolaires (Chantier C) : l'élève peut uploader ses propres
                                                // bulletins. Sécurité fine via @PreAuthorize (isOwner ou ADMIN).
                                                .requestMatchers(HttpMethod.GET, "/api/v1/eleves/*/bulletins")
                                                .authenticated()
                                                .requestMatchers(HttpMethod.POST, "/api/v1/eleves/*/bulletins",
                                                                "/api/v1/eleves/*/bulletins/batch")
                                                .authenticated()

                                                // Règles Administrateurs Globales appliquées ici (les autres rôles
                                                // seront gérés via @PreAuthorize pour une granularité fine)
                                                .requestMatchers(HttpMethod.POST, "/api/v1/eleves/**",
                                                                "/api/v1/parents/**",
                                                                "/api/v1/conseillers/**", "/api/v1/administrateurs/**")
                                                .hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/eleves/**",
                                                                "/api/v1/parents/**",
                                                                "/api/v1/conseillers/**", "/api/v1/administrateurs/**")
                                                .hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/eleves/**",
                                                                "/api/v1/parents/**",
                                                                "/api/v1/conseillers/**", "/api/v1/administrateurs/**")
                                                .hasRole("ADMIN")

                                                // Bibliothèque & FAQ (Ecriture) par Admin
                                                .requestMatchers(HttpMethod.POST, "/api/v1/bibliotheque/**")
                                                .hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/bibliotheque/**")
                                                .hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/bibliotheque/**")
                                                .hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/api/v1/bibliotheque/analytics/**")
                                                .hasRole("ADMIN")

                                                // Génération de quiz : tous les utilisateurs authentifiés peuvent lancer un quiz sur une fiche
                                                .requestMatchers(HttpMethod.POST, "/api/v1/quiz/generate")
                                                .authenticated()

                                                // Diagnostique Settings (Admin)
                                                .requestMatchers(HttpMethod.POST, "/api/v1/quiz/**",
                                                                "/api/v1/questions/**",
                                                                "/api/v1/reponses/**",
                                                                "/api/v1/score-matrices/**",
                                                                "/api/v1/seuils-admission/**")
                                                .hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/quiz/**",
                                                                "/api/v1/questions/**",
                                                                "/api/v1/reponses/**",
                                                                "/api/v1/score-matrices/**",
                                                                "/api/v1/seuils-admission/**")
                                                .hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/quiz/**",
                                                                "/api/v1/questions/**",
                                                                "/api/v1/reponses/**",
                                                                "/api/v1/score-matrices/**",
                                                                "/api/v1/seuils-admission/**")
                                                .hasRole("ADMIN")

                                                // Tout le reste nécessite d'être authentifié
                                                .anyRequest().authenticated())
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                                .addFilterBefore(rateLimitingFilter, JwtAuthenticationFilter.class)
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint((request, response, authException) -> response
                                                                .sendError(401, "Non authentifié"))
                                                .accessDeniedHandler(
                                                                (request, response, accessDeniedException) -> response
                                                                                .sendError(403, "Accès refusé")));

                return http.build();
        }

        @Bean
        public AuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
                provider.setPasswordEncoder(passwordEncoder());
                return provider;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder(12);
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOriginPatterns(List.of(
                        "http://localhost:*",
                        "https://localhost:*",
                        "http://127.0.0.1:*",
                        "https://*.activeducation.tg",
                        "capacitor://localhost",
                        "ionic://localhost"
                ));
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "x-retry-after-refresh"));
                configuration.setAllowCredentials(true);
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
