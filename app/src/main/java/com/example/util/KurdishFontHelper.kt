package com.example.util

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

data class KurdishFontItem(
    val id: String,
    val kurdishName: String,
    val englishName: String,
    val styleDescription: String,
    val fontUrl: String
) {
    val name: String get() = englishName
}

object KurdishFontHelper {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(4, TimeUnit.SECONDS)
        .build()

    // 20 Standard Kurdish & Arabic Google Fonts
    val FONT_LIST: List<KurdishFontItem> = listOf(
        KurdishFontItem(
            id = "vazirmatn",
            kurdishName = "وەزیرمەتن (Vazirmatn)",
            englishName = "Vazirmatn",
            styleDescription = "فۆنتی ستاندارد و هاوچەرخی کوردی",
            fontUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/vazirmatn/Vazirmatn%5Bwght%5D.ttf"
        ),
        KurdishFontItem(
            id = "noto_sans_arabic",
            kurdishName = "نۆتۆ سانز (Noto Sans Arabic)",
            englishName = "Noto Sans Arabic",
            styleDescription = "ڕوون و خوێندنەوەی زۆر ئاسان",
            fontUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/notosansarabic/NotoSansArabic%5Bwght%5D.ttf"
        ),
        KurdishFontItem(
            id = "noto_kufi_arabic",
            kurdishName = "کوفی مۆدێرن (Noto Kufi Arabic)",
            englishName = "Noto Kufi Arabic",
            styleDescription = "کوفی ئەندازەیی مۆدێرن بۆ لۆگۆ",
            fontUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/notokufiarabic/NotoKufiArabic%5Bwght%5D.ttf"
        ),
        KurdishFontItem(
            id = "noto_naskh_arabic",
            kurdishName = "نەسخی کلاسیک (Noto Naskh Arabic)",
            englishName = "Noto Naskh Arabic",
            styleDescription = "ستایلی نەسخی ئەدەبی و فەرمی",
            fontUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/notonaskharabic/NotoNaskhArabic%5Bwght%5D.ttf"
        ),
        KurdishFontItem(
            id = "cairo",
            kurdishName = "قاهیرە (Cairo)",
            englishName = "Cairo",
            styleDescription = "زۆر گونجاو و سەرنجڕاکێش بۆ لۆگۆ",
            fontUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/cairo/Cairo%5Bslnt%2Cwght%5D.ttf"
        ),
        KurdishFontItem(
            id = "tajawal",
            kurdishName = "تەجەوول (Tajawal)",
            englishName = "Tajawal",
            styleDescription = "مۆدێرن و گەنجانە بە پیتە نەرمەکان",
            fontUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/tajawal/Tajawal-Bold.ttf"
        ),
        KurdishFontItem(
            id = "almarai",
            kurdishName = "مەراعی (Almarai)",
            englishName = "Almarai",
            styleDescription = "پڕۆفیشناڵ و بازرگانی بۆ براندەکان",
            fontUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/almarai/Almarai-Bold.ttf"
        ),
        KurdishFontItem(
            id = "changa",
            kurdishName = "چانگا (Changa)",
            englishName = "Changa",
            styleDescription = "مۆدێرن، چوارگۆشەیی و ئەستوور",
            fontUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/changa/Changa%5Bwght%5D.ttf"
        ),
        KurdishFontItem(
            id = "lalezar",
            kurdishName = "لالەزار (Lalezar)",
            englishName = "Lalezar",
            styleDescription = "زۆر ئەستوور و تایبەت بۆ سەردێڕ و پۆستەر",
            fontUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/lalezar/Lalezar-Regular.ttf"
        ),
        KurdishFontItem(
            id = "readex_pro",
            kurdishName = "ڕیدێکس پرۆ (Readex Pro)",
            englishName = "Readex Pro",
            styleDescription = "ئەندازەیی، تەکنەلۆژی و مۆدێرن",
            fontUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/readexpro/ReadexPro%5BHEXP%2Cwght%5D.ttf"
        ),
        KurdishFontItem(
            id = "el_messiri",
            kurdishName = "مەسیری (El Messiri)",
            englishName = "El Messiri",
            styleDescription = "نەرم، هونەری و سەرنجڕاکێش",
            fontUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/elmessiri/ElMessiri%5Bwght%5D.ttf"
        ),
        KurdishFontItem(
            id = "amiri",
            kurdishName = "ئەمیری (Amiri)",
            englishName = "Amiri",
            styleDescription = "کەلەپووری، خەتخۆش و ڕەسەن",
            fontUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/amiri/Amiri-Bold.ttf"
        ),
        KurdishFontItem(
            id = "reem_kufi",
            kurdishName = "ڕیم کوفی (Reem Kufi)",
            englishName = "Reem Kufi",
            styleDescription = "کوفی شێوە دەستی و جوان",
            fontUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/reemkufi/ReemKufi%5Bwght%5D.ttf"
        ),
        KurdishFontItem(
            id = "mada",
            kurdishName = "مەدا (Mada)",
            englishName = "Mada",
            styleDescription = "سادە، مۆدێرن و پوخت",
            fontUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/mada/Mada%5Bwght%5D.ttf"
        ),
        KurdishFontItem(
            id = "ibm_plex_sans_arabic",
            kurdishName = "ئای بی ئێم (IBM Plex Sans Arabic)",
            englishName = "IBM Plex Sans Arabic",
            styleDescription = "تەکنەلۆژی، کۆمپانیا و سەردەمیانە",
            fontUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/ibmplexsansarabic/IBMPlexSansArabic-Bold.ttf"
        ),
        KurdishFontItem(
            id = "kufam",
            kurdishName = "کوفام (Kufam)",
            englishName = "Kufam",
            styleDescription = "کوفی تابلۆیی و لۆگۆی داهێنەرانە",
            fontUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/kufam/Kufam%5Bwght%5D.ttf"
        ),
        KurdishFontItem(
            id = "aref_ruqaa",
            kurdishName = "ڕوقعە (Aref Ruqaa)",
            englishName = "Aref Ruqaa",
            styleDescription = "خەتی ڕوقعەی نەریتی بۆ خۆشنووسی",
            fontUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/arefruqaa/ArefRuqaa-Bold.ttf"
        ),
        KurdishFontItem(
            id = "markazi_text",
            kurdishName = "مەرکەزی (Markazi Text)",
            englishName = "Markazi Text",
            styleDescription = "کتێبی، قورس و ڕێکوپێک",
            fontUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/markazitext/MarkaziText%5Bwght%5D.ttf"
        ),
        KurdishFontItem(
            id = "scheherazade_new",
            kurdishName = "شەهرەزاد (Scheherazade New)",
            englishName = "Scheherazade New",
            styleDescription = "نەسخی درێژکراوە و شکۆدار",
            fontUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/scheherazadenew/ScheherazadeNew-Bold.ttf"
        ),
        KurdishFontItem(
            id = "rakkas",
            kurdishName = "ڕەقاس (Rakkas)",
            englishName = "Rakkas",
            styleDescription = "ستایلی پۆستەر و سەرنجڕاکێشی جیاواز",
            fontUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/rakkas/Rakkas-Regular.ttf"
        )
    )

