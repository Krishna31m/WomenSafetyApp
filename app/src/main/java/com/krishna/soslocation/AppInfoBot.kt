package com.krishna.soslocation

class AppInfoBot {

    private val appName = "RakshaSetu"

    // SPECIFIC app-related keywords (more precise matching)
    private val specificAppKeywords = listOf(
        "raksha", "rakshasetu",
        "sos button", "sos feature", "emergency alert", "panic button",
        "emergency contact", "trusted contact", "save contact",
        "app feature", "app work", "how to use app", "use this app",
        "this app", "your app", "safety app","about you", "who are you", "what are you",
        "your name", "yourself", "tell me about yourself",
        "what can you do", "your features", "your capabilities",
        "nearby hospital", "nearby police", "nearby pharmacy", "nearby medical",
        "find hospital", "find police", "find pharmacy"
    )

    // General terms that need context (only app-related if combined with app context)
    private val contextualKeywords = listOf(
        "sos", "emergency", "location sharing", "gps tracking",
        "app setting", "configure app", "app permission"
    )

    fun isAppRelatedQuery(message: String): Boolean {
        val lowerMessage = message.lowercase().trim()

        // Always handle greetings locally for quick response
        if (containsAny(lowerMessage, listOf("hi", "hello", "hey", "hola", "good morning", "good evening"))) {
            return true
        }

        // Handle thank you and goodbye locally
        if (containsAny(lowerMessage, listOf("thank", "thanks", "bye", "goodbye"))) {
            return true
        }

        // Check for specific app-related phrases (high confidence)
        if (specificAppKeywords.any { lowerMessage.contains(it) }) {
            return true
        }

        // Check contextual keywords with app context
        if (contextualKeywords.any { lowerMessage.contains(it) }) {
            // Only treat as app-related if there's app context
            val hasAppContext = lowerMessage.contains("app") ||
                    lowerMessage.contains("this") ||
                    lowerMessage.contains("here") ||
                    lowerMessage.contains("how") ||
                    lowerMessage.contains("what")
            if (hasAppContext) {
                return true
            }
        }

        // Check for questions about features
        if ((lowerMessage.contains("what") || lowerMessage.contains("how")) &&
            (lowerMessage.contains("feature") || lowerMessage.contains("work") ||
                    lowerMessage.contains("use") || lowerMessage.contains("does"))) {
            return true
        }

        // Everything else goes to Gemini
        return false
    }

    fun getWelcomeMessage(): String {
        return "👋 Hi! I'm your Safety Assistant for $appName.\n\n" +
                "I can help you with:\n\n" +
                "🚨 **App Features:**\n" +
                "• SOS Emergency Alert\n" +
                "• Location Sharing\n" +
                "• Nearby Safety Locations\n" +
                "• Emergency Contacts\n" +
                "• App Settings & Privacy\n\n" +
                "💬 **General Questions:**\n" +
                "• General knowledge\n" +
                "• Anything you want to know!\n\n" +
                "What would you like to know?"
    }

