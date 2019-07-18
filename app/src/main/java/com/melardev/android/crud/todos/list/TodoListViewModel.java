package com.melardev.android.crud.todos.list;

import android.annotation.SuppressLint;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.melardev.android.crud.datasource.common.entities.Todo;
import com.melardev.android.crud.datasource.common.models.DataSourceOperation;
import com.melardev.android.crud.datasource.common.repositories.TodoRepository;

import java.util.List;

import javax.inject.Inject;

@SuppressWarnings("WeakerAccess")
public class TodoListViewModel extends ViewModel {
    private TodoRepository todoRepository;

    private MutableLiveData<DataSourceOperation<List<Todo>>> todoListLiveData = new MutableLiveData<>();

    @Inject
    public TodoListViewModel(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }


    @SuppressLint("CheckResult")
    public void loadMore() {
        todoRepository.getAll()
                .doOnNext(resource -> todoListLiveData.postValue(resource))
                .subscribe(resource -> todoListLiveData.postValue(resource));
    }

    public LiveData<DataSourceOperation<List<Todo>>> getTodoList() {
        return todoListLiveData;
    }
}
