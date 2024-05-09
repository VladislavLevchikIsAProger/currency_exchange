package com.vladlevchik.servlet.exchangeRate;

import com.vladlevchik.dto.ExchangeRateRequestDto;
import com.vladlevchik.dto.ExchangeRateResponseDto;
import com.vladlevchik.exception.InvalidParameterException;
import com.vladlevchik.model.ExchangeRate;
import com.vladlevchik.dao.ExchangeRateDao;
import com.vladlevchik.dao.JdbcExchangeRateDao;
import com.vladlevchik.service.ExchangeRateService;
import com.vladlevchik.servlet.BasicServlet;
import com.vladlevchik.utils.MappingUtils;
import com.vladlevchik.utils.ValidationUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static com.vladlevchik.utils.MappingUtils.convertToDto;
import static jakarta.servlet.http.HttpServletResponse.*;


@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends BasicServlet {
    private final ExchangeRateDao exchangeRateDao = new JdbcExchangeRateDao();
    private final ExchangeRateService exchangeRateService = new ExchangeRateService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        List<ExchangeRate> exchangeRates = exchangeRateDao.findAll();

        List<ExchangeRateResponseDto> exchangeRatesDto = exchangeRates.stream()
                .map(MappingUtils::convertToDto)
                .collect(Collectors.toList());

        doResponse(resp, SC_OK, exchangeRatesDto);

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String baseCurrencyCode = req.getParameter("baseCurrencyCode");
        String targetCurrencyCode = req.getParameter("targetCurrencyCode");
        String rateParam = req.getParameter("rate");

        if (rateParam == null || rateParam.isBlank()) {
            throw new InvalidParameterException("Missing parameter - rate");
        }

        ExchangeRateRequestDto exchangeRateRequestDto = new ExchangeRateRequestDto(baseCurrencyCode, targetCurrencyCode, convertToNumber(rateParam));

        ValidationUtils.validate(exchangeRateRequestDto);

        ExchangeRate exchangeRate = exchangeRateService.save(exchangeRateRequestDto);

        doResponse(resp, SC_CREATED, convertToDto(exchangeRate));

    }

    private static BigDecimal convertToNumber(String rate) {
        try {
            return new BigDecimal(rate);
        } catch (NumberFormatException e) {
            throw new InvalidParameterException("Parameter rate must be a number");
        }
    }


}
