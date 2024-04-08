package com.vladlevchik.servlet.exchangeRate;

import com.vladlevchik.model.ExchangeRate;
import com.vladlevchik.model.response.ErrorResponse;
import com.vladlevchik.repository.ExchangeRateRepository;
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
import java.util.NoSuchElementException;

import static javax.servlet.http.HttpServletResponse.*;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends BasicServlet {
    private final ExchangeRateRepository exchangeRateRepository = new JdbcExchangeRateRepository();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("PATCH".equals(req.getMethod())) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String currencyCodes = getCurrencyCodeFromURL(req);

        if (currencyCodes.length() != 6) {
            doResponse(resp, SC_BAD_REQUEST, new ErrorResponse("Incorrect long url"));
            return;
        }

        String baseCurrencyCode = currencyCodes.substring(0, 3);
        String targetCurrencyCode = currencyCodes.substring(3, 6);

        if (!Validation.isValidCurrencyCode(baseCurrencyCode)) {
            doResponse(resp, SC_BAD_REQUEST, new ErrorResponse("Base currency code must be in ISO 4217 format"));
            return;
        }
        if (!Validation.isValidCurrencyCode(targetCurrencyCode)) {
            doResponse(resp, SC_BAD_REQUEST, new ErrorResponse("Target currency code must be in ISO 4217 format"));
            return;
        }

        try {

            ExchangeRate exchangeRate = exchangeRateRepository.findByCodes(baseCurrencyCode, targetCurrencyCode).orElseThrow();
            doResponse(resp, SC_OK, exchangeRate);

        } catch (SQLException e) {
            handleDatabaseError(resp);
        } catch (NoSuchElementException e) {
            doResponse(resp, SC_NOT_FOUND, "There is no exchange rate for this currency pair");
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String currencyCodes = getCurrencyCodeFromURL(req);

        String parameter = req.getReader().readLine();
        if (parameter == null || !parameter.contains("rate")) {
            doResponse(resp, SC_BAD_REQUEST, new ErrorResponse("Missing required parameter rate"));
            return;
        }

        try {
            BigDecimal rate = new BigDecimal(parameter.replaceAll("rate=", ""));

            String baseCurrencyCode = currencyCodes.substring(0, 3);
            String targetCurrencyCode = currencyCodes.substring(3, 6);

            if (!Validation.isValidCurrencyCode(baseCurrencyCode)) {
                doResponse(resp, SC_BAD_REQUEST, new ErrorResponse("Base currency code must be in ISO 4217 format"));
                return;
            }
            if (!Validation.isValidCurrencyCode(targetCurrencyCode)) {
                doResponse(resp, SC_BAD_REQUEST, new ErrorResponse("Target currency code must be in ISO 4217 format"));
                return;
            }

            ExchangeRate exchangeRate = exchangeRateRepository.findByCodes(baseCurrencyCode, targetCurrencyCode).orElseThrow();

            exchangeRate.setRate(rate);

            exchangeRateRepository.update(exchangeRate);

            doResponse(resp, SC_OK, exchangeRate);

        } catch (NumberFormatException e) {
            doResponse(resp, SC_BAD_REQUEST, new ErrorResponse("Incorrect data - rate"));
        } catch (SQLException e) {
            handleDatabaseError(resp);
        } catch (NoSuchElementException e) {
            doResponse(resp, SC_NOT_FOUND, "There is no exchange rate for this currency pair");
        }
    }

}
