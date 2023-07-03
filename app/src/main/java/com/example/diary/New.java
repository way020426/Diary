package com.example.diary;

import static com.example.diary.MainActivity.TAG_INSERT;
import static com.example.diary.MainActivity.TAG_UPDATE;
import static com.example.diary.MainActivity.sqlHelper;
import static com.example.diary.SQLHelper.TABLE_NAME;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class New extends AppCompatActivity {
    private static final int PICK_PHOTO_REQUEST_CODE = 100;
    EditText title;  //标题
    EditText author; //作者
    TextView time;  //时间
    EditText content;//内容
    Button pictureChoice;//从相册选择照片
    ImageView picture;//照片
    private int tag;
    private int id;
    private SQLiteDatabase db;
    private Handler handler = new Handler();

    private Bitmap currentBitmap;  // 用于存储当前选择的图片


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);
        title = findViewById(R.id.new_title);
        author = findViewById(R.id.new_author);
        time = findViewById(R.id.new_time);
        content = findViewById(R.id.new_content);
        pictureChoice = findViewById(R.id.new_pictureChoice);
        picture = findViewById(R.id.new_picture);

        title.setSelection(title.getText().length());
        handler.post(runnable);
        // 这里需要使用本地存储
        author.setText("admin");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年mm月dd日 HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        time.setText(dateFormat.format(date));

        content.setSelection(content.getText().length());

        pictureChoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_PHOTO_REQUEST_CODE);
            }
        });

        db = sqlHelper.getWritableDatabase();

        Intent intent=getIntent();
        tag = intent.getIntExtra("TAG",-1);
        switch(tag){
            case TAG_INSERT:
                break;
            case TAG_UPDATE:
                id=intent.getIntExtra("ID",-1);
                Cursor cursor=db.query(TABLE_NAME,null,"id=?",
                        new String[]{String.valueOf(id)},null,null,null);
                if(cursor.moveToFirst()){
                    @SuppressLint("Range") String select_title=cursor.getString(cursor.getColumnIndex("title"));
                    @SuppressLint("Range") String select_author=cursor.getString(cursor.getColumnIndex("author"));
                    @SuppressLint("Range") String select_content=cursor.getString(cursor.getColumnIndex("content"));
                    title.setText(select_title);
                    author.setText(select_author);
                    content.setText(select_content);
                    @SuppressLint("Range") String picturePath = cursor.getString(cursor.getColumnIndex("picture"));
                    Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
                    picture.setImageBitmap(bitmap);
                }
                break;
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
            Date date = new Date(System.currentTimeMillis());
            time.setText(dateFormat.format(date));
            handler.postDelayed(this, 1000); // 每隔一秒更新一次时间
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri photoUri = data.getData();
                try {
                    currentBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                    picture.setImageBitmap(currentBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //将menu中的actionbar添加进来
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }
    //设置“保存”或者“删除”按钮点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.save) {
            if (tag == TAG_INSERT) {
                ContentValues values = new ContentValues();
                values.put("title", title.getText().toString());
                values.put("time", time.getText().toString());
                values.put("author", author.getText().toString());
                values.put("content", content.getText().toString());
                String filename = "image_" + System.currentTimeMillis() + ".png";
                String path = ImageHelper.saveImageToInternalStorage(this, currentBitmap, filename);
                if (path != null) {
                    values.put("picture", path);
                    db.insert(TABLE_NAME, null, values);
                    values.clear();
                    Toast.makeText(this, "Save", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    db.insert(TABLE_NAME, null, values);
                    values.clear();
                    Toast.makeText(this, "Save", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return true;
            } else if (tag == TAG_UPDATE) {
                // 修改title、content和picture
                String update_title = title.getText().toString();
                String update_author = author.getText().toString();
                String update_time = time.getText().toString();
                String update_content = content.getText().toString();
                ContentValues values = new ContentValues();
                values.put("title", update_title);
                values.put("author", update_author);
                values.put("content", update_content);
                String filename = "image_" + System.currentTimeMillis() + ".png";
                String path = ImageHelper.saveImageToInternalStorage(this, currentBitmap, filename);
                if (path != null) {
                    values.put("picture", path);
                    db.update(TABLE_NAME, values, "id=?", new String[]{String.valueOf(id)});
                    finish();
                    return true;
                } else {
                    db.update(TABLE_NAME, values, "id=?", new String[]{String.valueOf(id)});
                    finish();
                }
            }
        } else if (itemId == R.id.delete) {
            if (tag == TAG_UPDATE) {
                db.delete(TABLE_NAME, "id=?", new String[]{String.valueOf(id)});
            }
            Toast.makeText(this, "Delete", Toast.LENGTH_SHORT).show();
            finish();
        } else if (itemId == R.id.goBack) {
            finish();
        }
        return true;
    }
    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }
}
