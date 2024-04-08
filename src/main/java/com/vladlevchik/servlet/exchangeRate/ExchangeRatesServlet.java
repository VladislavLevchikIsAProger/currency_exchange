package com.vladlevchik.servlet.exchangeRate;

import com.vladlevchik.model.ExchangeRate;
import com.vladlevchik.model.response.ErrorResponse;
import com.vladlevchik.repository.CurrencyRepository;
import com.vladlevchik.repository.ExchangeRateRepository;
import com.vladlevchik.repository.JdbcCurrencyRepository;
import com.vladlevchik.repository.JdbcExchangeRateRepository;
import com.vladlevchik.servlet.BasicServlet;
import com.vladlevchik.util.Validation;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

import static javax.servlet.http.HttpServletResponse.*;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends BasicServlet {
    private final ExchangeRateRepository exchangeRateRepository = new JdbcExchangeRateRepository();
    private final CurrencyRepository currencyRepository = new JdbcCurrencyRepository();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            List<ExchangeRate> exchangeRateList = exchangeRateRepository.findAll();
            doResponse(resp, SC_OK, exchangeRateList);
        } catch (SQLException e) {
            handleDatabaseError(resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String baseCurrencyCode = req.getParameter("baseCurrencyCode");
        String targetCurrencyCode = req.getParameter("targetCurrencyCode");
        String rateParam = req.getParameter("rate");

        if (baseCurrencyCode == null || baseCurrencyCode.isBlank()) {
            doResponse(resp, SC_BAD_REQUEST, new ErrorResponse("Missing parameter - baseCurrencyCode"));
            return;
        }
        if (targetCurrencyCode == null || targetCurrencyCode.isBlank()) {
            doResponse(resp, SC_BAD_REQUEST, new ErrorResponse("Missing parameter - targetCurrencyCode"));
            return;
        }
        if (rateParam == null || rateParam.isBlank()) {
            doResponse(resp, SC_BAD_REQUEST, new ErrorResponse("Missing parameter - rate"));
            return;
        }

        if (!Validation.isValidCurrencyCode(baseCurrencyCode)) {
            doResponse(resp, SC_BAD_REQUEST, new ErrorResponse("Base currency code must be in ISO 4217 format"));
            return;
        }
        if (!Validation.isValidCurrencyCode(targetCurrencyCode)) {
            doResponse(resp, SC_BAD_REQUEST, new ErrorResponse("Target currency code must be in ISO 4217 format"));
            return;
        }

        try {
            BigDecimal rate = new BigDecimal(rateParam);

            ExchangeRate exchangeRate = new ExchangeRate(
                    currencyRepository.findByCode(baseCurrencyCode).orElseThrow(),
                    currencyRepository.findByCode(targetCurrencyCode).orElseThrow(),
                    rate);

            long generatedId = exchangeRateRepository.save(exchangeRate);
            exchangeRate.setId(generatedId);

            doResponse(resp, SC_CREATED, exchangeRate);
        } catch (NumberFormatException e) {
            doResponse(resp, SC_BAD_REQUEST, new ErrorResponse("Incorrect data - rate"));
        } catch (SQLException e) {

            if (e.getSQLState().equals("23505")) {
                doResponse(resp, SC_CONFLICT, new ErrorResponse("There is already such a exchange rate"));
                return;
            }

            handleDatabaseError(resp);
        } catch (NoSuchElementException e) {
            doResponse(
                    resp,
                    SC_NOT_FOUND,
                    new ErrorResponse("One or both currencies for which you are trying to add an exchange rate does not exist in the database")
            );
        }
    }
}
