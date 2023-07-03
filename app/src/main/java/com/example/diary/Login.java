package com.example.diary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

// 加密
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class Login extends AppCompatActivity{

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Button login;
    private EditText adminEdit;
    private EditText passwordEdit;
    private CheckBox savePassword;
    private CheckBox showPassword;

    // add this
    private UserDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        pref= PreferenceManager.getDefaultSharedPreferences(this);
        login=findViewById(R.id.login_button);
        adminEdit=findViewById(R.id.admin);
        passwordEdit=findViewById(R.id.password);
        savePassword=findViewById(R.id.save_password);
        showPassword=findViewById(R.id.show_password);

        // initialize dbHelper
        dbHelper = new UserDBHelper(this, "User.db", null, 1);

        boolean isSave=pref.getBoolean("save_password",false);
        if(isSave){
            String account=pref.getString("account","");
            String password=pref.getString("password","");
            adminEdit.setText(account);
            passwordEdit.setText(password);
            adminEdit.setSelection(adminEdit.getText().length());
            passwordEdit.setSelection(passwordEdit.getText().length());
            savePassword.setChecked(true);
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account=adminEdit.getText().toString();
                String password=passwordEdit.getText().toString();

                // check if user already exists in database
                List<Map<String, String>> users = dbHelper.getAll();
                for(Map<String, String> user : users){
                    if(user.get("username").equals(account)){
                        // user exists, check password
                        String salt = user.get("salt"); // get salt from database
                        String securePassword = get_SHA_256_SecurePassword(password, salt); // use salt from database
                        if(user.get("hashedPassword").equals(securePassword)){
                            // password correct
                            Toast.makeText(Login.this,"登录成功",Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(Login.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            return;
                        }else{
                            // password incorrect
                            Toast.makeText(Login.this,"登录失败,请重新输入！",Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }

                // user does not exist, register new user
                String salt = generateSalt();
                String securePassword = get_SHA_256_SecurePassword(password, salt);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("username", account);
                values.put("salt", salt);
                values.put("hashedPassword", securePassword);
                db.insert("Users", null, values);
                Toast.makeText(Login.this,"账户创建成功,已自动登录",Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(Login.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


        showPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(showPassword.isChecked()){
                    showOrHide(passwordEdit,true);
                }else{
                    showOrHide(passwordEdit,false);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        editor=pref.edit();
        String account=adminEdit.getText().toString();
        String password=passwordEdit.getText().toString();
        if(savePassword.isChecked()){
            editor.putBoolean("save_password",true);
            editor.putString("account",account);
            editor.putString("password",password);
        }else{
            editor.clear();
        }
        editor.apply();
    }

    private void showOrHide(EditText passwordEdit,boolean isShow){
        int pos = passwordEdit.getSelectionStart();
        if(isShow){
            passwordEdit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }else{
            passwordEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
        passwordEdit.setSelection(pos);
    }

    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[20];
        random.nextBytes(bytes);
        return bytes.toString();
    }

    public static String get_SHA_256_SecurePassword(String passwordToHash, String salt){
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] bytes = md.digest(passwordToHash.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++){
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }

}
