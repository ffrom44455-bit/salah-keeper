package com.salahkeeper.app.util

object ArabicNormalizer {

    private val DIACRITICS_REGEX = Regex("[\u064B-\u0652\u0653-\u065F\u0670\u06D6-\u06ED]")

    /**
     * Normalizes Arabic text by removing vowels/diacritics and normalizing variants of letters.
     */
    fun normalize(input: String): String {
        var text = input.trim()
        // Remove diacritics
        text = text.replace(DIACRITICS_REGEX, "")
        // Normalize Alef variants
        text = text.replace(Regex("[إأآٱ]"), "ا")
        // Normalize Alef Maksura to Yaa
        text = text.replace("ى", "ي")
        // Normalize Taa Marbuta to Haa
        text = text.replace("ة", "ه")
        // Strip tatweel (kashida)
        text = text.replace("\u0640", "")
        // Strip non-letter punctuation
        text = text.replace(Regex("[^\\p{L}\\p{Nd}\\s]"), " ")
        // Normalize whitespace
        return text.replace(Regex("\\s+"), " ").trim().lowercase()
    }

    /**
     * Checks if spoken text matches the beginning or core components of the Dua.
     * Matches either Arabic ("اللهم اعني") or transliterated speech ("allahumma a'inni").
     */
    fun matchesDua(spokenText: String): Boolean {
        if (spokenText.isBlank()) return false

        val normalizedInput = normalize(spokenText)
        val lowerRaw = spokenText.lowercase()

        // 1. Direct Arabic core keywords check
        val hasAllahumma = normalizedInput.contains("اللهم") || lowerRaw.contains("allahumma") || lowerRaw.contains("allahuma")
        val hasAinni = normalizedInput.contains("اعني") || normalizedInput.contains("عني") || lowerRaw.contains("ainni") || lowerRaw.contains("a'inni") || lowerRaw.contains("innee")
        val hasDhikr = normalizedInput.contains("ذكرك") || lowerRaw.contains("dhikr") || lowerRaw.contains("zikr")
        val hasShukr = normalizedInput.contains("شكرك") || lowerRaw.contains("shukr")
        val hasIbadat = normalizedInput.contains("عبادتك") || lowerRaw.contains("ibadat")

        // If user recites at least "اللهم اعني" or "Allahumma a'inni" or any 2 key dua words
        if (hasAllahumma && hasAinni) return true
        if (hasAllahumma && (hasDhikr || hasShukr || hasIbadat)) return true
        if (hasAinni && (hasDhikr || hasShukr)) return true

        // 2. Substring or Levenshtein similarity for first words
        val targetStart = "اللهم اعني"
        if (normalizedInput.contains(targetStart)) return true

        return false
    }
}
