package com.example.yourbar.activity

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.yourbar.R
import kotlin.math.hypot

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val rootView = findViewById<View>(android.R.id.content)

        window.decorView.postDelayed({
            val cx = rootView.width / 2
            val cy = rootView.height / 2

            // Радиус — от полного (весь экран) до нуля (центр)
            val initialRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
            val finalRadius = 0f

            // Запускаем MainActivity — он окажется за заставкой
            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            // Без анимации перехода — MainActivity просто появится за заставкой
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)

            // Круговое сжатие: заставка "съедается" от краёв к центру
            val reveal = ViewAnimationUtils.createCircularReveal(
                rootView, cx, cy, initialRadius, finalRadius
            )

            reveal.duration = 800
            reveal.addListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator) {
                    rootView.visibility = View.INVISIBLE
                    finish()
                }
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })

            reveal.start()
        }, 1500)
    }
}
