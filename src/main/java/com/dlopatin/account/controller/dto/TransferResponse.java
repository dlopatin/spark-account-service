package com.dlopatin.account.controller.dto;

public class TransferResponse {

    private final String message;

    private TransferResponse(String message) {
        this.message = message;
    }

    public static TransferResponse successful() {
        return new TransferResponse("ok");
    }


    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "TransferResponse{" +
                "message='" + message + '\'' +
                '}';
    }
}