    fun getResponse(userMessage: String): String {
        val message = userMessage.lowercase().trim()

        return when {
            // Greetings
            containsAny(message, listOf("hi", "hello", "hey", "hola", "good morning", "good evening")) -> {
                val greetings = listOf(
                    "Hello! 👋 How can I help you today?",
                    "Hi there! 😊 What can I do for you?",
                    "Hey! 👋 I'm here to help with $appName or answer any questions!",
                    "Hello! 🤖 Ask me about the app or anything else you'd like to know!"
                )
                greetings.random()
            }

            // About the bot/app
            containsAny(message, listOf("about you", "who are you", "what are you", "your name")) -> {
                "🤖 **About Me**\n\n" +
                        "I'm your Safety Assistant for $appName - a women's safety app!\n\n" +
                        "I can help you with:\n" +
                        "• Understanding app features\n" +
                        "• SOS emergency procedures\n" +
                        "• Location sharing guidance\n" +
                        "• Finding nearby safety locations\n" +
                        "• Answering general questions\n\n" +
                        "I'm here to keep you safe and informed!"
            }

            // SOS Feature
            containsAny(message, listOf("sos button", "sos feature", "sos alert", "panic button", "emergency button")) ||
                    (message.contains("sos") && (message.contains("how") || message.contains("what") || message.contains("use"))) -> {
                "🚨 **SOS Emergency Alert**\n\n" +
                        "The SOS button is your quick emergency response:\n\n" +
                        "• Press the SOS button to send instant alerts\n" +
                        "• Your current location is automatically shared\n" +
                        "• Emergency messages are sent to all your saved contacts\n" +
                        "• Works even in low network areas\n\n" +
                        "💡 Tip: Add trusted contacts in Settings for faster emergency response!"
            }

            // Location Sharing
            (message.contains("location") || message.contains("gps")) &&
                    (message.contains("share") || message.contains("tracking") || message.contains("how") || message.contains("work")) -> {
                "📍 **Location Sharing**\n\n" +
                        "Your safety through location:\n\n" +
                        "• Real-time GPS tracking\n" +
                        "• Instantly shares your exact location with saved contacts\n" +
                        "• Updates location continuously during emergency\n" +
                        "• Works with Google Maps for accurate positioning\n\n" +
                        "✅ Your location is only shared when YOU activate the SOS!"
            }

            // Nearby Hospital
            message.contains("nearby hospital") || message.contains("find hospital") -> {
                "🏥 **Find Nearby Hospitals**\n\n" +
                        "To find hospitals near you:\n\n" +
                        "1. Go to the Map section from main screen\n" +
                        "2. Tap the RED button (Hospital icon)\n" +
                        "3. See all nearby hospitals with distances\n" +
                        "4. Tap any hospital to get directions\n\n" +
                        "The app shows hospitals within 5km radius!"
            }

            // Nearby Police
            message.contains("nearby police") || message.contains("find police") -> {
                "👮 **Find Nearby Police Stations**\n\n" +
                        "To find police stations:\n\n" +
                        "1. Go to the Map section\n" +
                        "2. Tap the BLUE button (Police icon)\n" +
                        "3. See all nearby police stations with distances\n" +
                        "4. Tap to navigate instantly\n\n" +
                        "Quick access to law enforcement when you need help!"
            }

            // Nearby Pharmacy
            message.contains("nearby pharmacy") || message.contains("nearby medical") ||
                    message.contains("find pharmacy") -> {
                "💊 **Find Nearby Pharmacies**\n\n" +
                        "To find medical stores:\n\n" +
                        "1. Go to the Map section\n" +
                        "2. Tap the GREEN button (Pharmacy icon)\n" +
                        "3. See all nearby pharmacies with distances\n" +
                        "4. Get directions with one tap\n\n" +
                        "Find medicines and medical supplies near you!"
            }

            // Map Features in general
            message.contains("map") && (message.contains("how") || message.contains("use") || message.contains("work")) -> {
                "🗺️ **Nearby Safety Locations**\n\n" +
                        "Find help around you:\n\n" +
                        "🏥 **Hospitals** - Nearest medical facilities\n" +
                        "👮 **Police Stations** - Law enforcement help\n" +
                        "💊 **Pharmacies** - Medical stores for medicines\n\n" +
                        "**Features:**\n" +
                        "• View all locations on interactive map\n" +
                        "• See distance from your current location\n" +
                        "• Tap any location for navigation\n" +
                        "• Opens Google Maps for directions\n\n" +
                        "Access the map from the main screen!"
            }

            // Emergency Contacts
            containsAny(message, listOf("emergency contact", "trusted contact", "save contact", "add contact")) -> {
                "📞 **Emergency Contacts**\n\n" +
                        "Save your trusted contacts:\n\n" +
                        "• Add family, friends, or trusted persons\n" +
                        "• They receive instant SOS alerts\n" +
                        "• Get your real-time location updates\n" +
                        "• Can be called directly in emergencies\n\n" +
                        "💡 Recommendation: Add at least 3-5 emergency contacts for best safety coverage!"
            }

            // How to use app
            (message.contains("how") && (message.contains("use") || message.contains("work"))) &&
                    (message.contains("app") || message.contains("this")) -> {
                "📱 **How to Use $appName**\n\n" +
                        "**Step 1:** Add Emergency Contacts\n" +
                        "→ Go to Settings and save trusted contacts\n\n" +
                        "**Step 2:** Enable Permissions\n" +
                        "→ Allow location and SMS permissions\n\n" +
                        "**Step 3:** Use SOS Button\n" +
                        "→ Press when you need immediate help\n\n" +
                        "**Step 4:** Explore Safety Map\n" +
                        "→ Find nearby hospitals, police, pharmacies\n\n" +
                        "Always keep the app accessible for quick emergency response!"
            }

            // Features Overview / What can you do
            containsAny(message, listOf("what can you do", "your features", "your capabilities", "tell me about yourself")) ||
                    message.contains("feature") || message.contains("what can") ||
                    (message.contains("what") && message.contains("do")) -> {
                "⭐ **$appName Features**\n\n" +
                        "🚨 **SOS Emergency Alert**\n" +
                        "   → Quick panic button for instant help\n" +
                        "   → Sends alerts to all emergency contacts\n\n" +
                        "📍 **Live Location Sharing**\n" +
                        "   → Share real-time GPS coordinates\n" +
                        "   → Automatic location updates during emergency\n\n" +
                        "🗺️ **Safety Map**\n" +
                        "   → Find hospitals, police stations, pharmacies\n" +
                        "   → Navigate to nearest safety location\n\n" +
                        "📞 **Emergency Contacts**\n" +
                        "   → Save and manage trusted contacts\n" +
                        "   → Quick call/message in emergencies\n\n" +
                        "💬 **Smart AI Assistant (Me!)**\n" +
                        "   → Get help with app features\n" +
                        "   → Answer general questions\n\n" +
                        "What would you like to know more about?"
            }

            // Privacy & Security
            containsAny(message, listOf("privacy", "secure", "safe", "data protection", "permission")) -> {
                "🔒 **Privacy & Security**\n\n" +
                        "Your safety and privacy matter:\n\n" +
                        "✅ Location shared ONLY during SOS activation\n" +
                        "✅ No data stored on external servers\n" +
                        "✅ Contacts stored locally on your device\n" +
                        "✅ No tracking when app is not in use\n" +
                        "✅ You control all permissions\n\n" +
                        "We prioritize your privacy while ensuring your safety!"
            }

            // Settings
            message.contains("setting") || message.contains("configure") -> {
                "⚙️ **App Settings**\n\n" +
                        "Configure your safety preferences:\n\n" +
                        "• Add/Remove emergency contacts\n" +
                        "• Customize SOS message\n" +
                        "• Set emergency call preferences\n" +
                        "• Manage app permissions\n" +
                        "• Update personal information\n\n" +
                        "Access Settings from the main menu!"
            }

            // Battery & Performance
            containsAny(message, listOf("battery", "power", "performance")) -> {
                "🔋 **Battery & Performance**\n\n" +
                        "• App uses minimal battery power\n" +
                        "• GPS activated only when needed\n" +
                        "• Works efficiently even on low battery\n" +
                        "• SOS works in power-saving mode\n\n" +
                        "💡 Keep your phone charged when going out for extended periods!"
            }

            // Network/Internet
            containsAny(message, listOf("internet", "network", "offline", "connection")) -> {
                "📶 **Network Requirements**\n\n" +
                        "• SOS SMS works without internet\n" +
                        "• Location sharing needs GPS (no internet required)\n" +
                        "• Map features need internet connection\n" +
                        "• Basic emergency features work offline\n\n" +
                        "The app is designed to work in various network conditions!"
            }

            // Thank you
            containsAny(message, listOf("thank", "thanks", "appreciate")) -> {
                "You're welcome! 😊 I'm here to help keep you safe and answer your questions. Feel free to ask me anything!\n\nStay safe! 💪"
            }

            // Goodbye
            containsAny(message, listOf("bye", "goodbye", "see you")) -> {
                "Take care and stay safe! 🛡️ You can come back anytime if you need help with $appName. Goodbye!"
            }

            // Default - shouldn't normally reach here
            else -> {
                "I can help you with:\n\n" +
                        "• SOS emergency features\n" +
                        "• Location sharing\n" +
                        "• Finding nearby hospitals, police, pharmacies\n" +
                        "• App settings and privacy\n\n" +
                        "Or ask me anything else you'd like to know!"
            }
        }
    }

