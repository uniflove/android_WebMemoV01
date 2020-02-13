package com.example.prwebmemov1.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.prwebmemov1.CodeHelper.TaskHelper;
import com.example.prwebmemov1.DataWork.DBSchema.PubMemo;
import com.example.prwebmemov1.Feature.SQLTask;
import com.example.prwebmemov1.R;
import com.safp.ysb.lpui.SmartLog;

public class EditActivity extends AppCompatActivity {
    // Coding Helpers ///////////////////////////////////////////////////////////////////////
    private SmartLog slog = new SmartLog(this, true);

    public static final String EXTRA_REQUEST_CODE = "requestCode";
    public static final int REQUEST_CODE_ADD = 1;
    public static final int REQUEST_CODE_EDIT = 2;

    public static final String  EXTRA_SRC_ITEM = "srcItem";

    private ProgressDialog progressDialog;

    private PubMemo editItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        progressDialog = new ProgressDialog(this);

        editItem = (PubMemo)getIntent().getSerializableExtra(EXTRA_SRC_ITEM);
        if(editItem != null) {
            ((EditText)findViewById(R.id.etMemo)).setText(editItem.memo);
            ((EditText)findViewById(R.id.etTitle)).setText(editItem.title);
            ((EditText)findViewById(R.id.etCategory1)).setText(editItem.category1);
            ((EditText)findViewById(R.id.etCategory2)).setText(editItem.category2);
            ((EditText)findViewById(R.id.etCategory3)).setText(editItem.category3);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_ok:
                switch(getIntent().getIntExtra(EXTRA_REQUEST_CODE, -1)) {
                    case REQUEST_CODE_ADD:
                        progressDialog.show();
                        collectDataFromUI();
                        SQLTask.InsertMemo(weakHandler, editItem);
                        break;
                    case REQUEST_CODE_EDIT:
                        progressDialog.show();
                        collectDataFromUI();
                        SQLTask.UpdateMemo(weakHandler, editItem);
                        break;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void collectDataFromUI() {
        int tempId = (editItem == null) ? 1 : editItem.id;
        editItem = new PubMemo(tempId,
                ((EditText)findViewById(R.id.etMemo)).getText().toString(),
                ((EditText)findViewById(R.id.etTitle)).getText().toString(),
                ((EditText)findViewById(R.id.etCategory1)).getText().toString(),
                ((EditText)findViewById(R.id.etCategory2)).getText().toString(),
                ((EditText)findViewById(R.id.etCategory3)).getText().toString()
                );
    }

    private TaskHelper.GWeakHandler<EditActivity> weakHandler = new TaskHelper.GWeakHandler<>(EditActivity.this, new TaskHelper.IWeakHandler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case SQLTask.MSG_ERROR:
                    slog.toast("ERROR");
                    progressDialog.cancel();
                    break;
                case SQLTask.MSG_INSERT_MEMO:
                    slog.toast("saved");
                    progressDialog.cancel();
                    setResult(RESULT_OK);
                    finish();
                    break;
                case SQLTask.MSG_UPDATE_MEMO:
                    slog.toast("updated");
                    progressDialog.cancel();
                    setResult(RESULT_OK);
                    finish();
                    break;
            }
        }
    });

}
