package com.example.diary;

import static com.example.diary.SQLHelper.DB_NAME;
import static com.example.diary.SQLHelper.TABLE_NAME;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    SimpleAdapter adapter;
    private String select_item;
    private List<String> diary=new ArrayList<>();
    private List<Map<String,String>> dataList;
    private int Id;
    ListView listView;
    public static final int TAG_INSERT=1;
    public static final int TAG_UPDATE=0;
    public static SQLHelper sqlHelper; // 数据库实例
    private SQLiteDatabase db;
    int selectedItemPosition = -1; // 初始化为-1表示没有项目被选中
    private SwipeRefreshLayout swipeRefresh; // 下拉刷新的
    int selectedItemId = -1; // 初始化为-1表示没有项目被选中
    private boolean isMultiSelectMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button add = findViewById(R.id.add);
        Button del = findViewById(R.id.del);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout
                .OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        sqlHelper = new SQLHelper(MainActivity.this,DB_NAME,null, 1);
        sqlHelper.getWritableDatabase();
        init();

        // 点击新建跳转
        add.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,New.class);
                intent.putExtra("TAG",TAG_INSERT);
                startActivity(intent);
            }
        });
        // 点击list-item进行跳转
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (isMultiSelectMode) {
                    updateItemSelection(view, position); // 更新项目的选中状态
                }else {
                    Intent intent=new Intent(MainActivity.this,New.class);
                    Id=getDiaryId(position);
                    intent.putExtra("ID",Id);
                    intent.putExtra("TAG",TAG_UPDATE);
                    startActivity(intent);
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                setMultiSelectMode(true); // 启动多选模式
                updateItemSelection(view, position); // 更新项目的选中状态
                return true;
            }
        });

        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isMultiSelectMode) {
                    return;
                }

                Iterator<Map<String, String>> iterator = dataList.iterator();
                while (iterator.hasNext()) {
                    Map<String, String> data = iterator.next();
                    if ("true".equals(data.get("checked"))) {
                        String itemId = data.get("id");
                        db.delete(TABLE_NAME, "id = ?", new String[]{itemId});
                        iterator.remove();
                    }
                }

                isMultiSelectMode = false; // 关闭多选模式
                for (int i = 0; i < listView.getChildCount(); i++) {
                    View item = listView.getChildAt(i);
                    CheckBox checkbox = item.findViewById(R.id.checkbox);
                    checkbox.setVisibility(View.GONE); // 隐藏复选框
                }
                setMultiSelectMode(false); // 关闭多选模式
                adapter.notifyDataSetChanged(); // 刷新视图
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
        setMultiSelectMode(false); // 关闭多选模式
        for (int i = 0; i < listView.getChildCount(); i++) {
            View item = listView.getChildAt(i);
            CheckBox checkbox = item.findViewById(R.id.checkbox);
            checkbox.setVisibility(View.GONE); // 隐藏复选框
        }
    }


    private void refresh(){
        new Thread(new Runnable(){
            @Override
            public void run() {
                try{
                    Thread.sleep(1000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        init();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        }).start();
    }
    private void updateItemSelection(View view, int position) {
        Map<String, String> data = dataList.get(position);
        boolean isSelected = "true".equals(data.get("checked"));
        data.put("checked", isSelected ? "false" : "true");

        // 更新项目视图
        CheckBox checkbox = view.findViewById(R.id.checkbox);
        checkbox.setChecked(!isSelected);

        // 在多选模式下显示复选框，否则隐藏复选框
        checkbox.setVisibility(isMultiSelectMode ? View.VISIBLE : View.GONE);
    }

    // 获得 listview 数据并渲染
    @SuppressLint("Range")
    private void init(){
        dataList = sqlHelper.getAll();
        db=sqlHelper.getWritableDatabase();
        diary.clear();
        for (Map<String, String> data : dataList) {
            data.put("checked", "false");
        }
        //查询数据库，将title一列添加到列表项目中
        Cursor cursor = db.query(TABLE_NAME,null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            String diary_item;
            do{
                diary_item = cursor.getString(cursor.getColumnIndex("title"));
                Log.d("a",diary_item);
                diary.add(diary_item);
            }while(cursor.moveToNext());
        }
        cursor.close();
        adapter = new SimpleAdapter(
                this,
                dataList,
                R.layout.activity_listview,
                new String[]{"id", "title", "time", "author", "content"},
                new int[]{R.id._id, R.id.title, R.id.time, R.id.author, R.id.content}
        ) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                CheckBox checkbox = view.findViewById(R.id.checkbox);

                checkbox.setOnCheckedChangeListener(null); // 必须先清除监听器，防止因为列表项复用导致的混乱
                checkbox.setChecked("true".equals(dataList.get(position).get("checked"))); // 设置CheckBox的状态

                checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isMultiSelectMode) {
                            dataList.get(position).put("checked", isChecked ? "true" : "false"); // 修改对应的数据
                        }
                    }
                });

                checkbox.setVisibility(isMultiSelectMode ? View.VISIBLE : View.GONE); // 根据模式显示或隐藏复选框
                return view;
            }

        };
        listView=findViewById(R.id.list_item);
        listView.setAdapter(adapter);
    }
    @SuppressLint("Range")
    private int getDiaryId(int position){
        //获取所点击的日记的title
        int Id;
        select_item=diary.get(position);
        //获取id
        db=sqlHelper.getWritableDatabase();
        Cursor cursor=db.query(TABLE_NAME,new String[]{"id"},"title=?",
                new String[]{select_item},null,null,null);
        cursor.moveToFirst();
        Id=cursor.getInt(cursor.getColumnIndex("id"));
        return Id;
    }
    private void setMultiSelectMode(boolean enable) {
        isMultiSelectMode = enable;
        for (int i = 0; i < listView.getChildCount(); i++) {
            View item = listView.getChildAt(i);
            CheckBox checkbox = item.findViewById(R.id.checkbox);
            checkbox.setVisibility(enable ? View.VISIBLE : View.GONE);
        }
    }

}