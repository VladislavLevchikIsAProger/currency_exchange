package com.vladlevchik.repository;

import com.vladlevchik.model.ExchangeRate;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ExchangeRateRepository extends CrudRepository<ExchangeRate> {
    Optional<ExchangeRate> findByCodes(String baseCode, String targetCode) throws SQLException;

    List<ExchangeRate> findByCodesWithBaseCurrencyCodeIsUsd(String baseCode, String targetCode) throws SQLException;

    void update(ExchangeRate entity) throws SQLException;
}
