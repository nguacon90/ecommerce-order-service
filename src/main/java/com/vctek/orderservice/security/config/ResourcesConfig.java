package com.vctek.orderservice.security.config;

import com.vctek.security.BaseResourceConfig;
import com.vctek.service.TokenStoreService;
import com.vctek.service.UserService;
import com.vctek.service.impl.TokenStoreServiceImpl;
import com.vctek.service.impl.UserServiceImpl;
import com.vctek.util.AccountRoles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;

@Configuration
@EnableResourceServer
public class ResourcesConfig extends BaseResourceConfig {

    @Bean
    public TokenStoreService tokenStoreService() {
        return new TokenStoreServiceImpl(tokenStore());
    }

    @Bean
    public UserService userService() {
        UserServiceImpl userService = new UserServiceImpl();
        userService.setTokenStoreService(tokenStoreService());
        return userService;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.exceptionHandling()
                .accessDeniedHandler(new OAuth2AccessDeniedHandler())
                .and().authorizeRequests()
                .antMatchers("/actuator/**").permitAll()
                .antMatchers("/version").permitAll()
                .antMatchers("/health").permitAll()
                .antMatchers("/storefront/{\\d+}/orders/{\\d+}/cancel").hasAnyRole(AccountRoles.ROLE_MERCHANT.code(), AccountRoles.ROLE_EMPLOYEE.code(), AccountRoles.ROLE_CUSTOMER.code())
                .antMatchers("/storefront/**").permitAll()
                .antMatchers("/socket/**").permitAll()
                .antMatchers("/**").hasAnyRole(AccountRoles.ROLE_MERCHANT.code(), AccountRoles.ROLE_EMPLOYEE.code())
                .antMatchers(HttpMethod.GET, "/storefront/{\\d+}/orders/**").hasAnyRole(AccountRoles.ROLE_MERCHANT.code(), AccountRoles.ROLE_EMPLOYEE.code(), AccountRoles.ROLE_CUSTOMER.code())
                .anyRequest().authenticated();
    }


    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.tokenServices(super.tokenServices());
    }
}
