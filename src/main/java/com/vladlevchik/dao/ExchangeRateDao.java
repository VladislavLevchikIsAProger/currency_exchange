package com.vladlevchik.dao;

import com.vladlevchik.model.ExchangeRate;

import java.util.Optional;

public interface ExchangeRateDao extends CrudDao<ExchangeRate> {
    Optional<ExchangeRate> findByCodes(String baseCode, String targetCode);

    ExchangeRate update(ExchangeRate entity);
}
