package br.com.labs.labssdkjava.exceptionhandler;

import java.nio.file.AccessDeniedException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.PropertyBindingException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired
    private MessageSource messageSource;

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            Object body,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        if (body == null) {
            body = ApiError.builder()
                    .timestamp(OffsetDateTime.now())
                    .status(status)
                    .title(HttpStatus.valueOf(status.value()).getReasonPhrase())
                    .message(ApiMessage.ERROR_GENERAL_USER)
                    .build();
        } else if (body instanceof String) {
            body = ApiError.builder()
                    .timestamp(OffsetDateTime.now())
                    .status(status)
                    .title((String) body)
                    .message(ApiMessage.ERROR_GENERAL_USER)
                    .build();
        }

        return super.handleExceptionInternal(ex, body, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(
            HttpMediaTypeNotAcceptableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        return ResponseEntity.status(status).headers(headers).build();
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        return handleValidationInternal(ex, headers, status, request, ex.getBindingResult());
    }

    private ResponseEntity<Object> handleValidationInternal(Exception ex, HttpHeaders headers,
                                                            HttpStatusCode status, WebRequest request, BindingResult bindingResult) {
        String detail = ApiMessage.ERROR_INVALID_ATTRIBUTES;

        List<ApiError.Object> problemObjects = bindingResult.getAllErrors().stream()
                .map(objectError -> {
                    String message = messageSource.getMessage(objectError, LocaleContextHolder.getLocale());

                    String name = objectError.getObjectName();

                    if (objectError instanceof FieldError) {
                        name = ((FieldError) objectError).getField();
                    }

                    return ApiError.Object.builder()
                            .name(name)
                            .message(message)
                            .build();
                })
                .collect(Collectors.toList());

        ApiError error = createErrorBuilder(status, ApiMessage.ERROR_ENTITY_NOT_FOUND, detail)
                .message(detail)
                .objects(problemObjects)
                .build();

        return handleExceptionInternal(ex, error, headers, status, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUncaught(Exception ex, WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorMessage = ApiMessage.ERROR_SYSTEM;
        String detail = ApiMessage.ERROR_GENERAL_USER;

        log.error(ex.getMessage(), ex);
        ApiError apiError = createErrorBuilder(status, errorMessage, detail)
                .message(detail)
                .build();

        return handleExceptionInternal(ex, apiError, new HttpHeaders(), status, request);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex,
                                                                   HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String title = ApiMessage.MESSAGE_NOT_FOUND;
        String detail = String.format(ApiMessage.ERROR_RESOURCE_NOT_FOUND, ex.getRequestURL());

        ApiError apiError = createErrorBuilder(status, title, detail)
                .message(ApiMessage.ERROR_GENERAL_USER)
                .build();

        return handleExceptionInternal(ex, apiError, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
                                                        HttpStatusCode status, WebRequest request) {

        if (ex instanceof MethodArgumentTypeMismatchException) {
            return handleMethodArgumentTypeMismatch(
                    (MethodArgumentTypeMismatchException) ex, headers, status, request);
        }

        return super.handleTypeMismatch(ex, headers, status, request);
    }

    private ResponseEntity<Object> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        String errorMessage = ApiMessage.MESSAGE_INVALID_PARAM;

        String detail = String.format(ApiMessage.ERROR_INVALID_PARAM, ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName());

        ApiError apiError = createErrorBuilder(status, errorMessage, detail)
                .message(ApiMessage.ERROR_GENERAL_USER)
                .build();

        return handleExceptionInternal(ex, apiError, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Throwable rootCause = ExceptionUtils.getRootCause(ex);

        if (rootCause instanceof InvalidFormatException) {
            return handleInvalidFormat((InvalidFormatException) rootCause, headers, status, request);
        } else if (rootCause instanceof PropertyBindingException) {
            return handlePropertyBinding((PropertyBindingException) rootCause, headers, status, request);
        }
        String errorMessage = ApiMessage.MESSAGE_NOT_READABLE;
        String detail = ApiMessage.ERROR_NOT_READABLE;

        ApiError apiError = createErrorBuilder(status, errorMessage, detail)
                .message(ApiMessage.ERROR_GENERAL_USER)
                .build();

        return handleExceptionInternal(ex, apiError, headers, status, request);
    }

    private ResponseEntity<Object> handlePropertyBinding(PropertyBindingException ex,
                                                         HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String path = joinPath(ex.getPath());

        String errorMessage = ApiMessage.MESSAGE_NOT_READABLE;
        String detail =  String.format(ApiMessage.ERROR_INVALID_PROPERTY, path);

        ApiError apiError = createErrorBuilder(status, errorMessage, detail)
                .message(ApiMessage.ERROR_GENERAL_USER)
                .build();

        return handleExceptionInternal(ex, apiError, headers, status, request);
    }

    private ResponseEntity<Object> handleInvalidFormat(InvalidFormatException ex,
                                                       HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String path = joinPath(ex.getPath());

        String errorMessage = ApiMessage.MESSAGE_NOT_READABLE;
        String detail = String.format(ApiMessage.ERROR_INVALID_FORMAT,
                path, ex.getValue(), ex.getTargetType().getSimpleName());

        ApiError apiError = createErrorBuilder(status, errorMessage, detail)
                .message(ApiMessage.ERROR_GENERAL_USER)
                .build();

        return handleExceptionInternal(ex, apiError, headers, status, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex, WebRequest request) {

        HttpStatus status = HttpStatus.FORBIDDEN;
        String message = ApiMessage.MESSAGE_ACCESS_DENIED;
        String errorMessage = ApiMessage.ERROR_ACCESS_DENIED;
        String detail = ex.getMessage();

        ApiError apiError = createErrorBuilder(status, message, detail)
                .message(detail)
                .message(errorMessage)
                .build();

        return handleExceptionInternal(ex, apiError, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(EntityNotFound.class)
    public ResponseEntity<?> handleEntityNotFound(EntityNotFound ex,
                                                         WebRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        String errorMessage = ApiMessage.MESSAGE_RESOURCE_NOT_FOUND;
        String detail = ex.getMessage();

        ApiError apiError = createErrorBuilder(status, errorMessage, detail)
                .message(detail)
                .build();

        return handleExceptionInternal(ex, apiError, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(EntityInUseException.class)
    public ResponseEntity<?> handleEntityInUse(EntityInUseException ex, WebRequest request) {

        HttpStatus status = HttpStatus.CONFLICT;
        String errorMessage = ApiMessage.MESSAGE_ENTITY_IN_USE;

        String detail = ex.getMessage();

        ApiError apiError = createErrorBuilder(status, errorMessage, detail)
                .message(detail)
                .build();

        return handleExceptionInternal(ex, apiError, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusiness(BusinessException ex, WebRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;
        String errorMessage = ApiMessage.MESSAGE_ERROR_BUSINESS;
        String detail = ex.getMessage();

        ApiError apiError = createErrorBuilder(status, errorMessage, detail)
                .message(detail)
                .build();

        return handleExceptionInternal(ex, apiError, new HttpHeaders(), status, request);
    }

    private ApiError.ApiErrorBuilder createErrorBuilder(
                                                        HttpStatusCode status,
                                                        String errorMessage,
                                                        String detail) {
        return ApiError.builder()
                .timestamp(OffsetDateTime.now())
                .status(status)
                .title(errorMessage)
                .detail(detail);
    }

    private String joinPath(List<Reference> references) {
        return references.stream()
                .map(ref -> ref.getFieldName())
                .collect(Collectors.joining("."));
    }

}