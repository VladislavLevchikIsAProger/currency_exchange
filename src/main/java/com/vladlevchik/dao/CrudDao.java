package com.vladlevchik.dao;

import java.util.List;

public interface CrudDao<T> {
    List<T> findAll();

    T save(T entity);
}
