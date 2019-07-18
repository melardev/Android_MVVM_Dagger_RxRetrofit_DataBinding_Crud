package com.melardev.android.crud.todos.write;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.melardev.android.crud.R;
import com.melardev.android.crud.databinding.TodoWriteBinding;
import com.melardev.android.crud.datasource.common.entities.Todo;
import com.melardev.android.crud.factories.AppViewModelFactory;
import com.melardev.android.crud.todos.base.BaseActivity;
import com.melardev.android.crud.todos.list.MainActivity;

import javax.inject.Inject;

public class TodoCreateEditActivity extends BaseActivity {

    @Inject
    AppViewModelFactory appViewModelFactory;

    private TodoWriteViewModel todoListViewModel;
    private boolean editMode;
    private long todoId;

    private TodoWriteBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            this.todoId = intent.getLongExtra("TODO_ID", -1);
            this.editMode = todoId != -1;
        }
        initView();
        initViewModel();
    }

    private void initView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_todo_create_edit);
        if (editMode) {
            binding.btnSaveTodo.setText("Save Changes");
        } else {
            binding.btnSaveTodo.setText("Create");
        }
    }


    private void initViewModel() {

        todoListViewModel = ViewModelProviders.of(this, appViewModelFactory)
                .get(TodoWriteViewModel.class);

        if (editMode) {
            // Observe the list, if
            todoListViewModel.getTodo().observe(this, resource -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Looper.getMainLooper().isCurrentThread())
                        throw new AssertionError("");
                } else {
                    if (Looper.getMainLooper() != Looper.myLooper())
                        throw new AssertionError("");
                }

                if (resource.isLoading()) {
                    // displayLoader();
                } else if (resource.data != null) {
                    //(resource.data);
                    Todo todo = resource.data;
                    binding.eTxtTitle.setText(todo.getTitle());
                    binding.eTxtDescription.setText(todo.getDescription());
                    binding.txtId.setText(String.valueOf(todo.getId()));
                } else {
                    // onError();
                }
            });

            todoListViewModel.loadTodo(todoId);
        }
    }

    public void saveTodo(View view) {
        String title = binding.eTxtTitle.getText().toString();
        String description = binding.eTxtDescription.getText().toString();

        if (editMode) {
        } else {
            todoListViewModel.createTodo(title, description);
        }

        todoListViewModel.getWriteTodoOperation().observe(this, resource -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Looper.getMainLooper().isCurrentThread())
                    throw new AssertionError("");
            } else {
                if (Looper.getMainLooper() != Looper.myLooper())
                    throw new AssertionError("");
            }

            if (resource.isLoading()) {
                // displayLoader();
            } else if (resource.data != null) {

                Toast.makeText(TodoCreateEditActivity.this, "Todo Created!", Toast.LENGTH_LONG);
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {   // handleErrorResponse();
            }
        });
    }
}
