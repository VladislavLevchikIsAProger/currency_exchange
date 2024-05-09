package com.vladlevchik.servlet.currency;

import com.vladlevchik.exception.NotFoundException;
import com.vladlevchik.model.Currency;
import com.vladlevchik.dao.CurrencyDao;
import com.vladlevchik.dao.JdbcCurrencyDao;
import com.vladlevchik.servlet.BasicServlet;
import com.vladlevchik.utils.ValidationUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static com.vladlevchik.utils.MappingUtils.convertToDto;
import static jakarta.servlet.http.HttpServletResponse.*;


@WebServlet("/currency/*")
public class CurrencyServlet extends BasicServlet {

    private final CurrencyDao currencyDao = new JdbcCurrencyDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String code = req.getPathInfo().replaceFirst("/", "");

        ValidationUtils.validateCurrencyCode(code);

        Currency currency = currencyDao.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Currency with code " + code + " not found"));

        doResponse(resp, SC_OK, convertToDto(currency));

    }
}
