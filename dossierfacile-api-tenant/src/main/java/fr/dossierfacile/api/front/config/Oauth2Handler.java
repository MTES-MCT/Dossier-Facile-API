package fr.dossierfacile.api.front.config;

import ch.qos.logback.classic.Level;
import fr.dossierfacile.common.utils.LoggerUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.util.Collections;

@Slf4j
@Configuration
public class Oauth2Handler {

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            LoggerUtil.prepareMDCForHttpRequest(request, Collections.emptyMap());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            LoggerUtil.addRequestStatusToMdc(HttpServletResponse.SC_UNAUTHORIZED);

            var logMessage = String.format(
                    "Request completed: URI:%s, Method:%s, Status:%d",
                    request.getRequestURI(),
                    request.getMethod(),
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            LoggerUtil.sendEnrichedLogs(Level.WARN, logMessage);
        };
    }

}
