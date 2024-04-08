package com.vladlevchik.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladlevchik.model.response.ErrorResponse;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

public class BasicServlet extends HttpServlet {
    private final ObjectMapper objectMapper = new ObjectMapper();

    protected void doResponse(HttpServletResponse response, int status, Object value) throws IOException {
        response.setStatus(status);
        objectMapper.writeValue(response.getWriter(), value);
    }

    protected void handleDatabaseError(HttpServletResponse resp) throws IOException {
        doResponse(resp, SC_INTERNAL_SERVER_ERROR, new ErrorResponse("Something happened with the database, try again later!"));
    }

    protected String getCurrencyCodeFromURL(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        return pathInfo.substring(pathInfo.lastIndexOf("/") + 1);
    }
}
