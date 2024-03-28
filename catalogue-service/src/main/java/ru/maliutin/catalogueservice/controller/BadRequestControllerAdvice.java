package ru.maliutin.catalogueservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.Locale;

/**
 * Контроллер обработки исключений.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class BadRequestControllerAdvice {
    /**
     * Объект для создания сообщений с учетом локализации.
     */
    private final MessageSource messageSource;

    /**
     * Обработка исключений валидации.
     * @param exception исключение при валидации.
     * @param locale локаль.
     * @return ответ с детализаций проблем.
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ProblemDetail> handleBindException(BindException exception, Locale locale){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, messageSource.getMessage(
                        "errors.400.title",
                        new Object[0],
                        "errors.400.title",
                        locale
                )
        );
        // Помещаем в ProblemDetail зависимость с ошибками из BindException
        problemDetail.setProperty("errors",
                exception.getAllErrors().stream()
                        // Получение сообщений с ошибками от BindException
                        .map(ObjectError::getDefaultMessage)
                        .toList()
        );
        return ResponseEntity.badRequest().body(problemDetail);
    }

}
