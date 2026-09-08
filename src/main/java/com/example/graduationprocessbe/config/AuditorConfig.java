//package com.example.graduationprocessbe.config;
//
//import jakarta.validation.constraints.NotNull;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.domain.AuditorAware;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//
//import java.util.Optional;
//
//@Configuration
//public class AuditorConfig {
//
//    @Bean
//    public AuditorAware<String> auditorProvider() {
//        return new SpringSecurityAuditorAware();
//    }
//
//    static class SpringSecurityAuditorAware implements AuditorAware<String> {
//        @NotNull
//        @Override
//        public Optional<String> getCurrentAuditor() {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            if (authentication == null || !authentication.isAuthenticated()) {
//                return Optional.of("System");
//            }
//            return Optional.of(authentication.getName());
//        }
//    }
//}

//audit doi  apply security