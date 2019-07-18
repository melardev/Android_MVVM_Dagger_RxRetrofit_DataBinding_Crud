package com.melardev.android.crud.todos.list;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.melardev.android.crud.R;
import com.melardev.android.crud.databinding.MainActivityBinding;
import com.melardev.android.crud.datasource.common.entities.Todo;
import com.melardev.android.crud.factories.AppViewModelFactory;
import com.melardev.android.crud.todos.base.BaseActivity;
import com.melardev.android.crud.todos.show.TodoDetailsActivity;
import com.melardev.android.crud.todos.write.TodoCreateEditActivity;

import java.util.List;

import javax.inject.Inject;

public class MainActivity extends BaseActivity implements TodoListAdapter.TodoRowEventListener {

    @Inject
    AppViewModelFactory appViewModelFactory;

    private MainActivityBinding binding;

    private TodoListViewModel todoListViewModel;

    private TodoListAdapter todoListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initViewModel();
    }

    private void initView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        todoListAdapter = new TodoListAdapter(this);
        binding.rvTodos.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false));
        binding.rvTodos.setAdapter(todoListAdapter);
    }


    private void initViewModel() {

        todoListViewModel = ViewModelProviders.of(this, appViewModelFactory)
                .get(TodoListViewModel.class);

        displayLoader();
        // Observe the list, if
        todoListViewModel.getTodoList().observe(this, resource -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Looper.getMainLooper().isCurrentThread())
                    throw new AssertionError("");
            } else {
                if (Looper.getMainLooper() != Looper.myLooper())
                    throw new AssertionError("");
            }

            if (resource.isLoading()) {
                displayLoader();
            } else if (resource.data != null && !resource.data.isEmpty()) {
                populateRecyclerView(resource.data);
            } else
                handleErrorResponse();
        });


        todoListViewModel.loadMore();
    }

    protected void displayLoader() {
        super.displayLoader();
        binding.rvTodos.setVisibility(View.GONE);
    }

    protected void hideLoader() {
        super.hideLoader();
        binding.rvTodos.setVisibility(View.VISIBLE);
    }

    private void populateRecyclerView(List<Todo> todos) {
        hideLoader();
        binding.rvTodos.setVisibility(View.VISIBLE);
        todoListAdapter.setItems(todos);
    }

    private void handleErrorResponse() {
        hideLoader();
        binding.rvTodos.setVisibility(View.GONE);
    }

    public void createTodo(View view) {
        Intent intent = new Intent(this, TodoCreateEditActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClicked(Todo todo) {
        Intent intent = new Intent(this, TodoDetailsActivity.class);
        intent.putExtra("TODO_ID", todo.getId());
        startActivity(intent);
    }
}
