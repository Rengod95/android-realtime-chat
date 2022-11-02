package com.example.practiceproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.practiceproject.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvMessage.text = "뷰 바인딩 테스트"

    }
}