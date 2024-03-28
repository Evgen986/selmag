package ru.maliutin.manager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.web.client.RestClient;
import ru.maliutin.manager.securitu.OAuthClientRequestInterceptor;
import ru.maliutin.manager.client.RestClientProductsRestClient;

/**
 * Конфигурационный класс для подключения клиентов удаленных api.
 */
@Configuration
public class ClientBeans {
    /**
     * Бин объекта клиента.
     * @return созданный объект.
     */
    @Bean
    public RestClientProductsRestClient clientProductsRestClient(
            @Value("${selmag.services.catalogue.uri:http://localhost:8081}") String catalogueBaseUri,
            // Репозиторий хранящий данные об не авторизированном клиенте (приложении), идентификатор, секретный ключ и т.д.
            ClientRegistrationRepository clientRegistrationRepository,
            // Репозиторий, который хранит данный об авторизированном клиенте (приложении), хранит токен доступа к серверу авторизации, после успешной авторизации приложения
            OAuth2AuthorizedClientRepository auth2AuthorizedClientRepository,
            // Идентификатор клиента, подтягивается из application.yaml
            @Value("${selmag.services.catalogue.registration-id:keycloak}") String registrationId){
        return new RestClientProductsRestClient(RestClient.builder()
                // Задаем базовый url для подключения к удаленному api из application.yaml
                .baseUrl(catalogueBaseUri)
                // Перехватчик запросов, для добавления аутентификации
                .requestInterceptor(
                        // создаем объект собственного перехватчика запросов
                        new OAuthClientRequestInterceptor(
                                // Менеджер авторизации клиента (приложения)
                                new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository,
                                        auth2AuthorizedClientRepository), registrationId
                        ))
                .build()
        );
    }
}
