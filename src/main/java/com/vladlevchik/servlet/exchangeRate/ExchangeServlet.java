package com.vladlevchik.servlet.exchangeRate;

import com.vladlevchik.dto.ExchangeRequestDto;
import com.vladlevchik.dto.ExchangeResponseDto;
import com.vladlevchik.exception.InvalidParameterException;
import com.vladlevchik.service.ExchangeService;
import com.vladlevchik.servlet.BasicServlet;
import com.vladlevchik.utils.ValidationUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

import static jakarta.servlet.http.HttpServletResponse.*;


@WebServlet(urlPatterns = "/exchange")
public class ExchangeServlet extends BasicServlet {

    private final ExchangeService exchangeService = new ExchangeService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String baseCurrencyCode = req.getParameter("from");
        String targetCurrencyCode = req.getParameter("to");
        String amountParameter = req.getParameter("amount");

        if (amountParameter == null || amountParameter.isBlank()) {
            throw new InvalidParameterException("Missing parameter - amount");
        }

        ExchangeRequestDto exchangeRequestDto = new ExchangeRequestDto(baseCurrencyCode, targetCurrencyCode, convertToNumber(amountParameter));

        ValidationUtils.validate(exchangeRequestDto);

        ExchangeResponseDto exchangeResponse = exchangeService.exchange(exchangeRequestDto);

        doResponse(resp, SC_OK, exchangeResponse);


    }

    private static BigDecimal convertToNumber(String amount) {
        try {
            return new BigDecimal(amount);
        } catch (NumberFormatException e) {
            throw new InvalidParameterException("Parameter amount must be a number");
        }
    }
}
