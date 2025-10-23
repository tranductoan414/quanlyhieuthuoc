package com.example.quanlyhieuthuoc;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class themnv extends AppCompatActivity {

    private EditText edt_tennv, edt_dchi, edt_sdt, edt_email, edt_ngaysinh;
    private Button btn_themnv;
    SQLiteDatabase mydatabase;
    String DB_PATH_SUFFIX = "/databases/";
    String DATABASE_NAME = "hieuthuoc.db";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.themnv);

        // Khởi tạo các thành phần UI
        edt_tennv = findViewById(R.id.edt_tennv);
        edt_dchi = findViewById(R.id.edt_dchi);
        edt_sdt = findViewById(R.id.edt_sdt);
        edt_email = findViewById(R.id.edt_email);
        edt_ngaysinh = findViewById(R.id.edt_ngaysinh);
        btn_themnv = findViewById(R.id.btn_themnv);

        // Mở cơ sở dữ liệu
        mydatabase = openOrCreateDatabase("hieuthuoc.db", MODE_PRIVATE, null);

        // Lắng nghe sự kiện nhấn nút lưu
        btn_themnv.setOnClickListener(view -> {
            String employeeName = edt_tennv.getText().toString();
            if (!employeeName.isEmpty()) {
                String ten = edt_tennv.getText().toString();
                String dchi = edt_dchi.getText().toString();
                String sdt = edt_sdt.getText().toString();
                String email = edt_email.getText().toString();
                String ngaysinh = edt_ngaysinh.getText().toString();

                // Kiểm tra dữ liệu hợp lệ
                if (ten.isEmpty() || dchi.isEmpty() || sdt.isEmpty() || email.isEmpty() || ngaysinh.isEmpty()) {
                    Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Câu lệnh SQL INSERT
                String sql = "INSERT INTO nhanvien (ten_nv, dia_chi, sdt, email, ngay_sinh) VALUES (?, ?, ?, ?, ?)";
                try {
                    mydatabase.execSQL(sql, new Object[]{ten, dchi, sdt, email, ngaysinh});
                    Toast.makeText(this, "Đã thêm nhân viên: " + employeeName, Toast.LENGTH_SHORT).show();
                    finish();
                    // Đóng Activity và quay lại Activity trước đó
                } catch (Exception e) {
                    Toast.makeText(this, "Lỗi khi thêm nhân viên: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Vui lòng nhập tên nhân viên", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
