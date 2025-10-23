package com.example.quanlyhieuthuoc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class trangchu extends AppCompatActivity {

    private Button btnThuoc, btnNhanVien, btnNhaCungCap, btnThongTinKhac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trangchu);

        // Khởi tạo các nút
        btnThuoc = findViewById(R.id.btnThuoc);
        btnNhanVien = findViewById(R.id.btnNhanVien);
        btnNhaCungCap = findViewById(R.id.btnNhaCungCap);
        btnThongTinKhac = findViewById(R.id.btnThongTinKhac);

        // Thiết lập sự kiện cho các nút
        btnThuoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Mở Activity Thuốc
                startActivity(new Intent(trangchu.this, MainActivity.class));
            }
        });

        btnNhanVien.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Mở Activity Nhân viên
                startActivity(new Intent(trangchu.this, nhanvien.class));
            }
        });

        btnNhaCungCap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Mở Activity Nhà cung cấp
                startActivity(new Intent(trangchu.this, nhacungcap.class));
            }
        });

        btnThongTinKhac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Mở Activity Thông tin khác
                startActivity(new Intent(trangchu.this, thongtinkhac.class));
            }
        });
    }
}
