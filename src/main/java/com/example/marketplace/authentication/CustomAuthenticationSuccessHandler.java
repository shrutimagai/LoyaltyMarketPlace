package com.example.marketplace.authentication;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * @author ankurmundra
 * October 23, 2021
 */
@Configuration
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        if(roles.isEmpty())
            response.sendRedirect("/");
        else
        {
            String role = roles.iterator().next();
            role = role.split("_")[1].toLowerCase();
            response.sendRedirect("/"+role);
        }
    }
}
