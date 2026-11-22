package com.api.gateway.pokemon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;

@Configuration
public class RouteConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .pathMatchers(
                                "/auth/**",
                                "/login/**",
                                "/v1/auth/**",
                                "/v1/auth",
                                "/v1/create",
                                "/v1/create/**",
                                "/v1/logout",
                                "/v1/logout/**",
                                "/usuario/cadastrar"
                        ).permitAll()
                        // Listar e deletar usuários é só para administradores.
                        // Pesquisar e atualizar o próprio perfil continuam liberados para qualquer usuário logado (o frontend usa).
                        .pathMatchers(HttpMethod.GET, "/usuario/listar").hasAuthority("ROLE_ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/usuario/**").hasAuthority("ROLE_ADMIN")
                        // Gerenciar cards e baixar estoque é só para administradores.
                        // Consultar cards (select e select-all) continua liberado para qualquer usuário logado.
                        .pathMatchers(
                                "/card-pokemon/save",
                                "/card-pokemon/update",
                                "/card-pokemon/delete/**",
                                "/card-pokemon/stock-out"
                        ).hasAuthority("ROLE_ADMIN")
                        .anyExchange().authenticated()
                ).oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))
                .build();
    }

    @Bean
    public ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthoritiesClaimName("roles");
        converter.setAuthorityPrefix("");

        ReactiveJwtAuthenticationConverter reactiveConverter = new ReactiveJwtAuthenticationConverter();
        reactiveConverter.setJwtGrantedAuthoritiesConverter(
                jwt -> Flux.fromIterable(converter.convert(jwt))
        );

        return reactiveConverter;
    }
}