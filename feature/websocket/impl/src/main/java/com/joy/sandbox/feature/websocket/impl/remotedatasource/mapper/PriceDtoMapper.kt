package com.joy.sandbox.feature.websocket.impl.remotedatasource.mapper

import com.joy.sandbox.feature.websocket.impl.remotedatasource.model.MarketHoursType
import com.joy.sandbox.feature.websocket.impl.remotedatasource.model.PriceDto
import com.joy.sandbox.feature.websocket.impl.remotedatasource.model.QuoteType
import com.joy.sandbox.feature.websocket.impl.remotedatasource.proto.Yaticker

internal fun Yaticker.mapToPriceDto(): PriceDto {
    return PriceDto(
        id = id,
        price = price,
        timeMillis = time,
        currency = currency,
        exchange = exchange,
        quoteType = quoteType.mapToQuoteType(),
        marketHours = marketHours.mapToMarketHoursType(),
        changePercent = changePercent,
        dayVolume = dayVolume,
        dayHigh = dayHigh,
        dayLow = dayLow,
        change = change,
        shortName = shortName,
        marketClosed = false,
    )
}

private fun Yaticker.QuoteType.mapToQuoteType(): QuoteType {
    return when (this) {
        Yaticker.QuoteType.NONE -> QuoteType.NONE
        Yaticker.QuoteType.ALTSYMBOL -> QuoteType.ALTSYMBOL
        Yaticker.QuoteType.HEARTBEAT -> QuoteType.HEARTBEAT
        Yaticker.QuoteType.EQUITY -> QuoteType.EQUITY
        Yaticker.QuoteType.INDEX -> QuoteType.INDEX
        Yaticker.QuoteType.MUTUALFUND -> QuoteType.MUTUALFUND
        Yaticker.QuoteType.MONEYMARKET -> QuoteType.MONEYMARKET
        Yaticker.QuoteType.OPTION -> QuoteType.OPTION
        Yaticker.QuoteType.CURRENCY -> QuoteType.CURRENCY
        Yaticker.QuoteType.WARRANT -> QuoteType.WARRANT
        Yaticker.QuoteType.BOND -> QuoteType.BOND
        Yaticker.QuoteType.FUTURE -> QuoteType.FUTURE
        Yaticker.QuoteType.ETF -> QuoteType.ETF
        Yaticker.QuoteType.COMMODITY -> QuoteType.COMMODITY
        Yaticker.QuoteType.ECNQUOTE -> QuoteType.ECNQUOTE
        Yaticker.QuoteType.CRYPTOCURRENCY -> QuoteType.CRYPTOCURRENCY
        Yaticker.QuoteType.INDICATOR -> QuoteType.INDICATOR
        Yaticker.QuoteType.INDUSTRY -> QuoteType.INDUSTRY
        Yaticker.QuoteType.UNRECOGNIZED -> QuoteType.UNKNOWN
    }
}

private fun Yaticker.MarketHoursType.mapToMarketHoursType(): MarketHoursType {
    return when (this) {
        Yaticker.MarketHoursType.PRE_MARKET -> MarketHoursType.PRE_MARKET
        Yaticker.MarketHoursType.REGULAR_MARKET -> MarketHoursType.REGULAR_MARKET
        Yaticker.MarketHoursType.POST_MARKET -> MarketHoursType.POST_MARKET
        Yaticker.MarketHoursType.EXTENDED_HOURS_MARKET -> MarketHoursType.EXTENDED_HOURS_MARKET
        Yaticker.MarketHoursType.UNRECOGNIZED -> MarketHoursType.UNKNOWN
    }
}
