package com.vladlevchik.service;

import com.vladlevchik.dto.ExchangeRequestDto;
import com.vladlevchik.dto.ExchangeResponseDto;
import com.vladlevchik.exception.NotFoundException;
import com.vladlevchik.model.ExchangeRate;
import com.vladlevchik.dao.ExchangeRateDao;
import com.vladlevchik.dao.JdbcExchangeRateDao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static com.vladlevchik.utils.MappingUtils.convertToDto;
import static java.math.MathContext.DECIMAL64;

public class ExchangeService {

    private final ExchangeRateDao exchangeRateDao = new JdbcExchangeRateDao();

    public ExchangeResponseDto exchange(ExchangeRequestDto exchangeRequestDto) {
        ExchangeRate exchangeRate = findExchangeRate(exchangeRequestDto).orElseThrow(
                () -> new NotFoundException(
                        String.format("Exchange rate '%s' - '%s' not found in the database",
                                exchangeRequestDto.getBaseCurrencyCode(), exchangeRequestDto.getTargetCurrencyCode()))
        );

        BigDecimal amount = exchangeRequestDto.getAmount();
        BigDecimal convertedAmount = amount.multiply(exchangeRate.getRate()).setScale(2, RoundingMode.HALF_EVEN);

        return new ExchangeResponseDto(
                convertToDto(exchangeRate.getBaseCurrency()),
                convertToDto(exchangeRate.getTargetCurrency()),
                exchangeRate.getRate(),
                exchangeRequestDto.getAmount(),
                convertedAmount
        );
    }

    private Optional<ExchangeRate> findExchangeRate(ExchangeRequestDto exchangeRequestDto) {
        Optional<ExchangeRate> exchangeRate = findByDirectRate(exchangeRequestDto);

        if (exchangeRate.isEmpty()) {
            exchangeRate = findByIndirectRate(exchangeRequestDto);
        }

        if (exchangeRate.isEmpty()) {
            exchangeRate = findByCrossRate(exchangeRequestDto);
        }

        return exchangeRate;
    }

    private Optional<ExchangeRate> findByDirectRate(ExchangeRequestDto exchangeRequestDto) {
        return exchangeRateDao.findByCodes(exchangeRequestDto.getBaseCurrencyCode(), exchangeRequestDto.getTargetCurrencyCode());
    }

    private Optional<ExchangeRate> findByIndirectRate(ExchangeRequestDto exchangeRequestDto) {
        Optional<ExchangeRate> exchangeRateOptional = exchangeRateDao
                .findByCodes(exchangeRequestDto.getTargetCurrencyCode(), exchangeRequestDto.getBaseCurrencyCode());

        if (exchangeRateOptional.isEmpty()) {
            return Optional.empty();
        }

        ExchangeRate indirectExchangeRate = exchangeRateOptional.get();

        BigDecimal rate = BigDecimal.ONE.divide(indirectExchangeRate.getRate(), DECIMAL64)
                .setScale(6, RoundingMode.HALF_EVEN);

        ExchangeRate directExchangeRate = new ExchangeRate(
                indirectExchangeRate.getTargetCurrency(),
                indirectExchangeRate.getBaseCurrency(),
                rate);

        return Optional.of(directExchangeRate);

    }

    private Optional<ExchangeRate> findByCrossRate(ExchangeRequestDto exchangeRequestDto) {
        Optional<ExchangeRate> usdToBaseOptional = exchangeRateDao.findByCodes("USD", exchangeRequestDto.getBaseCurrencyCode());
        Optional<ExchangeRate> usdToTargetOptional = exchangeRateDao.findByCodes("USD", exchangeRequestDto.getTargetCurrencyCode());

        if (usdToBaseOptional.isEmpty() || usdToTargetOptional.isEmpty()) {
            return Optional.empty();
        }

        ExchangeRate usdToBase = usdToBaseOptional.get();
        ExchangeRate usdToTarget = usdToTargetOptional.get();

        BigDecimal rate = usdToTarget.getRate().divide(usdToBase.getRate(), DECIMAL64)
                .setScale(6, RoundingMode.HALF_EVEN);

        ExchangeRate directExchangeRate = new ExchangeRate(
                usdToBase.getTargetCurrency(),
                usdToTarget.getTargetCurrency(),
                rate
        );

        return Optional.of(directExchangeRate);
    }

}
