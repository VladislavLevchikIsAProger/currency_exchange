package com.vladlevchik.servlet.exchangeRate;

import com.vladlevchik.model.response.ErrorResponse;
import com.vladlevchik.model.response.ExchangeResponse;
import com.vladlevchik.service.ExchangeRateService;
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

@WebServlet(urlPatterns = "/exchange")
public class ExchangeServlet extends BasicServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String baseCurrencyCode = req.getParameter("from");
        String targetCurrencyCode = req.getParameter("to");
        String amountParameter = req.getParameter("amount");

        if (baseCurrencyCode == null || baseCurrencyCode.isBlank()) {
            doResponse(resp, SC_BAD_REQUEST, new ErrorResponse("Missing parameter - base currency code"));
            return;
        }
        if (targetCurrencyCode == null || targetCurrencyCode.isBlank()) {
            doResponse(resp, SC_BAD_REQUEST, new ErrorResponse("Missing parameter - target currency code"));
            return;
        }
        if (amountParameter == null || amountParameter.isBlank()) {
            doResponse(resp, SC_BAD_REQUEST, new ErrorResponse("Missing parameter - amount"));
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

            BigDecimal amount = new BigDecimal(amountParameter);
            ExchangeResponse exchangeResponse = ExchangeRateService.getExchangeResponse(baseCurrencyCode, targetCurrencyCode, amount);
            doResponse(resp, SC_OK, exchangeResponse);

        } catch (NumberFormatException e) {
            doResponse(resp, SC_BAD_REQUEST, new ErrorResponse("Incorrect data - amount"));
        } catch (SQLException e) {
            handleDatabaseError(resp);
        } catch (NoSuchElementException e) {
            doResponse(resp, SC_NOT_FOUND, new ErrorResponse("Impossible to calculate exchange rate"));
        }
    }
}
