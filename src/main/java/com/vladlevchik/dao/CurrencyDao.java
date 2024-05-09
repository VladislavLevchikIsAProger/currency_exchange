package com.vladlevchik.dao;

import com.vladlevchik.model.Currency;

import java.util.Optional;

public interface CurrencyDao extends CrudDao<Currency> {
    Optional<Currency> findByCode(String code);
}
