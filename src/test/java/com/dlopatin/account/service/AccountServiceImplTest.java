package com.dlopatin.account.service;

import com.dlopatin.account.model.Account;
import com.dlopatin.account.model.Currency;
import com.dlopatin.account.model.Transaction;
import com.dlopatin.account.repository.AccountInMemoryDao;
import com.dlopatin.account.repository.TransactionInMemoryDao;
import com.dlopatin.account.service.TransferException.TransferError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountServiceImplTest {

    private AccountService accountService;

    @Mock
    private AccountInMemoryDao accountDao;
    @Mock
    private TransactionInMemoryDao transactionDao;

    @BeforeEach
    public void before() {
        accountService = new AccountServiceImpl(accountDao, transactionDao);
    }

    @Test
    void testCreate_negativeBalance() {
        assertThrows(IllegalArgumentException.class, () -> accountService.create(Currency.EUR, -2000));
    }

    @Test
    void testCreate_nullCurrency() {
        assertThrows(IllegalArgumentException.class, () -> accountService.create(null, 2000));
    }

    @Test
    void testCreate_emptyBalance() {
        // when
        Account account = accountService.create(Currency.EUR, 0);

        // then
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountDao).create(captor.capture());

        assertThat(account.getCurrency(), is(Currency.EUR));
        assertThat(account.getVersion(), is(0));
        assertThat(account.getBalance(), is(0L));

        Account accountToBeSaved = captor.getValue();
        assertThat(accountToBeSaved.getCurrency(), is(Currency.EUR));
        assertThat(accountToBeSaved.getVersion(), is(0));
        assertThat(accountToBeSaved.getBalance(), is(0L));
    }

    @Test
    void testCreate_notEmptyBalance() {
        // when
        Account account = accountService.create(Currency.EUR, 2000);

        // then
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountDao).create(captor.capture());

        assertThat(account.getCurrency(), is(Currency.EUR));
        assertThat(account.getVersion(), is(0));
        assertThat(account.getBalance(), is(2000L));

        Account accountToBeSaved = captor.getValue();
        assertThat(accountToBeSaved.getCurrency(), is(Currency.EUR));
        assertThat(accountToBeSaved.getVersion(), is(0));
        assertThat(accountToBeSaved.getBalance(), is(2000L));
    }

    @Test
    void testGet_accountNotStored() {
        int id = 10;
        when(accountDao.get(id)).thenReturn(Optional.empty());

        Optional<Account> result = accountService.get(id);
        assertFalse(result.isPresent());
    }

    @Test
    void testGet_accountStored() {
        // given
        int id = 10;
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(id);
        when(account.getBalance()).thenReturn(100L);
        when(account.getCurrency()).thenReturn(Currency.USD);
        when(account.getVersion()).thenReturn(2);

        // when
        when(accountDao.get(id)).thenReturn(Optional.of(account));

        // then
        Optional<Account> result = accountService.get(id);
        assertTrue(result.isPresent());
        assertThat(result.get().getId(), is(id));
        assertThat(result.get().getBalance(), is(100L));
        assertThat(result.get().getCurrency(), is(Currency.USD));
        assertThat(result.get().getVersion(), is(2));
    }

    @Test
    void testTransfer_amountNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.transfer(1, 2, -100, 1));
    }

    @Test
    void testTransfer_fromAccountNotFound() {
        int fromId = 1;
        int toId = 2;
        when(accountDao.get(fromId)).thenReturn(Optional.empty());
        when(accountDao.get(toId)).thenReturn(Optional.of(new Account(toId, Currency.EUR, 100)));

        TransferException exception = assertThrows(TransferException.class,
                () -> accountService.transfer(fromId, toId, 100, 1));
        assertThat(exception.getTransferError(), is(TransferError.ACCOUNT_FROM_NOT_FOUND));
    }

    @Test
    void testTransfer_toAccountNotFound() {
        int fromId = 1;
        int toId = 2;
        when(accountDao.get(fromId)).thenReturn(Optional.of(new Account(toId, Currency.EUR, 100)));
        when(accountDao.get(toId)).thenReturn(Optional.empty());

        TransferException exception = assertThrows(TransferException.class,
                () -> accountService.transfer(fromId, toId, 100, 1));
        assertThat(exception.getTransferError(), is(TransferError.ACCOUNT_TO_NOT_FOUND));
    }

    @Test
    void testTransfer_differentCurrencies() {
        int fromId = 1;
        int toId = 2;
        when(accountDao.get(fromId)).thenReturn(Optional.of(new Account(toId, Currency.EUR, 100)));
        when(accountDao.get(toId)).thenReturn(Optional.of(new Account(fromId, Currency.RUB, 100)));

        TransferException exception = assertThrows(TransferException.class,
                () -> accountService.transfer(fromId, toId, 100, 1));
        assertThat(exception.getTransferError(), is(TransferError.DIFFERENT_ACCOUNT_CURRENCIES));
    }

    @Test
    void testTransfer_insufficientBalance() {
        int fromId = 1;
        int toId = 2;
        when(accountDao.get(fromId)).thenReturn(Optional.of(new Account(toId, Currency.EUR, 20)));
        when(accountDao.get(toId)).thenReturn(Optional.of(new Account(fromId, Currency.EUR, 100)));

        TransferException exception = assertThrows(TransferException.class,
                () -> accountService.transfer(fromId, toId, 100, 1));
        assertThat(exception.getTransferError(), is(TransferError.INSUFFICIENT_BALANCE));
    }

    @Test
    void testTransfer_transferAlreadyProcessed() {
        int fromId = 1;
        int toId = 2;
        when(accountDao.get(fromId)).thenReturn(Optional.of(new Account(toId, Currency.EUR, 200)));
        when(accountDao.get(toId)).thenReturn(Optional.of(new Account(fromId, Currency.EUR, 100)));

        int operationId = 1;
        when(transactionDao.hasTransactions(operationId)).thenReturn(true);

        TransferException exception = assertThrows(TransferException.class,
                () -> accountService.transfer(fromId, toId, 100, operationId));
        assertThat(exception.getTransferError(), is(TransferError.TRANSFER_ALREADY_PROCESSED));
    }


    @Test
    void testTransfer_normalProcessing() {
        int fromId = 1;
        int toId = 2;
        Account fromAccount = new Account(toId, Currency.EUR, 200);
        Account toAccount = new Account(fromId, Currency.EUR, 100);

        when(accountDao.get(fromId)).thenReturn(Optional.of(fromAccount));
        when(accountDao.get(toId)).thenReturn(Optional.of(toAccount));

        accountService.transfer(fromId, toId, 100, 1);
        ArgumentCaptor<Account> storedAccounts = ArgumentCaptor.forClass(Account.class);
        verify(accountDao, times(2)).update(storedAccounts.capture());
        assertThat(storedAccounts.getAllValues(), hasSize(2));

        ArgumentCaptor<Transaction> storedTransactions = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionDao, times(2)).insert(storedTransactions.capture());

        assertThat(storedTransactions.getAllValues(), hasSize(2));
        assertThat(fromAccount.getVersion(), is(1));
        assertThat(toAccount.getVersion(), is(1));
        assertThat(fromAccount.getBalance(), is(100L));
        assertThat(toAccount.getBalance(), is(200L));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void testTransfer_accountLockOrder_reverseInitialOrder() {
        int fromId = 2;
        int toId = 1;
        Account fromAccount = givenMockedAccount(fromId);
        Account toAccount = givenMockedAccount(toId);

        when(accountDao.get(fromId)).thenReturn(Optional.of(fromAccount));
        when(accountDao.get(toId)).thenReturn(Optional.of(toAccount));

        accountService.transfer(fromId, toId, 100, 1);
        InOrder inOrder = inOrder(toAccount, fromAccount);
        inOrder.verify(toAccount).getLock();
        inOrder.verify(fromAccount).getLock();
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void testTransfer_accountLockOrder_orderedInitial() {
        int fromId = 1;
        int toId = 2;
        Account fromAccount = givenMockedAccount(fromId);
        Account toAccount = givenMockedAccount(toId);

        when(accountDao.get(fromId)).thenReturn(Optional.of(fromAccount));
        when(accountDao.get(toId)).thenReturn(Optional.of(toAccount));

        accountService.transfer(fromId, toId, 100, 1);
        InOrder inOrder = inOrder(toAccount, fromAccount);
        inOrder.verify(fromAccount).getLock();
        inOrder.verify(toAccount).getLock();
    }

    private Account givenMockedAccount(int id) {
        Account mock = mock(Account.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getCurrency()).thenReturn(Currency.EUR);
        when(mock.getBalance()).thenReturn(1000L);
        when(mock.getLock()).thenReturn(new Object());
        return mock;
    }

}