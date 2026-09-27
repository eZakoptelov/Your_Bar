package com.example.yourbar.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.yourbar.R

class LoginActivity : AppCompatActivity() {

    companion object {
        const val ADMIN_PASSWORD = "1234"
        const val EXTRA_IS_ADMIN = "is_admin"
    }

    private lateinit var btnUser: Button
    private lateinit var btnAdmin: Button
    private lateinit var adminPanel: LinearLayout
    private lateinit var etPassword: EditText
    private lateinit var btnConfirmAdmin: Button
    private lateinit var btnCancelAdmin: Button
    private lateinit var spacerTop: View
    private lateinit var spacerBottom: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btnUser = findViewById(R.id.btnUserLogin)
        btnAdmin = findViewById(R.id.btnAdminLogin)
        adminPanel = findViewById(R.id.adminPanel)
        etPassword = findViewById(R.id.etAdminPassword)
        btnConfirmAdmin = findViewById(R.id.btnConfirmAdmin)
        btnCancelAdmin = findViewById(R.id.btnCancelAdmin)
        spacerTop = findViewById(R.id.spacerTop)
        spacerBottom = findViewById(R.id.spacerBottom)

        // Обычный вход
        btnUser.setOnClickListener {
            startMainActivity(false)
        }

        // Нажатие на "Вход администратора" — панель пароля по центру
        btnAdmin.setOnClickListener {
            adminPanel.visibility = View.VISIBLE
            btnAdmin.visibility = View.GONE
            btnUser.visibility = View.GONE
            spacerTop.visibility = View.GONE
            spacerBottom.visibility = View.VISIBLE
        }

        // Отмена — возвращаемся к исходному экрану
        btnCancelAdmin.setOnClickListener {
            resetLoginUI()
        }

        // Подтверждение пароля
        btnConfirmAdmin.setOnClickListener {
            val entered = etPassword.text.toString()
            if (entered == ADMIN_PASSWORD) {
                startMainActivity(true)
            } else {
                Toast.makeText(this, "Неверный пароль", Toast.LENGTH_SHORT).show()
                etPassword.text.clear()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        resetLoginUI()
    }

    private fun resetLoginUI() {
        adminPanel.visibility = View.GONE
        btnAdmin.visibility = View.VISIBLE
        btnUser.visibility = View.VISIBLE
        spacerTop.visibility = View.VISIBLE
        spacerBottom.visibility = View.GONE
        etPassword.text.clear()
    }

    private fun startMainActivity(isAdmin: Boolean) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(EXTRA_IS_ADMIN, isAdmin)
        startActivity(intent)
        @Suppress("DEPRECATION")
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
