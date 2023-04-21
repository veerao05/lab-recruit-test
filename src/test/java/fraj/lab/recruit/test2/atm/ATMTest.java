package fraj.lab.recruit.test2.atm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ATMTest {

    private static final int mockUserZeroAmount = 0;
    private static final int mockUserNegAmount = -10;
    private static final int mockUserNonZeroAmount = 10;

    private ATM atm;
    private AmountSelector amountSelector;
    private CashManager cashManager;
    private PaymentProcessor paymentProcessor;


    @BeforeEach
    public void setUp() {

        amountSelector = mock(AmountSelector.class);
        cashManager = mock(CashManager.class);
        paymentProcessor = mock(PaymentProcessor.class);
        atm = new ATM(amountSelector, cashManager, paymentProcessor);
    }


    @Test
    void atmInvalidUserZeroAmountTest() {

        //given
        when(amountSelector.selectAmount()).thenReturn(mockUserZeroAmount);

        //when
        final Exception exception = assertThrows(Exception.class, () -> {
            atm.runCashWithdrawal();
        });

        // then
        assertTrue(exception instanceof ATMTechnicalException);
    }

    @Test
    void atmInvalidUserNegativeAmountTest() {

        //given
        when(amountSelector.selectAmount()).thenReturn(mockUserNegAmount);

        //when
        final Exception exception = assertThrows(Exception.class, () -> {
            atm.runCashWithdrawal();
        });

        // then
        assertTrue(exception instanceof ATMTechnicalException);
    }

    @Test
    void atmValidUserPositiveAmountTest() throws ATMTechnicalException {

        //given
        when(amountSelector.selectAmount()).thenReturn(mockUserNonZeroAmount);
        when(cashManager.canDeliver(mockUserNonZeroAmount)).thenReturn(true);
        when(paymentProcessor.pay(mockUserNonZeroAmount)).thenReturn(PaymentStatus.SUCCESS);

        //when
        final ATMStatus status = atm.runCashWithdrawal();

        //then
        assertEquals(ATMStatus.DONE, status);
        verify(paymentProcessor, times(1)).pay(mockUserNonZeroAmount);
        verify(cashManager, times(1)).deliver(mockUserNonZeroAmount);

    }


    @Test
    void atmWithDrawAmountNoCashTest() throws ATMTechnicalException {

        //given
        when(amountSelector.selectAmount()).thenReturn(mockUserNonZeroAmount);
        when(cashManager.canDeliver(mockUserNonZeroAmount)).thenReturn(false);

        //when
        final ATMStatus status = atm.runCashWithdrawal();

        //then
        assertEquals(ATMStatus.CASH_NOT_AVAILABLE, status);

    }


    @Test
    void atmWithDrawAmountFailedPaymentTest() throws ATMTechnicalException {

        //given
        when(amountSelector.selectAmount()).thenReturn(mockUserNonZeroAmount);
        when(cashManager.canDeliver(mockUserNonZeroAmount)).thenReturn(true);
        when(paymentProcessor.pay(mockUserNonZeroAmount)).thenReturn(PaymentStatus.FAILURE);

        //when
        final ATMStatus status = atm.runCashWithdrawal();

        //then
        assertEquals(ATMStatus.PAYMENT_REJECTED, status);
        verify(cashManager, times(0)).deliver(mockUserNonZeroAmount);

    }


}
