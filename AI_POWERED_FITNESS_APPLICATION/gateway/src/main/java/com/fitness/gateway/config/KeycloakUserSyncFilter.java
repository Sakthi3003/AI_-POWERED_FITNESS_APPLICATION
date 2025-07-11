package com.fitness.gateway.config;

import com.fitness.gateway.user.UserService;

import com.fitness.gateway.user.model.RegisterRequest;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class KeycloakUserSyncFilter implements WebFilter {
    private final UserService userService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        RegisterRequest request = getUser(token);

        if(userId == null){
            userId = request.getKeycloakId();
        }
        if (request.getKeycloakId()!= null && token != null) {
            val userService1 = userService;
            String finalUserId = userId;
            return userService1.validateUser(request.getKeycloakId())
                    .flatMap(exist -> {
                        if (!exist) {
//                            RegisterRequest request = getUser(token);
                            if(request!=null){
                                return userService.registerUser(request)
                                .then(Mono.empty());
                            }
                        } else {
                            log.info("User {} already exist", request.getKeycloakId());
                            return Mono.empty();
                        }
                        return Mono.empty();
                    })
                    .then(Mono.defer(() -> {
                        ServerHttpRequest mutatedRequest = exchange.getRequest()
                                .mutate()
                                .header("X-User-Id", finalUserId)
                                .build();
                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    }));
        }
        return chain.filter(exchange);
    }

    private RegisterRequest getUser(String token) {
        try{
            String tokenWithoutBearer = token.replace("Bearer", "").trim();
            SignedJWT signedJWT = SignedJWT.parse(tokenWithoutBearer);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            RegisterRequest register = new RegisterRequest();
            register.setEmail(claims.getStringClaim("email"));
            register.setKeycloakId(claims.getStringClaim("sub"));
            String toekn = claims.getStringClaim("sub");
            System.out.println(token);
            register.setLastName(claims.getStringClaim("given_name"));
            register.setFirstName(claims.getStringClaim("family_name"));
            register.setPassword(claims.getStringClaim("1234"));
            return register;

        }catch(Exception ex){
            log.info("");
            return null;
        }
    }

}
