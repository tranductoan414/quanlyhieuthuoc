package com.example.quanlyhieuthuoc;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class nhacungcap extends AppCompatActivity {
    EditText edtid, edttenncc, edtdiachi, edthopdong, edtsdt, edtemail;
    Button btnthem, btnsua, btnxoa, btntim;

    ListView lv;
    ArrayList<String> myList;
    ArrayList<Integer> idList;
    ArrayAdapter<String> myAdapter;

    SQLiteDatabase mydatabase;
    String DB_PATH_SUFFIX = "/databases/";
    String DATABASE_NAME = "qlht.db";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nhacungcap);

        edtid = findViewById(R.id.edtid);
        edttenncc = findViewById(R.id.edttenncc);
        edtdiachi = findViewById(R.id.edtdiachi);
        edthopdong = findViewById(R.id.edthopdong);
        edtsdt = findViewById(R.id.edtsdt);
        edtemail = findViewById(R.id.edtemail);
        btnthem = findViewById(R.id.btnthem);
        btnsua = findViewById(R.id.btnsua);
        btnxoa = findViewById(R.id.btnxoa);
        btntim = findViewById(R.id.btntim);


        lv = findViewById(R.id.lv);
        myList = new ArrayList<>();
        idList = new ArrayList<>();
        myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, myList);
        lv.setAdapter(myAdapter);

        processCopy();
        loadDataFromDatabase();


        btnthem.setOnClickListener(view -> them());
        btnsua.setOnClickListener(view -> sua());
        btnxoa.setOnClickListener(view -> xoa());
        btntim.setOnClickListener(view -> searchSupplierByName());


        lv.setOnItemClickListener((parent, view, position, id) -> {

            int selectedId = idList.get(position);
            edtid.setText(String.valueOf(selectedId));
            btnxoa.setTag(selectedId);

            String selectedData = myList.get(position);
            String[] dataParts = selectedData.split(" - ");
            if (dataParts.length >= 6) {
                edttenncc.setText(dataParts[1].split(":")[1].trim());
                edtdiachi.setText(dataParts[2].split(":")[1].trim());
                edthopdong.setText(dataParts[3].split(":")[1].trim());
                edtsdt.setText(dataParts[4].split(":")[1].trim());
                edtemail.setText(dataParts[5].split(":")[1].trim());
            }


            Toast.makeText(nhacungcap.this, "ID được chọn: " + selectedId, Toast.LENGTH_SHORT).show();
        });

    }

    private void processCopy() {
        File dbFile = getDatabasePath(DATABASE_NAME);
        if (!dbFile.exists()) {
            try {
                CopyDataBaseFromAsset();
                Toast.makeText(this, "Sao chép thành công từ thư mục Assets", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            }
        }
        mydatabase = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
    }
    // Lấy đường dẫn tới database trong thư mục cài đặt ứng dụng
    private String getDatabasePath() {
        return getApplicationInfo().dataDir + DB_PATH_SUFFIX + DATABASE_NAME;
    }

    // Hàm sao chép database từ thư mục assets
    public void CopyDataBaseFromAsset() {
        try {
            InputStream myInput = getAssets().open(DATABASE_NAME);
            String outFileName = getDatabasePath();
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

    // Hàm tải dữ liệu từ cơ sở dữ liệu vào ListView
    private void loadDataFromDatabase() {
        myList.clear();
        idList.clear();
        Cursor c = mydatabase.query("nhacungcap", null, null, null, null, null, null);
        if (c.moveToFirst()) {
            do {
                // Hiển thị tất cả thông tin trong ListView
                String data = "id: " + c.getInt(0) +
                        " - Ten nha cung cap: " + c.getString(1) +
                        " - Dia chi: " + c.getString(2) +
                        " - Hop dong: " + c.getString(3) +
                        " - Sdt: " + c.getString(4) +
                        " - Email: " + c.getString(5);
                myList.add(data);
                idList.add(c.getInt(0)); // Lưu ID vào danh sách
            } while (c.moveToNext());
        }
        c.close();
        myAdapter.notifyDataSetChanged(); // Cập nhật ListView
    }

    //btnthem
    private void them() {
        String tenncc = edttenncc.getText().toString();
        String diachi = edtdiachi.getText().toString();
        String hopdong = edthopdong.getText().toString();
        String sdt = edtsdt.getText().toString();
        String email = edtemail.getText().toString();

        if (tenncc.isEmpty() || diachi.isEmpty() || hopdong.isEmpty() || sdt.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Vui long dien day du ten nha cung cap", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String sql = "INSERT INTO nhacungcap (ten_ncc, dia_chi, hopdong, sdt, email) VALUES (?, ?, ?, ?, ?)";
            mydatabase.execSQL(sql, new Object[]{tenncc, diachi, hopdong, sdt, email});
            Toast.makeText(this, "Them nha cung cap thanh cong", Toast.LENGTH_SHORT).show();
            loadDataFromDatabase();
            xoadulieu();
        } catch (Exception e) {
            Toast.makeText(this, "Loi khi them du lieu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    //btnsua

    private void sua() {
        int selectedId;
        try {
            selectedId = Integer.parseInt(edtid.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Hay chon nha cung cap", Toast.LENGTH_SHORT).show();
            return;
        }

        String tenncc = edttenncc.getText().toString();
        String diachi = edtdiachi.getText().toString();
        String hopdong = edthopdong.getText().toString();
        String sdt = edtsdt.getText().toString();
        String email = edtemail.getText().toString();

        if (tenncc.isEmpty() || diachi.isEmpty() || hopdong.isEmpty() || sdt.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Hay dien day du thong tin nha cung cap", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String sql = "UPDATE nhacungcap SET ten_ncc = ?, dia_chi = ?, hopdong = ?, sdt = ?, email = ? WHERE id = ?";
            mydatabase.execSQL(sql, new Object[]{tenncc, diachi, hopdong, sdt, email, selectedId});
            Toast.makeText(this, "Cap nhap thong tin thanh cong", Toast.LENGTH_SHORT).show();
            loadDataFromDatabase();
            xoadulieu();
        } catch (Exception e) {
            Toast.makeText(this, "Loi khi cap nhap du lieu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    private void xoa() {
        Integer selectedId = (Integer) btnxoa.getTag();
        if (selectedId == null || selectedId == 0) {
            Toast.makeText(this, "Chon nha cung cap de xoa", Toast.LENGTH_SHORT).show();
            return;
        }

        int deletedRows = mydatabase.delete("nhacungcap", "id = ?", new String[]{String.valueOf(selectedId)});

        if (deletedRows > 0) {
            Toast.makeText(this, "Xoa thanh cong", Toast.LENGTH_SHORT).show();
            loadDataFromDatabase();
            btnxoa.setTag(0);
            xoadulieu();

        } else {
            Toast.makeText(this, "Loi", Toast.LENGTH_SHORT).show();
        }
    }

    private void searchSupplierByName() {
        String searchTerm = edttenncc.getText().toString().trim(); // Lấy giá trị nhập vào trong trường tên nhà cung cấp

        // Kiểm tra nếu trường tìm kiếm trống
        if (searchTerm.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên nhà cung cấp để tìm kiếm", Toast.LENGTH_SHORT).show();
            return; // Nếu không có tên, dừng việc tìm kiếm
        }

        // Xóa danh sách hiện tại trước khi hiển thị kết quả mới
        myList.clear();
        idList.clear();

        // Thực hiện câu truy vấn SQL với từ khóa tìm kiếm, sử dụng LIKE để tìm tên nhà cung cấp gần giống
        String query = "SELECT * FROM nhacungcap WHERE ten_ncc LIKE ?";
        Cursor c = mydatabase.rawQuery(query, new String[]{"%" + searchTerm + "%"});

        // Kiểm tra xem có dữ liệu trả về không
        if (c != null && c.moveToFirst()) {
            do {
                // Tạo chuỗi thông tin cho mỗi nhà cung cấp
                String data = "id: " + c.getInt(0) +
                        " - Ten nha cung cap: " + c.getString(1) +
                        " - Dia chi: " + c.getString(2) +
                        " - Hop dong: " + c.getString(3) +
                        " - Sdt: " + c.getString(4) +
                        " - Email: " + c.getString(5);
                myList.add(data); // Thêm thông tin vào danh sách hiển thị
                idList.add(c.getInt(0)); // Lưu ID nhà cung cấp để sử dụng sau
            } while (c.moveToNext());
        } else {
            Toast.makeText(this, "Không tìm thấy nhà cung cấp nào", Toast.LENGTH_SHORT).show();
        }

        // Đóng con trỏ (cursor) và cập nhật lại ListView
        c.close();
        myAdapter.notifyDataSetChanged(); // Cập nhật giao diện ListView
    }

     private void xoadulieu(){
        edttenncc.setText("");
        edtdiachi.setText("");
        edthopdong.setText("");
        edtsdt.setText("");
        edtemail.setText("");
        edtid.setText("");
    }
}