package com.vladlevchik.repository;

import java.sql.SQLException;
import java.util.List;

public interface CrudRepository<T> {
    List<T> findAll() throws SQLException;

    Long save(T entity) throws SQLException;
}
