package com.example.quanlyhieuthuoc;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class dangnhap extends AppCompatActivity {
    EditText edt_taikhoan, edt_matkhau;
    Button btn_dangnhap;
    SQLiteDatabase mydatabase;
    String DB_PATH_SUFFIX = "/databases/";
    String DATABASE_NAME = "qlht.db";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dangnhap);

        edt_taikhoan = findViewById(R.id.edt_taikhoan);
        edt_matkhau = findViewById(R.id.edt_matkhau);
        btn_dangnhap = findViewById(R.id.btn_dangnhap);


        processCopy();

        btn_dangnhap.setOnClickListener(view -> dangnhaptaikhoan());
    }

    private void dangnhaptaikhoan() {
        String taikhoan = edt_taikhoan.getText().toString();
        String matkhau = edt_matkhau.getText().toString();

        if (taikhoan.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền tài khoản", Toast.LENGTH_SHORT).show();
            edt_taikhoan.requestFocus();
            return;
        } else if (matkhau.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền mật khẩu", Toast.LENGTH_SHORT).show();
            edt_matkhau.requestFocus();
            return;
        }

        try {
            // Mở cơ sở dữ liệu
            mydatabase = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);

            // Truy vấn cơ sở dữ liệu để kiểm tra thông tin đăng nhập
            Cursor cursor = mydatabase.rawQuery("SELECT * FROM taikhoan WHERE taikhoan = ? AND matkhau = ?", new String[]{taikhoan, matkhau});

            if (cursor != null && cursor.moveToFirst()) {

                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(dangnhap.this, trangchu.class);
                startActivity(intent);
                finish();
            } else {

                Toast.makeText(this, "Tài khoản hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
            }

            cursor.close();
        } catch (Exception e) {
            Toast.makeText(this, "Đã xảy ra lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void processCopy() {
        // Kiểm tra xem cơ sở dữ liệu đã được sao chép chưa
        File dbFile = getDatabasePath(DATABASE_NAME);
        if (!dbFile.exists()) {
            try {
                // Nếu chưa có, sao chép cơ sở dữ liệu từ thư mục assets
                CopyDataBaseFromAsset();
                Toast.makeText(this, "Sao chép thành công từ thư mục Assets", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi sao chép cơ sở dữ liệu: " + e.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

    // Hàm sao chép cơ sở dữ liệu từ thư mục assets
    public void CopyDataBaseFromAsset() {
        try {
            InputStream myInput = getAssets().open(DATABASE_NAME);
            String outFileName = getDatabaseFilePath();
            File f = new File(getApplicationInfo().dataDir + DB_PATH_SUFFIX);
            if (!f.exists()) {
                f.mkdir();
            }
            OutputStream myOutput = new FileOutputStream(outFileName);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
            myOutput.flush();
            myOutput.close();
            myInput.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getDatabaseFilePath() {
        return getApplicationInfo().dataDir + DB_PATH_SUFFIX + DATABASE_NAME;
    }
}
