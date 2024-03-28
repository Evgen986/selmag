package ru.maliutin.manager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import java.util.Optional;

@Configuration
public class SecurityBeans {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        return http
                // Любой запрос к приложению требует роли MANAGER
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().hasRole("MANAGER"))
                // авторизация конкретного клиента производится с помощью oauth2 с настройками по умолчанию
                .oauth2Login(Customizer.withDefaults())
                // авторизация приложения производится с помощью oauth2 с настройками по умолчанию
                .oauth2Client(Customizer.withDefaults())
                .build();
    }

    /**
     * Бин получающий роли пользователя на основе его групп.
     * @return
     */
    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oAuth2UserService(){
        // Класс использующий для загрузки информации о пользователе из поступивших утверждений в OidcUserRequest
        OidcUserService oidcUserService = new OidcUserService();
        return userRequest -> {
            // используя метод loadUser загружаем полученные данные в объект OidcUser
            OidcUser oidcUser = oidcUserService.loadUser(userRequest);
            // получаем список прав пользователя, на основании групп в которых состоит пользователь.
            List<SimpleGrantedAuthority> authorities = Optional.ofNullable(oidcUser.getClaimAsStringList("groups"))
                    // или создаем пустой не изменяемый список
                    .orElseGet(List::of)
                    // перебираем его
                    .stream()
                    // фильтруем роли, которые начинаются с ROLE_
                    .filter(role -> role.startsWith("ROLE_"))
                    // и преобразуем их в разрешения для пользователя
                    .map(SimpleGrantedAuthority::new)
                    .toList();
            // возвращаем объект аутентифицированного пользователя с его правами и токеном аутентификации и информации о пользователе
            return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
        };
    }
}
