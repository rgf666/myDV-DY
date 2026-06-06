package com.example.tvdouyin

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class SettingsActivity : AppCompatActivity() {

    private lateinit var cookieInput: EditText
    private lateinit var saveButton: Button
    private lateinit var clearButton: Button
    private lateinit var testButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        const val PREFS_NAME = "DouyinSettings"
        const val COOKIE_KEY = "douyin_cookie"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initViews()
        loadSavedCookie()
        setupListeners()
    }

    private fun initViews() {
        cookieInput = findViewById(R.id.et_cookie)
        saveButton = findViewById(R.id.btn_save)
        clearButton = findViewById(R.id.btn_clear)
        testButton = findViewById(R.id.btn_test)
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun loadSavedCookie() {
        val savedCookie = sharedPreferences.getString(COOKIE_KEY, "")
        cookieInput.setText(savedCookie)
    }

    private fun setupListeners() {
        saveButton.setOnClickListener {
            saveCookie()
        }

        clearButton.setOnClickListener {
            clearCookie()
        }

        testButton.setOnClickListener {
            testCookie()
        }
    }

    /**
     * Save Cookie to SharedPreferences
     */
    private fun saveCookie() {
        val cookieString = cookieInput.text.toString().trim()

        if (cookieString.isEmpty()) {
            Toast.makeText(this, "请输入Cookie", Toast.LENGTH_SHORT).show()
            return
        }

        // Save to SharedPreferences
        sharedPreferences.edit().apply {
            putString(COOKIE_KEY, cookieString)
            apply()
        }

        // Apply Cookie to WebView CookieManager
        applyCookieToWebView(cookieString)

        Toast.makeText(this, "Cookie保存成功", Toast.LENGTH_SHORT).show()
    }

    /**
     * Clear Cookie
     */
    private fun clearCookie() {
        cookieInput.setText("")

        sharedPreferences.edit().apply {
            remove(COOKIE_KEY)
            apply()
        }

        // Clear WebView cookies
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()

        Toast.makeText(this, "Cookie已清除", Toast.LENGTH_SHORT).show()
    }

    /**
     * Test Cookie by loading Douyin website
     */
    private fun testCookie() {
        val cookieString = cookieInput.text.toString().trim()

        if (cookieString.isEmpty()) {
            Toast.makeText(this, "请先输入Cookie", Toast.LENGTH_SHORT).show()
            return
        }

        applyCookieToWebView(cookieString)

        // You can open a test WebView here to verify login status
        Toast.makeText(this, "Cookie已应用，请返回主页刷新", Toast.LENGTH_LONG).show()
    }

    /**
     * Apply Cookie string to WebView CookieManager
     */
    private fun applyCookieToWebView(cookieString: String) {
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)

        // Parse Cookie string and set each cookie
        val cookies = parseCookieString(cookieString)

        val douyinDomains = listOf(
            "https://www.douyin.com",
            "https://douyin.com"
        )

        for (domain in douyinDomains) {
            for (cookie in cookies) {
                cookieManager.setCookie(domain, cookie)
            }
        }

        cookieManager.flush()
    }

    /**
     * Parse Cookie string (format: "key1=value1; key2=value2")
     */
    private fun parseCookieString(cookieString: String): List<String> {
        return cookieString.split(";").map { it.trim() }
    }

    /**
     * Get saved Cookie (static method for other classes to use)
     */
    companion object {
        fun getSavedCookie(context: Context): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(COOKIE_KEY, "") ?: ""
        }

        fun hasSavedCookie(context: Context): Boolean {
            return getSavedCookie(context).isNotEmpty()
        }
    }
}
