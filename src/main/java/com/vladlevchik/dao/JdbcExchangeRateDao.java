package com.vladlevchik.dao;

import com.vladlevchik.exception.DatabaseOperationException;
import com.vladlevchik.exception.EntityExistException;
import com.vladlevchik.model.Currency;
import com.vladlevchik.model.ExchangeRate;
import com.vladlevchik.ConnectionManager;
import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.math.RoundingMode.HALF_EVEN;

public class JdbcExchangeRateDao implements ExchangeRateDao {

    @Override
    public Optional<ExchangeRate> findByCodes(String baseCode, String targetCode) {
        final String query = "SELECT e.ID,\n" +
                "       b.id        AS Base_Id,\n" +
                "       b.Full_Name AS Base_Full_Name,\n" +
                "       b.Code      AS Base_Code,\n" +
                "       b.Sign      AS Base_Sign,\n" +
                "       t.id        AS Target_id,\n" +
                "       t.Full_Name AS Target_Full_Name,\n" +
                "       t.Code      AS Target_Code,\n" +
                "       t.Sign      AS Target_Sign,\n" +
                "       e.rate\n" +
                "FROM Exchange_Rates e\n" +
                "         JOIN Currencies b ON e.Base_Currency_Id = b.ID\n" +
                "         JOIN Currencies t ON e.Target_Currency_Id = t.ID\n" +
                "WHERE (Base_Currency_Id = (SELECT c.id FROM currencies c WHERE c.code = ?) AND\n" +
                "      Target_Currency_Id = (SELECT c2.id FROM currencies c2 WHERE c2.code = ?));";

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, baseCode);
            preparedStatement.setString(2, targetCode);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                return Optional.empty();
            }

            return Optional.of(getExchangeRate(resultSet));
        } catch (SQLException e) {
            throw new DatabaseOperationException(
                    String.format("Failed to read exchange rate '%s' to '%s' from the database",
                            baseCode, targetCode)
            );
        }
    }

    @Override
    public List<ExchangeRate> findAll() {
        final String query = "SELECT e.ID,\n" +
                "       b.id as base_id, b.Full_Name AS Base_Full_Name, b.Code AS Base_Code, b.Sign AS Base_Sign,\n" +
                "       t.id as target_id, t.Full_Name AS Target_Full_Name, t.Code AS Target_Code, t.Sign AS Target_Sign,\n" +
                "       e.rate\n" +
                "FROM Exchange_Rates e\n" +
                "         JOIN Currencies b ON e.Base_Currency_Id = b.ID\n" +
                "         JOIN Currencies t ON e.Target_Currency_Id = t.ID;";

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            List<ExchangeRate> list = new ArrayList<>();

            while (resultSet.next()) {
                list.add(getExchangeRate(resultSet));
            }

            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to read exchange rates from the database");
        }
    }

    @Override
    public ExchangeRate update(ExchangeRate entity) {
        final String query = "UPDATE exchange_rates SET rate = ? WHERE base_currency_id = ? AND target_currency_id = ? RETURNING *";

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setBigDecimal(1, entity.getRate());
            preparedStatement.setLong(2, entity.getBaseCurrency().getId());
            preparedStatement.setLong(3, entity.getTargetCurrency().getId());

            ResultSet resultSet = preparedStatement.executeQuery();

            entity.setRate(resultSet.getBigDecimal("rate"));

            return entity;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update exchange rate with id " + entity.getId() + " in database");
        }
    }

    @Override
    public ExchangeRate save(ExchangeRate entity) {
        final String query = "INSERT INTO exchange_rates(base_currency_id, target_currency_id, rate) VALUES (?,?,?) RETURNING id";


        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setLong(1, entity.getBaseCurrency().getId());
            preparedStatement.setLong(2, entity.getTargetCurrency().getId());
            preparedStatement.setBigDecimal(3, entity.getRate());

            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                throw new DatabaseOperationException(
                        String.format("Failed to save exchange rate '%s' to '%s' to the database",
                                entity.getBaseCurrency().getCode(), entity.getTargetCurrency().getCode())
                );
            }

            entity.setId(resultSet.getLong("id"));

            return entity;
        } catch (SQLException e) {
            if (e instanceof SQLiteException) {
                SQLiteException exception = (SQLiteException) e;
                if (exception.getResultCode().code == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE.code) {
                    throw new EntityExistException(
                            String.format("Exchange rate '%s' to '%s' already exists",
                                    entity.getBaseCurrency().getCode(), entity.getTargetCurrency().getCode())
                    );
                }
            }
            throw new DatabaseOperationException(
                    String.format("Failed to save exchange rate '%s' to '%s' to the database",
                            entity.getBaseCurrency().getCode(), entity.getTargetCurrency().getCode())
            );
        }
    }


    private ExchangeRate getExchangeRate(ResultSet resultSet) throws SQLException {
        return new ExchangeRate(
                resultSet.getLong("id"),
                new Currency(
                        resultSet.getLong("base_id"),
                        resultSet.getString("base_code"),
                        resultSet.getString("base_full_name"),
                        resultSet.getString("base_sign")
                ),
                new Currency(
                        resultSet.getLong("target_id"),
                        resultSet.getString("target_code"),
                        resultSet.getString("target_full_name"),
                        resultSet.getString("target_sign")
                ),
                resultSet.getBigDecimal("rate").setScale(2, HALF_EVEN)
        );
    }

}
