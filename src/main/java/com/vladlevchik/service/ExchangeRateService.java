package com.vladlevchik.service;

import com.vladlevchik.model.ExchangeRate;
import com.vladlevchik.model.response.ExchangeResponse;
import com.vladlevchik.repository.ExchangeRateRepository;
import com.vladlevchik.repository.JdbcExchangeRateRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static java.math.MathContext.DECIMAL64;
import static java.math.RoundingMode.HALF_EVEN;

public class ExchangeRateService {

    private static final ExchangeRateRepository exchangeRateRepository = new JdbcExchangeRateRepository();

    public static ExchangeResponse getExchangeResponse(String baseCurrencyCode, String targetCurrencyCode, BigDecimal amount) throws SQLException {
        ExchangeRate exchangeRate = getExchangeRate(baseCurrencyCode, targetCurrencyCode).orElseThrow();
        return new ExchangeResponse(
                exchangeRate.getBaseCurrency(),
                exchangeRate.getTargetCurrency(),
                exchangeRate.getRate(),
                amount,
                exchangeRate.getRate().multiply(amount).setScale(2, HALF_EVEN));
    }

    private static Optional<ExchangeRate> getExchangeRate(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        Optional<ExchangeRate> exchangeRate = getExchangeRateByCode(baseCurrencyCode, targetCurrencyCode);

        if (exchangeRate.isEmpty()) {
            exchangeRate = getReverseExchangeRateByCode(targetCurrencyCode, baseCurrencyCode);
        }

        if (exchangeRate.isEmpty()) {
            exchangeRate = getExchangeRateByCodeWithUsd(baseCurrencyCode, targetCurrencyCode);
        }

        return exchangeRate;
    }

    private static Optional<ExchangeRate> getReverseExchangeRateByCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {

        Optional<ExchangeRate> optionalExchangeRate = getExchangeRateByCode(baseCurrencyCode, targetCurrencyCode);

        if (optionalExchangeRate.isEmpty()) {
            return Optional.empty();
        }

        ExchangeRate reverseExchangeRate = optionalExchangeRate.get();

        BigDecimal reverseRate = BigDecimal.ONE.divide(reverseExchangeRate.getRate(), DECIMAL64);

        ExchangeRate exchangeRate = new ExchangeRate(
                reverseExchangeRate.getTargetCurrency(),
                reverseExchangeRate.getBaseCurrency(),
                reverseRate);

        return Optional.of(exchangeRate);
    }


    private static Optional<ExchangeRate> getExchangeRateByCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        return exchangeRateRepository.findByCodes(baseCurrencyCode, targetCurrencyCode);
    }

    private static Optional<ExchangeRate> getExchangeRateByCodeWithUsd(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        List<ExchangeRate> exchangeRateListWithUsdBase = exchangeRateRepository.findByCodesWithBaseCurrencyCodeIsUsd(baseCurrencyCode, targetCurrencyCode);

        ExchangeRate usdWithBaseCodeExchangeRate = getExchangeRateFromList(exchangeRateListWithUsdBase, baseCurrencyCode);
        ExchangeRate usdWithTargetCodeExchangeRate = getExchangeRateFromList(exchangeRateListWithUsdBase, targetCurrencyCode);

        if (usdWithBaseCodeExchangeRate != null && usdWithTargetCodeExchangeRate != null) {
            BigDecimal usdToBaseRate = usdWithBaseCodeExchangeRate.getRate();
            BigDecimal usdToTargetRate = usdWithTargetCodeExchangeRate.getRate();

            BigDecimal baseToTargetRate = usdToTargetRate.divide(usdToBaseRate, DECIMAL64).setScale(2, HALF_EVEN);

            ExchangeRate exchangeRate = new ExchangeRate(
                    usdWithBaseCodeExchangeRate.getTargetCurrency(),
                    usdWithTargetCodeExchangeRate.getTargetCurrency(),
                    baseToTargetRate
            );

            return Optional.of(exchangeRate);

        }

        return Optional.empty();
    }

    //TODO мб сделать orElseThrow
    private static ExchangeRate getExchangeRateFromList(List<ExchangeRate> exchangeRateListWithUsdBase, String currencyCode) {

        for (ExchangeRate exchangeRate : exchangeRateListWithUsdBase) {
            if (exchangeRate.getTargetCurrency().getCode().equals(currencyCode)) {
                return exchangeRate;
            }
        }

        return null;
    }
}
