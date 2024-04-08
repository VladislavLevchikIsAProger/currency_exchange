package com.vladlevchik.repository;

import com.vladlevchik.model.Currency;
import com.vladlevchik.util.ConnectionManager;

import java.sql.*;
import java.util.List;
import java.util.Optional;

public class JdbcCurrencyRepository extends JdbcCrudRepository<Currency> implements CurrencyRepository {

    @Override
    public Optional<Currency> findByCode(String code) throws SQLException {
        final String query = "SELECT * FROM currencies WHERE code = ?";


        try (Connection connection = ConnectionManager.open();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, code);

            return getObjectFromDB(preparedStatement);
        }
    }

    @Override
    public List<Currency> findAll() throws SQLException{
        final String query = "SELECT * FROM currencies";

        try (Connection connection = ConnectionManager.open()) {
            return getList(connection, query);
        }
    }

    @Override
    public Long save(Currency entity) throws SQLException {
        final String query = "INSERT INTO currencies(code, full_name, sign) VALUES (?,?,?)";


        try (Connection connection = ConnectionManager.open();
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, entity.getCode());
            preparedStatement.setString(2, entity.getFullName());
            preparedStatement.setString(3, entity.getSign());

            return getGeneratedId(preparedStatement);
        }
    }

    @Override
    protected Currency getNewObject(ResultSet resultSet) throws SQLException {
        return new Currency(
                resultSet.getLong("id"),
                resultSet.getString("code"),
                resultSet.getString("full_name"),
                resultSet.getString("sign")
        );
    }

}
