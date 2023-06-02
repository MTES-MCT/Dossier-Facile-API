package fr.gouv.bo.configuration;

import fr.gouv.bo.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
@Slf4j
public class BOConnectionContextFilter implements Filter {
    private static final String URI = "uri";
    private static final String EMAIL = "email";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        try {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            if ( !httpServletRequest.getRequestURI().matches("(/assets/|/js/|/css/|/fonts/|/webjars/:?).*") ) {
                MDC.put(URI, httpServletRequest.getRequestURI());

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.getPrincipal() != null && authentication.getPrincipal() instanceof UserPrincipal) {
                    MDC.put(EMAIL, ((UserPrincipal) authentication.getPrincipal()).getEmail());
                }
                log.info("Call " + httpServletRequest.getRequestURI());
            }
        } catch (Exception e) {
            // Something wrong but service should stay up
            log.error("Unable to inject data in MDC !!!");
        }
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(URI);
            MDC.remove(EMAIL);
        }
    }
}