    private fun containsAny(text: String, keywords: List<String>): Boolean {
        return keywords.any { text.contains(it) }
    }
}

//package com.krishna.soslocation
//
//// Bot that handles app-specific queries
//class AppInfoBot {
//
//    private val appName = "RakshaSetu" // Change to your app name
//
//    // Keywords that indicate app-related queries
//    private val appKeywords = listOf(
//        "about you", "who are you", "what are you",
//        "your name", "tell me about yourself",
//        "what can you do", "your features", "your capabilities",
//        "app feature", "app work", "how to use",
//        "this app", "your app", appName.lowercase()
//    )
//
//    fun isAppRelatedQuery(message: String): Boolean {
//        val lowerMessage = message.lowercase().trim()
//
//        // Check for greetings
//        if (containsAny(lowerMessage, listOf("hi", "hello", "hey", "hola"))) {
//            return true
//        }
//
//        // Check for app-specific keywords
//        return appKeywords.any { lowerMessage.contains(it) }
//    }
//
//    fun getWelcomeMessage(): String {
//        return "👋 Hello! I'm your AI Assistant.\n\n" +
//                "I can help you in two ways:\n\n" +
//                "1️⃣ **App Information**\n" +
//                "Ask me about app features, how to use it, etc.\n\n" +
//                "2️⃣ **General Questions**\n" +
//                "Ask me anything - weather, facts, advice, calculations!\n\n" +
//                "What would you like to know? 😊"
//    }
//
//    fun getResponse(userMessage: String): String {
//        val message = userMessage.lowercase().trim()
//
//        return when {
//            // Greetings
//            containsAny(message, listOf("hi", "hello", "hey", "hola")) -> {
//                "Hello! 👋 How can I help you today? Ask me about the app or anything else!"
//            }
//
//            // About the bot/app
//            containsAny(message, listOf("about you", "who are you", "what are you", "your name")) -> {
//                "🤖 **About Me**\n\n" +
//                        "I'm an AI Assistant powered by advanced AI technology!\n\n" +
//                        "I can help you with:\n" +
//                        "• Understanding app features\n" +
//                        "• Answering general questions\n" +
//                        "• Having conversations\n" +
//                        "• Providing information\n\n" +
//                        "I'm here to make your experience better!"
//            }
//
//            // What can you do / Features
//            containsAny(message, listOf("what can you do", "your features", "your capabilities", "tell me about yourself")) -> {
//                "⭐ **What I Can Do**\n\n" +
//                        "📱 **App Features:**\n" +
//                        "• Login System - Secure authentication\n" +
//                        "• SOS Emergency Alert\n" +
//                        "• Location Sharing\n" +
//                        "• Nearby Safety Locations\n" +
//                        "• Map Integrated\n" +
//                        "• Phamacy, Policestation and Hospital Loacator\n\n" +
//                        "💬 **AI Conversations:**\n" +
//                        "• Answer any question\n" +
//                        "• Help with information\n" +
//                        "• Provide suggestions\n" +
//                        "• Have friendly chats\n\n" +
//                        "What would you like to explore?"
//            }
//
//            // App features specifically
//            containsAny(message, listOf("app feature", "features of app", "what does app do")) -> {
//                "📱 **$appName Features**\n\n" +
//                        "🚨 **SOS Emergency Alert**\n\n" +
//                        "The SOS button is your quick emergency response:\n\n" +
//                        "• Press the SOS button to send instant alerts\n" +
//                        "• Your current location is automatically shared\n" +
//                        "• Emergency messages are sent to all your saved contacts\n" +
//                        "• Works even in low network areas\n\n" +
//                        "💡 Tip: Add trusted contacts in Settings for faster emergency response!"
//            }
//
//            // How to use
//            containsAny(message, listOf("how to use", "how does it work", "guide", "tutorial")) -> {
//                "📖 **How to Use the App**\n\n" +
//                        "**Step 1: Sign Up/Login**\n" +
//                        "→ Create account or login\n\n" +
//                        "**Step 2: Set Up Your Profile**\n" +
//                        "→ Add your preferences\n\n" +
//                        "**Step 3: Start Tracking**\n" +
//                        "→ Begin logging your activities\n\n" +
//                        "**Step 4: View Insights**\n" +
//                        "→ Check your progress and analytics\n\n" +
//                        "**Step 5: Customize**\n" +
//                        "→ Adjust settings to your needs\n\n" +
//                        "Need help with something specific?"
//            }
//
//            // Thank you
//            containsAny(message, listOf("thank", "thanks")) -> {
//                "You're welcome! 😊 Feel free to ask me anything anytime!"
//            }
//
//            // Default for app-related queries
//            else -> {
//                "I can help you with:\n\n" +
//                        "• App features and how to use them\n" +
//                        "• General questions and information\n" +
//                        "• Tips and suggestions\n\n" +
//                        "What would you like to know?"
//            }
//        }
//    }
//
//    private fun containsAny(text: String, keywords: List<String>): Boolean {
//        return keywords.any { text.contains(it) }
//    }
//}