
package com.example.ecomer

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class EditProductActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_product)

        val edtTen = findViewById<EditText>(R.id.edtTenSanPham)
        val edtLoai = findViewById<EditText>(R.id.edtLoaiSanPham)
        val edtGia = findViewById<EditText>(R.id.edtGia)
        val btnCapNhat =
            findViewById<Button>(R.id.btnCapNhatSanPham)

        val productId = intent.getStringExtra("productId")

        if (productId.isNullOrBlank()) {
            Toast.makeText(
                this,
                "Không tìm thấy mã sản phẩm",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        edtTen.setText(intent.getStringExtra("tenSanPham") ?: "")
        edtLoai.setText(intent.getStringExtra("loaiSanPham") ?: "")

        val giaBanDau = intent.getDoubleExtra("gia", 0.0)
        edtGia.setText(
            if (giaBanDau % 1.0 == 0.0) {
                giaBanDau.toLong().toString()
            } else {
                giaBanDau.toString()
            }
        )

        btnCapNhat.setOnClickListener {
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

            val data = mapOf(
                "tenSanPham" to ten,
                "loaiSanPham" to loai,
                "gia" to gia
            )

            btnCapNhat.isEnabled = false

            db.collection("products")
                .document(productId)
                .update(data)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Cập nhật thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                .addOnFailureListener { error ->
                    btnCapNhat.isEnabled = true
                    Toast.makeText(
                        this,
                        "Lỗi: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }
}
