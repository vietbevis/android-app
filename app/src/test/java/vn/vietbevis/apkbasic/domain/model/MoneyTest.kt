package vn.vietbevis.apkbasic.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyTest {
    @Test
    fun vndFormatsWithDotGrouping() {
        assertEquals("1.250.000 d", Money.vnd(1_250_000).formatVnd())
    }

    @Test
    fun positiveAmountIsValidForTransactions() {
        assertTrue(Money.vnd(1).isPositive)
        assertFalse(Money.vnd(0).isPositive)
        assertFalse(Money.vnd(-1).isPositive)
    }
}
