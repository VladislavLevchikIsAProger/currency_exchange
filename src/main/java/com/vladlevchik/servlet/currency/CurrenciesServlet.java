package com.vladlevchik.servlet.currency;

import com.vladlevchik.dto.CurrencyRequestDto;
import com.vladlevchik.dto.CurrencyResponseDto;
import com.vladlevchik.model.Currency;
import com.vladlevchik.dao.CurrencyDao;
import com.vladlevchik.dao.JdbcCurrencyDao;
import com.vladlevchik.servlet.BasicServlet;
import com.vladlevchik.utils.MappingUtils;
import com.vladlevchik.utils.ValidationUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.vladlevchik.utils.MappingUtils.convertToDto;
import static com.vladlevchik.utils.MappingUtils.convertToEntity;
import static jakarta.servlet.http.HttpServletResponse.*;

@WebServlet(urlPatterns = "/currencies")
public class CurrenciesServlet extends BasicServlet {

    private final CurrencyDao currencyDao = new JdbcCurrencyDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<Currency> currencies = currencyDao.findAll();

        List<CurrencyResponseDto> currenciesDto = currencies.stream()
                        .map(MappingUtils::convertToDto)
                                .collect(Collectors.toList());

        doResponse(resp, SC_OK, currenciesDto);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String sign = req.getParameter("sign");

        CurrencyRequestDto currencyRequestDto = new CurrencyRequestDto(name, code, sign);

        ValidationUtils.validate(currencyRequestDto);

        Currency currency = currencyDao.save(convertToEntity(currencyRequestDto));

        doResponse(resp, SC_CREATED, convertToDto(currency));

    }
}
