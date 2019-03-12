package com.dlopatin.account.controller;

import com.dlopatin.account.controller.dto.ErrorMessage;
import com.dlopatin.account.controller.dto.ErrorResponse;
import com.dlopatin.account.controller.dto.GetAccountResponse;
import com.dlopatin.account.controller.dto.NewAccountRequest;
import com.dlopatin.account.controller.dto.NewAccountResponse;
import com.dlopatin.account.controller.dto.StatusCodes;
import com.dlopatin.account.controller.dto.TransferRequest;
import com.dlopatin.account.controller.dto.TransferResponse;
import com.dlopatin.account.model.Account;
import com.dlopatin.account.service.AccountService;
import com.dlopatin.account.service.TransferException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.dlopatin.account.controller.ContentType.APPLICATION_JSON;
import static spark.Spark.*;

/**
 * Processes requests related to account manipulation.
 */
public class AccountController implements SparkController {

    private static final String VALIDATION_ERROR_CODE = "VALIDATION_ERROR";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final AccountService accountService;
    private final ObjectMapper objectMapper;

    public AccountController(AccountService accountService, ObjectMapper objectMapper) {
        this.accountService = accountService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void init() {
        path("/api/v1", () ->
                path("/account", () -> {
                    post("", APPLICATION_JSON, processCreateAccount(), objectMapper::writeValueAsString);
                    post("/transfer", APPLICATION_JSON, processTransfer(), objectMapper::writeValueAsString);
                    get("/:id", APPLICATION_JSON, processGetAccount(), objectMapper::writeValueAsString);
                }));
    }

    private Route processCreateAccount() {
        return (request, response) -> {
            log.debug("Request content type: {}", request.contentType());
            response.type(APPLICATION_JSON);
            // TODO: mapping can gives 500 error code, validation should be performed and returned 400 code
            NewAccountRequest newAccount = objectMapper.readValue(request.body(), NewAccountRequest.class);
            Account account = accountService.create(newAccount.getCurrency(), newAccount.getBalance());
            response.status(StatusCodes.CREATED);
            return new NewAccountResponse(account.getId());
        };
    }

    private Route processTransfer() {
        return (request, response) -> {
            response.type(APPLICATION_JSON);
            // TODO: mapping can gives 500 error code, validation should be performed and returned 400 code
            TransferRequest transferRequest = objectMapper.readValue(request.body(), TransferRequest.class);

            List<ErrorMessage> errorMessages = validateTransferRequest(transferRequest);
            if (!errorMessages.isEmpty()) {
                response.status(StatusCodes.BAD_REQUEST);
                return new ErrorResponse(errorMessages);
            }

            try {
                accountService.transfer(
                        transferRequest.getAccountFrom(),
                        transferRequest.getAccountTo(),
                        transferRequest.getAmount(),
                        transferRequest.getOperationId());
            } catch (TransferException e) {
                response.status(StatusCodes.BAD_REQUEST);
                return new ErrorResponse(new ErrorMessage(e.getTransferError().toString()));
            }
            response.status(StatusCodes.OK);
            return TransferResponse.successful();
        };
    }

    private List<ErrorMessage> validateTransferRequest(TransferRequest request) {
        List<ErrorMessage> errors = new ArrayList<>();
        if (request.getAccountFrom() <= 0) {
            errors.add(new ErrorMessage(VALIDATION_ERROR_CODE, "From account must be positive"));
        }
        if (request.getAccountTo() <= 0) {
            errors.add(new ErrorMessage(VALIDATION_ERROR_CODE, "To account must be positive"));
        }
        if (request.getAmount() <= 0) {
            errors.add(new ErrorMessage(VALIDATION_ERROR_CODE, "Amount te be transferred must positive"));
        }
        return errors;
    }

    private Route processGetAccount() {
        return (request, response) -> {
            response.type(APPLICATION_JSON);
            int id = Integer.parseInt(request.params(":id"));
            Optional<Account> account = accountService.get(id);
            if (account.isPresent()) {
                response.status(StatusCodes.OK);
                return GetAccountResponse.from(account.get());
            } else {
                response.status(StatusCodes.NOT_FOUND);
                return new ErrorResponse(new ErrorMessage(
                        "ACCOUNT_NOT_FOUND",
                        String.format("Account by id=%d not found", id)));
            }
        };
    }


}