    val kurdishFonts: List<KurdishFontItem> get() = FONT_LIST

    private val typefaceCache = mutableMapOf<String, Typeface>()

    fun getFontItem(id: String): KurdishFontItem {
        return FONT_LIST.firstOrNull { it.id == id } ?: FONT_LIST[0]
    }

    /**
     * Get or download font Typeface asynchronously
     */
    suspend fun getTypeface(context: Context, fontId: String): Typeface = withContext(Dispatchers.IO) {
        typefaceCache[fontId]?.let { return@withContext it }

        val fontItem = getFontItem(fontId)
        val fontsDir = File(context.cacheDir, "kurdish_fonts")
        if (!fontsDir.exists()) fontsDir.mkdirs()

        val fontFile = File(fontsDir, "${fontItem.id}.ttf")
        if (fontFile.exists() && fontFile.length() > 5000) {
            try {
                val tf = Typeface.createFromFile(fontFile)
                typefaceCache[fontId] = tf
                return@withContext tf
            } catch (_: Exception) {
                // Ignore and re-download if cache was corrupted
            }
        }

        // Download the TTF file
        try {
            val request = Request.Builder().url(fontItem.fontUrl).build()
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.byteStream()?.use { input ->
                    FileOutputStream(fontFile).use { output ->
                        input.copyTo(output)
                    }
                }
                if (fontFile.exists() && fontFile.length() > 5000) {
                    val tf = Typeface.createFromFile(fontFile)
                    typefaceCache[fontId] = tf
                    return@withContext tf
                }
            }
        } catch (_: Exception) {
            // Fallback gracefully without flooding logs
        }

        // Fallback to system Arabic font
        val fallback = Typeface.create("sans-serif", Typeface.BOLD)
        typefaceCache[fontId] = fallback
        return@withContext fallback
    }
}
