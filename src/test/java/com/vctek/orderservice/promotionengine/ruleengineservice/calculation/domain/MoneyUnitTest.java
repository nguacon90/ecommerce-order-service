package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain;

import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.AmountException;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.CurrenciesAreNotEqualException;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;

import static org.junit.Assert.*;

public class MoneyUnitTest {
    private final Currency euro = new Currency("EUR", 2);

    @Test
    public void testCreateMoney() throws Exception
    {
        //those should work
        checkCreateMoneyProcess(BigDecimal.ZERO, 0, BigInteger.valueOf(0), false);
        checkCreateMoneyProcess(BigDecimal.ZERO, 10, BigInteger.valueOf(0), false);
        checkCreateMoneyProcess(BigDecimal.ONE, 0, BigInteger.valueOf(1), false);
        checkCreateMoneyProcess(BigDecimal.ONE, 3, BigInteger.valueOf(1000), false);
        checkCreateMoneyProcess(BigDecimal.valueOf(3.5), 3, BigInteger.valueOf(3500), false);
        checkCreateMoneyProcess(BigDecimal.valueOf(3.5), 1, BigInteger.valueOf(35), false);
        checkCreateMoneyProcess(new BigDecimal("1.000"), 0, BigInteger.valueOf(1), false);
        checkCreateMoneyProcess(new BigDecimal("1.2345"), 4, BigInteger.valueOf(12345), false);
        checkCreateMoneyProcess(new BigDecimal("1.2345"), 5, BigInteger.valueOf(123450), false);

        //working negative value
        checkCreateMoneyProcess(new BigDecimal("-1.2"), 5, BigInteger.valueOf(-120000), false);

        checkCreateMoneyProcess(new BigDecimal("1.2"), 0, BigInteger.valueOf(1), false);
        checkCreateMoneyProcess(new BigDecimal("1.000001"), 2, BigInteger.valueOf(100), false);

        //those should NOT work
        checkCreateMoneyProcess(new BigDecimal("1.2"), -1, null, true);

        //check other constructors
        assertEquals(new Money(new BigDecimal("1.2"), euro), new Money(120, euro));
    }

    private void checkCreateMoneyProcess(final BigDecimal amount, final int digits, final BigInteger unscaledValue,
                                         final boolean shouldFail) {
        try
        {
            final Currency dummyCurr = new Currency("dummyCurrency", digits);
            final Money money = new Money(amount, dummyCurr);
            if (shouldFail)
            {
                fail("expected  ArithmeticException!");
            }
            assertEquals(unscaledValue, money.getAmount().unscaledValue());
        }
        catch (final Exception e)
        {
            if (!shouldFail)
            {
                throw e;
            }
        }
    }


    @Test(expected = IllegalArgumentException.class)
    public void testCheckCurrencyWithNull()
    {
        final Money amount1 = new Money(BigDecimal.valueOf(3.45), new Currency("TietzeT", 4));
        amount1.assertCurrenciesAreEqual((Currency) null);
    }

    @Test
    public void testCheckCurrencyEqual()
    {
        final Money amount1 = new Money(BigDecimal.valueOf(3.45), new Currency("TietzeT", 4));
        final Money amount2 = new Money(BigDecimal.valueOf(3343.45), new Currency("TietzeT", 4));

        amount1.assertCurrenciesAreEqual(amount2.getCurrency());
        amount2.assertCurrenciesAreEqual(amount1);

        final Currency xxx = new Currency("xXx", 3);
        final Money amount3 = new Money(BigDecimal.valueOf(322.45), xxx);
        final Money amount4 = new Money(BigDecimal.valueOf(33343.45), xxx);

        amount3.assertCurrenciesAreEqual(amount4.getCurrency());
        amount4.assertCurrenciesAreEqual(amount3);
        amount3.assertCurrenciesAreEqual(amount3.getCurrency());
    }

    @Test
    public void testCheckCurrencyDifferent()
    {
        final Money amount1 = new Money(BigDecimal.valueOf(3.45), new Currency("TietzeT", 4));
        final Money amount2 = new Money(BigDecimal.valueOf(3343.45), new Currency("TietzeT", 3));
        final Money amount3 = new Money(BigDecimal.valueOf(3343.45), new Currency("xxx", 3));

        checkCurrAndFail(amount1, amount2);
        checkCurrAndFail(amount2, amount1);
        checkCurrAndFail(amount2, amount3);
        checkCurrAndFail(amount3, amount2);
        checkCurrAndFail(amount1, amount3);
        checkCurrAndFail(amount3, amount1);

    }

    private void checkCurrAndFail(final Money amt1, final Money amt2)
    {
        try
        {
            amt1.assertCurrenciesAreEqual(amt2.getCurrency());
            fail("expected CurrenciesAreNotEqualException");
        }
        catch (final CurrenciesAreNotEqualException e)
        {
            //fine
        }
        catch (final Exception e2)
        {
            fail("got unexpected exception: " + e2);
        }
    }


