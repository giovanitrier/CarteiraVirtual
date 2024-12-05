package com.example.carteira

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class ListarRecursosActivity : AppCompatActivity() {
    private val moedas = listOf("R$", "USD", "EUR", "BTC", "ETH")
    private val sharedPreferences by lazy { getSharedPreferences("app_prefs", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listar_recursos)

        val lvRecursos = findViewById<ListView>(R.id.lvRecursos)

        // Mapeando todas as moedas, incluindo as que possuem saldo 0
        val recursos = moedas.map { moeda ->
            val chave = if (moeda == "R$") "saldo_real" else "saldo_$moeda"
            val saldo = sharedPreferences.getFloat(chave, 0f)
            "$moeda: %.2f".format(saldo)
        }

        // Adicionar uma mensagem se não houver saldos (apenas para quando todos os saldos forem 0)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            if (recursos.isNotEmpty()) recursos else listOf("Nenhum saldo disponível.")
        )
        lvRecursos.adapter = adapter
    }



}
