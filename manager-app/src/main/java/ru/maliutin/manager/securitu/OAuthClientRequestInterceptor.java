package ru.maliutin.manager.securitu;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;

import java.io.IOException;

@RequiredArgsConstructor
public class OAuthClientRequestInterceptor implements ClientHttpRequestInterceptor {
    /**
     * Менеджер управления авторизированными клиентами OAuth2
     */
    private final OAuth2AuthorizedClientManager authorizedClientManager;
    /**
     * Идентификатор регистрации клиента.
     * (передается в конструктор при создании бина clientProductsRestClient
     * в классе ClientBeans).
     */
    private final String registrationId;
    /**
     * Контекст безопасности.
     */
    private SecurityContextHolderStrategy securityContextHolderStrategy =
            SecurityContextHolder.getContextHolderStrategy();

    /**
     * Метод перехвата и изменения http запросов перед их выполнением.
     * Если запрос не содержит заголовка авторизации, метод автоматически добавляет его с токеном доступа пользователя.
     * @param request HTTP запрос, содержащий метод, URI и заголовки
     * @param body тело запроса
     * @param execution исполнение запроса
     * @return измененный запрос
     * @throws IOException в случае возникновения ошибки ввода-вывода
     */
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // Проверяем имеет ли запрос в заголовке авторизацию, если нет, то вшивается заголовок с токеном авторизации.
        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)){
            // Получаем авторизированного клиента (текущее приложение)
            OAuth2AuthorizedClient authorizedClient = this.authorizedClientManager.authorize(
                    // Отправляем запрос авторизированного клиента (текущего приложения)
                    OAuth2AuthorizeRequest
                            // Передавая идентификатор приложения
                            .withClientRegistrationId(this.registrationId)
                            // и данные конкретного авторизированного пользователя
                            .principal(this.securityContextHolderStrategy
                                    // Извлекаем контекст из контекста безопасности
                                    .getContext()
                                    // и получаем аутентификацию конкретного пользователя
                                    .getAuthentication())
                            .build());
            // В запрос вшиваем заголовок авторизации с токеном полученным от сервера аутентификации
            request.getHeaders().setBearerAuth(authorizedClient.getAccessToken().getTokenValue());
        }
        // возвращаем измененный запрос
        return execution.execute(request, body);
    }
}
