package com.vladlevchik.dao;

import com.vladlevchik.exception.DatabaseOperationException;
import com.vladlevchik.exception.EntityExistException;
import com.vladlevchik.model.Currency;
import com.vladlevchik.ConnectionManager;
import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCurrencyDao implements CurrencyDao {

    @Override
    public Optional<Currency> findByCode(String code) {
        final String query = "SELECT * FROM currencies WHERE code = ?";


        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, code);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                return Optional.empty();
            }

            return Optional.of(getCurrency(resultSet));
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to read currency with code " + code + " from the database");
        }
    }

    @Override
    public List<Currency> findAll() {
        final String query = "SELECT * FROM currencies";

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            ResultSet resultSet = statement.executeQuery();

            List<Currency> list = new ArrayList<>();

            while (resultSet.next()) {
                list.add(getCurrency(resultSet));
            }

            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to read currencies from the database");
        }
    }

    @Override
    public Currency save(Currency entity) {
        final String query = "INSERT INTO currencies(code, full_name, sign) VALUES (?,?,?) RETURNING id";

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, entity.getCode());
            statement.setString(2, entity.getFullName());
            statement.setString(3, entity.getSign());

            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                throw new DatabaseOperationException("Failed to save currency with code " + entity.getCode() + " to the database");
            }

            entity.setId(resultSet.getLong("id"));
            return entity;
        } catch (SQLException e) {
            if (e instanceof SQLiteException) {
                SQLiteException exception = (SQLiteException) e;
                if (exception.getResultCode().code == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE.code) {
                    throw new EntityExistException("Currency with code " + entity.getCode() + " already exists");
                }
            }

            throw new DatabaseOperationException("Failed to save currency with code " + entity.getCode() + " to the database");
        }
    }

    private Currency getCurrency(ResultSet resultSet) throws SQLException {
        return new Currency(
                resultSet.getLong("id"),
                resultSet.getString("code"),
                resultSet.getString("full_name"),
                resultSet.getString("sign")
        );
    }


}
