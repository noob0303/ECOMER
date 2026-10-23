
package com.example.ecomer

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AddProductActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        val edtTen = findViewById<EditText>(R.id.edtTenSanPham)
        val edtLoai = findViewById<EditText>(R.id.edtLoaiSanPham)
        val edtGia = findViewById<EditText>(R.id.edtGia)
        val btnLuu = findViewById<Button>(R.id.btnLuuSanPham)

        btnLuu.setOnClickListener {
            val ten = edtTen.text.toString().trim()
            val loai = edtLoai.text.toString().trim()
            val giaText = edtGia.text.toString().trim()

            if (ten.isEmpty() || loai.isEmpty() || giaText.isEmpty()) {
                Toast.makeText(
                    this,
                    "Vui lòng nhập đầy đủ thông tin",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val gia = giaText.toDoubleOrNull()

            if (gia == null || !gia.isFinite() || gia < 0) {
                Toast.makeText(
                    this,
                    "Giá sản phẩm không hợp lệ",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val product = hashMapOf(
                "tenSanPham" to ten,
                "loaiSanPham" to loai,
                "gia" to gia
            )

            btnLuu.isEnabled = false

            db.collection("products")
                .add(product)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Thêm sản phẩm thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                .addOnFailureListener { error ->
                    btnLuu.isEnabled = true
                    Toast.makeText(
                        this,
                        "Lỗi: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }
}
