package com.vladlevchik.service;

import com.vladlevchik.dao.CurrencyDao;
import com.vladlevchik.dao.ExchangeRateDao;
import com.vladlevchik.dao.JdbcCurrencyDao;
import com.vladlevchik.dao.JdbcExchangeRateDao;
import com.vladlevchik.dto.ExchangeRateRequestDto;
import com.vladlevchik.exception.NotFoundException;
import com.vladlevchik.model.Currency;
import com.vladlevchik.model.ExchangeRate;

public class ExchangeRateService {

    private final CurrencyDao currencyDao = new JdbcCurrencyDao();
    private final ExchangeRateDao exchangeRateDao = new JdbcExchangeRateDao();

    public ExchangeRate save(ExchangeRateRequestDto exchangeRateRequestDto) {
        String baseCurrencyCode = exchangeRateRequestDto.getBaseCurrencyCode();
        String targetCurrencyCode = exchangeRateRequestDto.getTargetCurrencyCode();

        Currency baseCurrency = currencyDao.findByCode(baseCurrencyCode)
                .orElseThrow(() -> new NotFoundException("Currency with code " + baseCurrencyCode + " not found"));
        Currency targetCurrency = currencyDao.findByCode(targetCurrencyCode)
                .orElseThrow(() -> new NotFoundException("Currency with code " + targetCurrencyCode + " not found"));

        ExchangeRate exchangeRate = new ExchangeRate(baseCurrency, targetCurrency, exchangeRateRequestDto.getRate());

        return exchangeRateDao.save(exchangeRate);
    }

}
