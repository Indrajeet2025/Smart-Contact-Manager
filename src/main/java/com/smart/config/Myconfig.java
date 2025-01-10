package com.smart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class Myconfig {

	 // UserDetailsService bean
    @Bean
    public UserDetailsService getUserDetailService() {
        return new UserDetailsServiceimpl();
    }

    // BCryptPasswordEncoder bean for password encoding
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // DaoAuthenticationProvider bean for authentication with DB/memory
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(this.getUserDetailService());
        daoAuthenticationProvider.setPasswordEncoder(this.passwordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests()
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .requestMatchers("/user/**").hasRole("USER")
            .requestMatchers("/**").permitAll()
            .and()
            .formLogin()
                .loginPage("/signin")
                .loginProcessingUrl("/dologin")
                .defaultSuccessUrl("/user/index", true) // Redirect to /user/index after successful login
                .permitAll()
            .and()
            .logout()
                .logoutUrl("/logout")  // The URL for logging out
                .logoutSuccessUrl("/signin?logout")  // Redirect to signin with 'logout' parameter
                .permitAll()
            .and()
            .csrf().disable();  // Disable CSRF for simplicity (enable in production)

        return http.build();
    }



    // AuthenticationManager bean (used instead of configure method in WebSecurityConfigurerAdapter)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


}