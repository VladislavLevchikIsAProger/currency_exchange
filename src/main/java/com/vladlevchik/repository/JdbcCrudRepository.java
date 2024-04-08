package com.vladlevchik.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class JdbcCrudRepository<T> {

    protected List<T> getList(Connection connection, String query) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        ResultSet resultSet = preparedStatement.executeQuery();

        List<T> list = new ArrayList<>();

        while (resultSet.next()) {
            list.add(getNewObject(resultSet));
        }

        return list;
    }

    protected Long getGeneratedId(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.execute();

        ResultSet resultSet = preparedStatement.getGeneratedKeys();
        resultSet.next();

        return resultSet.getLong("id");
    }

    protected Optional<T> getObjectFromDB(PreparedStatement preparedStatement) throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();

        if (!resultSet.next()) {
            return Optional.empty();
        }

        return Optional.of(getNewObject(resultSet));
    }


    protected abstract T getNewObject(ResultSet resultSet) throws SQLException;
}
