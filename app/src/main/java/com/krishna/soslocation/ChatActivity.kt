package com.krishna.soslocation

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var emptyStateText: TextView

    private val messageList = mutableListOf<ChatMessage>()
    private val chatbotAssistant = SafetyAppChatbot()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Safety Assistant 🤖"
            setDisplayHomeAsUpEnabled(true)
        }

        initializeViews()
        setupRecyclerView()
        setupClickListeners()

        // Add welcome message
        addBotMessage(chatbotAssistant.getWelcomeMessage())
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewMessages)
        messageInput = findViewById(R.id.editTextMessage)
        sendButton = findViewById(R.id.buttonSend)
        emptyStateText = findViewById(R.id.emptyStateText)
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(messageList)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }
    }

    private fun setupClickListeners() {
        sendButton.setOnClickListener {
            sendMessage()
        }

        messageInput.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }
    }

    private fun sendMessage() {
        val messageText = messageInput.text.toString().trim()

        if (messageText.isNotEmpty()) {
            // Hide empty state
            emptyStateText.visibility = View.GONE

            // Add user message
            val userMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                text = messageText,
                timestamp = System.currentTimeMillis(),
                isSentByMe = true,
                status = MessageStatus.SENT
            )

            addMessage(userMessage)
            messageInput.text.clear()

            // Get bot response
            recyclerView.postDelayed({
                val botResponse = chatbotAssistant.getResponse(messageText)
                addBotMessage(botResponse)
            }, 800)
        }
    }

    private fun addMessage(message: ChatMessage) {
        messageList.add(message)
        messageAdapter.notifyItemInserted(messageList.size - 1)
        recyclerView.smoothScrollToPosition(messageList.size - 1)
    }

    private fun addBotMessage(text: String) {
        val botMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = text,
            timestamp = System.currentTimeMillis(),
            isSentByMe = false,
            status = MessageStatus.DELIVERED
        )
        addMessage(botMessage)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

// Intelligent Chatbot for Women Safety App
class SafetyAppChatbot {

    private val appName = "ShieldUp"

    fun getWelcomeMessage(): String {
        return "👋 Hi! I'm your Safety Assistant.\n\n" +
                "I can help you understand how to use the $appName. Ask me anything about:\n\n" +
                "🚨 SOS Emergency Alert\n" +
                "📍 Location Sharing\n" +
                "🗺️ Nearby Safety Locations\n" +
                "📱 App Features\n\n" +
                "What would you like to know?"
    }

