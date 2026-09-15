package com.example.data.remote

import java.net.URLEncoder

object PollinationsAiService {

    /**
     * Builds the direct Pollinations.ai image URL using the exact requested parameters:
     * prompt: "${projectName} professional vector logo, clean design, minimalist, 8k"
     * width=800&height=800&nologo=true&seed=${System.currentTimeMillis()}
     */
    fun getPollinationsImageUrl(projectName: String, extraDetails: String = ""): String {
        val cleanName = projectName.ifBlank { "Apex Brand" }
        val suffix = if (extraDetails.isNotBlank()) ", $extraDetails" else ""
        val prompt = "$cleanName professional vector logo, clean design, minimalist, 8k$suffix"
        val encodedPrompt = URLEncoder.encode(prompt, "UTF-8")
        val seed = System.currentTimeMillis()
        return "https://image.pollinations.ai/prompt/$encodedPrompt?width=800&height=800&nologo=true&seed=$seed"
    }

    fun buildProfessionalLogoPrompt(category: String = "", projectName: String, details: String = ""): String {
        val cleanName = projectName.ifBlank { "Apex Brand" }
        val suffix = if (details.isNotBlank()) ", $details" else ""
        return "$cleanName professional vector logo, clean design, minimalist, 8k$suffix"
    }
}
