
package com.example.ecomer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private val products: MutableList<Product>,
    private val onEdit: (Product) -> Unit,
    private val onDelete: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {
        val tvTen: TextView = view.findViewById(R.id.tvTenSanPham)
        val tvLoai: TextView = view.findViewById(R.id.tvLoaiSanPham)
        val tvGia: TextView = view.findViewById(R.id.tvGia)
        val btnSua: Button = view.findViewById(R.id.btnSua)
        val btnXoa: Button = view.findViewById(R.id.btnXoa)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ProductViewHolder,
        position: Int
    ) {
        val product = products[position]

        holder.tvTen.text = product.tenSanPham
        holder.tvLoai.text = "Loại: ${product.loaiSanPham}"

        val formatter = NumberFormat.getNumberInstance(
            Locale("vi", "VN")
        )
        holder.tvGia.text = "Giá: ${formatter.format(product.gia)} đ"

        holder.btnSua.setOnClickListener {
            onEdit(product)
        }

        holder.btnXoa.setOnClickListener {
            onDelete(product)
        }
    }

    override fun getItemCount(): Int = products.size

    fun updateData(newProducts: List<Product>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }
}
