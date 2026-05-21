package com.example.api

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.Locale
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class Part(val text: String)

@JsonClass(generateAdapter = true)
data class Content(val parts: List<Part>)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(val contents: List<Content>)

@JsonClass(generateAdapter = true)
data class Candidate(val content: Content)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(val candidates: List<Candidate>?)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiCategorizer {
    private const val TAG = "GeminiCategorizer"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val api: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    val CATEGORIES = listOf(
        "Alimentação",
        "Transporte",
        "Lazer",
        "Moradia",
        "Compras",
        "Saúde",
        "Educação",
        "Outros"
    )

    /**
     * Categorizes a transaction description automatically.
     * Uses Gemini API or matches local keywords if the API isn't available or fails.
     */
    suspend fun categorize(description: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val trimmedDesc = description.trim()
        if (trimmedDesc.isEmpty()) return@withContext "Outros"

        // Rule: If key is default/placeholder or empty, use fallback directly to save network
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("placeholder", ignoreCase = true)) {
            Log.d(TAG, "Gemini API Key is empty or placeholder, using offline categorizer.")
            return@withContext fallbackCategorization(trimmedDesc)
        }

        val prompt = """
            Dada a seguinte descrição de uma transação financeira: "$trimmedDesc", responda APENAS com uma das seguintes categorias predefinidas que melhor se encaixe:
            - Alimentação
            - Transporte
            - Lazer
            - Moradia
            - Compras
            - Saúde
            - Educação
            - Outros

            Instruções estritas:
            1. Responda apenas com o nome da categoria exato.
            2. Não use quebras de linha, Markdown, pontos ou textos adicionais.
            3. Se não tiver certeza absoluta, classifique de forma sensata ou retorne "Outros".
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )

        try {
            val response = api.generateContent(apiKey, request)
            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
            if (replyText != null) {
                // Find a matching pre-defined category, cleaning the AI Response
                val matched = CATEGORIES.firstOrNull { cat ->
                    replyText.contains(cat, ignoreCase = true)
                }
                if (matched != null) {
                    Log.d(TAG, "Gemini auto-categorized '$trimmedDesc' as: $matched")
                    return@withContext matched
                }
            }
            Log.d(TAG, "Gemini returned unmatched category, using keyword fallback.")
            fallbackCategorization(trimmedDesc)
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API call failed: ${e.message}. Using offline fallback.", e)
            fallbackCategorization(trimmedDesc)
        }
    }

    private fun fallbackCategorization(desc: String): String {
        val lower = desc.lowercase(Locale.getDefault())
        return when {
            // Food & Groceries
            containsAny(lower, "almoço", "jantar", "café", "restaurante", "mcdonald", "burger", "pizza", "comida", "padaria", "supermercado", "mercado", "açougue", "feira", "lanche", "ifood", "snack") -> "Alimentação"
            // Transport & Travel
            containsAny(lower, "uber", "99taxi", "táxi", "ônibus", "metro", "metrô", "combustível", "gasolina", "posto", "br", "shell", "ipiranga", "pedágio", "passagem", "viagem", "estacionamento") -> "Transporte"
            // Leisure & Entertainment
            containsAny(lower, "cinema", "show", "bar", "cerveja", "balada", "festa", "netflix", "spotify", "steam", "playstation", "jogos", "teatro", "clube", "parque", "viagem", "resort", "hotel") -> "Lazer"
            // Housing & Bills
            containsAny(lower, "aluguel", "condomínio", "energia", "luz", "água", "saneamento", "gás", "internet", "eletropaulo", "sabesp", "reforma", "enxoval", "móveis") -> "Moradia"
            // Shopping
            containsAny(lower, "shopping", "loja", "roupa", "calçado", "tênis", "livro", "eletrônico", "celular", "computador", "amazon", "mercado livre", "shopee", "shein", "carrefour", "magalu", "bahia", "renner", "c&a", "cea") -> "Compras"
            // Health
            containsAny(lower, "farmácia", "drogaria", "remédio", "médico", "consulta", "exame", "hospital", "dentista", "plano de saúde", "psicólogo", "academia", "crossfit") -> "Saúde"
            // Education
            containsAny(lower, "escola", "faculdade", "curso", "mensalidade", "livros", "material escolar", "udemy", "inglês", "pós-graduação", "mba") -> "Educação"
            // Default
            else -> "Outros"
        }
    }

    private fun containsAny(text: String, vararg keywords: String): Boolean {
        return keywords.any { key -> text.contains(key) }
    }
}
