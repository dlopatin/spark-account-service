package com.dlopatin.account.controller;

import com.dlopatin.account.model.Account;
import com.dlopatin.account.model.Currency;
import com.dlopatin.account.model.Transaction;
import com.dlopatin.account.model.TransactionType;
import com.dlopatin.account.repository.AccountDao;
import com.dlopatin.account.repository.AccountInMemoryDao;
import com.dlopatin.account.repository.TransactionDao;
import com.dlopatin.account.repository.TransactionInMemoryDao;
import com.dlopatin.account.service.AccountServiceImpl;
import com.dlopatin.account.service.TransferException.TransferError;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import spark.Spark;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccountControllerFunctionalTest {

    private static final int PORT = 8081;
    private AccountDao accountDao;
    private TransactionDao transactionDao;

    @BeforeEach
    public void before() {
        Spark.port(PORT);
        accountDao = new AccountInMemoryDao();
        transactionDao = new TransactionInMemoryDao();
        new AccountController(new AccountServiceImpl(accountDao, transactionDao), new ObjectMapper()).init();
        Spark.awaitInitialization();
    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        Spark.stop();
        // dirty hack to wait for termination
        TimeUnit.MILLISECONDS.sleep(500);
    }

    @Test
    @Disabled("For unknown reason Spark doesn't check content type")
    public void testCreateAccount_incorrectContentType() throws IOException, InterruptedException {
        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(makeUrl("account")))
                .POST(BodyPublishers.ofString("{\"currency\":\"GBP\", \"balance\":\"0\"}"))
                .header("Content-Type", "application/xml")
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(createRequest, BodyHandlers.ofString());
        assertThat(response.statusCode(), is(400));
    }

    @Test
    public void testCreateAccount_emptyBalance() throws IOException, InterruptedException {
        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(makeUrl("account")))
                .POST(BodyPublishers.ofString("{\"currency\":\"GBP\", \"balance\":\"0\"}"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(createRequest, BodyHandlers.ofString());
        assertThat(response.statusCode(), is(201));
        assertThat(response.body(), hasJsonPath("$.id", is(1)));

        Optional<Account> account = accountDao.get(1);
        assertTrue(account.isPresent());
        assertThat(account.get().getId(), is(1));
        assertThat(account.get().getBalance(), is(0L));
        assertThat(account.get().getCurrency(), is(Currency.GBP));
        assertThat(account.get().getVersion(), is(0));
    }

    @Test
    public void testCreateAccount_notEmptyBalance() throws IOException, InterruptedException {
        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(makeUrl("account")))
                .POST(BodyPublishers.ofString("{\"currency\":\"GBP\", \"balance\":\"802\"}"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(createRequest, BodyHandlers.ofString());
        assertThat(response.statusCode(), is(201));
        assertThat(response.body(), hasJsonPath("$.id", is(1)));

        Optional<Account> account = accountDao.get(1);
        assertTrue(account.isPresent());
        assertThat(account.get().getId(), is(1));
        assertThat(account.get().getBalance(), is(802L));
        assertThat(account.get().getCurrency(), is(Currency.GBP));
        assertThat(account.get().getVersion(), is(0));
    }

    @Test
    public void testGetAccount_notExists() throws IOException, InterruptedException {
        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(makeUrl("account/10")))
                .GET()
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(createRequest, BodyHandlers.ofString());
        assertThat(response.statusCode(), is(404));
        assertThat(response.body(), hasJsonPath("$.errors[0].code", is("ACCOUNT_NOT_FOUND")));
        assertThat(response.body(), hasJsonPath("$.errors[0].detail", is("Account by id=10 not found")));
    }

    @Test
    public void testGetAccount_exists() throws IOException, InterruptedException {
        givenAccount(1, Currency.GBP, 100);

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(makeUrl("account/1")))
                .GET()
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(createRequest, BodyHandlers.ofString());
        assertThat(response.statusCode(), is(200));
        assertThat(response.body(), hasJsonPath("$.id", is(1)));
        assertThat(response.body(), hasJsonPath("$.currency", is(Currency.GBP.getCurrency().toString())));
        assertThat(response.body(), hasJsonPath("$.balance", is(100)));
    }

    @Test
    public void testTransfer_fromAccountNegative() throws IOException, InterruptedException {
        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(makeUrl("account/transfer")))
                .POST(BodyPublishers.ofString(
                        "{\"accountFrom\":\"-1\", \"accountTo\":\"2\", \"amount\":\"10\", \"operationId\":1}"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(createRequest, BodyHandlers.ofString());
        assertThat(response.statusCode(), is(400));
        assertThat(response.body(), hasJsonPath("$.errors[0].code", is("VALIDATION_ERROR")));
    }

    @Test
    public void testTransfer_fromAccountZero() throws IOException, InterruptedException {
        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(makeUrl("account/transfer")))
                .POST(BodyPublishers.ofString(
                        "{\"accountFrom\":\"0\", \"accountTo\":\"2\", \"amount\":\"10\", \"operationId\":1}"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(createRequest, BodyHandlers.ofString());
        assertThat(response.statusCode(), is(400));
        assertThat(response.body(), hasJsonPath("$.errors[0].code", is("VALIDATION_ERROR")));
    }

    @Test
    public void testTransfer_toAccountNegative() throws IOException, InterruptedException {
        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(makeUrl("account/transfer")))
                .POST(BodyPublishers.ofString(
                        "{\"accountFrom\":\"1\", \"accountTo\":\"-2\", \"amount\":\"10\", \"operationId\":1}"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(createRequest, BodyHandlers.ofString());
        assertThat(response.statusCode(), is(400));
        assertThat(response.body(), hasJsonPath("$.errors[0].code", is("VALIDATION_ERROR")));
    }

    @Test
    public void testTransfer_toAccountZero() throws IOException, InterruptedException {
        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(makeUrl("account/transfer")))
                .POST(BodyPublishers.ofString(
                        "{\"accountFrom\":\"1\", \"accountTo\":\"0\", \"amount\":\"10\", \"operationId\":1}"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(createRequest, BodyHandlers.ofString());
        assertThat(response.statusCode(), is(400));
        assertThat(response.body(), hasJsonPath("$.errors[0].code", is("VALIDATION_ERROR")));
    }

    @Test
    public void testTransfer_amountNegative() throws IOException, InterruptedException {
        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(makeUrl("account/transfer")))
                .POST(BodyPublishers.ofString(
                        "{\"accountFrom\":\"1\", \"accountTo\":\"2\", \"amount\":\"-10\", \"operationId\":1}"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(createRequest, BodyHandlers.ofString());
        assertThat(response.statusCode(), is(400));
        assertThat(response.body(), hasJsonPath("$.errors[0].code", is("VALIDATION_ERROR")));
    }

    @Test
    public void testTransfer_amountZero() throws IOException, InterruptedException {
        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(makeUrl("account/transfer")))
                .POST(BodyPublishers.ofString(
                        "{\"accountFrom\":\"1\", \"accountTo\":\"2\", \"amount\":\"0\", \"operationId\":1}"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(createRequest, BodyHandlers.ofString());
        assertThat(response.statusCode(), is(400));
        assertThat(response.body(), hasJsonPath("$.errors[0].code", is("VALIDATION_ERROR")));
    }

    @Test
    public void testTransfer_incorrectFromAccount() throws IOException, InterruptedException {
        givenAccount(2, Currency.GBP, 100);
        HttpRequest createRequest = givenAccountTransferRequest();

        HttpResponse<String> response = HttpClient.newHttpClient().send(createRequest, BodyHandlers.ofString());
        assertThat(response.statusCode(), is(400));
        assertThat(response.body(), hasJsonPath("$.errors[0].code",
                is(TransferError.ACCOUNT_FROM_NOT_FOUND.toString())));
    }

    @Test
    public void testTransfer_incorrectToAccount() throws IOException, InterruptedException {
        givenAccount(1, Currency.GBP, 100);
        HttpRequest createRequest = givenAccountTransferRequest();

        HttpResponse<String> response = HttpClient.newHttpClient().send(createRequest, BodyHandlers.ofString());
        assertThat(response.statusCode(), is(400));
        assertThat(response.body(), hasJsonPath("$.errors[0].code",
                is(TransferError.ACCOUNT_TO_NOT_FOUND.toString())));
    }

    @Test
    public void testTransfer_insufficientBalance() throws IOException, InterruptedException {
        givenAccount(1, Currency.GBP, 2);
        givenAccount(2, Currency.GBP, 0);
        HttpRequest createRequest = givenAccountTransferRequest();

        HttpResponse<String> response = HttpClient.newHttpClient().send(createRequest, BodyHandlers.ofString());
        assertThat(response.statusCode(), is(400));
        assertThat(response.body(), hasJsonPath("$.errors[0].code",
                is(TransferError.INSUFFICIENT_BALANCE.toString())));
    }

    @Test
    public void testTransfer_differentAccountCurrencies() throws IOException, InterruptedException {
        givenAccount(1, Currency.GBP, 100);
        givenAccount(2, Currency.EUR, 200);
        HttpRequest createRequest = givenAccountTransferRequest();

        HttpResponse<String> response = HttpClient.newHttpClient().send(createRequest, BodyHandlers.ofString());
        assertThat(response.statusCode(), is(400));
        assertThat(response.body(), hasJsonPath("$.errors[0].code",
                is(TransferError.DIFFERENT_ACCOUNT_CURRENCIES.toString())));
    }

    @Test
    public void testTransfer_operationAlreadyProcessed() throws IOException, InterruptedException {
        givenAccount(1, Currency.GBP, 100);
        givenAccount(2, Currency.GBP, 200);
        givenTransaction(1, 1, TransactionType.CREDIT, 10);
        HttpRequest createRequest = givenAccountTransferRequest();

        HttpResponse<String> response = HttpClient.newHttpClient().send(createRequest, BodyHandlers.ofString());
        assertThat(response.statusCode(), is(400));
        assertThat(response.body(), hasJsonPath("$.errors[0].code",
                is(TransferError.TRANSFER_ALREADY_PROCESSED.toString())));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void testTransfer_correctBehaviour() throws IOException, InterruptedException {
        givenAccount(1, Currency.GBP, 100);
        givenAccount(2, Currency.GBP, 200);
        HttpRequest createRequest = givenAccountTransferRequest();

        HttpResponse<String> response = HttpClient.newHttpClient().send(createRequest, BodyHandlers.ofString());
        assertThat(response.statusCode(), is(200));
        assertThat(response.body(), hasJsonPath("$.message", is("ok")));

        assertThat(accountDao.get(1).get().getVersion(), is(1));
        assertThat(accountDao.get(1).get().getBalance(), is(90L));
        assertThat(accountDao.get(2).get().getVersion(), is(1));
        assertThat(accountDao.get(2).get().getBalance(), is(210L));

        assertThat(transactionDao.list(1), hasSize(2));
    }

    private void givenAccount(int id, Currency currency, long balance) {
        accountDao.create(new Account(id, currency, balance));
    }

    private void givenTransaction(int operationId, int accountId, TransactionType transactionType, long amount) {
        transactionDao.insert(new Transaction(operationId, accountId, transactionType, amount));
    }

    private HttpRequest givenAccountTransferRequest() {
        return HttpRequest.newBuilder()
                .uri(URI.create(makeUrl("account/transfer")))
                .POST(BodyPublishers.ofString(
                        "{\"accountFrom\":\"1\", \"accountTo\":\"2\", \"amount\":\"10\", \"operationId\":1}"))
                .header("Content-Type", "application/json")
                .build();
    }

    private String makeUrl(String path) {
        return String.format("http://localhost:%d/api/v1/%s", PORT, path);
    }

}