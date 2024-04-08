package com.vladlevchik.repository;

import com.vladlevchik.model.Currency;
import com.vladlevchik.model.ExchangeRate;
import com.vladlevchik.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.math.RoundingMode.HALF_EVEN;

public class JdbcExchangeRateRepository extends JdbcCrudRepository<ExchangeRate> implements ExchangeRateRepository {

    @Override
    public Optional<ExchangeRate> findByCodes(String baseCode, String targetCode) throws SQLException {
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

        try (Connection connection = ConnectionManager.open();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, baseCode);
            preparedStatement.setString(2, targetCode);

            return getObjectFromDB(preparedStatement);
        }
    }

    @Override
    public List<ExchangeRate> findByCodesWithBaseCurrencyCodeIsUsd(String baseCode, String targetCode) throws SQLException {
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
                "WHERE (Base_Currency_Id = (SELECT c.id FROM currencies c WHERE c.code = 'USD') AND\n" +
                "       Target_Currency_Id = (SELECT c2.id FROM currencies c2 WHERE c2.code = ?) OR\n" +
                "       Target_Currency_Id = (SELECT c3.id FROM currencies c3 WHERE c3.code = ?));";

        try (Connection connection = ConnectionManager.open();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, baseCode);
            preparedStatement.setString(2, targetCode);
            ResultSet resultSet = preparedStatement.executeQuery();

            List<ExchangeRate> exchangeRatesList = new ArrayList<>();
            while (resultSet.next()) {
                exchangeRatesList.add(getNewObject(resultSet));
            }

            return exchangeRatesList;
        }
    }

    @Override
    public List<ExchangeRate> findAll() throws SQLException {
        final String query = "SELECT e.ID,\n" +
                "       b.id as base_id, b.Full_Name AS Base_Full_Name, b.Code AS Base_Code, b.Sign AS Base_Sign,\n" +
                "       t.id as target_id, t.Full_Name AS Target_Full_Name, t.Code AS Target_Code, t.Sign AS Target_Sign,\n" +
                "       e.rate\n" +
                "FROM Exchange_Rates e\n" +
                "         JOIN Currencies b ON e.Base_Currency_Id = b.ID\n" +
                "         JOIN Currencies t ON e.Target_Currency_Id = t.ID;";

        try (Connection connection = ConnectionManager.open()) {
            return getList(connection, query);
        }
    }

    @Override
    public void update(ExchangeRate entity) throws SQLException {
        final String query = "UPDATE exchange_rates SET rate = ? WHERE base_currency_id = ? AND target_currency_id = ?";

        try (Connection connection = ConnectionManager.open();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setBigDecimal(1, entity.getRate());
            preparedStatement.setLong(2, entity.getBaseCurrency().getId());
            preparedStatement.setLong(3, entity.getTargetCurrency().getId());

            preparedStatement.execute();
        }
    }

    @Override
    public Long save(ExchangeRate entity) throws SQLException {
        final String query = "INSERT INTO exchange_rates(base_currency_id, target_currency_id, rate) VALUES (?,?,?)";


        try (Connection connection = ConnectionManager.open();
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setLong(1, entity.getBaseCurrency().getId());
            preparedStatement.setLong(2, entity.getTargetCurrency().getId());
            preparedStatement.setBigDecimal(3, entity.getRate());

            return getGeneratedId(preparedStatement);
        }
    }

    @Override
    protected ExchangeRate getNewObject(ResultSet resultSet) throws SQLException {
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
