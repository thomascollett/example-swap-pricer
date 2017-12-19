/*
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.swappricer;

import com.google.common.collect.ImmutableMap;
import com.opengamma.strata.basics.StandardId;
import com.opengamma.strata.data.FieldName;
import com.opengamma.strata.market.observable.QuoteId;

/**
 * The quotes to be used for building the curves. These are statically defined here, but a factory could be written
 * to perturbe the market data for different scenarios.
 */
public class Quotes {

  /** The quotes to be used for building the curves. */
  public static final ImmutableMap<QuoteId, Double> QUOTES;

  static {
    QUOTES = ImmutableMap.<QuoteId, Double>builder()
        .put(ogTicker("USD-DEP-ON"), 0.00058)
        .put(ogTicker("USD-DEP-TN"), 0.00061)
        .put(ogTicker("USD-DEP-1W"), 0.00068)
        .put(ogTicker("USD-OIS-1M"), 0.00072)
        .put(ogTicker("USD-OIS-2M"), 0.00082)
        .put(ogTicker("USD-OIS-3M"), 0.00093)
        .put(ogTicker("USD-OIS-6M"), 0.0009)
        .put(ogTicker("USD-OIS-9M"), 0.00105)
        .put(ogTicker("USD-OIS-1Y"), 0.001185)
        .put(ogTicker("USD-OIS-2Y"), 0.0031865)
        .put(ogTicker("USD-OIS-3Y"), 0.00704)
        .put(ogTicker("USD-OIS-4Y"), 0.011215)
        .put(ogTicker("USD-OIS-5Y"), 0.01515)
        .put(ogTicker("USD-OIS-6Y"), 0.018455)
        .put(ogTicker("USD-OIS-7Y"), 0.02111)
        .put(ogTicker("USD-OIS-8Y"), 0.02332)
        .put(ogTicker("USD-OIS-9Y"), 0.025135)
        .put(ogTicker("USD-OIS-10Y"), 0.026685)
        .put(ogTicker("USD-OIS-30Y"), 0.027254)

        .put(ogTicker("USD-Fixing-3M"), 0.002366)
        .put(ogTicker("USD-FRA-3Mx6M"), 0.0025825)
        .put(ogTicker("USD-FRA-6Mx9M"), 0.0029605)
        .put(ogTicker("USD-IRS3M-1Y"), 0.002943)
        .put(ogTicker("USD-IRS3M-2Y"), 0.00503)
        .put(ogTicker("USD-IRS3M-3Y"), 0.0093915)
        .put(ogTicker("USD-IRS3M-4Y"), 0.013808)
        .put(ogTicker("USD-IRS3M-5Y"), 0.01732)
        .put(ogTicker("USD-IRS3M-7Y"), 0.023962)
        .put(ogTicker("USD-IRS3M-10Y"), 0.0293)
        .put(ogTicker("USD-IRS3M-12Y"), 0.03195)
        .put(ogTicker("USD-IRS3M-15Y"), 0.034235)
        .put(ogTicker("USD-IRS3M-20Y"), 0.036155)
        .put(ogTicker("USD-IRS3M-25Y"), 0.0369685)
        .put(ogTicker("USD-IRS3M-30Y"), 0.037345)
        .put(settlementPrice("Ibor-USD-LIBOR-3M-Seq3"), 0.999799)
        .put(settlementPrice("Ibor-USD-LIBOR-3M-Seq5"), 0.999801)
        .put(settlementPrice("Ibor-USD-LIBOR-3M-Dec16"), 0.999879)
        .build();
  }

  private static QuoteId ogTicker(String index) {
    return QuoteId.of(StandardId.of("OG-Ticker", index), FieldName.MARKET_VALUE);
  }

  private static QuoteId settlementPrice(String index) {
    return QuoteId.of(StandardId.of("OG-Future", index), FieldName.SETTLEMENT_PRICE);
  }
}
