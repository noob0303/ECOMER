
package com.example.ecomer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val products = mutableListOf<Product>()
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rvSanPham = findViewById<RecyclerView>(R.id.rvSanPham)
        val btnThem = findViewById<Button>(R.id.btnThemSanPham)

        adapter = ProductAdapter(
            products,
            onEdit = { product ->
                val intent = Intent(this, EditProductActivity::class.java)
                intent.putExtra("productId", product.id)
                intent.putExtra("tenSanPham", product.tenSanPham)
                intent.putExtra("loaiSanPham", product.loaiSanPham)
                intent.putExtra("gia", product.gia)
                startActivity(intent)
            },
            onDelete = { product ->
                xacNhanXoa(product)
            }
        )

        rvSanPham.layoutManager = LinearLayoutManager(this)
        rvSanPham.adapter = adapter

        btnThem.setOnClickListener {
            startActivity(
                Intent(this, AddProductActivity::class.java)
            )
        }

        langNgheSanPham()
    }

    override fun onResume() {
        super.onResume()
        taiSanPham()
    }

    private fun langNgheSanPham() {
        db.collection("products")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(
                        this,
                        "Lỗi tải sản phẩm: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addSnapshotListener
                }

                val danhSachMoi = snapshot?.documents?.map { doc ->
                    Product(
                        id = doc.id,
                        tenSanPham = doc.getString("tenSanPham") ?: "",
                        loaiSanPham = doc.getString("loaiSanPham") ?: "",
                        gia = doc.getDouble("gia")
                            ?: doc.getLong("gia")?.toDouble()
                            ?: 0.0
                    )
                } ?: emptyList()

                adapter.updateData(danhSachMoi)
            }
    }

    private fun taiSanPham() {
        // Danh sách được cập nhật tự động bằng SnapshotListener.
    }

    private fun xacNhanXoa(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Xóa sản phẩm")
            .setMessage(
                "Bạn có chắc muốn xóa ${product.tenSanPham} không?"
            )
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Xóa") { _, _ ->
                db.collection("products")
                    .document(product.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Đã xóa sản phẩm",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(
                            this,
                            "Xóa thất bại: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .show()
    }
}
