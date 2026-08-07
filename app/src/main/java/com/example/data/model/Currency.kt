package com.example.data.model

import java.util.Locale

enum class Currency(
    val code: String,
    val symbol: String,
    val displayName: String,
    var rateInUSD: Double // Rate relative to 1 USD
) {
    USD("USD", "$", "US Dollar", 1.0),
    EUR("EUR", "€", "Euro", 0.92),
    GBP("GBP", "£", "British Pound", 0.79),
    JPY("JPY", "¥", "Japanese Yen", 155.0),
    TK("TK", "৳", "Bangladeshi Taka (TK)", 118.0),
    INR("INR", "₹", "Indian Rupee", 83.5),
    CAD("CAD", "CA$", "Canadian Dollar", 1.37),
    AUD("AUD", "AU$", "Australian Dollar", 1.52);

    companion object {
        fun fromCode(code: String): Currency {
            val upper = code.uppercase()
            if (upper == "BDT" || upper == "TK") return TK
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: USD
        }

        fun updateRate(code: String, rate: Double) {
            val currency = fromCode(code)
            currency.rateInUSD = rate
        }

        fun convert(amount: Double, from: Currency, to: Currency): Double {
            if (from == to) return amount
            val amountInUSD = amount / from.rateInUSD
            return amountInUSD * to.rateInUSD
        }

        fun format(amount: Double, currency: Currency): String {
            return when (currency) {
                JPY -> String.format(Locale.US, "%s%.0f", currency.symbol, amount)
                TK, INR -> String.format(Locale.US, "%s%.2f", currency.symbol, amount)
                else -> String.format(Locale.US, "%s%.2f", currency.symbol, amount)
            }
        }
    }
}

