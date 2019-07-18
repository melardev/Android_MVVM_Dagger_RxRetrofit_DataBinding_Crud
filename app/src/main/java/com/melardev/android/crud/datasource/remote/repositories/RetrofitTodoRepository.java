package com.melardev.android.crud.datasource.remote.repositories;

import com.google.gson.Gson;
import com.melardev.android.crud.datasource.common.entities.Todo;
import com.melardev.android.crud.datasource.common.models.DataSourceOperation;
import com.melardev.android.crud.datasource.common.repositories.TodoRepository;
import com.melardev.android.crud.datasource.remote.api.RxTodoApi;
import com.melardev.android.crud.datasource.remote.dtos.responses.ErrorDataResponse;
import com.melardev.android.crud.datasource.remote.dtos.responses.SuccessResponse;

import java.io.IOException;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;

public class RetrofitTodoRepository implements TodoRepository {
    private final Gson gson;
    private RxTodoApi todoApi;

    public RetrofitTodoRepository(RxTodoApi todoApi, Gson gson) {
        this.todoApi = todoApi;
        this.gson = gson;
    }

    @Override
    public Observable<DataSourceOperation<List<Todo>>> getAll() {

        Observable<DataSourceOperation<List<Todo>>> observable = Observable.just(DataSourceOperation.loading(null));

        return observable
                .doOnNext(data -> System.out.println("Loading"))
                .flatMap((Function<DataSourceOperation<List<Todo>>, ObservableSource<DataSourceOperation<List<Todo>>>>) listDataSourceOperation -> todoApi.fetchTodos()
                        .flatMap(todoList -> Observable.just(DataSourceOperation.success(todoList)))
                        .subscribeOn(Schedulers.io())
                        .doOnNext(apiResponse -> System.out.println("Received " + apiResponse.data.size()))
                        .onErrorReturn(throwable -> {
                            // We can not do it with lambda expressions
                            // return buildDataSourceError(List.class, throwable);
                            if (throwable.getClass() == HttpException.class) {
                                Response<?> response = ((HttpException) throwable).response();
                                if (response != null) {
                                    ErrorDataResponse errorDataResponse = buildErrorDataResponse(response);
                                    if (errorDataResponse != null && errorDataResponse.getFullMessages() != null)
                                        return DataSourceOperation.error(errorDataResponse.getFullMessages(), null);
                                }
                            }
                            return DataSourceOperation.error(new String[]{throwable.getMessage()}, null);
                        })
                        .observeOn(AndroidSchedulers.mainThread()));

        /*
        return todoApi.fetchTodos()
                .flatMap(todoList -> Observable.just(DataSourceOperation.success(todoList)))
                .subscribeOn(Schedulers.io())
                .doOnNext(apiResponse -> System.out.println("Received " + apiResponse.data.size()))
                .onErrorReturn(throwable -> {
                    // We can not do it with lambda expressions
                    // return buildDataSourceError(List.class, throwable);
                    if (throwable.getClass() == HttpException.class) {
                        Response<?> response = ((HttpException) throwable).response();
                        if (response != null) {
                            ErrorDataResponse errorDataResponse = buildErrorDataResponse(response);
                            if (errorDataResponse != null && errorDataResponse.getFullMessages() != null)
                                return DataSourceOperation.error(errorDataResponse.getFullMessages(), null);
                        }
                    }
                    return DataSourceOperation.error(new String[]{throwable.getMessage()}, null);
                })
                .observeOn(AndroidSchedulers.mainThread());
        */
    }

    @Override
    public Observable<DataSourceOperation<Todo>> getById(long todoId) {
        return todoApi.fetchTodo(todoId)
                .flatMap(todo -> Observable.just(DataSourceOperation.success(todo)))
                .subscribeOn(Schedulers.io())
                .doOnNext(apiResponse -> System.out.println("Received " + apiResponse.data))
                .onErrorReturn(throwable -> buildDataSourceError(Todo.class, throwable))
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<DataSourceOperation<Todo>> create(Todo todo) {
        return todoApi.createTodo(todo)
                .flatMap(response -> Observable.just(DataSourceOperation.success(response)))
                .subscribeOn(Schedulers.io())
                .doOnNext(apiResponse -> System.out.println("Received " + apiResponse.data))
                .onErrorReturn(throwable -> buildDataSourceError(Todo.class, throwable))
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<DataSourceOperation<Todo>> update(Todo todo) {
        return todoApi.update(todo.getId(), todo)
                .flatMap(response -> Observable.just(DataSourceOperation.success(response)))
                .subscribeOn(Schedulers.io())
                .doOnNext(apiResponse -> System.out.println("Received " + apiResponse.data))
                .onErrorReturn(throwable -> buildDataSourceError(Todo.class, throwable))
                .observeOn(AndroidSchedulers.mainThread());
    }
    @Override
    public Observable<DataSourceOperation<SuccessResponse>> delete(Long id) {
        return todoApi.deleteTodo(id)
                .flatMap(response -> Observable.just(DataSourceOperation.success(response)))
                .subscribeOn(Schedulers.io())
                .doOnNext(apiResponse -> System.out.println("Received " + apiResponse.data))
                .onErrorReturn(throwable -> buildDataSourceError(SuccessResponse.class, throwable))
                .observeOn(AndroidSchedulers.mainThread());
    }

    private <T> DataSourceOperation<T> buildDataSourceError(Class<T> clazz, Throwable throwable) throws IOException {

        if (throwable.getClass() == retrofit2.adapter.rxjava2.HttpException.class) {
            Response<?> response = ((retrofit2.adapter.rxjava2.HttpException) throwable).response();
            if (response != null) {
                ErrorDataResponse errorDataResponse = buildErrorDataResponse(response);
                if (errorDataResponse != null && errorDataResponse.getFullMessages() != null)
                    return DataSourceOperation.error(errorDataResponse.getFullMessages(), null);
            }
        }
        return DataSourceOperation.error(new String[]{throwable.getMessage()}, null);
    }

    private ErrorDataResponse buildErrorDataResponse(Response<?> responseBody) throws IOException {
        ResponseBody body = responseBody.errorBody();
        if (body == null)
            body = (ResponseBody) responseBody.body();

        if (body == null) return null;

        return gson.fromJson(body.string(), ErrorDataResponse.class);
    }
}
