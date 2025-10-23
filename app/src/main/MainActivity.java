package com.example.myapplication;

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
    Button btnthem, btnxoa;
    ListView lv;
    ArrayList<String> myList;
    ArrayList<Integer> idList; // Danh sách chứa các ID
    ArrayAdapter<String> myAdapter;

    SQLiteDatabase mydatabase;
    String DB_PATH_SUFFIX = "/databases/";
    String DATABASE_NAME = "hieuthuoc.db";

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

        // Thiết lập sự kiện cho ListView
        lv.setOnItemClickListener((parent, view, position, id) -> {
            // Lấy ID từ danh sách và thiết lập vào EditText (để xóa)
            int selectedId = idList.get(position);
            edtId.setText(String.valueOf(selectedId)); // Hiển thị ID vào EditText
            // Không thiết lập tên thuốc ở đây
            Toast.makeText(MainActivity.this, "ID được chọn: " + selectedId, Toast.LENGTH_SHORT).show();
            btnxoa.setTag(selectedId); // Lưu ID vào nút xóa để xóa khi nhấn
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
        int selectedId = (Integer) btnxoa.getTag(); // Lấy ID đã lưu trong nút xóa

        if (selectedId == 0) {
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

    // Hàm để tải dữ liệu từ cơ sở dữ liệu vào ListView
    private void loadDataFromDatabase() {
        myList.clear(); // Xóa danh sách hiện tại
        idList.clear(); // Xóa danh sách ID hiện tại
        Cursor c = mydatabase.query("thuoc", null, null, null, null, null, null);
        if (c.moveToFirst()) {
            do {
                // Hiển thị tất cả thông tin trong ListView
                String data = "ID: " + c.getInt(0) +
                        " - Tên thuốc: " + c.getString(1) +
                        " - Nhà cung cấp: " + c.getString(2) +
                        " - Ngày sản xuất: " + c.getString(3) +
                        " - Hạn sử dụng: " + c.getString(4) +
                        " - Số lượng: " + c.getInt(5);
                myList.add(data);
                idList.add(c.getInt(0)); // Lưu ID vào danh sách
            } while (c.moveToNext());
        }
        c.close();
        myAdapter.notifyDataSetChanged(); // Cập nhật ListView
    }

    // Hàm sao chép cơ sở dữ liệu từ thư mục assets
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

    // Hàm để xóa các trường nhập sau khi thêm/xóa
    private void clearFields() {
        edttenthuoc.setText("");
        edtnhacungcap.setText("");
        edtngaysanxuat.setText("");
        edthansudung.setText("");
        edtsoluong.setText("");
        edtId.setText(""); // Xóa ID
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mydatabase != null) {
            mydatabase.close(); // Đóng cơ sở dữ liệu khi Activity bị hủy
        }
    }
}
