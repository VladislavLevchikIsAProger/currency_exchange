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
import java.util.List;

import static javax.servlet.http.HttpServletResponse.*;

@WebServlet(urlPatterns = "/currencies")
public class CurrenciesServlet extends BasicServlet {

    private final CurrencyRepository currencyRepository = new JdbcCurrencyRepository();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {

            List<Currency> currencyList = currencyRepository.findAll();
            doResponse(resp, SC_OK, currencyList);

        } catch (SQLException e) {
            handleDatabaseError(resp);
        }

    }

    //TODO СДЕЛАНО!
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String sign = req.getParameter("sign");

        if (name == null || name.isBlank()) {
            doResponse(resp, SC_BAD_REQUEST, new ErrorResponse("Missing parameter - name"));
            return;
        }
        if (code == null || code.isBlank()) {
            doResponse(resp, SC_BAD_REQUEST, new ErrorResponse("Missing parameter - code"));
            return;
        }
        if (sign == null || sign.isBlank()) {
            doResponse(resp, SC_BAD_REQUEST, new ErrorResponse("Missing parameter - sign"));
            return;
        }

        if (!Validation.isValidCurrencyName(name)) {
            doResponse(resp, SC_BAD_REQUEST, new ErrorResponse("Incorrect data - name"));
            return;
        }
        if (!Validation.isValidCurrencyCode(code)) {
            doResponse(resp, SC_BAD_REQUEST, new ErrorResponse("Currency code must be in ISO 4217 format"));
            return;
        }
        if (!Validation.isValidCurrencySign(sign)) {
            doResponse(resp, SC_BAD_REQUEST, new ErrorResponse("Incorrect data - sign"));
            return;
        }

        try {
            Currency currency = new Currency(code, name, sign);
            long id = currencyRepository.save(currency);
            currency.setId(id);

            doResponse(resp, SC_CREATED, currency);
        } catch (SQLException e) {

            if (e.getSQLState().equals("23505")) {
                doResponse(resp, SC_CONFLICT, new ErrorResponse("There is already such a currency"));
                return;
            }

            handleDatabaseError(resp);
        }
    }
}
