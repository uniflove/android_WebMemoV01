package com.example.prwebmemov1.UI;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.prwebmemov1.CodeHelper.TaskHelper;
import com.example.prwebmemov1.DataWork.DBSchema.PubMemo;
import com.example.prwebmemov1.Feature.SQLTask;
import com.example.prwebmemov1.R;
import com.safp.ysb.lpui.SmartLog;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private SmartLog slog = new SmartLog(this, true);

    private ArrayAdapter<PubMemo> adapterPubMemo;

    private WebView wvMemo;
    private int selectedItemPos;
    private ListView lvMemo;
    private boolean[] searchOption = {false, true, false, false, false, false, false}; // index 0 = not in use, 1 = all
    private AlertDialog searchDlg;
    private ProgressDialog progressDialog;

    private String html_header = "<!doctype html>\n" +
            "<html lang=\"en\">\n" +
            "  <head>\n" +
            "    <!-- Required meta tags -->\n" +
            "    <meta charset=\"utf-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">\n" +
            "\n" +
            "    <!-- Bootstrap CSS -->\n" +
            "    <link rel=\"stylesheet\" href=\"https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css\" integrity=\"sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh\" crossorigin=\"anonymous\">\n" +
            "\n" +
            "    <title>Hello, world!</title>\n" +
            "  </head>\n" +
            "  <body>";
    private String html_tail = "    <script src=\"https://code.jquery.com/jquery-3.4.1.slim.min.js\" integrity=\"sha384-J6qa4849blE2+poT4WnyKhv5vZF5SrPo0iEjwBvKU7imGFAV0wwj1yYfoRSJoZ+n\" crossorigin=\"anonymous\"></script>\n" +
            "    <script src=\"https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js\" integrity=\"sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo\" crossorigin=\"anonymous\"></script>\n" +
            "    <script src=\"https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js\" integrity=\"sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6\" crossorigin=\"anonymous\"></script>\n" +
            "  </body>\n" +
            "</html>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wvMemo = findViewById(R.id.wvMemo);
        wvMemo.getSettings().setJavaScriptEnabled(true);
        wvMemo.getSettings().setDomStorageEnabled(true);

        progressDialog = new ProgressDialog(this);

        adapterPubMemo = new ArrayAdapter<PubMemo>(this, R.layout.list_item_pubmemo, new ArrayList<PubMemo>()) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if(convertView == null) {
                    convertView = View.inflate(MainActivity.this, R.layout.list_item_pubmemo, null);
                }
                PubMemo model = getItem(position);
                ((TextView)convertView.findViewById(R.id.tvMemo)).setText(model.memo);
                ((TextView)convertView.findViewById(R.id.tvTitle)).setText(model.title);
                ((TextView)convertView.findViewById(R.id.tvCategory1)).setText(model.category1);
                ((TextView)convertView.findViewById(R.id.tvCategory2)).setText(model.category2);
                ((TextView)convertView.findViewById(R.id.tvCategory3)).setText(model.category3);
                return convertView;
            }
        };
        adapterPubMemo.setNotifyOnChange(true);
        lvMemo = findViewById(R.id.lvPubMemo);
        lvMemo.setAdapter(adapterPubMemo);
        lvMemo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedItemPos = position;
                DisplayHTML(adapterPubMemo.getItem(position));
            }
        });

        ((EditText)findViewById(R.id.etSearch)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    String keyword = v.getEditableText().toString();
                    boolean optionChecked = false;
                    for(boolean option : searchOption) {
                        optionChecked |= option;
                    }
                    if(keyword.length() >0 && optionChecked) {
                        progressDialog.show();
                        SQLTask.Search(weakHandler, v.getEditableText().toString(), searchOption);
                    }
                    return true;
                }
                return false;
            }
        });

        createSearchDlg();

        ((EditText)findViewById(R.id.etPassword)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    String server = ((EditText)findViewById(R.id.etServer)).getText().toString();
                    String db = ((EditText)findViewById(R.id.etDB)).getText().toString();
                    String user = ((EditText)findViewById(R.id.etUser)).getText().toString();
                    String pass = ((EditText)findViewById(R.id.etPassword)).getText().toString();
                    SharedPreferences pref = getSharedPreferences(getResources().getString(R.string.pref_login_info), MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("server", server);
                    editor.putString("db", db);
                    editor.putString("user", user);
                    editor.putString("password", pass);
                    editor.apply();
                    SQLTask.setLoginInfo(server, db, user, pass);
                    SQLTask.GetAllRecord(weakHandler);
                    findViewById(R.id.llLogin).setVisibility(View.INVISIBLE);
                    findViewById(R.id.llMemoList).setVisibility(View.VISIBLE);
                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.btSearchOption).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchDlg.show();
                ((CheckBox)searchDlg.findViewById(R.id.cbAll)).setChecked(searchOption[1]); // all
                ((CheckBox)searchDlg.findViewById(R.id.cbMemo)).setChecked(searchOption[PubMemo.INDEX_MEMO]);
                ((CheckBox)searchDlg.findViewById(R.id.cbTitle)).setChecked(searchOption[PubMemo.INDEX_TITLE]);
                ((CheckBox)searchDlg.findViewById(R.id.cbCate1)).setChecked(searchOption[PubMemo.INDEX_CATEGORY1]);
                ((CheckBox)searchDlg.findViewById(R.id.cbCate2)).setChecked(searchOption[PubMemo.INDEX_CATEGORY2]);
                ((CheckBox)searchDlg.findViewById(R.id.cbCate3)).setChecked(searchOption[PubMemo.INDEX_CATEGORY3]);
            }
        });


        SharedPreferences pref = getSharedPreferences(getResources().getString(R.string.pref_login_info), MODE_PRIVATE);
        if(pref.getString("server", null) == null) {
            findViewById(R.id.llMemoList).setVisibility(View.INVISIBLE);
            findViewById(R.id.llLogin).setVisibility(View.VISIBLE);
        } else {
            String server, db, user, pass;
            server = pref.getString("server", "");
            db = pref.getString("db", "");
            user = pref.getString("user", "");
            pass = pref.getString("password", "");
            SQLTask.setLoginInfo(server, db, user, pass);
            SQLTask.GetAllRecord(weakHandler);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        PubMemo src;
        switch(item.getItemId()) {
            case R.id.action_add:
                intent = new Intent(this, EditActivity.class);
                intent.putExtra(EditActivity.EXTRA_REQUEST_CODE, EditActivity.REQUEST_CODE_ADD);
                startActivityForResult(intent, EditActivity.REQUEST_CODE_ADD);
                break;
            case R.id.action_edit:
                intent = new Intent(this, EditActivity.class);
                src = (PubMemo)lvMemo.getItemAtPosition(selectedItemPos);
                intent.putExtra(EditActivity.EXTRA_SRC_ITEM, src);
                intent.putExtra(EditActivity.EXTRA_REQUEST_CODE, EditActivity.REQUEST_CODE_EDIT);
                startActivityForResult(intent, EditActivity.REQUEST_CODE_EDIT);
                break;
            case R.id.action_delete:
                src = (PubMemo)lvMemo.getItemAtPosition(selectedItemPos);
                AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
                dlgBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PubMemo deleteItem = (PubMemo)(lvMemo.getItemAtPosition(selectedItemPos));
                        SQLTask.DeleteMemo(weakHandler, deleteItem);
                    }
                });
                dlgBuilder.setNegativeButton(R.string.cancel, null);
                dlgBuilder.setTitle(src.title);
                dlgBuilder.setMessage("삭제 하시겠습니가?");
                dlgBuilder.create().show();
                break;
            case R.id.action_test:
                SQLTask.GetAllRecord(weakHandler);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == EditActivity.REQUEST_CODE_ADD || resultCode == EditActivity.REQUEST_CODE_EDIT) {
            if(resultCode == RESULT_OK) {
                SQLTask.GetAllRecord(weakHandler);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void DisplayHTML(PubMemo src) {
        findViewById(R.id.llMemoList).setVisibility(View.INVISIBLE);
        wvMemo.setVisibility(View.VISIBLE);
        StringBuffer sb = new StringBuffer();
        String html = sb.append(html_header).append(src.memo).append(html_tail).toString();
        String bb = Base64.encodeToString(html.getBytes(), Base64.NO_PADDING);
        wvMemo.loadData(bb, "text/html", "base64");
//        wvMemo.loadUrl("http://uniflove.ipdisk.co.kr/homework/2015_avr_udp.html");
    }

    private TaskHelper.GWeakHandler<MainActivity> weakHandler =
            new TaskHelper.GWeakHandler<>(MainActivity.this, new TaskHelper.IWeakHandler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case SQLTask.MSG_SEARCH_MEMO:
                    progressDialog.cancel();
                case SQLTask.MSG_GET_ALL:
                    @SuppressWarnings("unchecked")
                    List<PubMemo> result = (List<PubMemo>)msg.obj;
                    adapterPubMemo.clear();
                    adapterPubMemo.addAll(result);
                    if(wvMemo.getVisibility() == View.VISIBLE) {
                        wvMemo.setVisibility(View.INVISIBLE);
                        findViewById(R.id.llMemoList).setVisibility(View.VISIBLE);
                    }
                    break;
                case SQLTask.MSG_DELETE_MEMO:
                    SQLTask.GetAllRecord(weakHandler);
                    break;
            }
        }
    });

    @Override
    public void onBackPressed() {
        if(wvMemo.getVisibility() == View.VISIBLE) {
            wvMemo.setVisibility(View.INVISIBLE);
            findViewById(R.id.llMemoList).setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    private void createSearchDlg() {
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        dlgBuilder.setTitle(R.string.search_option);
        LayoutInflater inflater = this.getLayoutInflater();
        dlgBuilder.setView(inflater.inflate(R.layout.search_option, null));
        dlgBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                searchOption[1] = ((CheckBox)searchDlg.findViewById(R.id.cbAll)).isChecked(); // all
                searchOption[PubMemo.INDEX_MEMO] = ((CheckBox)searchDlg.findViewById(R.id.cbMemo)).isChecked();
                searchOption[PubMemo.INDEX_TITLE] = ((CheckBox)searchDlg.findViewById(R.id.cbTitle)).isChecked();
                searchOption[PubMemo.INDEX_CATEGORY1] = ((CheckBox)searchDlg.findViewById(R.id.cbCate1)).isChecked();
                searchOption[PubMemo.INDEX_CATEGORY2] = ((CheckBox)searchDlg.findViewById(R.id.cbCate2)).isChecked();
                searchOption[PubMemo.INDEX_CATEGORY3] = ((CheckBox)searchDlg.findViewById(R.id.cbCate3)).isChecked();
            }
        });
        dlgBuilder.setNegativeButton(R.string.cancel, null);

        searchDlg = dlgBuilder.create();
    }
}
