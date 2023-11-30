package br.com.labs.labssdkjava.exceptionhandler;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
public class ApiError {

    private HttpStatusCode status;

    private String title;

    private String detail;

    private String message;

    private OffsetDateTime timestamp;

    private List<Object> objects;

    @Getter
    @Builder
    public static class Object {
        private String name;
        private String message;

    }
}
