package com.melardev.android.crud.datasource.common.repositories;

import com.melardev.android.crud.datasource.common.entities.Todo;
import com.melardev.android.crud.datasource.common.models.DataSourceOperation;
import com.melardev.android.crud.datasource.remote.dtos.responses.SuccessResponse;

import java.util.List;

import io.reactivex.Observable;

public interface BaseRepository<T> {

    Observable<DataSourceOperation<List<T>>> getAll();

    Observable<DataSourceOperation<T>> getById(long id);

    Observable<DataSourceOperation<T>> create(T todo);

    Observable<DataSourceOperation<T>> update(Todo todo);

    Observable<DataSourceOperation<SuccessResponse>> delete(Long id);
}
