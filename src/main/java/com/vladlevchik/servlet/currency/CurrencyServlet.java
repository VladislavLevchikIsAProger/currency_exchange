package com.vladlevchik.servlet.currency;

import com.vladlevchik.model.Currency;
import com.vladlevchik.model.response.ErrorResponse;
import com.vladlevchik.repository.CurrencyRepository;
import com.vladlevchik.repository.JdbcCurrencyRepository;
import com.vladlevchik.servlet.BasicServlet;
import com.vladlevchik.util.Validation;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import static javax.servlet.http.HttpServletResponse.*;

@WebServlet("/currency/*")
public class CurrencyServlet extends BasicServlet {

    private final CurrencyRepository currencyRepository = new JdbcCurrencyRepository();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String currencyCode = getCurrencyCodeFromURL(req);

        if (currencyCode == null || currencyCode.isBlank()) {
            doResponse(resp, SC_BAD_REQUEST, new ErrorResponse("Missing parameter - code"));
            return;
        }

        if (currencyCode.length() != 3) {
            doResponse(resp, SC_BAD_REQUEST, new ErrorResponse("Incorrect long url"));
            return;
        }

        if (!Validation.isValidCurrencyCode(currencyCode)) {
            doResponse(resp, SC_BAD_REQUEST, new ErrorResponse("Currency code must be in ISO 4217 format"));
            return;
        }

        try {

            Currency currency = currencyRepository.findByCode(currencyCode).orElseThrow();
            doResponse(resp, SC_OK, currency);

        } catch (SQLException e) {
            handleDatabaseError(resp);
        } catch (NoSuchElementException e) {
            doResponse(resp, SC_NOT_FOUND, new ErrorResponse("Such currency is not in the database"));
        }
    }
}