    @Test
    public void testAddMoneySuccess()
    {
        final Currency curr = new Currency("EUR", 25);
        final Money val1 = new Money(new BigDecimal(2), curr);
        final Money val2 = new Money(new BigDecimal(3), curr);
        final Money expectedRes = new Money(new BigDecimal(5), curr);
        final Money actualRes = val1.add(val2);

        assertEquals(expectedRes, actualRes);
        assertFalse(expectedRes.hashCode() == actualRes.hashCode());

        //BIG NUMBERS NOW!!!!!

        final Money val3 = new Money(BigDecimal.valueOf(8250325.12), curr);
        final Money val4 = new Money(BigDecimal.valueOf(4321456.31), curr);
        final Money expectedRes2 = new Money(BigDecimal.valueOf(12571781.43), curr);
        assertEquals(expectedRes2, val3.add(val4));

        final Money val5 = new Money(new BigDecimal("8273872368712658763457862348566489.7162578164578825032512"), curr);
        final Money val6 = new Money(new BigDecimal("8762347526136571645982560956723521.8374618726457432145631"), curr);
        final Money expectedRes3 = new Money(new BigDecimal("17036219894849230409440423305290011.5537196891036257178143"), curr);
        assertEquals(expectedRes3, val5.add(val6));
    }

    @Test
    public void testAddMoneyFailure()
    {
        final Money val3 = new Money(BigDecimal.valueOf(8250325.12), new Currency("xXx", 2));
        final Money val4 = new Money(BigDecimal.valueOf(4321456.31), new Currency("XxX", 3));

        try
        {
            val3.add(val4);
            fail("expected CurrenciesAreNotEqualException here");
        }
        catch (final CurrenciesAreNotEqualException e)
        {
            //fine
        }
        catch (final Exception e)
        {
            fail("unexpected exception: " + e);
        }
    }

    @Test
    public void testSubtract() throws CurrenciesAreNotEqualException
    {
        final Currency curr = new Currency("EUR", 25);
        final Money val1 = new Money(new BigDecimal(5), curr);
        final Money val2 = new Money(new BigDecimal(2), curr);
        final Money expectedRes = new Money(new BigDecimal(3), curr);
        final Money actualRes = val1.subtract(val2);

        assertEquals(expectedRes, actualRes);
        assertFalse(expectedRes.hashCode() == actualRes.hashCode());

        final Money expectedRes2 = new Money(new BigDecimal(-3), curr);
        assertEquals(expectedRes2, val2.subtract(val1));

        final Money val5 = new Money(new BigDecimal("8273872368712658763457862348566489.7162578164578825032512"), curr);
        final Money val6 = new Money(new BigDecimal("8273872368712658763457862348566489.7162578164578825032511"), curr);
        final Money expectedRes3 = new Money(new BigDecimal("0.0000000000000000000001"), curr);
        assertEquals(expectedRes3, val5.subtract(val6));
    }

    @Test
    public void testSubtractMoneyFailure()
    {
        final Money val3 = new Money(BigDecimal.valueOf(8250325.12), new Currency("xXx", 2));
        final Money val4 = new Money(BigDecimal.valueOf(4321456.31), new Currency("XxX", 3));

        try
        {
            val3.subtract(val4);
            fail("expected CurrenciesAreNotEqualException here");
        }
        catch (final CurrenciesAreNotEqualException e)
        {
            //fine
        }
        catch (final Exception e)
        {
            fail("unexpected exception: " + e);
        }
    }

    @Test
    public void testMoneyEquals()
    {
        final Money val1 = new Money(BigDecimal.valueOf(8250325.12), new Currency("xXx", 2));
        final Money val2 = new Money(BigDecimal.valueOf(8250325.120000), new Currency("xXx", 2));
        final Money val3 = new Money(BigDecimal.valueOf(8250325.12), euro);
        final Money val4 = new Money(BigDecimal.valueOf(8250325.12), new Currency("EUR", 3));

        assertTrue(val1.equals(val2));
        assertTrue(val2.equals(val1));
        assertTrue(val1.equals(val1));
        assertFalse(val1.equals(val3));
        assertFalse(val1.equals(val4));
        assertFalse(val4.equals(val3));
    }


    @Test
    public void testSumMoney()
    {
        final Money val1 = new Money("20.3", euro);
        final Money val2 = new Money("100", euro);
        final Money val3 = new Money("5", euro);
        assertEquals(new Money("125.3", euro), Money.sum(val1, val2, val3));

        final Money neg = new Money("-20.3", euro);
        assertEquals(new Money("0", euro), Money.sum(val1, neg));
        assertEquals(val1, Money.sum(val1));

        final Money otherMoney = new Money("5", new Currency("$", 2));
        try
        {
            Money.sum(otherMoney, val1);
            fail("Expected CurrenciesAreNotEqualException");
        }
        catch (final CurrenciesAreNotEqualException e)
        {
            //fine
        }

        try
        {
            Money.sum();
            fail("Expected MoneyException");
        }
        catch (final AmountException e)
        {
            //fine
        }
    }

    @Test(expected = AmountException.class)
    public void testSumMoneyCollection()
    {
        Money.sum(Collections.EMPTY_SET);
    }

}
