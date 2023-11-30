package br.com.labs.labssdkjava.exceptionhandler;

import java.util.Locale;
import java.util.ResourceBundle;

public class ApiMessage {
    private static final ResourceBundle messages = ResourceBundle.getBundle("Messages", Locale.getDefault());

    public static final String MESSAGE_NOT_FOUND = messages.getString("message.not.found");
    public static final String MESSAGE_INVALID_PARAM = messages.getString("message.invalid.param");
    public static final String MESSAGE_NOT_READABLE = messages.getString("message.not.readable");
    public static final String MESSAGE_ACCESS_DENIED = messages.getString("message.access.denied");
    public static final String MESSAGE_RESOURCE_NOT_FOUND = messages.getString("message.resource.not.found");
    public static final String MESSAGE_ENTITY_IN_USE = messages.getString("message.entity.in.use");
    public static final String MESSAGE_ERROR_BUSINESS = messages.getString("message.error.business");


    public static final String ERROR_GENERAL_USER = messages.getString("error.general.user");
    public static final String ERROR_ENTITY_NOT_FOUND = messages.getString("error.entity.not.found");
    public static final String ERROR_SYSTEM = messages.getString("error.system");
    public static final String ERROR_INVALID_ATTRIBUTES = messages.getString("error.invalid.attributes");
    public static final String ERROR_RESOURCE_NOT_FOUND = messages.getString("error.resource.not.found");
    public static final String ERROR_INVALID_PARAM = messages.getString("error.invalid.param");
    public static final String ERROR_NOT_READABLE = messages.getString("error.not.readable");
    public static final String ERROR_INVALID_PROPERTY = messages.getString("error.invalid.property");
    public static final String ERROR_INVALID_FORMAT = messages.getString("error.invalid.format");
    public static final String ERROR_ACCESS_DENIED = messages.getString("error.access.denied");

}
