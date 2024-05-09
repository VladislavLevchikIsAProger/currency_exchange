package com.vladlevchik;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter(
        value = {
                "/currencies", "/currency/*", "/exchangeRate/*",
                "/exchangeRates", "/exchange"
        }
)
public class EncodingFilter extends HttpFilter {

    @Override
    public void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        res.setCharacterEncoding("UTF-8");
        req.setCharacterEncoding("UTF-8");

        res.setContentType("application/json");

        super.doFilter(req, res, chain);
    }
}
