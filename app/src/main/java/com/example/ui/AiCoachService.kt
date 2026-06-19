package com.example.ui

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AiCoachService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    suspend fun getStudyCoachFeeback(
        apiKey: String,
        username: String,
        studyHours: Double,
        streak: Int,
        weakAreas: String,
        sessionsCount: Int,
        loggedSessionsSummary: String,
        recentJournalWin: String,
        recentJournalConfusion: String
    ): String = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Please add your GEMINI_API_KEY in the Secrets panel of AI Studio to enable personalized smart coaching."
        }

        val prompt = """
            You are the Student Life OS AI Study Coach. Provide actionable study recommendations.
            Here is the user's REAL tracked work data for user '$username':
            - Total study hours logged this week: ${String.format("%.2f", studyHours)} hrs
            - Current habit streak: $streak days
            - Logged sessions: $sessionsCount
            - Study log summary: $loggedSessionsSummary
            - Weak areas / Difficulties noted: $weakAreas
            - Recent positive win: $recentJournalWin
            - Recent confusion: $recentJournalConfusion

            Give the student absolute genuine feedback based ONLY on this structured info. 
            Do not make up fake homework or grades. Keep your tone encouraging, practical, and highly strategic.
            Divide your advice into 3 quick, actionable headings:
            1. 📊 LOGS FEEDBACK (Analysis of logged hours and consistency)
            2. 📚 RECOMMENDED REVISION (Targeted chapters or subjects to master)
            3. ⚡ WEEKLY WORK ACTION PLAN (Clear tactical actions)
            
            Keep your paragraphs punchy, stylish and formatted clearly. Max 350 words.
        """.trimIndent()

        val jsonRequest = JSONObject().apply {
            val contentObj = JSONObject().apply {
                put("parts", org.json.JSONArray().put(JSONObject().apply { put("text", prompt) }))
            }
            put("contents", org.json.JSONArray().put(contentObj))
        }

        val body = jsonRequest.toString().toRequestBody("application/json".toMediaType())
        val urlWithKey = "$BASE_URL?key=$apiKey"

        val request = Request.Builder()
            .url(urlWithKey)
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "AI Coach is currently offline (Error code ${response.code}). Build your study history in the meantime!"
                }
                val responseBodyStr = response.body?.string() ?: return@withContext "No analysis feedback generated."
                val jsonResponse = JSONObject(responseBodyStr)
                val candidates = jsonResponse.getJSONArray("candidates")
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.getJSONObject("content")
                val parts = content.getJSONArray("parts")
                parts.getJSONObject(0).getString("text")
            }
        } catch (e: Exception) {
            "AI Recommendation unavailable: ${e.localizedMessage}. Try logging more real study sessions first!"
        }
    }
}
