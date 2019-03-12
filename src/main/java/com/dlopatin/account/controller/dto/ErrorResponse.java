package com.dlopatin.account.controller.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Container for errors
 */
public class ErrorResponse {

    private final List<ErrorMessage> errors;

    public ErrorResponse(ErrorMessage errorMessage) {
        errors = Collections.singletonList(errorMessage);
    }

    public ErrorResponse(List<ErrorMessage> errors) {
        this.errors = new ArrayList<>(errors);
    }

    public List<ErrorMessage> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "errors=" + errors +
                '}';
    }
}
