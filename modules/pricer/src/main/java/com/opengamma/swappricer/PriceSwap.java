/*
 * Copyright (C) 2017 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.swappricer;

import static com.opengamma.strata.collect.Guavate.toImmutableList;
import static com.opengamma.strata.measure.StandardComponents.marketDataFactory;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.MoreExecutors;
import com.opengamma.strata.basics.ReferenceData;
import com.opengamma.strata.basics.StandardId;
import com.opengamma.strata.calc.CalculationRules;
import com.opengamma.strata.calc.CalculationRunner;
import com.opengamma.strata.calc.Column;
import com.opengamma.strata.calc.Results;
import com.opengamma.strata.calc.marketdata.MarketDataConfig;
import com.opengamma.strata.calc.marketdata.MarketDataRequirements;
import com.opengamma.strata.calc.runner.CalculationFunctions;
import com.opengamma.strata.collect.io.ResourceLocator;
import com.opengamma.strata.collect.timeseries.LocalDateDoubleTimeSeries;
import com.opengamma.strata.data.MarketData;
import com.opengamma.strata.data.ObservableId;
import com.opengamma.strata.loader.csv.FixingSeriesCsvLoader;
import com.opengamma.strata.loader.csv.RatesCalibrationCsvLoader;
import com.opengamma.strata.market.curve.CurveGroupDefinition;
import com.opengamma.strata.market.curve.CurveGroupName;
import com.opengamma.strata.market.observable.QuoteId;
import com.opengamma.strata.measure.Measures;
import com.opengamma.strata.measure.StandardComponents;
import com.opengamma.strata.measure.rate.RatesMarketDataLookup;
import com.opengamma.strata.product.Trade;
import com.opengamma.strata.product.TradeAttributeType;
import com.opengamma.strata.product.TradeInfo;
import com.opengamma.strata.product.common.BuySell;
import com.opengamma.strata.product.swap.type.FixedIborSwapConventions;

/**
 * Example of a simple Swap pricer using the Strata calculation runner. 100k USD LIBOR fixed-float interest rate swaps
 * are generated, with a randomised floating leg. The quotes are statically defined in {@link Quotes}.
 * <p>
 * The calculations are executed on a single thread.
 */
public class PriceSwap {

  private static final Logger log = LoggerFactory.getLogger(PriceSwap.class);

  /**
   * The valuation date.
   */
  private static final LocalDate VAL_DATE = LocalDate.of(2015, 7, 21);
  /**
   * The curve group name.
   */
  private static final CurveGroupName CURVE_GROUP_NAME = CurveGroupName.of("USD-DSCON-LIBOR3M");
  /**
   * The location of the data files.
   */
  private static final String PATH_CONFIG = "src/main/resources/";
  /**
   * The location of the curve calibration groups file.
   */
  private static final ResourceLocator GROUPS_RESOURCE =
      ResourceLocator.ofFile(new File(PATH_CONFIG + "groups.csv"));
  /**
   * The location of the curve calibration settings file.
   */
  private static final ResourceLocator SETTINGS_RESOURCE =
      ResourceLocator.ofFile(new File(PATH_CONFIG + "settings.csv"));
  /**
   * The location of the curve calibration nodes file.
   */
  private static final ResourceLocator CALIBRATION_RESOURCE =
      ResourceLocator.ofFile(new File(PATH_CONFIG + "calibrations.csv"));
  /**
   * The location of the historical fixing file.
   */
  private static final ResourceLocator FIXINGS_RESOURCE =
      ResourceLocator.ofFile(new File(PATH_CONFIG + "usd-libor-3m.csv"));

  /**
   * Runs the example swap pricer.
   *
   * @param args  ignored
   */
  public static void main(String[] args) {
    // setup calculation runner component, which needs life-cycle management
    // a typical application might use dependency injection to obtain the instance
    try (CalculationRunner runner = CalculationRunner.of(MoreExecutors.newDirectExecutorService())) {
      calculate(runner);
    }
  }

  // obtains the data and calculates the grid of results
  private static void calculate(CalculationRunner runner) {
    // the trades that will have measures calculated
    List<Trade> trades = createSwapTrades();

    // the columns, specifying the measures to be calculated
    List<Column> columns = ImmutableList.of(Column.of(Measures.PRESENT_VALUE));

    // load quotes
    ImmutableMap<QuoteId, Double> quotes = Quotes.QUOTES;

    // load fixings
    ImmutableMap<ObservableId, LocalDateDoubleTimeSeries> fixings = FixingSeriesCsvLoader.load(FIXINGS_RESOURCE);

    // create the market data
    MarketData marketData = MarketData.of(VAL_DATE, quotes, fixings);

    // the reference data, such as holidays and securities
    ReferenceData refData = ReferenceData.standard();

    // load the curve definition
    Map<CurveGroupName, CurveGroupDefinition> defns =
        RatesCalibrationCsvLoader.load(GROUPS_RESOURCE, SETTINGS_RESOURCE, CALIBRATION_RESOURCE);
    CurveGroupDefinition curveGroupDefinition = defns.get(CURVE_GROUP_NAME).filtered(VAL_DATE, refData);

    // the configuration that defines how to create the curves when a curve group is requested
    MarketDataConfig marketDataConfig = MarketDataConfig.builder()
        .add(CURVE_GROUP_NAME, curveGroupDefinition)
        .build();

    // the complete set of rules for calculating measures
    CalculationFunctions functions = StandardComponents.calculationFunctions();
    RatesMarketDataLookup ratesLookup = RatesMarketDataLookup.of(curveGroupDefinition);
    CalculationRules rules = CalculationRules.of(functions, ratesLookup);

    // calibrate the curves and calculate the results
    MarketDataRequirements reqs = MarketDataRequirements.of(rules, trades, columns, refData);
    MarketData calibratedMarketData = marketDataFactory().create(reqs, marketDataConfig, marketData, refData);
    Results results = runner.calculate(rules, trades, columns, calibratedMarketData, refData);
    log.info("Results: {}", results);
  }

  //-----------------------------------------------------------------------
  // Create 100k swap trades with random floating legs
  private static List<Trade> createSwapTrades() {
    Random random = new Random(System.currentTimeMillis());

    return random.doubles(100000)
        .mapToObj(PriceSwap::createVanillaFixedVsLibor30ySwap)
        .collect(toImmutableList());
  }

  //-----------------------------------------------------------------------
  // Create a vanilla fixed vs libor 30Y swap
  private static Trade createVanillaFixedVsLibor30ySwap(double fixedRate) {
    TradeInfo tradeInfo = TradeInfo.builder()
        .id(StandardId.of("example", "1"))
        .addAttribute(TradeAttributeType.DESCRIPTION, "Fixed vs Libor 3m")
        .counterparty(StandardId.of("example", "A"))
        .settlementDate(LocalDate.of(2014, 9, 12))
        .build();
    return FixedIborSwapConventions.USD_FIXED_6M_LIBOR_3M.toTrade(
        tradeInfo,
        LocalDate.of(2014, 9, 12), // the start date
        LocalDate.of(2044, 9, 12), // the end date
        BuySell.BUY,               // indicates wheter this trade is a buy or sell
        100_000_000,               // the notional amount
        fixedRate);                    // the fixed interest rate
  }
}
