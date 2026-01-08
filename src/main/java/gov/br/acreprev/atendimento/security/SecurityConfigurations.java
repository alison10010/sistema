package gov.br.acreprev.atendimento.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfigurations {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf().disable()
            
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .and()

            .authorizeRequests()
                .antMatchers(
                    "/javax.faces.resource/**",
                    "/jakarta.faces.resource/**",
                    "/resources/**",
                    "/static/**",
                    "/css/**",
                    "/js/**",
                    "/img/**",
                    "/bootstrap/**"
                ).permitAll()
                
                .antMatchers("/painel", "/dashboard").authenticated()
                .antMatchers("/relatorio", "/gerenciar-telas").authenticated()
                .antMatchers("/usuario/**", "/gerenciar-telas").authenticated()
                .antMatchers("/view/livre/**", "/relatorio-periodo").permitAll()
                
                // ✅ MUITO IMPORTANTE (SEM /sistema):
                .antMatchers("/ws/**").permitAll()
                .antMatchers("/websocket/**").permitAll() // se você usou esse path em algum lugar
                .antMatchers("/sockjs/**").permitAll()    // opcional, se existir

                .anyRequest().permitAll()
            .and()

            .formLogin()
                .loginPage("/login")              // GET
                .loginProcessingUrl("/login")     // POST
                // ✅ MUITO IMPORTANTE:
                // se seu form for JSF (h:form), os names normalmente viram "formLogin:username"
                // então ajuste aqui para bater com o "name" real dos inputs
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/dashboard", true)
                .failureHandler(failureHandler())
                .permitAll()
            .and()

            .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll();

        return http.build();
    }

    @Bean
    public AuthenticationFailureHandler failureHandler() {
        return (request, response, exception) -> {
        	System.out.println(new BCryptPasswordEncoder(12).encode("admin123"));

            System.out.println("LOGIN FAIL: " + exception.getClass().getName() + " - " + exception.getMessage());
            response.sendRedirect(request.getContextPath() + "/login?error=true");
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}