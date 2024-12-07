import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.hewan.MainActivity
import com.example.hewan.LoginActivity
import com.example.hewan.R
import java.io.File
import java.io.FileOutputStream

class RegisterActivity : AppCompatActivity() {

    private lateinit var inputName: EditText
    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnLogin: TextView
    private lateinit var btnUploadProfile: Button
    private lateinit var profileImageView: ImageView
    private var profileImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)

        inputName = findViewById(R.id.inputName)
        inputEmail = findViewById(R.id.inputEmail)
        inputPassword = findViewById(R.id.inputPassword)
        btnRegister = findViewById(R.id.btnRegister)
        btnLogin = findViewById(R.id.btnLogin)
        btnUploadProfile = findViewById(R.id.btnUploadProfile)
        profileImageView = findViewById(R.id.profileImageView)

        btnUploadProfile.setOnClickListener {
            pickImageFromGallery()
        }

        btnRegister.setOnClickListener {
            val name = inputName.text.toString().trim()
            val email = inputEmail.text.toString().trim()
            val password = inputPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (profileImageUri != null) {
                saveProfileImage(profileImageUri!!)?.let { imagePath ->
                    saveUserDataLocally(name, email, password, imagePath)
                } ?: Toast.makeText(this, "Gagal menyimpan foto profil!", Toast.LENGTH_SHORT).show()
            } else {
                saveUserDataLocally(name, email, password, null)
            }
        }

        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            profileImageUri = data?.data
            profileImageView.setImageURI(profileImageUri)
        }
    }

    private fun saveProfileImage(uri: Uri): String? {
        return try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            val file = File(cacheDir, "profile_${System.currentTimeMillis()}.png")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveUserDataLocally(name: String, email: String, password: String, imagePath: String?) {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("name", name)
        editor.putString("email", email)
        editor.putString("password", password)
        editor.putString("profileImagePath", imagePath)
        editor.apply()

        Toast.makeText(this, "Registrasi berhasil! Data disimpan secara lokal.", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
