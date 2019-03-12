package com.dlopatin.account.controller;

import com.dlopatin.account.controller.dto.ErrorMessage;
import com.dlopatin.account.controller.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import static com.dlopatin.account.controller.ContentType.APPLICATION_JSON;
import static spark.Spark.*;

/**
 * Handles general errors
 */
public class ErrorController implements SparkController {

    private final ObjectMapper objectMapper;

    public ErrorController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void init() {
        notFound((request, response) -> {
            response.type(APPLICATION_JSON);
            return objectMapper.writeValueAsString(new ErrorResponse(new ErrorMessage("REQUEST_HANDLER_NOT_FOUND")));
        });
        internalServerError((request, response) -> {
            response.type(APPLICATION_JSON);
            return objectMapper.writeValueAsString(new ErrorResponse(new ErrorMessage("INTERNAL_SERVER_ERROR")));
        });


    }

}
