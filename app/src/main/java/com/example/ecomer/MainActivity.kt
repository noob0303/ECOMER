
package com.example.ecomer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                ProductScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen() {
    val db = remember { FirebaseFirestore.getInstance() }

    val products = remember {
        mutableStateListOf<Product>()
    }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    // Trạng thái hiển thị biểu mẫu
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    var productToDelete by remember {
        mutableStateOf<Product?>(null)
    }

    var isDeleting by remember {
        mutableStateOf(false)
    }

    var deleteError by remember {
        mutableStateOf("")
    }
    var selectedProduct by remember {
        mutableStateOf<Product?>(null)
    }
    // Dữ liệu nhập sản phẩm
    var tenSanPham by remember { mutableStateOf("") }
    var loaiSanPham by remember { mutableStateOf("") }
    var gia by remember { mutableStateOf("") }

    var formError by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    // Lấy dữ liệu từ Firestore
    DisposableEffect(Unit) {
        val registration: ListenerRegistration =
            db.collection("products")
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        errorMessage = error.localizedMessage
                            ?: "Không thể tải dữ liệu"
                        isLoading = false
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        products.clear()

                        for (document in snapshot.documents) {
                            products.add(
                                Product(
                                    id = document.id,
                                    tenSanPham = document.getString("tenSanPham") ?: "",
                                    loaiSanPham = document.getString("loaiSanPham") ?: "",
                                    gia = document.getLong("gia") ?: 0L
                                )
                            )
                        }
                    }

                    errorMessage = ""
                    isLoading = false
                }

        onDispose {
            registration.remove()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QUẢN LÝ SẢN PHẨM") }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            Button(
                onClick = {
                    tenSanPham = ""
                    loaiSanPham = ""
                    gia = ""
                    formError = ""
                    showAddDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("THÊM SẢN PHẨM")
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    CircularProgressIndicator()
                }

                errorMessage.isNotEmpty() -> {
                    Text(
                        text = "Lỗi: $errorMessage",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                products.isEmpty() -> {
                    Text("Chưa có sản phẩm nào.")
                }

                else -> {
                    LazyColumn {
                        items(
                            items = products,
                            key = { it.id }
                        ) { product ->

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = product.tenSanPham,
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Text("Loại: ${product.loaiSanPham}")
                                    Text("Giá: ${product.gia} VNĐ")
                                    Spacer(modifier = Modifier.height(12.dp))


                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                selectedProduct = product
                                                showEditDialog = true
                                                formError = ""
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("SỬA")
                                        }

                                        Button(
                                            onClick = {
                                                productToDelete = product
                                                deleteError = ""
                                                showDeleteDialog = true
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.error
                                            )
                                        ) {
                                            Text("XÓA")
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Biểu mẫu thêm sản phẩm
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isSaving) {
                    showAddDialog = false
                }
            },

            title = {
                Text("THÊM SẢN PHẨM")
            },

            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    OutlinedTextField(
                        value = tenSanPham,
                        onValueChange = { tenSanPham = it },
                        label = { Text("Tên sản phẩm") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = loaiSanPham,
                        onValueChange = { loaiSanPham = it },
                        label = { Text("Loại sản phẩm") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = gia,
                        onValueChange = { gia = it },
                        label = { Text("Giá sản phẩm (VNĐ)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true
                    )

                    if (formError.isNotEmpty()) {
                        Text(
                            text = formError,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },

            confirmButton = {
                Button(
                    enabled = !isSaving,
                    onClick = {

                        val ten = tenSanPham.trim()
                        val loai = loaiSanPham.trim()
                        val giaTien = gia.toLongOrNull()

                        when {
                            ten.isEmpty() -> {
                                formError = "Vui lòng nhập tên sản phẩm."
                            }

                            loai.isEmpty() -> {
                                formError = "Vui lòng nhập loại sản phẩm."
                            }

                            giaTien == null || giaTien <= 0L -> {
                                formError = "Giá phải là số nguyên lớn hơn 0."
                            }

                            else -> {
                                isSaving = true
                                formError = ""

                                val productData = hashMapOf(
                                    "tenSanPham" to ten,
                                    "loaiSanPham" to loai,
                                    "gia" to giaTien
                                )

                                db.collection("products")
                                    .add(productData)
                                    .addOnSuccessListener {
                                        isSaving = false
                                        showAddDialog = false
                                        tenSanPham = ""
                                        loaiSanPham = ""
                                        gia = ""
                                    }
                                    .addOnFailureListener { exception ->
                                        isSaving = false
                                        formError = exception.localizedMessage
                                            ?: "Không thể lưu sản phẩm."
                                    }
                            }
                        }
                    }
                ) {
                    Text(if (isSaving) "ĐANG LƯU..." else "LƯU")
                }
            },

            dismissButton = {
                TextButton(
                    enabled = !isSaving,
                    onClick = {
                        showAddDialog = false
                    }
                ) {
                    Text("HỦY")
                }
            }
        )
    }

// Biểu mẫu sửa sản phẩm
    if (showEditDialog && selectedProduct != null) {

        val product = selectedProduct!!

        var editTen by remember(product.id) {
            mutableStateOf(product.tenSanPham)
        }

        var editLoai by remember(product.id) {
            mutableStateOf(product.loaiSanPham)
        }

        var editGia by remember(product.id) {
            mutableStateOf(product.gia.toString())
        }

        var editError by remember(product.id) {
            mutableStateOf("")
        }

        var isUpdating by remember(product.id) {
            mutableStateOf(false)
        }

        AlertDialog(
            onDismissRequest = {
                if (!isUpdating) {
                    showEditDialog = false
                    selectedProduct = null
                }
            },

            title = {
                Text("SỬA SẢN PHẨM")
            },

            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = editTen,
                        onValueChange = { editTen = it },
                        label = { Text("Tên sản phẩm") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = editLoai,
                        onValueChange = { editLoai = it },
                        label = { Text("Loại sản phẩm") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = editGia,
                        onValueChange = { editGia = it },
                        label = { Text("Giá sản phẩm (VNĐ)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (editError.isNotEmpty()) {
                        Text(
                            text = editError,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },

            confirmButton = {
                Button(
                    enabled = !isUpdating,
                    onClick = {
                        val ten = editTen.trim()
                        val loai = editLoai.trim()
                        val giaTien = editGia.toLongOrNull()

                        when {
                            ten.isEmpty() -> {
                                editError = "Vui lòng nhập tên sản phẩm."
                            }

                            loai.isEmpty() -> {
                                editError = "Vui lòng nhập loại sản phẩm."
                            }

                            giaTien == null || giaTien <= 0L -> {
                                editError = "Giá phải lớn hơn 0."
                            }

                            else -> {
                                isUpdating = true
                                editError = ""

                                val updatedData = mapOf(
                                    "tenSanPham" to ten,
                                    "loaiSanPham" to loai,
                                    "gia" to giaTien
                                )

                                db.collection("products")
                                    .document(product.id)
                                    .update(updatedData)
                                    .addOnSuccessListener {
                                        isUpdating = false
                                        showEditDialog = false
                                        selectedProduct = null
                                    }
                                    .addOnFailureListener { exception ->
                                        isUpdating = false
                                        editError = exception.localizedMessage
                                            ?: "Không thể cập nhật sản phẩm."
                                    }
                            }
                        }
                    }
                ) {
                    Text(if (isUpdating) "ĐANG LƯU..." else "LƯU")
                }
            },

            dismissButton = {
                TextButton(
                    enabled = !isUpdating,
                    onClick = {
                        showEditDialog = false
                        selectedProduct = null
                    }
                ) {
                    Text("HỦY")
                }
            }
        )
    }

// Hộp thoại xác nhận xóa sản phẩm
    if (showDeleteDialog && productToDelete != null) {

        val product = productToDelete!!

        AlertDialog(
            onDismissRequest = {
                if (!isDeleting) {
                    showDeleteDialog = false
                    productToDelete = null
                }
            },

            title = {
                Text("XÁC NHẬN XÓA")
            },

            text = {
                Column {
                    Text(
                        "Bạn có chắc chắn muốn xóa sản phẩm này không?"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = product.tenSanPham,
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (deleteError.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = deleteError,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },

            confirmButton = {
                Button(
                    enabled = !isDeleting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    onClick = {
                        isDeleting = true
                        deleteError = ""

                        db.collection("products")
                            .document(product.id)
                            .delete()
                            .addOnSuccessListener {
                                isDeleting = false
                                showDeleteDialog = false
                                productToDelete = null
                            }
                            .addOnFailureListener { exception ->
                                isDeleting = false
                                deleteError = exception.localizedMessage
                                    ?: "Không thể xóa sản phẩm."
                            }
                    }
                ) {
                    Text(if (isDeleting) "ĐANG XÓA..." else "XÓA")
                }
            },

            dismissButton = {
                TextButton(
                    enabled = !isDeleting,
                    onClick = {
                        showDeleteDialog = false
                        productToDelete = null
                    }
                ) {
                    Text("HỦY")
                }
            }
        )
    }


}
