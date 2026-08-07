package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.BillingCycle
import com.example.data.model.Currency
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Subify", appName)
  }

  @Test
  fun `currency conversion works correctly`() {
    val usdAmount = 100.0
    val eurConverted = Currency.convert(usdAmount, Currency.USD, Currency.EUR)
    assertEquals(92.0, eurConverted, 0.01)
  }

  @Test
  fun `billing cycle monthly conversion works correctly`() {
    val yearlyPrice = 120.0
    val monthlyEquivalent = BillingCycle.YEARLY.toMonthlyAmount(yearlyPrice)
    assertEquals(10.0, monthlyEquivalent, 0.01)
  }
}
