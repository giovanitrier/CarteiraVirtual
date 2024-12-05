package com.example.carteira

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var saldoTextView: TextView
    private val sharedPreferences by lazy { getSharedPreferences("app_prefs", MODE_PRIVATE) }
    private val moedas = listOf("R$", "USD", "EUR", "BTC", "ETH") // Adicionando a lista de moedas

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        saldoTextView = findViewById(R.id.tvSaldo)
        val btnDepositar = findViewById<Button>(R.id.btnDepositar)
        val btnListarRecursos = findViewById<Button>(R.id.btnListarRecursos)
        val btnConverterRecursos = findViewById<Button>(R.id.btnConverterRecursos)

        atualizarSaldo()
        btnDepositar.setOnClickListener {
            startActivity(Intent(this, DepositarActivity::class.java))
        }
        btnListarRecursos.setOnClickListener {
            startActivity(Intent(this, ListarRecursosActivity::class.java))
        }
        btnConverterRecursos.setOnClickListener {
            startActivity(Intent(this, ConverterRecursosActivity::class.java))
        }
    }

    private fun atualizarSaldo() {
        val saldo = sharedPreferences.getFloat("saldo_real", 0f)
        saldoTextView.text = "Saldo: R$ %.2f".format(saldo)
    }


    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "Atualizando saldo ao retornar.")
        atualizarSaldo()
    }
}