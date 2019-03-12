package com.dlopatin.account;

import com.dlopatin.account.controller.AccountController;
import com.dlopatin.account.controller.ErrorController;
import com.dlopatin.account.controller.SparkController;
import com.dlopatin.account.repository.AccountInMemoryDao;
import com.dlopatin.account.repository.TransactionInMemoryDao;
import com.dlopatin.account.service.AccountServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.List;

public class App {

    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        List<SparkController> controllers = List.of(
                new AccountController(new AccountServiceImpl(
                        new AccountInMemoryDao(),
                        new TransactionInMemoryDao()),
                        objectMapper),
                new ErrorController(objectMapper));
        controllers.forEach(SparkController::init);
    }


}