    fun getResponse(userMessage: String): String {
        val message = userMessage.lowercase().trim()

        return when {
            // Greetings
            containsAny(message, listOf("hi", "hello", "hey", "hola")) -> {
                "Hello! 👋 How can I help you today? Feel free to ask about any feature of the $appName!"
            }

            // SOS Feature
            containsAny(message, listOf("sos", "emergency", "alert", "panic", "danger", "help button")) -> {
                "🚨 **SOS Emergency Alert**\n\n" +
                        "The SOS button is your quick emergency response:\n\n" +
                        "• Press the SOS button to send instant alerts\n" +
                        "• Your current location is automatically shared\n" +
                        "• Emergency messages are sent to all your saved contacts\n" +
                        "• Works even in low network areas\n\n" +
                        "💡 Tip: Add trusted contacts in Settings for faster emergency response!"
            }

            // Location Sharing
            containsAny(message, listOf("location", "share location", "gps", "tracking", "where am i")) -> {
                "📍 **Location Sharing**\n\n" +
                        "Your safety through location:\n\n" +
                        "• Real-time GPS tracking\n" +
                        "• Instantly shares your exact location with saved contacts\n" +
                        "• Updates location continuously during emergency\n" +
                        "• Works with Google Maps for accurate positioning\n\n" +
                        "Your location is only shared when YOU activate the SOS!"
            }

            // Map Features
            containsAny(message, listOf("map", "nearby", "find", "hospital", "police", "medical", "pharmacy", "station")) -> {
                "🗺️ **Nearby Safety Locations**\n\n" +
                        "Find help around you:\n\n" +
                        "🏥 **Hospitals** - Nearest medical facilities\n" +
                        "👮 **Police Stations** - Law enforcement help\n" +
                        "💊 **Medical Stores** - Pharmacies for medicines\n\n" +
                        "• View all locations on interactive map\n" +
                        "• See distance from your current location\n" +
                        "• Tap any location for navigation\n" +
                        "• Opens Google Maps for directions\n\n" +
                        "Access the map from the main screen!"
            }

            // Contacts
            containsAny(message, listOf("contact", "save contact", "emergency contact", "trusted", "family", "friend")) -> {
                "📞 **Emergency Contacts**\n\n" +
                        "Save your trusted contacts:\n\n" +
                        "• Add family, friends, or trusted persons\n" +
                        "• They receive instant SOS alerts\n" +
                        "• Get your real-time location updates\n" +
                        "• Can be called directly in emergencies\n\n" +
                        "Add at least 3-5 emergency contacts for best safety coverage!"
            }

            // How to use app
            containsAny(message, listOf("how to use", "how does", "how it works", "guide", "tutorial", "instructions")) -> {
                "📱 **How to Use the App**\n\n" +
                        "**Step 1:** Add Emergency Contacts\n" +
                        "→ Go to Settings and save trusted contacts\n\n" +
                        "**Step 2:** Enable Permissions\n" +
                        "→ Allow location and SMS permissions\n\n" +
                        "**Step 3:** Use SOS Button\n" +
                        "→ Press when you need help\n\n" +
                        "**Step 4:** Explore Map\n" +
                        "→ Find nearby safety locations\n\n" +
                        "Always keep the app ready for quick access!"
            }

            // Privacy & Safety
            containsAny(message, listOf("privacy", "safe", "secure", "data", "information", "permission")) -> {
                "🔒 **Privacy & Security**\n\n" +
                        "Your safety and privacy matter:\n\n" +
                        "✅ Location shared ONLY during SOS\n" +
                        "✅ No data stored on external servers\n" +
                        "✅ Contacts stored locally on your device\n" +
                        "✅ No tracking when app is not in use\n" +
                        "✅ You control all permissions\n\n" +
                        "We prioritize your privacy while ensuring your safety!"
            }

            // Features Overview
            containsAny(message, listOf("features", "what can", "capabilities", "options", "functions", "what does")) -> {
                "⭐ **App Features**\n\n" +
                        "🚨 **SOS Emergency Alert**\n" +
                        "   → Quick panic button for instant help\n\n" +
                        "📍 **Live Location Sharing**\n" +
                        "   → Share real-time GPS coordinates\n\n" +
                        "🗺️ **Safety Map**\n" +
                        "   → Find hospitals, police stations, pharmacies\n\n" +
                        "📞 **Emergency Contacts**\n" +
                        "   → Save and manage trusted contacts\n\n" +
                        "💬 **Safety Assistant (Me!)**\n" +
                        "   → Get help and information anytime\n\n" +
                        "What would you like to know more about?"
            }

            // Battery/Power
            containsAny(message, listOf("battery", "power", "charging", "low battery")) -> {
                "🔋 **Battery & Performance**\n\n" +
                        "• App uses minimal battery power\n" +
                        "• GPS activated only when needed\n" +
                        "• Works efficiently even on low battery\n" +
                        "• SOS works in power-saving mode\n\n" +
                        "💡 Keep your phone charged when going out for extended periods!"
            }

            // Network/Internet
            containsAny(message, listOf("internet", "network", "offline", "wifi", "data", "connection")) -> {
                "📶 **Network Requirements**\n\n" +
                        "• SOS SMS works without internet\n" +
                        "• Location sharing needs GPS (no internet required)\n" +
                        "• Map features need internet connection\n" +
                        "• Basic emergency features work offline\n\n" +
                        "The app is designed to work in various network conditions!"
            }

            // Settings
            containsAny(message, listOf("setting", "configure", "setup", "customize", "preferences")) -> {
                "⚙️ **App Settings**\n\n" +
                        "Configure your safety preferences:\n\n" +
                        "• Add/Remove emergency contacts\n" +
                        "• Customize SOS message\n" +
                        "• Set emergency call preferences\n" +
                        "• Manage app permissions\n" +
                        "• Update personal information\n\n" +
                        "Access Settings from the main menu!"
            }

            // Thank you
            containsAny(message, listOf("thank", "thanks", "appreciate", "helpful")) -> {
                "You're welcome! 😊 I'm here to help keep you safe. If you have any other questions about the $appName, feel free to ask!\n\nStay safe! 💪"
            }

            // Goodbye
            containsAny(message, listOf("bye", "goodbye", "see you", "exit", "close")) -> {
                "Take care and stay safe! 🛡️ You can come back anytime if you need help with the app. Goodbye!"
            }

            // Specific "nearby" queries
            message.contains("nearby hospital") || message.contains("find hospital") -> {
                "🏥 To find nearby hospitals:\n\n1. Go to the Map section\n2. Tap the RED button (Hospital)\n3. See all nearby hospitals with distances\n4. Tap any hospital to get directions\n\nThe app shows hospitals within 5km radius!"
            }

            message.contains("nearby police") || message.contains("find police") -> {
                "👮 To find nearby police stations:\n\n1. Go to the Map section\n2. Tap the BLUE button (Police)\n3. See all nearby police stations\n4. Tap to navigate instantly\n\nQuick access to law enforcement when needed!"
            }

            message.contains("nearby medical") || message.contains("nearby pharmacy") -> {
                "💊 To find nearby pharmacies:\n\n1. Go to the Map section\n2. Tap the GREEN button (Pharmacy)\n3. See all nearby medical stores\n4. Get directions with one tap\n\nFind medicines and medical supplies nearby!"
            }

            // Default response for unclear queries
            else -> {
                "I'm here to help! 🤖 I can answer questions about:\n\n" +
                        "• How to use SOS emergency alert\n" +
                        "• Location sharing features\n" +
                        "• Finding nearby hospitals, police stations, pharmacies\n" +
                        "• App settings and privacy\n" +
                        "• Emergency contacts management\n\n" +
                        "Could you please be more specific? Or try asking in a different way!"
            }
        }
    }

    private fun containsAny(text: String, keywords: List<String>): Boolean {
        return keywords.any { text.contains(it) }
    }
}
