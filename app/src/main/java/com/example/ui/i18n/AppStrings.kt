package com.example.ui.i18n

enum class SupportedLanguage(val code: String, val displayName: String, val localName: String) {
    ENGLISH("en", "English", "English"),
    TAMIL("ta", "Tamil", "தமிழ்"),
    HINDI("hi", "Hindi", "हिन्दी"),
    TELUGU("te", "Telugu", "తెలుగు"),
    MALAYALAM("ml", "Malayalam", "മലയാളം"),
    KANNADA("kn", "Kannada", "ಕನ್ನಡ");

    companion object {
        fun fromCode(code: String): SupportedLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}

object AppStrings {
    fun get(key: String, langCode: String): String {
        val lang = SupportedLanguage.fromCode(langCode)
        return STRINGS[key]?.get(lang) ?: STRINGS[key]?.get(SupportedLanguage.ENGLISH) ?: key
    }

    val STRINGS: Map<String, Map<SupportedLanguage, String>> = mapOf(
        "app_title" to mapOf(
            SupportedLanguage.ENGLISH to "DMM Chord&Lyrics",
            SupportedLanguage.TAMIL to "DMM கார்ட் & வரிகள்",
            SupportedLanguage.HINDI to "DMM कॉर्ड और बोल",
            SupportedLanguage.TELUGU to "DMM కార్డ్ & సాహిత్యం",
            SupportedLanguage.MALAYALAM to "DMM കോഡ് & വരികൾ",
            SupportedLanguage.KANNADA to "DMM ಕಾರ್ಡ್ ಮತ್ತು ಸಾಹಿತ್ಯ"
        ),
        "text_to_chordpro" to mapOf(
            SupportedLanguage.ENGLISH to "Text to ChordPro",
            SupportedLanguage.TAMIL to "கார்ட்ப்ரோ மாற்றி",
            SupportedLanguage.HINDI to "टेक्स्ट से कॉर्डप्रो",
            SupportedLanguage.TELUGU to "టెక్స్ట్ నుండి కార్డ్‌ప్రో",
            SupportedLanguage.MALAYALAM to "ടെക്സ്റ്റ് ടു കോഡ്പ്രോ",
            SupportedLanguage.KANNADA to "ಪಠ್ಯದಿಂದ ಕಾರ್ಡ್‌ಪ್ರೊ"
        ),
        "convert_to_chordpro" to mapOf(
            SupportedLanguage.ENGLISH to "Convert to ChordPro",
            SupportedLanguage.TAMIL to "கார்ட்ப்ரோவாக மாற்றுக",
            SupportedLanguage.HINDI to "कॉर्डप्रो में बदलें",
            SupportedLanguage.TELUGU to "కార్డ్‌ప్రోగా మార్చండి",
            SupportedLanguage.MALAYALAM to "കോഡ്പ്രോ ആക്കുക",
            SupportedLanguage.KANNADA to "ಕಾರ್ಡ್‌ಪ್ರೊಗೆ ಪರಿವರ್ತಿಸಿ"
        ),
        "input_text_label" to mapOf(
            SupportedLanguage.ENGLISH to "Source Text (Lyrics & Chords)",
            SupportedLanguage.TAMIL to "மூல உரை (வரிகள் & கார்ட்கள்)",
            SupportedLanguage.HINDI to "मूल पाठ (बोल और कॉर्ड)",
            SupportedLanguage.TELUGU to "మూల వచనం (సాహిత్యం & కార్డ్‌లు)",
            SupportedLanguage.MALAYALAM to "ഉറവിട വാചകം",
            SupportedLanguage.KANNADA to "ಮೂಲ ಪಠ್ಯ (ಸಾಹಿತ್ಯ & ಕಾರ್ಡ್‌ಗಳು)"
        ),
        "input_text_hint" to mapOf(
            SupportedLanguage.ENGLISH to "Paste lyrics with chords on top lines, or type standard chords…",
            SupportedLanguage.TAMIL to "வரிகளுக்கு மேல் உள்ள கார்டுகளுடன் உரையை ஒட்டவும்…",
            SupportedLanguage.HINDI to "बोल के ऊपर कॉर्ड वाली पंक्तियाँ पेस्ट करें…",
            SupportedLanguage.TELUGU to "సాహిత్యం పైన కార్డ్‌లు ఉన్న వచనాన్ని అతికించండి…",
            SupportedLanguage.MALAYALAM to "വരികൾക്ക് മുകളിലുള്ള കോഡുകൾ ഒട്ടിക്കുക…",
            SupportedLanguage.KANNADA to "ಸಾಹಿತ್ಯದ ಮೇಲಿನ ಕಾರ್ಡ್‌ಗಳೊಂದಿಗೆ ಪಠ್ಯವನ್ನು ಅಂಟಿಸಿ…"
        ),
        "output_text_label" to mapOf(
            SupportedLanguage.ENGLISH to "Converted ChordPro Format",
            SupportedLanguage.TAMIL to "மாற்றப்பட்ட கார்ட்ப்ரோ வடிவம்",
            SupportedLanguage.HINDI to "परिवर्तित कॉर्डप्रो प्रारूप",
            SupportedLanguage.TELUGU to "మార్చబడిన కార్డ్‌ప్రో ఫార్మాట్",
            SupportedLanguage.MALAYALAM to "പരിവർത്തനം ചെയ്ത കോഡ്പ്രോ",
            SupportedLanguage.KANNADA to "ಪರಿವರ್ತಿತ ಕಾರ್ಡ್‌ಪ್ರೊ ಫಾರ್ಮ್ಯಾಟ್"
        ),
        "copy" to mapOf(
            SupportedLanguage.ENGLISH to "Copy",
            SupportedLanguage.TAMIL to "நகலெடு",
            SupportedLanguage.HINDI to "कॉपी करें",
            SupportedLanguage.TELUGU to "కాపీ చేయండి",
            SupportedLanguage.MALAYALAM to "കോപ്പി ചെയ്യുക",
            SupportedLanguage.KANNADA to "ನಕಲಿಸಿ"
        ),
        "copied" to mapOf(
            SupportedLanguage.ENGLISH to "Copied to clipboard!",
            SupportedLanguage.TAMIL to "நகலெடுக்கப்பட்டது!",
            SupportedLanguage.HINDI to "क्लिपबोर्ड पर कॉपी किया गया!",
            SupportedLanguage.TELUGU to "క్లిప్‌బోర్డ్‌కి కాపీ చేయబడింది!",
            SupportedLanguage.MALAYALAM to "പകർത്തി!",
            SupportedLanguage.KANNADA to "ನಕಲಿಸಲಾಗಿದೆ!"
        ),
        "clear" to mapOf(
            SupportedLanguage.ENGLISH to "Clear",
            SupportedLanguage.TAMIL to "அழி",
            SupportedLanguage.HINDI to "साफ़ करें",
            SupportedLanguage.TELUGU to "క్లియర్ చేయండి",
            SupportedLanguage.MALAYALAM to "മായ്ക്കുക",
            SupportedLanguage.KANNADA to "ತೆರವುಗೊಳಿಸಿ"
        ),
        "save_to_library" to mapOf(
            SupportedLanguage.ENGLISH to "Save to My Songs",
            SupportedLanguage.TAMIL to "என் பாடல்களில் சேமி",
            SupportedLanguage.HINDI to "मेरे गीतों में सहेजें",
            SupportedLanguage.TELUGU to "నా పాటల్లో సేవ్ చేయండి",
            SupportedLanguage.MALAYALAM to "എന്റെ ഗാനങ്ങളിൽ ചേർക്കുക",
            SupportedLanguage.KANNADA to "ನನ್ನ ಹಾಡುಗಳಲ್ಲಿ ಉಳಿಸಿ"
        ),
        "save_dialog_title" to mapOf(
            SupportedLanguage.ENGLISH to "Save Converted Song",
            SupportedLanguage.TAMIL to "மாற்றப்பட்ட பாடலைச் சேமிக்கவும்",
            SupportedLanguage.HINDI to "परिवर्तित गीत सहेजें",
            SupportedLanguage.TELUGU to "మార్చబడిన పాటను సేవ్ చేయండి",
            SupportedLanguage.MALAYALAM to "ഗാനം സൂക്ഷിക്കുക",
            SupportedLanguage.KANNADA to "ಪರಿವರ್ತಿತ ಹಾಡನ್ನು ಉಳಿಸಿ"
        ),
        "save_name_label" to mapOf(
            SupportedLanguage.ENGLISH to "Song / File Name",
            SupportedLanguage.TAMIL to "பாடல் / கோப்பு பெயர்",
            SupportedLanguage.HINDI to "गीत / फ़ाइल का नाम",
            SupportedLanguage.TELUGU to "పాట / ఫైల్ పేరు",
            SupportedLanguage.MALAYALAM to "ഗാനത്തിന്റെ പേര്",
            SupportedLanguage.KANNADA to "ಹಾಡು / ಫೈಲ್ ಹೆಸರು"
        ),
        "save_location_label" to mapOf(
            SupportedLanguage.ENGLISH to "Choose Save Location",
            SupportedLanguage.TAMIL to "சேமிக்கும் இடம்",
            SupportedLanguage.HINDI to "सहेजने का स्थान",
            SupportedLanguage.TELUGU to "సేవ్ చేసే స్థానం",
            SupportedLanguage.MALAYALAM to "സൂക്ഷിക്കേണ്ട സ്ഥലം",
            SupportedLanguage.KANNADA to "ಉಳಿಸುವ ಸ್ಥಳ"
        ),
        "save_location_storage" to mapOf(
            SupportedLanguage.ENGLISH to "Device Storage (Folder / SD Card)",
            SupportedLanguage.TAMIL to "சாதன சேமிப்பகம் (கோப்புறை)",
            SupportedLanguage.HINDI to "डिवाइस स्टोरेज (फ़ोल्डर)",
            SupportedLanguage.TELUGU to "పరికర నిల్వ (ఫోల్డర్)",
            SupportedLanguage.MALAYALAM to "ഡിവൈസ് സ്റ്റോറേജ്",
            SupportedLanguage.KANNADA to "ಸಾಧನದ ಸಂಗ್ರಹಣೆ (ಫೋಲ್ಡರ್)"
        ),
        "save_location_storage_desc" to mapOf(
            SupportedLanguage.ENGLISH to "Pick folder on device, SD card or Google Drive (.cho file)",
            SupportedLanguage.TAMIL to "தொலைபேசி அல்லது டிரைவில் கோப்புறையைத் தேர்வுசெய்க (.cho)",
            SupportedLanguage.HINDI to "फ़ोन या गूगल ड्राइव पर फ़ोल्डर चुनें (.cho फ़ाइल)",
            SupportedLanguage.TELUGU to "ఫోన్ లేదా గూగుల్ డ్రైవ్‌లో ఫోల్డర్‌ను ఎంచుకోండి (.cho ఫైల్)",
            SupportedLanguage.MALAYALAM to "ഫോണിൽ ഫോൾഡർ തിരഞ്ഞെടുക്കുക (.cho ഫയൽ)",
            SupportedLanguage.KANNADA to "ಫೋನ್‌ನಲ್ಲಿ ಫೋಲ್ಡರ್ ಆಯ್ಕೆಮಾಡಿ (.cho ಫೈಲ್)"
        ),
        "save_location_library" to mapOf(
            SupportedLanguage.ENGLISH to "App Library (My Songs)",
            SupportedLanguage.TAMIL to "செயலி நூலகம் (என் பாடல்கள்)",
            SupportedLanguage.HINDI to "ऐप लाइब्रेरी (मेरे गीत)",
            SupportedLanguage.TELUGU to "యాప్ లైబ్రరీ (నా పాటలు)",
            SupportedLanguage.MALAYALAM to "ആപ്പ് ലൈബ്രറി (എന്റെ ഗാനങ്ങൾ)",
            SupportedLanguage.KANNADA to "ಅಪ್ಲಿಕೇಶನ್ ಲೈಬ್ರರಿ (ನನ್ನ ಹಾಡುಗಳು)"
        ),
        "save_location_library_desc" to mapOf(
            SupportedLanguage.ENGLISH to "Direct stage view with instant transpose & auto-scroll",
            SupportedLanguage.TAMIL to "நேரடி மேடைப் பார்வை, உடனடி கார்ட் மாற்றம்",
            SupportedLanguage.HINDI to "लाइव स्टेज व्यू, तत्काल ट्रांसपोज़ और ऑटो-स्क्रॉल",
            SupportedLanguage.TELUGU to "తక్షణ ట్రాన్స్‌పోజ్ మరియు ఆటో-స్క్రోల్‌తో లైవ్ స్టేజ్ వీక్షణ",
            SupportedLanguage.MALAYALAM to "തത്സമയ വേദി കാഴ്ച, ഓട്ടോ-സ്ക്രോൾ",
            SupportedLanguage.KANNADA to "ತ್ವರಿತ ಟ್ರಾನ್ಸ್‌ಪೋಸ್ ಮತ್ತು ಆಟೋ-ಸ್ಕ್ರಾಲ್‌ನೊಂದಿಗೆ ಲೈವ್ ವೀಕ್ಷಣೆ"
        ),
        "save_location_both" to mapOf(
            SupportedLanguage.ENGLISH to "Save to Both (Device & App Library)",
            SupportedLanguage.TAMIL to "இரண்டிலும் சேமி (சாதனம் & செயலி)",
            SupportedLanguage.HINDI to "दोनों में सहेजें (डिवाइस और ऐप लाइब्रेरी)",
            SupportedLanguage.TELUGU to "రెండింటిలోనూ సేవ్ చేయండి (పరికరం & యాప్)",
            SupportedLanguage.MALAYALAM to "രണ്ടിലും സൂക്ഷിക്കുക",
            SupportedLanguage.KANNADA to "ಎರಡರಲ್ಲೂ ಉಳಿಸಿ (ಸಾಧನ ಮತ್ತು ಲೈಬ್ರರಿ)"
        ),
        "save_success" to mapOf(
            SupportedLanguage.ENGLISH to "Saved successfully!",
            SupportedLanguage.TAMIL to "வெற்றிகரமாகச் சேமிக்கப்பட்டது!",
            SupportedLanguage.HINDI to "सफलतापूर्वक सहेजा गया!",
            SupportedLanguage.TELUGU to "విజయవంతంగా సేవ్ చేయబడింది!",
            SupportedLanguage.MALAYALAM to "വിജയകരമായി സൂക്ഷിച്ചു!",
            SupportedLanguage.KANNADA to "ಯಶಸ್ವಿಯಾಗಿ ಉಳಿಸಲಾಗಿದೆ!"
        ),
        "my_songs" to mapOf(
            SupportedLanguage.ENGLISH to "My Songs",
            SupportedLanguage.TAMIL to "என் பாடல்கள்",
            SupportedLanguage.HINDI to "मेरे गीत",
            SupportedLanguage.TELUGU to "నా పాటలు",
            SupportedLanguage.MALAYALAM to "എന്റെ ഗാനങ്ങൾ",
            SupportedLanguage.KANNADA to "ನನ್ನ ಹಾಡುಗಳು"
        ),
        "import_chordpro" to mapOf(
            SupportedLanguage.ENGLISH to "+ Import ChordPro",
            SupportedLanguage.TAMIL to "+ கார்ட்ப்ரோ இறக்குமதி",
            SupportedLanguage.HINDI to "+ कॉर्डप्रो आयात करें",
            SupportedLanguage.TELUGU to "+ కార్డ్‌ప్రో దిగుమతి",
            SupportedLanguage.MALAYALAM to "+ കോഡ്പ്രോ ഇമ്പോർട്ട്",
            SupportedLanguage.KANNADA to "+ ಕಾರ್ಡ್‌ಪ್ರೊ ಆಮದು"
        ),
        "search_songs" to mapOf(
            SupportedLanguage.ENGLISH to "Search songs by title or artist…",
            SupportedLanguage.TAMIL to "தலைப்பு அல்லது பாடகர் மூலம் தேடுக…",
            SupportedLanguage.HINDI to "शीर्षक या गायक द्वारा खोजें…",
            SupportedLanguage.TELUGU to "శీర్షిక లేదా కళాకారుని ద్వారా శోధించండి…",
            SupportedLanguage.MALAYALAM to "ഗാനം തിരയുക…",
            SupportedLanguage.KANNADA to "ಹಾಡುಗಳನ್ನು ಹುಡುಕಿ…"
        ),
        "all" to mapOf(
            SupportedLanguage.ENGLISH to "All",
            SupportedLanguage.TAMIL to "அனைத்தும்",
            SupportedLanguage.HINDI to "सभी",
            SupportedLanguage.TELUGU to "అన్నీ",
            SupportedLanguage.MALAYALAM to "എല്ലാം",
            SupportedLanguage.KANNADA to "ಎಲ್ಲವೂ"
        ),
        "favorites" to mapOf(
            SupportedLanguage.ENGLISH to "Favorites",
            SupportedLanguage.TAMIL to "விருப்பமானவை",
            SupportedLanguage.HINDI to "पसंदीदा",
            SupportedLanguage.TELUGU to "ఇష్టమైనవి",
            SupportedLanguage.MALAYALAM to "ഇഷ്ടപ്പെട്ടവ",
            SupportedLanguage.KANNADA to "ಮೆಚ್ಚಿನವುಗಳು"
        ),
        "key" to mapOf(
            SupportedLanguage.ENGLISH to "Key",
            SupportedLanguage.TAMIL to "சுரம்",
            SupportedLanguage.HINDI to "स्केल",
            SupportedLanguage.TELUGU to "కీ",
            SupportedLanguage.MALAYALAM to "കീ",
            SupportedLanguage.KANNADA to "ಕೀ"
        ),
        "capo" to mapOf(
            SupportedLanguage.ENGLISH to "Capo",
            SupportedLanguage.TAMIL to "காப்போ",
            SupportedLanguage.HINDI to "कैपो",
            SupportedLanguage.TELUGU to "కాపో",
            SupportedLanguage.MALAYALAM to "കാപ്പോ",
            SupportedLanguage.KANNADA to "ಕ್ಯಾಪೋ"
        ),
        "tempo" to mapOf(
            SupportedLanguage.ENGLISH to "Tempo",
            SupportedLanguage.TAMIL to "வேகம்",
            SupportedLanguage.HINDI to "लय",
            SupportedLanguage.TELUGU to "వేగం",
            SupportedLanguage.MALAYALAM to "ടെമ്പോ",
            SupportedLanguage.KANNADA to "ಗತಿ"
        ),
        "page_up" to mapOf(
            SupportedLanguage.ENGLISH to "▲ PAGE UP",
            SupportedLanguage.TAMIL to "▲ பக்கம் மேலே",
            SupportedLanguage.HINDI to "▲ पृष्ठ ऊपर",
            SupportedLanguage.TELUGU to "▲ పేజీ పైకి",
            SupportedLanguage.MALAYALAM to "▲ പേജ് മുകളിലേക്ക്",
            SupportedLanguage.KANNADA to "▲ ಪುಟ ಮೇಲೆ"
        ),
        "page_down" to mapOf(
            SupportedLanguage.ENGLISH to "▼ PAGE DOWN",
            SupportedLanguage.TAMIL to "▼ பக்கம் கீழே",
            SupportedLanguage.HINDI to "▼ पृष्ठ नीचे",
            SupportedLanguage.TELUGU to "▼ పేజీ క్రిందికి",
            SupportedLanguage.MALAYALAM to "▼ പേജ് താഴേക്ക്",
            SupportedLanguage.KANNADA to "▼ ಪುಟ ಕೆಳಗೆ"
        ),
        "transpose" to mapOf(
            SupportedLanguage.ENGLISH to "TRANSPOSE",
            SupportedLanguage.TAMIL to "சுர மாற்றம்",
            SupportedLanguage.HINDI to "ट्रांसपोज़",
            SupportedLanguage.TELUGU to "ట్రాన్స్పోజ్",
            SupportedLanguage.MALAYALAM to "ട്രാൻസ്പോസ്",
            SupportedLanguage.KANNADA to "ಟ್ರಾನ್ಸ್‌ಪೋಸ್"
        ),
        "reset_transpose" to mapOf(
            SupportedLanguage.ENGLISH to "Reset",
            SupportedLanguage.TAMIL to "மீட்டமை",
            SupportedLanguage.HINDI to "रीसेट",
            SupportedLanguage.TELUGU to "రీసెట్",
            SupportedLanguage.MALAYALAM to "റീസെറ്റ്",
            SupportedLanguage.KANNADA to "ಮರುಹೊಂದಿಸು"
        ),
        "auto_scroll" to mapOf(
            SupportedLanguage.ENGLISH to "Auto Scroll",
            SupportedLanguage.TAMIL to "தானியங்கி உருளல்",
            SupportedLanguage.HINDI to "ऑटो स्क्रॉल",
            SupportedLanguage.TELUGU to "ఆటో స్క్రోల్",
            SupportedLanguage.MALAYALAM to "ഓട്ടോ സ്ക്രോൾ",
            SupportedLanguage.KANNADA to "ಆಟೋ ಸ್ಕ್ರಾಲ್"
        ),
        "speed" to mapOf(
            SupportedLanguage.ENGLISH to "Speed",
            SupportedLanguage.TAMIL to "வேகம்",
            SupportedLanguage.HINDI to "गति",
            SupportedLanguage.TELUGU to "వేగం",
            SupportedLanguage.MALAYALAM to "വേഗത",
            SupportedLanguage.KANNADA to "ವೇಗ"
        ),
        "settings" to mapOf(
            SupportedLanguage.ENGLISH to "Settings",
            SupportedLanguage.TAMIL to "அமைப்புகள்",
            SupportedLanguage.HINDI to "सेटिंग्स",
            SupportedLanguage.TELUGU to "సెట్టింగ్‌లు",
            SupportedLanguage.MALAYALAM to "ക്രമീകരണങ്ങൾ",
            SupportedLanguage.KANNADA to "ಸೆಟ್ಟಿಂಗ್‌ಗಳು"
        ),
        "language" to mapOf(
            SupportedLanguage.ENGLISH to "App Language",
            SupportedLanguage.TAMIL to "செயலி மொழி",
            SupportedLanguage.HINDI to "ऐप की भाषा",
            SupportedLanguage.TELUGU to "యాప్ భాష",
            SupportedLanguage.MALAYALAM to "ആപ്പ് ഭാഷ",
            SupportedLanguage.KANNADA to "ಆ್ಯಪ್ ಭಾಷೆ"
        ),
        "font_controls" to mapOf(
            SupportedLanguage.ENGLISH to "Typography & Size",
            SupportedLanguage.TAMIL to "எழுத்து அளவு",
            SupportedLanguage.HINDI to "फ़ॉन्ट और आकार",
            SupportedLanguage.TELUGU to "ఫాంట్ & పరిమాణం",
            SupportedLanguage.MALAYALAM to "ഫോണ്ട് വലുപ്പം",
            SupportedLanguage.KANNADA to "ಅಕ್ಷರ ಗಾತ್ರ"
        ),
        "lyrics_font_size" to mapOf(
            SupportedLanguage.ENGLISH to "Lyrics Font Size",
            SupportedLanguage.TAMIL to "பாடல் வரிகள் அளவு",
            SupportedLanguage.HINDI to "गीत के बोल का आकार",
            SupportedLanguage.TELUGU to "సాహిత్యం ఫాంట్ పరిమాణం",
            SupportedLanguage.MALAYALAM to "വരികളുടെ ഫോണ്ട് വലുപ്പം",
            SupportedLanguage.KANNADA to "ಸಾಹಿತ್ಯದ ಅಕ್ಷರ ಗಾತ್ರ"
        ),
        "chord_font_size" to mapOf(
            SupportedLanguage.ENGLISH to "Chord Font Size",
            SupportedLanguage.TAMIL to "கார்ட் எழுத்து அளவு",
            SupportedLanguage.HINDI to "कॉर्ड फ़ॉन्ट का आकार",
            SupportedLanguage.TELUGU to "కార్డ్ ఫాంట్ పరిమాణం",
            SupportedLanguage.MALAYALAM to "കോഡ് ഫോണ്ട് വലുപ്പം",
            SupportedLanguage.KANNADA to "ಕಾರ್ಡ್ ಅಕ್ಷರ ಗಾತ್ರ"
        ),
        "line_spacing" to mapOf(
            SupportedLanguage.ENGLISH to "Line Spacing",
            SupportedLanguage.TAMIL to "வரி இடைவெளி",
            SupportedLanguage.HINDI to "पंक्ति अंतराल",
            SupportedLanguage.TELUGU to "వరుస ఖాళీ",
            SupportedLanguage.MALAYALAM to "വരികൾ തമ്മിലുള്ള അകലം",
            SupportedLanguage.KANNADA to "ಸಾಲುಗಳ ಅಂತರ"
        ),
        "color_theme" to mapOf(
            SupportedLanguage.ENGLISH to "Display Theme",
            SupportedLanguage.TAMIL to "காட்சி தோற்றம்",
            SupportedLanguage.HINDI to "प्रदर्शन थीम",
            SupportedLanguage.TELUGU to "డిస్ప్లే థీమ్",
            SupportedLanguage.MALAYALAM to "തീം",
            SupportedLanguage.KANNADA to "ಥೀಮ್"
        ),
        "chord_color" to mapOf(
            SupportedLanguage.ENGLISH to "Chord Highlight Color",
            SupportedLanguage.TAMIL to "கார்ட் நிறம்",
            SupportedLanguage.HINDI to "कॉर्ड का रंग",
            SupportedLanguage.TELUGU to "కార్డ్ రంగు",
            SupportedLanguage.MALAYALAM to "കോഡ് നിറം",
            SupportedLanguage.KANNADA to "ಕಾರ್ಡ್ ಬಣ್ಣ"
        ),
        "live_preview" to mapOf(
            SupportedLanguage.ENGLISH to "Live Preview",
            SupportedLanguage.TAMIL to "நேரடி மாதிரி",
            SupportedLanguage.HINDI to "लाइव पूर्वावलोकन",
            SupportedLanguage.TELUGU to "లైవ్ ప్రివ్యూ",
            SupportedLanguage.MALAYALAM to "ലൈവ് പ്രിവ്യൂ",
            SupportedLanguage.KANNADA to "ಲೈವ್ ಮುನ್ನೋಟ"
        ),
        "keep_screen_on" to mapOf(
            SupportedLanguage.ENGLISH to "Keep Screen On (Stage Wake Lock)",
            SupportedLanguage.TAMIL to "திரையை ஒளிர வைக்கவும் (மேடை முறை)",
            SupportedLanguage.HINDI to "स्क्रीन चालू रखें (स्टेज मोड)",
            SupportedLanguage.TELUGU to "స్క్రీన్ ఆన్‌లో ఉంచండి (స్టేజ్ మోడ్)",
            SupportedLanguage.MALAYALAM to "സ്ക്രീൻ ഓൺ ആക്കി വെക്കുക",
            SupportedLanguage.KANNADA to "ಸ್ಕ್ರೀನ್ ಆನ್ ಆಗಿರಲಿ (ಸ್ಟೇಜ್ ಮೋಡ್)"
        ),
        "wake_lock_desc" to mapOf(
            SupportedLanguage.ENGLISH to "Prevents the screen from dimming or turning off during live performance.",
            SupportedLanguage.TAMIL to "நிகழ்ச்சியின் போது திரை அணைவதைத் தடுக்கிறது.",
            SupportedLanguage.HINDI to "लाइव प्रदर्शन के दौरान स्क्रीन को बंद होने से रोकता है।",
            SupportedLanguage.TELUGU to "ప్రదర్శన సమయంలో స్క్రీన్ ఆపివేయబడకుండా నిరోధిస్తుంది.",
            SupportedLanguage.MALAYALAM to "തത്സമയ പ്രകടന വേളയിൽ സ്ക്രീൻ ഓഫാകുന്നത് തടയുന്നു.",
            SupportedLanguage.KANNADA to "ಪ್ರದರ್ಶನದ ಸಮಯದಲ್ಲಿ ಪರದೆ ಆಫ್ ಆಗುವುದನ್ನು ತಡೆಯುತ್ತದೆ."
        ),
        "rename" to mapOf(
            SupportedLanguage.ENGLISH to "Rename",
            SupportedLanguage.TAMIL to "மறுபெயரிடு",
            SupportedLanguage.HINDI to "नाम बदलें",
            SupportedLanguage.TELUGU to "పేరు మార్చండి",
            SupportedLanguage.MALAYALAM to "പേരുമാറ്റുക",
            SupportedLanguage.KANNADA to "ಮರುಹೆಸರಿಸಿ"
        ),
        "delete" to mapOf(
            SupportedLanguage.ENGLISH to "Delete",
            SupportedLanguage.TAMIL to "நீக்கு",
            SupportedLanguage.HINDI to "हटाएं",
            SupportedLanguage.TELUGU to "తొలగించు",
            SupportedLanguage.MALAYALAM to "ഡിലീറ്റ് ചെയ്യുക",
            SupportedLanguage.KANNADA to "ಅಳಿಸಿ"
        ),
        "cancel" to mapOf(
            SupportedLanguage.ENGLISH to "Cancel",
            SupportedLanguage.TAMIL to "ரத்து செய்",
            SupportedLanguage.HINDI to "रद्द करें",
            SupportedLanguage.TELUGU to "రద్దు చేయండి",
            SupportedLanguage.MALAYALAM to "റദ്ദാക്കുക",
            SupportedLanguage.KANNADA to "ರದ್ದುಮಾಡಿ"
        ),
        "save" to mapOf(
            SupportedLanguage.ENGLISH to "Save",
            SupportedLanguage.TAMIL to "சேமி",
            SupportedLanguage.HINDI to "सहेजें",
            SupportedLanguage.TELUGU to "సేవ్ చేయండి",
            SupportedLanguage.MALAYALAM to "സൂക്ഷിക്കുക",
            SupportedLanguage.KANNADA to "ಉಳಿಸಿ"
        ),
        "delete_confirm_title" to mapOf(
            SupportedLanguage.ENGLISH to "Delete Song?",
            SupportedLanguage.TAMIL to "பாடலை நீக்கவா?",
            SupportedLanguage.HINDI to "गीत हटाएं?",
            SupportedLanguage.TELUGU to "పాటను తొలగించాలా?",
            SupportedLanguage.MALAYALAM to "ഗാനം ഇല്ലാതാക്കണോ?",
            SupportedLanguage.KANNADA to "ಹಾಡನ್ನು ಅಳಿಸಬೇಕೇ?"
        ),
        "delete_confirm_desc" to mapOf(
            SupportedLanguage.ENGLISH to "Are you sure you want to delete this song from your library? This action cannot be undone.",
            SupportedLanguage.TAMIL to "இந்தப் பாடலை உங்கள் நூலகத்திலிருந்து நீக்க விரும்புகிறீர்களா? இதை மீட்டெடுக்க முடியாது.",
            SupportedLanguage.HINDI to "क्या आप इस गीत को अपनी लाइब्रेरी से हटाना चाहते हैं? यह क्रिया पूर्ववत नहीं की जा सकती।",
            SupportedLanguage.TELUGU to "మీ లైబ్రరీ నుండి ఈ పాటను తొలగించాలనుకుంటున్నారా? దీన్ని రద్దు చేయలేరు.",
            SupportedLanguage.MALAYALAM to "ഈ ഗാനം ലൈബ്രറിയിൽ നിന്ന് നീക്കം ചെയ്യണോ? ഇത് മാറ്റാനാകില്ല.",
            SupportedLanguage.KANNADA to "ನಿಮ್ಮ ಲೈಬ್ರರಿಯಿಂದ ಈ ಹಾಡನ್ನು ಅಳಿಸಲು ಖಚಿತವಾಗಿ ಬಯಸುವಿರಾ?"
        ),
        "import_file" to mapOf(
            SupportedLanguage.ENGLISH to "Choose File (.cho, .chordpro, .pro, .txt)",
            SupportedLanguage.TAMIL to "கோப்பைத் தேர்வுசெய்க (.cho, .chordpro, .pro, .txt)",
            SupportedLanguage.HINDI to "फ़ाइल चुनें (.cho, .chordpro, .pro, .txt)",
            SupportedLanguage.TELUGU to "ఫైల్‌ని ఎంచుకోండి (.cho, .chordpro, .pro, .txt)",
            SupportedLanguage.MALAYALAM to "ഫയൽ തിരഞ്ഞെടുക്കുക",
            SupportedLanguage.KANNADA to "ಫೈಲ್ ಆಯ್ಕೆಮಾಡಿ"
        ),
        "paste_chordpro" to mapOf(
            SupportedLanguage.ENGLISH to "Paste ChordPro Text",
            SupportedLanguage.TAMIL to "கார்ட்ப்ரோ உரையை ஒட்டுக",
            SupportedLanguage.HINDI to "कॉर्डप्रो टेक्स्ट पेस्ट करें",
            SupportedLanguage.TELUGU to "కార్డ్‌ప్రో వచనాన్ని అతికించండి",
            SupportedLanguage.MALAYALAM to "കോഡ്പ്രോ ടെക്സ്റ്റ് പേസ്റ്റ് ചെയ്യുക",
            SupportedLanguage.KANNADA to "ಕಾರ್ಡ್‌ಪ್ರೊ ಪಠ್ಯವನ್ನು ಅಂಟಿಸಿ"
        ),
        "import_success" to mapOf(
            SupportedLanguage.ENGLISH to "Song imported successfully!",
            SupportedLanguage.TAMIL to "பாடல் வெற்றிகரமாக இறக்குமதி செய்யப்பட்டது!",
            SupportedLanguage.HINDI to "गीत सफलतापूर्वक आयात किया गया!",
            SupportedLanguage.TELUGU to "పాట విజయవంతంగా దిగుమతి చేయబడింది!",
            SupportedLanguage.MALAYALAM to "ഗാനം വിജയകരമായി ചേർത്തു!",
            SupportedLanguage.KANNADA to "ಹಾಡನ್ನು ಯಶಸ್ವಿಯಾಗಿ ಆಮದು ಮಾಡಿಕೊಳ್ಳಲಾಗಿದೆ!"
        ),
        "import_error" to mapOf(
            SupportedLanguage.ENGLISH to "Could not import file. Please check that it is a valid ChordPro or text file.",
            SupportedLanguage.TAMIL to "கோப்பை இறக்குமதி செய்ய முடியவில்லை. சரியான கார்ட்ப்ரோ கோப்புதானா என சரிபார்க்கவும்.",
            SupportedLanguage.HINDI to "फ़ाइल आयात नहीं की जा सकी। कृपया जांचें कि यह एक वैध कॉर्डप्रो या टेक्स्ट फ़ाइल है।",
            SupportedLanguage.TELUGU to "ఫైల్ దిగుమతి కాలేదు. దయచేసి సరైన ఫైల్ కాదా సరిచూడండి.",
            SupportedLanguage.MALAYALAM to "ഫയൽ ഇമ്പോർട്ട് ചെയ്യാൻ കഴിഞ്ഞില്ല. സാധുതയുള്ള ഫയലാണോ എന്ന് പരിശോധിക്കുക.",
            SupportedLanguage.KANNADA to "ಫೈಲ್ ಆಮದು ಮಾಡಲು ಸಾಧ್ಯವಾಗಲಿಲ್ಲ. ದಯವಿಟ್ಟು ಪರಿಶೀಲಿಸಿ."
        ),
        "empty_library" to mapOf(
            SupportedLanguage.ENGLISH to "No songs found in your library.",
            SupportedLanguage.TAMIL to "உங்கள் நூலகத்தில் பாடல்கள் எதுவும் இல்லை.",
            SupportedLanguage.HINDI to "आपकी लाइब्रेरी में कोई गीत नहीं मिला।",
            SupportedLanguage.TELUGU to "మీ లైబ్రరీలో పాటలు లేవు.",
            SupportedLanguage.MALAYALAM to "ലൈബ്രറിയിൽ ഗാനങ്ങളൊന്നും കണ്ടെത്തിയില്ല.",
            SupportedLanguage.KANNADA to "ಯಾವುದೇ ಹಾಡುಗಳು ಕಂಡುಬಂದಿಲ್ಲ."
        ),
        "empty_library_hint" to mapOf(
            SupportedLanguage.ENGLISH to "Tap '+ Import ChordPro' above to import your .cho, .chordpro, or .txt files, or paste songs directly.",
            SupportedLanguage.TAMIL to "உங்கள் கார்ட்ப்ரோ கோப்புகளை இறக்குமதி செய்ய மேலே உள்ள '+ கார்ட்ப்ரோ இறக்குமதி' என்பதைத் தட்டவும்.",
            SupportedLanguage.HINDI to "अपनी कॉर्डप्रो या टेक्स्ट फ़ाइलें आयात करने के लिए ऊपर दिए गए '+ कॉर्डप्रो आयात करें' पर टैप करें।",
            SupportedLanguage.TELUGU to "మీ పాటల ఫైళ్లను దిగుమతి చేసుకోవడానికి పైన ఉన్న బటన్‌ను నొక్కండి.",
            SupportedLanguage.MALAYALAM to "നിങ്ങളുടെ ഗാനങ്ങൾ ചേർക്കാൻ മുകളിലെ ബട്ടൺ അമർത്തുക.",
            SupportedLanguage.KANNADA to "ನಿಮ್ಮ ಹಾಡುಗಳನ್ನು ಆಮದು ಮಾಡಲು ಮೇಲಿನ ಬಟನ್ ಒತ್ತಿ."
        ),
        "theme_stage_dark" to mapOf(
            SupportedLanguage.ENGLISH to "Stage Dark",
            SupportedLanguage.TAMIL to "மேடை இருள்",
            SupportedLanguage.HINDI to "स्टेज डार्क",
            SupportedLanguage.TELUGU to "స్టేజ్ డార్క్",
            SupportedLanguage.MALAYALAM to "സ്റ്റേജ് ഡാർക്ക്",
            SupportedLanguage.KANNADA to "ಸ್ಟೇಜ್ ಡಾರ್ಕ್"
        ),
        "theme_oled_black" to mapOf(
            SupportedLanguage.ENGLISH to "OLED Pure Black",
            SupportedLanguage.TAMIL to "ஓஎல்இடி ஆழ் கருப்பு",
            SupportedLanguage.HINDI to "ओएलईडी शुद्ध काला",
            SupportedLanguage.TELUGU to "ఓఎల్ఈడీ బ్లాక్",
            SupportedLanguage.MALAYALAM to "ഒഎൽഇഡി ബ്ലാക്ക്",
            SupportedLanguage.KANNADA to "ಒಎಲ್‌ಇಡಿ ಬ್ಲ್ಯಾಕ್"
        ),
        "theme_midnight_blue" to mapOf(
            SupportedLanguage.ENGLISH to "Midnight Navy",
            SupportedLanguage.TAMIL to "நள்ளிரவு நீலம்",
            SupportedLanguage.HINDI to "मिडनाइट नेवी",
            SupportedLanguage.TELUGU to "మిడ్‌నైట్ బ్లూ",
            SupportedLanguage.MALAYALAM to "മിഡ്നൈറ്റ് നേവി",
            SupportedLanguage.KANNADA to "ಮಿಡ್‌ನೈಟ್ ನೇವಿ"
        ),
        "theme_studio_light" to mapOf(
            SupportedLanguage.ENGLISH to "Studio Light",
            SupportedLanguage.TAMIL to "ஸ்டுடியோ வெளிச்சம்",
            SupportedLanguage.HINDI to "स्टूडियो लाइट",
            SupportedLanguage.TELUGU to "స్టూడియో లైట్",
            SupportedLanguage.MALAYALAM to "സ്റ്റുഡിയോ ലൈറ്റ്",
            SupportedLanguage.KANNADA to "ಸ್ಟುಡಿಯೋ ಲೈಟ್"
        ),
        "theme_warm_paper" to mapOf(
            SupportedLanguage.ENGLISH to "Warm Paper",
            SupportedLanguage.TAMIL to "வெதுவெதுப்பான காகிதம்",
            SupportedLanguage.HINDI to "वार्म पेपर",
            SupportedLanguage.TELUGU to "వార్మ్ పేపర్",
            SupportedLanguage.MALAYALAM to "വാം പേപ്പർ",
            SupportedLanguage.KANNADA to "ವಾರ್ಮ್ ಪೇಪರ್"
        ),
        "fullscreen" to mapOf(
            SupportedLanguage.ENGLISH to "Fullscreen Mode",
            SupportedLanguage.TAMIL to "முழுத்திரை முறை",
            SupportedLanguage.HINDI to "फुलस्क्रीन मोड",
            SupportedLanguage.TELUGU to "పూర్తి స్క్రీన్ మోడ్",
            SupportedLanguage.MALAYALAM to "ഫുൾസ്ക്രീൻ മോഡ്",
            SupportedLanguage.KANNADA to "ಪೂರ್ಣಪರದೆ ಮೋಡ್"
        ),
        "original_key" to mapOf(
            SupportedLanguage.ENGLISH to "Original",
            SupportedLanguage.TAMIL to "அசல்",
            SupportedLanguage.HINDI to "मूल",
            SupportedLanguage.TELUGU to "అసలు",
            SupportedLanguage.MALAYALAM to "യഥാർത്ഥം",
            SupportedLanguage.KANNADA to "ಮೂಲ"
        ),
        "instrument_tuner" to mapOf(
            SupportedLanguage.ENGLISH to "Instrument Tuner",
            SupportedLanguage.TAMIL to "இசைக்கருவி ட்யூனர்",
            SupportedLanguage.HINDI to "वाद्य ट्यूनर",
            SupportedLanguage.TELUGU to "ఇన్స్ట్రుమెంట్ ట్యూనర్",
            SupportedLanguage.MALAYALAM to "ഇൻസ്ട്രുമെന്റ് ട്യൂണർ",
            SupportedLanguage.KANNADA to "ವಾದ್ಯ ಟ್ಯೂನರ್"
        ),
        "tuner_quick_desc" to mapOf(
            SupportedLanguage.ENGLISH to "Guitar & Piano Tuner",
            SupportedLanguage.TAMIL to "கிட்டார் & பியானோ ட்யூனர்",
            SupportedLanguage.HINDI to "गिटार और पियानो ट्यूनर",
            SupportedLanguage.TELUGU to "గిటార్ & పియానో ట్యూనర్",
            SupportedLanguage.MALAYALAM to "ഗിറ്റാർ & പിയാനോ ട്യൂണർ",
            SupportedLanguage.KANNADA to "ಗಿಟಾರ್ & ಪಿಯಾನೋ ಟ್ಯೂನರ್"
        )
    )
}
