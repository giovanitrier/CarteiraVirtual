package com.example.carteira

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import org.json.JSONException



class ConverterRecursosActivity : AppCompatActivity() {
    private val moedas = listOf("R$", "USD", "EUR", "BTC", "ETH")
    private val sharedPreferences by lazy { getSharedPreferences("app_prefs", MODE_PRIVATE) }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_converter_recursos)

        val spinnerOrigem = findViewById<Spinner>(R.id.spinnerOrigem)
        val spinnerDestino = findViewById<Spinner>(R.id.spinnerDestino)
        val etValorConverter = findViewById<EditText>(R.id.etValorConverter)
        val btnConverter = findViewById<Button>(R.id.btnConverter)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvResultado = findViewById<TextView>(R.id.tvResultado)

        // Filtra moedas com saldo > 0
        val moedasComSaldo = moedas.filter { moeda ->
            val chave = if (moeda == "R$") "saldo_real" else "saldo_$moeda"
            sharedPreferences.getFloat(chave, 0f) > 0
        }
        spinnerOrigem.setSelection(moedasComSaldo.indexOf("R$"))

        val adapterOrigem = ArrayAdapter(this, android.R.layout.simple_spinner_item, moedasComSaldo)
        adapterOrigem.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerOrigem.adapter = adapterOrigem

        val adapterDestino = ArrayAdapter(this, android.R.layout.simple_spinner_item, moedas)
        adapterDestino.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDestino.adapter = adapterDestino

        val tvSaldo = findViewById<TextView>(R.id.tvSaldo)
        spinnerOrigem.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val moedaOrigem = parent.getItemAtPosition(position).toString()
                // Verifique se tvResultado está inicializado
                tvResultado.text = "Você selecionou $moedaOrigem"
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Lógica caso nada seja selecionado
            }
        })

        btnConverter.setOnClickListener {
            val moedaOrigem = spinnerOrigem.selectedItem?.toString() ?: return@setOnClickListener
            val moedaDestino = spinnerDestino.selectedItem?.toString() ?: return@setOnClickListener
            val valor = etValorConverter.text.toString().toFloatOrNull()

            if (moedaOrigem == moedaDestino) {
                Toast.makeText(this, "Selecione moedas diferentes.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (valor == null || valor <= 0) {
                Toast.makeText(this, "Digite um valor válido.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val saldoOrigem = sharedPreferences.getFloat("saldo_$moedaOrigem", 0f)
            if (saldoOrigem < valor) {
                Toast.makeText(this, "Saldo insuficiente na moeda de origem.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            tvResultado.text = "Processando..."

            realizarConversao(moedaOrigem, moedaDestino, valor) { sucesso, valorConvertido ->
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    if (sucesso) {
                        atualizarSaldos(moedaOrigem, moedaDestino, valor, valorConvertido)
                        tvResultado.text =
                            "Conversão realizada! $valor $moedaOrigem = $valorConvertido $moedaDestino"
                    } else {
                        tvResultado.text = "Erro ao realizar conversão."
                        Toast.makeText(this, "Erro ao realizar conversão.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }

    private fun realizarConversao(
        moedaOrigem: String,
        moedaDestino: String,
        valor: Float,
        callback: (Boolean, Float) -> Unit
    ) {
        val client = OkHttpClient()
        val url = "https://economia.awesomeapi.com.br/last/${moedaOrigem}-${moedaDestino}"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                // Exibe uma mensagem de erro caso a requisição falhe
                callback(false, 0f)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                try {
                    response.use {
                        val responseBody = response.body?.string() ?: throw IOException("Corpo da resposta vazio")
                        val json = JSONObject(responseBody)
                        val key = "${moedaOrigem}${moedaDestino}"
                        if (json.has(key)) {
                            val cotacao = json.getJSONObject(key).getString("bid").toFloat()
                            callback(true, valor * cotacao)
                        } else {
                            throw JSONException("Chave $key não encontrada no JSON")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback(false, 0f)
                }
            }

        })
    }


    private fun atualizarSaldos(
        moedaOrigem: String,
        moedaDestino: String,
        valorOrigem: Float,
        valorDestino: Float
    ) {
        val saldoOrigem = sharedPreferences.getFloat("saldo_$moedaOrigem", 0f)
        val saldoDestino = sharedPreferences.getFloat("saldo_$moedaDestino", 0f)

        sharedPreferences.edit()
            .putFloat("saldo_$moedaOrigem", saldoOrigem - valorOrigem)
            .putFloat("saldo_$moedaDestino", saldoDestino + valorDestino)
            .apply()
    }

}
