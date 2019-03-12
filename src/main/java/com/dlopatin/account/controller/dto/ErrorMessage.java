package com.dlopatin.account.controller.dto;

/**
 * Represents error structure in response
 */
public class ErrorMessage {
    private final String code;
    private String detail;

    public ErrorMessage(String code) {
        this.code = code;
    }

    public ErrorMessage(String code, String detail) {
        this.code = code;
        this.detail = detail;
    }

    public String getCode() {
        return code;
    }

    public String getDetail() {
        return detail;
    }

    @Override
    public String toString() {
        return "ErrorMessage{" +
                "code='" + code + '\'' +
                ", detail='" + detail + '\'' +
                '}';
    }
}
