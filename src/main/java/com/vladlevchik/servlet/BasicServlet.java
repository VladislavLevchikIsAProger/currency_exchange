package com.vladlevchik.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;


public class BasicServlet extends HttpServlet {
    private final ObjectMapper objectMapper = new ObjectMapper();

    protected void doResponse(HttpServletResponse response, int status, Object value) throws IOException {
        response.setStatus(status);
        objectMapper.writeValue(response.getWriter(), value);
    }
}
