package com.dlopatin.account.controller.dto;

public class NewAccountResponse {

    private final int id;

    public NewAccountResponse(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "NewAccountResponse{" +
                "id=" + id +
                '}';
    }
}
