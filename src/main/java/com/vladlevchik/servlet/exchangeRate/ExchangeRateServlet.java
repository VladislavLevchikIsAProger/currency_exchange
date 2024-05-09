package com.vladlevchik.servlet.exchangeRate;

import com.vladlevchik.exception.InvalidParameterException;
import com.vladlevchik.exception.NotFoundException;
import com.vladlevchik.model.ExchangeRate;
import com.vladlevchik.dao.ExchangeRateDao;
import com.vladlevchik.dao.JdbcExchangeRateDao;
import com.vladlevchik.servlet.BasicServlet;
import com.vladlevchik.utils.ValidationUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

import static com.vladlevchik.utils.MappingUtils.convertToDto;
import static jakarta.servlet.http.HttpServletResponse.*;


@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends BasicServlet {
    private final ExchangeRateDao exchangeRateDao = new JdbcExchangeRateDao();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        if ("PATCH".equals(req.getMethod())) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String currencyCodes = req.getPathInfo().replaceFirst("/", "");

        if (currencyCodes.length() != 6) {
            throw new InvalidParameterException("Currency codes are either not provided or provided in an incorrect format");
        }

        String baseCurrencyCode = currencyCodes.substring(0, 3);
        String targetCurrencyCode = currencyCodes.substring(3, 6);

        ValidationUtils.validateCurrencyCode(baseCurrencyCode);
        ValidationUtils.validateCurrencyCode(targetCurrencyCode);

        ExchangeRate exchangeRate = exchangeRateDao.findByCodes(baseCurrencyCode, targetCurrencyCode)
                .orElseThrow(() -> new NotFoundException("Exchange rate " + baseCurrencyCode + " - " + targetCurrencyCode + " not found in database"));

        doResponse(resp, SC_OK, convertToDto(exchangeRate));

    }

    //TODO Валидация и rate
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String currencyCodes = req.getPathInfo().replaceFirst("/", "");

        if (currencyCodes.length() != 6) {
            throw new InvalidParameterException("Currency codes are either not provided or provided in an incorrect format");
        }

        String parameter = req.getReader().readLine();
        if (parameter == null || !parameter.contains("rate")) {
            throw new InvalidParameterException("Missing parameter - rate");
        }

        String rateFromParameter = parameter.replace("rate=", "");

        if (rateFromParameter.isBlank()) {
            throw new InvalidParameterException("Missing parameter - rate");
        }

        String baseCurrencyCode = currencyCodes.substring(0, 3);
        String targetCurrencyCode = currencyCodes.substring(3, 6);

        BigDecimal rate = convertToNumber(rateFromParameter);

        ValidationUtils.validateCurrencyCode(baseCurrencyCode);
        ValidationUtils.validateCurrencyCode(targetCurrencyCode);

        ExchangeRate exchangeRate = exchangeRateDao.findByCodes(baseCurrencyCode, targetCurrencyCode)
                .orElseThrow(() -> new NotFoundException("Exchange rate " + baseCurrencyCode + " - " + targetCurrencyCode + " not found in database"));

        exchangeRate.setRate(rate);

        ExchangeRate updatedExchangeRate = exchangeRateDao.update(exchangeRate);

        doResponse(resp, SC_OK, convertToDto(updatedExchangeRate));

    }

    private static BigDecimal convertToNumber(String rate) {
        try {
            return new BigDecimal(rate);
        } catch (NumberFormatException e) {
            throw new InvalidParameterException("Parameter rate must be a number");
        }
    }

}
