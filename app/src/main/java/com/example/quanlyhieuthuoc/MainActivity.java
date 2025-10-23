package com.example.quanlyhieuthuoc;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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

public class MainActivity extends AppCompatActivity {
    EditText edttenthuoc, edtnhacungcap, edtngaysanxuat, edthansudung, edtsoluong, edtId; // Thêm EditText cho ID
    Button btnthem, btnxoa, btnsua;
    ListView lv;
    ArrayList<String> myList;
    ArrayList<Integer> idList; // Danh sách chứa các ID
    ArrayAdapter<String> myAdapter;

    SQLiteDatabase mydatabase;
    String DB_PATH_SUFFIX = "/databases/";
    String DATABASE_NAME = "qlht.db";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Đảm bảo bạn đã thiết lập layout ở đây

        // Khởi tạo các view
        edttenthuoc = findViewById(R.id.edttenthuoc);
        edtnhacungcap = findViewById(R.id.edtnhacungcap);
        edtngaysanxuat = findViewById(R.id.edtngaysanxuat);
        edthansudung = findViewById(R.id.edthansudung);
        edtsoluong = findViewById(R.id.edtsoluong);
        edtId = findViewById(R.id.edtId); // Khai báo EditText cho ID

        btnthem = findViewById(R.id.btnthem);
        btnxoa = findViewById(R.id.btnxoa);
        btnsua = findViewById(R.id.btnsua); // Nút "Sửa"
        lv = findViewById(R.id.lv);

