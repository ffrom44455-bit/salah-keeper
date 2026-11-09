package com.salahkeeper.app.data.model

data class DuaModel(
    val arabic: String = "اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ وَشُكْرِكَ وَحُسْنِ عِبَادَتِكَ",
    val transliteration: String = "Allahumma a'inni 'ala dhikrika wa shukrika wa husni 'ibadatik",
    val translation: String = "O Allah, help me remember You, thank You, and worship You in the best way.",
    val keyWordsNormalized: List<String> = listOf("اللهم", "اعني", "على", "ذكرك")
)
