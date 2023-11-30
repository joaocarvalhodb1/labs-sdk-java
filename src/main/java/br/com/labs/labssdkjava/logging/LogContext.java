package br.com.labs.labssdkjava.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class LogContext {

    private String key;
    private Object value;

    private LogContext() {
    }

    private LogContext(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public static LogContext context(String key, Object value) {
        return new LogContext(key, value);
    }

    public static LogContext error(Object value) {
        return new LogContext("error", value);
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String toJson() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }
}