        myList = new ArrayList<>();
        idList = new ArrayList<>(); // Khởi tạo danh sách ID
        myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, myList);
        lv.setAdapter(myAdapter);

        processCopy(); // Sao chép cơ sở dữ liệu từ assets
        loadDataFromDatabase(); // Tải dữ liệu từ cơ sở dữ liệu

        // Thiết lập sự kiện cho nút "Thêm"
        btnthem.setOnClickListener(v -> addDataToDatabase());

        // Thiết lập sự kiện cho nút "Xóa"
        btnxoa.setOnClickListener(v -> deleteDataFromDatabase());

        // Thiết lập sự kiện cho nút "Sửa"
        btnsua.setOnClickListener(v -> updateDataInDatabase());

        // Thiết lập sự kiện cho ListView
        lv.setOnItemClickListener((parent, view, position, id) -> {
            // Lấy ID từ danh sách và thiết lập vào EditText (để sửa)
            int selectedId = idList.get(position);
            edtId.setText(String.valueOf(selectedId)); // Hiển thị ID vào EditText

            // Cập nhật tag của nút xóa với ID đã chọn
            btnxoa.setTag(selectedId); // Set selected ID as the tag of the delete button

            // Hiển thị tất cả thông tin vào các EditText
            String selectedData = myList.get(position);
            String[] dataParts = selectedData.split(" - ");
            if (dataParts.length >= 6) {
                edttenthuoc.setText(dataParts[1].split(":")[1].trim());
                edtnhacungcap.setText(dataParts[2].split(":")[1].trim());
                edtngaysanxuat.setText(dataParts[3].split(":")[1].trim());
                edthansudung.setText(dataParts[4].split(":")[1].trim());
                edtsoluong.setText(dataParts[5].split(":")[1].trim());
            }

            // Thông báo ID được chọn
            Toast.makeText(MainActivity.this, "ID được chọn: " + selectedId, Toast.LENGTH_SHORT).show();
        });
    }

    // Hàm để thêm dữ liệu vào cơ sở dữ liệu
    private void addDataToDatabase() {
        String tenthuoc = edttenthuoc.getText().toString();
        String nhacungcap = edtnhacungcap.getText().toString();
        String ngaysanxuat = edtngaysanxuat.getText().toString();
        String hansudung = edthansudung.getText().toString();
        String soluongStr = edtsoluong.getText().toString();

        // Kiểm tra dữ liệu hợp lệ
        if (tenthuoc.isEmpty() || nhacungcap.isEmpty() || ngaysanxuat.isEmpty() || hansudung.isEmpty() || soluongStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        int soluong = Integer.parseInt(soluongStr);

        // Thêm dữ liệu vào bảng
        String sql = "INSERT INTO thuoc (tenthuoc, manhacungcap, ngaysanxuat, hansudung, soluong) VALUES (?, ?, ?, ?, ?)";
        mydatabase.execSQL(sql, new Object[]{tenthuoc, nhacungcap, ngaysanxuat, hansudung, soluong});
        Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show();

        // Tải lại dữ liệu vào ListView
        loadDataFromDatabase();
        clearFields(); // Xóa các trường nhập
    }

    // Hàm để xóa dữ liệu từ cơ sở dữ liệu
    private void deleteDataFromDatabase() {
        // Lấy ID đã lưu trong nút xóa (lấy từ Tag của nút)
        Integer selectedId = (Integer) btnxoa.getTag();

        if (selectedId == null || selectedId == 0) {
            Toast.makeText(this, "Vui lòng chọn một bản ghi để xóa", Toast.LENGTH_SHORT).show();
            return;
        }

        // Thực hiện xóa từ cơ sở dữ liệu
        int deletedRows = mydatabase.delete("thuoc", "id = ?", new String[]{String.valueOf(selectedId)});

        // Thông báo và cập nhật lại ListView
        if (deletedRows > 0) {
            Toast.makeText(this, "Xóa thành công", Toast.LENGTH_SHORT).show();
            loadDataFromDatabase(); // Tải lại dữ liệu sau khi xóa
            clearFields(); // Xóa các trường sau khi xóa thành công
            btnxoa.setTag(0); // Đặt lại ID trong nút xóa
            edtId.setText(""); // Xóa ID khỏi EditText
        } else {
            Toast.makeText(this, "Không tìm thấy bản ghi để xóa", Toast.LENGTH_SHORT).show();
        }
    }

    // Hàm để cập nhật dữ liệu trong cơ sở dữ liệu
    private void updateDataInDatabase() {
        int selectedId = Integer.parseInt(edtId.getText().toString());

        // Kiểm tra ID hợp lệ
        if (selectedId == 0) {
            Toast.makeText(this, "Vui lòng chọn bản ghi cần sửa", Toast.LENGTH_SHORT).show();
            return;
        }

        String tenthuoc = edttenthuoc.getText().toString();
        String nhacungcap = edtnhacungcap.getText().toString();
        String ngaysanxuat = edtngaysanxuat.getText().toString();
        String hansudung = edthansudung.getText().toString();
        String soluongStr = edtsoluong.getText().toString();

        // Kiểm tra dữ liệu hợp lệ
        if (tenthuoc.isEmpty() || nhacungcap.isEmpty() || ngaysanxuat.isEmpty() || hansudung.isEmpty() || soluongStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        int soluong = Integer.parseInt(soluongStr);

        // Cập nhật dữ liệu trong cơ sở dữ liệu
        String sql = "UPDATE thuoc SET tenthuoc = ?, manhacungcap = ?, ngaysanxuat = ?, hansudung = ?, soluong = ? WHERE id = ?";
        mydatabase.execSQL(sql, new Object[]{tenthuoc, nhacungcap, ngaysanxuat, hansudung, soluong, selectedId});
        Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();

        // Tải lại dữ liệu vào ListView
        loadDataFromDatabase();
        clearFields(); // Xóa các trường nhập
    }

    // Hàm để tải dữ liệu từ cơ sở dữ liệu vào ListView
    private void loadDataFromDatabase() {
        myList.clear(); // Xóa danh sách hiện tại
        idList.clear(); // Xóa danh sách ID hiện tại
        Cursor c = mydatabase.query("thuoc", null, null, null, null, null, null);
        if (c.moveToFirst()) {
            do {
                int id = c.getInt(c.getColumnIndex("id"));
                String tenthuoc = c.getString(c.getColumnIndex("tenthuoc"));
                String manhacungcap = c.getString(c.getColumnIndex("manhacungcap"));
                String ngaysanxuat = c.getString(c.getColumnIndex("ngaysanxuat"));
                String hansudung = c.getString(c.getColumnIndex("hansudung"));
                int soluong = c.getInt(c.getColumnIndex("soluong"));

                myList.add("ID: " + id + " - Tên thuốc: " + tenthuoc + " - Nhà cung cấp: " + manhacungcap +
                        " - Ngày sản xuất: " + ngaysanxuat + " - Hạn sử dụng: " + hansudung + " - Số lượng: " + soluong);
                idList.add(id); // Lưu ID vào danh sách
            } while (c.moveToNext());
        }
        c.close();

        myAdapter.notifyDataSetChanged(); // Cập nhật ListView
    }

    // Hàm để sao chép cơ sở dữ liệu từ assets vào bộ nhớ trong của thiết bị
    private void processCopy() {
        File dbFile = getApplicationContext().getDatabasePath(DATABASE_NAME);
        if (!dbFile.exists()) {
            try {
                InputStream is = getAssets().open(DATABASE_NAME);
                OutputStream os = new FileOutputStream(dbFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                os.flush();
                os.close();
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mydatabase = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
    }

    // Hàm để xóa tất cả các trường nhập
    private void clearFields() {
        edttenthuoc.setText("");
        edtnhacungcap.setText("");
        edtngaysanxuat.setText("");
        edthansudung.setText("");
        edtsoluong.setText("");
        edtId.setText("");
    }
}
