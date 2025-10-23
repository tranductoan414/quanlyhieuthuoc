package com.example.quanlyhieuthuoc;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class nhanvien extends AppCompatActivity {

    // Khai báo các biến cho các thành phần UI

    EditText edt_ten, edt_diachi,edt_sdt,edt_email, edt_ngaysinh,edt_tknv,edt_ma_nv;
    Button btn_themnv,btn_sua,btn_xoa,btn_timkiem;
    ListView lv_nv;
    ArrayList<String> myList;
    ArrayList<Integer> idList; // Danh sách chứa các ID
    ArrayAdapter<String> myAdapter;
    SQLiteDatabase mydatabase;
    String DB_PATH_SUFFIX = "/databases/";
    String DATABASE_NAME = "qlht.db";

    @SuppressLint("MissingInflatedId")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the layout for the Activity
        setContentView(R.layout.nhanvien);

        // Initialize UI components
        edt_ma_nv = findViewById(R.id.edt_ma_nv);  // Ensure edt_ma_nv is declared in your class


        edt_ten = findViewById(R.id.edt_ten);
        edt_diachi = findViewById(R.id.edt_diachi);
        edt_sdt = findViewById(R.id.edt_sdt);
        edt_email = findViewById(R.id.edt_email);
        edt_ngaysinh = findViewById(R.id.edt_ngaysinh);
        edt_tknv = findViewById(R.id.edt_tknv);

        btn_timkiem = findViewById(R.id.btn_timkiem);
        btn_xoa = findViewById(R.id.btn_xoa);
        lv_nv = findViewById(R.id.lv_nv);
        btn_sua = findViewById(R.id.btn_sua);
        btn_themnv = findViewById(R.id.btn_themnv);

        // Initialize lists and adapter
        myList = new ArrayList<>();
        idList = new ArrayList<>();
        myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, myList);
        lv_nv.setAdapter(myAdapter);

        // Copy the database and load data
        processCopy();
        loadDataFromDatabase();

        // Event listener for adding employee
        btn_themnv.setOnClickListener(view -> addDataToDatabase());

        // Event listener for updating employee
        btn_sua.setOnClickListener(view -> sua_nv());

        // Event listener for searching employee
        btn_timkiem.setOnClickListener(view -> {
            String keyword = edt_tknv.getText().toString();
            searchEmployee(keyword);
        });

        // Event listener for deleting employee
        btn_xoa.setOnClickListener(view -> deleteDataFromDatabase());

        // Event listener for item selection in ListView
        lv_nv.setOnItemClickListener((parent, view, position, id) -> {
            // Get selected ID and display it in edt_ma_nv (non-editable)
            int selectedId = idList.get(position);
            edt_ma_nv.setText(String.valueOf(selectedId));
            btn_xoa.setTag(selectedId);
            // Display all other information in respective EditText fields
            String selectedData = myList.get(position);
            String[] dataParts = selectedData.split(" - ");
            if (dataParts.length >= 6) {
                edt_ten.setText(dataParts[1].split(":")[1].trim());
                edt_diachi.setText(dataParts[2].split(":")[1].trim());
                edt_sdt.setText(dataParts[3].split(":")[1].trim());
                edt_email.setText(dataParts[4].split(":")[1].trim());
                edt_ngaysinh.setText(dataParts[5].split(":")[1].trim());
            }

            // Show a toast with the selected ID
            Toast.makeText(nhanvien.this, "ID được chọn: " + selectedId, Toast.LENGTH_SHORT).show();
        });
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

    // Hàm tải dữ liệu từ cơ sở dữ liệu vào ListView
    private void loadDataFromDatabase() {
        myList.clear(); // Xóa danh sách hiện tại
        idList.clear(); // Xóa danh sách ID hiện tại
        Cursor c = mydatabase.query("nhanvien", null, null, null, null, null, null);
        if (c.moveToFirst()) {
            do {
                // Hiển thị tất cả thông tin trong ListView
                String data = "MA_NV: " + c.getInt(0) +
                        " - Tên nhân vien: " + c.getString(1) +
                        " - Đia Chỉ: " + c.getString(2) +
                        " - So DT: " + c.getString(3) +
                        " - Email: " + c.getString(4) +
                        " - Ngay sinh: " + c.getInt(5);
                myList.add(data);
                idList.add(c.getInt(0)); // Lưu ID vào danh sách
            } while (c.moveToNext());
        }
        c.close();
        myAdapter.notifyDataSetChanged(); // Cập nhật ListView
    }

    // Hàm tìm kiếm nhân viên theo tên
    private void searchEmployee(String keyword) {
        myList.clear(); // Xóa danh sách hiện tại
        Cursor c = mydatabase.rawQuery("SELECT * FROM nhanvien WHERE ten_nv LIKE ?", new String[]{"%" + keyword + "%"});

        c.close();
        myAdapter.notifyDataSetChanged(); // Cập nhật ListView
    }
    private void addDataToDatabase() {
        String tennv = edt_ten.getText().toString();
        String diachi = edt_diachi.getText().toString();
        String sdt = edt_sdt.getText().toString();
        String email = edt_email.getText().toString();
        String ngaysinh = edt_ngaysinh.getText().toString();

        if (tennv.isEmpty() || diachi.isEmpty() || sdt.isEmpty() || email.isEmpty() || ngaysinh.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin nhân viên", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String sql = "INSERT INTO nhanvien (ten_nv, dia_chi, sdt, email, ngay_sinh) VALUES (?, ?, ?, ?, ?)";
            mydatabase.execSQL(sql, new Object[]{tennv, diachi, sdt, email, ngaysinh});
            Toast.makeText(this, "Thêm nhân viên thành công", Toast.LENGTH_SHORT).show();
            loadDataFromDatabase();
            xoadulieu();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi thêm dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void sua_nv() {
        int selectedId;
        try {
            selectedId = Integer.parseInt(edt_ma_nv.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng chọn nhân viên hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        String tennv = edt_ten.getText().toString();
        String diachi = edt_diachi.getText().toString();
        String sdt = edt_sdt.getText().toString();
        String email = edt_email.getText().toString();
        String ngaysinh = edt_ngaysinh.getText().toString();

        if (tennv.isEmpty() || diachi.isEmpty() || sdt.isEmpty() || email.isEmpty() || ngaysinh.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin nhân viên", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String sql = "UPDATE nhanvien SET ten_nv = ?, dia_chi = ?, sdt = ?, email = ?, ngay_sinh = ? WHERE ma_nv = ?";
            mydatabase.execSQL(sql, new Object[]{tennv, diachi, sdt, email, ngaysinh, selectedId});
            Toast.makeText(this, "Cập nhật thông tin nhân viên thành công", Toast.LENGTH_SHORT).show();
            loadDataFromDatabase();
            xoadulieu();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi cập nhật dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // Hàm xóa dữ liệu từ cơ sở dữ liệu
    private void deleteDataFromDatabase() {
        // Lấy ID đã lưu trong nút xóa (lấy từ Tag của nút)
        Integer selectedId = (Integer) btn_xoa.getTag();

        if (selectedId == null || selectedId == 0) {
            Toast.makeText(this, "Vui lòng chọn một bản ghi để xóa", Toast.LENGTH_SHORT).show();
            return;
        }

        // Thực hiện xóa từ cơ sở dữ liệu
        int deletedRows = mydatabase.delete("nhanvien", "ma_nv = ?", new String[]{String.valueOf(selectedId)});

        // Thông báo và cập nhật lại ListView
        if (deletedRows > 0) {
            Toast.makeText(this, "Xóa thành công", Toast.LENGTH_SHORT).show();
            loadDataFromDatabase(); // Tải lại dữ liệu sau khi xóa
           // Xóa các trường sau khi xóa thành công
            btn_xoa.setTag(0); // Đặt lại ID trong nút xóa
            xoadulieu();

        } else {
            Toast.makeText(this, "Không tìm thấy bản ghi để xóa", Toast.LENGTH_SHORT).show();
        }
    }
    private void xoadulieu(){
        edt_ten.setText("");
        edt_diachi.setText("");
        edt_sdt.setText("");
        edt_email.setText("");
        edt_ngaysinh.setText("");
        edt_ma_nv.setText("");

    }
}
