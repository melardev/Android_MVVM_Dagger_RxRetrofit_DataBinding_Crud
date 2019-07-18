package com.melardev.android.crud.todos.show;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.melardev.android.crud.R;
import com.melardev.android.crud.databinding.TodoDetailsBinding;
import com.melardev.android.crud.datasource.common.entities.Todo;
import com.melardev.android.crud.datasource.common.models.DataSourceOperation;
import com.melardev.android.crud.datasource.remote.dtos.responses.SuccessResponse;
import com.melardev.android.crud.factories.AppViewModelFactory;
import com.melardev.android.crud.todos.base.BaseActivity;
import com.melardev.android.crud.todos.write.TodoCreateEditActivity;

import javax.inject.Inject;

public class TodoDetailsActivity extends BaseActivity {

    private TodoDetailsBinding binding;
    private long todoId;
    private TodoDetailsViewModel todoDetailsViewModel;

    @Inject
    AppViewModelFactory appViewModelFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        todoId = intent.getLongExtra("TODO_ID", -1);
        initView();
        initViewModel();
    }

    private void initViewModel() {

        todoDetailsViewModel = ViewModelProviders.of(this, appViewModelFactory)
                .get(TodoDetailsViewModel.class);

        todoDetailsViewModel.getTodo().observe(this, resource -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Looper.getMainLooper().isCurrentThread())
                    throw new AssertionError("");
            } else {
                if (Looper.getMainLooper() != Looper.myLooper())
                    throw new AssertionError("");
            }

            if (resource.isLoading()) {
                displayLoader();
            } else if (resource.data != null) {
                Todo todo = resource.data;
                binding.txtDetailsId.setText(String.valueOf(todo.getId()));
                binding.txtDetailsTitle.setText(todo.getTitle());
                binding.txtDetailsDescription.setText(todo.getDescription());
            } else {
                handleErrorResponse(resource.fullMessages);
                finish();
            }
        });

        todoDetailsViewModel.loadTodo(todoId);
    }

    private void initView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_todo_details);
    }

    public void onButtonClicked(View view) {
        Intent intent = new Intent();
        if (binding.btnDetailsEditTodo == view) {
            intent.setComponent(new ComponentName(this, TodoCreateEditActivity.class));
            startActivity(intent);
        } else if (binding.btnDetailsDeleteTodo == view) {
            delete();
            return;
        } else if (binding.btnDetailsGoHome == view) {
            finish();
        }
    }

    private void delete() {

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setMessage("Are you sure You want to delete this todo?")
                .setPositiveButton("Yes",
                        (dialogInterface, id) -> {
                            todoDetailsViewModel.deleteById(todoId);
                            todoDetailsViewModel.getDeleteOperation().observe(TodoDetailsActivity.this, new Observer<DataSourceOperation<SuccessResponse>>() {
                                @Override
                                public void onChanged(DataSourceOperation<SuccessResponse> response) {
                                    if (response.isSuccess()) {
                                        Toast.makeText(TodoDetailsActivity.this, "Todo Deleted successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else if (response.isLoading()) {
                                        displayLoader();
                                    } else {
                                        if (response.fullMessages != null) {
                                            Toast.makeText(TodoDetailsActivity.this,
                                                    TextUtils.join(",", response.fullMessages), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                        })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

}

