# WomenSafetyApp

# 🛡️ RakshaSetu - Women Safety Application

<div align="center">

![RakshaSetu](https://drive.google.com/file/d/12zrfGauL3WZOpbLCtZHfKDC9quzhkVFS/view)
[![Android](https://drive.google.com/file/d/1y9FroBja9XEA2Tlz-apBNMW6WK7R3tk0/view)
[![API](https://drive.google.com/file/d/1AxCMxzh1M5rM8b_gAhBoaxD1vKmfvtLj/view)

**Empowering Safety Through Technology**

[Features](#-features) • [Demo](#-demo) • [Installation](#-installation) • [Usage](#-usage) • [Tech Stack](#-tech-stack) • [Contributing](#-contributing)

</div>

---

## 📋 Table of Contents

- [About](#-about)
- [Features](#-features)
- [Demo](#-demo)
- [Screenshots](#-screenshots)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Usage](#-usage)
- [API Integration](#-api-integration)
- [Roadmap](#-roadmap)
- [Contributing](#-contributing)
- [License](#-license)
- [Contact](#-contact)
- [Acknowledgments](#-acknowledgments)

---

## 🎯 About

**RakshaSetu** is a comprehensive Android application designed to enhance women's safety through technology. The app provides emergency SOS alerts, real-time location tracking, nearby safety resource locator, and an AI-powered chatbot assistant.

### Problem Statement
- 1 in 3 women in India face safety concerns daily
- Delayed emergency response times
- Limited awareness of nearby safety resources
- Need for instant communication with trusted contacts

### Our Solution
RakshaSetu combines emergency response systems, GPS tracking, and AI assistance to create a comprehensive safety platform that's easy to use during high-stress situations.

---

## ✨ Features

### 🚨 **SOS Emergency Alert**
- One-tap emergency activation
- Instant SMS alerts to emergency contacts
- Automatic location sharing
- Works in low network conditions
- Background monitoring service

### 📍 **Real-Time Location Tracking**
- High-accuracy GPS tracking
- Continuous location updates during emergency
- Google Maps integration
- Privacy-focused (only active during SOS)
- Location history tracking

### 🗺️ **Interactive Safety Map**
- Find nearby hospitals (🏥)
- Locate police stations (👮)
- Search pharmacies (💊)
- Distance calculation
- One-tap navigation
- Filter by resource type

### 🤖 **AI-Powered Chat Assistant**
- **Dual-Mode Intelligence:**
  - Local bot for app-specific queries
  - Gemini AI for general questions
- Smart query routing
- Instant responses (<2 seconds)
- Natural language processing
- Context-aware conversations

### 📞 **Emergency Contact Management**
- Add/edit/remove contacts
- Contact verification
- Quick dial functionality
- Priority contact designation
- Secure local storage

### 🔒 **Privacy & Security**
- Location shared only during SOS
- No background tracking
- Encrypted local storage
- Secure API communication
- User-controlled permissions

---

## 🎥 Demo

### Video Demonstration
<!-- Add your demo video link or GIF here -->
![App Demo](link-to-demo-gif)

### Live APK
📱 [Download APK](link-to-apk-file)

---

## 📱 Screenshots

<div align="center">

| Home Screen | Safety Map | AI Chat | Emergency Contacts |
|-------------|------------|---------|-------------------|
| ![Home](https://drive.google.com/uc?export=view&id=12zrfGauL3WZOpbLCtZHfKDC9quzhkVFS) | ![Map](https://drive.google.com/uc?export=view&id=1AxCMxzh1M5rM8b_gAhBoaxD1vKmfvtLj) | ![Chat](https://drive.google.com/uc?export=view&id=1DZiq5IihPbcHjqMJjozknlHYIGmYh4aA) | ![Profile](https://drive.google.com/uc?export=view&id=1-FkWOjSstINOqdgtxk9gaAJuJBrSsXte) |

</div>

---

## 🛠️ Tech Stack

### Frontend
- **Language:** Kotlin
- **UI Framework:** Android XML, Material Design
- **Architecture:** MVVM (Model-View-ViewModel)
- **Async:** Kotlin Coroutines

### Backend & APIs
- **AI:** Google Gemini API 2.5 Flash
- **Maps:** Google Maps API, Places API
- **Location:** FusedLocationProviderClient
- **Communication:** Android SMS Manager

### Libraries & Dependencies

```gradle
dependencies {
    // Core Android
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    
    // UI Components
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    
    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3'
    
    // Google Services
    implementation 'com.google.android.gms:play-services-maps:18.2.0'
    implementation 'com.google.android.gms:play-services-location:21.0.1'
    
    // Networking
    implementation 'com.squareup.okhttp3:okhttp:4.11.0'
    
    // Database
    implementation 'androidx.room:room-runtime:2.6.1'
    kapt 'androidx.room:room-compiler:2.6.1'
}
```

---

## 🏗️ Architecture

### MVVM Pattern

```
┌─────────────────────────────────────────────────────┐
│                    Presentation Layer               │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────┐ │
│  │   Activity   │  │   Fragment   │  │  Adapter  │ │
│  └──────┬───────┘  └──────┬───────┘  └─────┬─────┘ │
│         │                 │                 │       │
│         └─────────────────┴─────────────────┘       │
└────────────────────────┬────────────────────────────┘
                         │
┌────────────────────────┴────────────────────────────┐
│                   ViewModel Layer                   │
│  ┌──────────────────────────────────────────────┐  │
│  │            LiveData / StateFlow              │  │
│  └──────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────┘
                         │
┌────────────────────────┴────────────────────────────┐
│                    Domain Layer                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────┐  │
│  │  Use Cases   │  │  Repository  │  │  Models  │  │
│  └──────────────┘  └──────────────┘  └──────────┘  │
└────────────────────────┬────────────────────────────┘
                         │
┌────────────────────────┴────────────────────────────┐
│                     Data Layer                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────┐  │
│  │   Local DB   │  │  Remote API  │  │  Prefs   │  │
│  └──────────────┘  └──────────────┘  └──────────┘  │
└─────────────────────────────────────────────────────┘
```

### Project Structure

```
com.krishna.soslocation/
├── ui/
│   ├── activities/
│   │   ├── MainActivity.kt
│   │   ├── ChatActivity.kt
│   │   └── MapActivity.kt
│   ├── fragments/
│   │   ├── HomeFragment.kt
│   │   └── ContactsFragment.kt
│   └── adapters/
│       ├── MessageAdapter.kt
│       └── ContactAdapter.kt
├── viewmodel/
│   ├── ChatViewModel.kt
│   └── MapViewModel.kt
├── data/
│   ├── repository/
│   │   ├── ContactRepository.kt
│   │   └── LocationRepository.kt
│   ├── local/
│   │   ├── database/
│   │   │   └── AppDatabase.kt
│   │   └── dao/
│   │       └── ContactDao.kt
│   └── remote/
│       └── GeminiApiService.kt
├── model/
│   ├── ChatMessage.kt
│   ├── Contact.kt
│   └── Location.kt
├── services/
│   ├── SOSService.kt
│   └── LocationService.kt
├── utils/
│   ├── Constants.kt
│   ├── Extensions.kt
│   └── PermissionHelper.kt
└── chatbot/
    └── AppInfoBot.kt
```

---

## 📥 Installation

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 26 (Android 8.0) or higher
- Kotlin 1.9.0+
- Google Maps API Key
- Gemini API Key

### Step 1: Clone the Repository

```bash
git clone https://github.com/krishna/rakshasetu.git
cd rakshasetu
```

### Step 2: Open in Android Studio

1. Open Android Studio
2. Click on **File > Open**
3. Navigate to the cloned repository
4. Click **OK**

### Step 3: Sync Gradle

Android Studio will automatically sync Gradle files. If not:
- Click on **File > Sync Project with Gradle Files**

### Step 4: Configure API Keys (See Configuration section)

### Step 5: Build and Run

```bash
# Using Gradle
./gradlew assembleDebug

# Or click the Run button in Android Studio
```

---

## ⚙️ Configuration

### 1. Google Maps API Key

Create a `local.properties` file in the root directory:

```properties
MAPS_API_KEY=your_google_maps_api_key_here
```

Add to `AndroidManifest.xml`:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="${MAPS_API_KEY}" />
```

**Get API Key:** [Google Cloud Console](https://console.cloud.google.com/)

### 2. Gemini API Key

In `ChatActivity.kt`:

```kotlin
private val GEMINI_API_KEY = "your_gemini_api_key_here"
```

**Get API Key:** [Google AI Studio](https://makersuite.google.com/app/apikey)

### 3. Firebase Configuration (Optional)

If using Firebase for authentication:

1. Download `google-services.json` from Firebase Console
2. Place it in the `app/` directory
3. Add Firebase SDK to `build.gradle`

### 4. Permissions

Ensure these permissions are in `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.READ_CONTACTS" />
<uses-permission android:name="android.permission.CALL_PHONE" />
<uses-permission android:name="android.permission.VIBRATE" />
```

---

## 🚀 Usage

### Basic Workflow

1. **First Time Setup**
   ```kotlin
   - Launch the app
   - Grant necessary permissions
   - Create account / Login
   - Add emergency contacts
   ```

2. **Using SOS Feature**
   ```kotlin
   - Press the large SOS button on home screen
   - Confirm emergency alert
   - SMS sent automatically to all contacts
   - Location shared continuously
   ```

3. **Finding Safety Resources**
   ```kotlin
   - Open Map screen
   - Select resource type (Hospital/Police/Pharmacy)
   - View nearby locations with distances
   - Tap location for navigation
   ```

4. **Using AI Chatbot**
   ```kotlin
   - Open Chat screen
   - Type your question
   - App-related queries → Instant response
   - General queries → AI-powered response
   ```

### Code Examples

#### Sending SOS Alert

```kotlin
class SOSService : Service() {
    fun sendSOSAlert(contacts: List<Contact>, location: Location) {
        val smsManager = SmsManager.getDefault()
        val message = "EMERGENCY! I need help. My location: ${location.latitude},${location.longitude}"
        
        contacts.forEach { contact ->
            try {
                smsManager.sendTextMessage(
                    contact.phoneNumber,
                    null,
                    message,
                    null,
                    null
                )
            } catch (e: Exception) {
                Log.e("SOS", "Failed to send SMS: ${e.message}")
            }
        }
    }
}
```

#### Getting Location

```kotlin
class LocationHelper(private val context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    
    @SuppressLint("MissingPermission")
    fun getCurrentLocation(callback: (Location?) -> Unit) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                callback(location)
            }
            .addOnFailureListener {
                callback(null)
            }
    }
}
```

#### AI Chatbot Query

```kotlin
suspend fun getGeminiResponse(prompt: String): String = withContext(Dispatchers.IO) {
    val url = URL("$GEMINI_API_URL?key=$GEMINI_API_KEY")
    val connection = url.openConnection() as HttpURLConnection
    
    connection.requestMethod = "POST"
    connection.setRequestProperty("Content-Type", "application/json")
    connection.doOutput = true
    
    val requestBody = JSONObject().apply {
        put("contents", JSONArray().apply {
            put(JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", prompt)
                    })
                })
            })
        })
    }
    
    connection.outputStream.use { it.write(requestBody.toString().toByteArray()) }
    
    val response = connection.inputStream.bufferedReader().readText()
    // Parse and return response
}
```

---

## 🔌 API Integration

### Gemini AI API

**Endpoint:** `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent`

**Request Format:**
```json
{
  "contents": [{
    "parts": [{
      "text": "Your query here"
    }]
  }],
  "generationConfig": {
    "temperature": 0.9,
    "topK": 40,
    "topP": 0.95,
    "maxOutputTokens": 2048
  }
}
```

**Response Format:**
```json
{
  "candidates": [{
    "content": {
      "parts": [{
        "text": "AI response here"
      }]
    }
  }]
}
```

### Google Maps API

**Place Search:**
```kotlin
val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
    "location=${lat},${lng}" +
    "&radius=5000" +
    "&type=hospital" +
    "&key=$MAPS_API_KEY"
```

---

## 🗺️ Roadmap

### Version 1.0 ✅ (Current)
- [x] SOS Emergency Alert
- [x] Real-time Location Tracking
- [x] Interactive Safety Map
- [x] AI Chatbot (Dual-mode)
- [x] Emergency Contact Management
- [x] Privacy & Security Features

### Version 1.1 🔄 (In Progress)
- [ ] Voice Command Integration
- [ ] Multi-language Support (Hindi, Tamil, Telugu)
- [ ] Dark Mode
- [ ] Widget for Quick SOS

### Version 2.0 🔮 (Planned)
- [ ] Safe Route Recommendations
- [ ] Incident Reporting
- [ ] Community Features
- [ ] Video Recording during Emergency
- [ ] Wearable Device Integration
- [ ] Offline Mode Enhancement

### Version 3.0 🚀 (Future)
- [ ] Government Integration
- [ ] IoT Device Connectivity
- [ ] Advanced AI Threat Detection
- [ ] Mental Health Support Resources

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

### How to Contribute

1. **Fork the Repository**
   ```bash
   git clone https://github.com/your-username/rakshasetu.git
   ```

2. **Create a Feature Branch**
   ```bash
   git checkout -b feature/AmazingFeature
   ```

3. **Commit Your Changes**
   ```bash
   git commit -m 'Add some AmazingFeature'
   ```

4. **Push to Branch**
   ```bash
   git push origin feature/AmazingFeature
   ```

5. **Open a Pull Request**

### Contribution Guidelines

- Follow Kotlin coding conventions
- Write clear commit messages
- Add comments for complex logic
- Update documentation as needed
- Test thoroughly before submitting PR
- One feature per pull request

### Code Style

```kotlin
// Use meaningful variable names
val emergencyContacts = getEmergencyContacts()

// Add comments for complex logic
// Calculate distance using Haversine formula
val distance = calculateDistance(lat1, lon1, lat2, lon2)

// Use proper indentation and spacing
class ChatActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
    }
}
```

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2024 Krishna

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

---

## 📞 Contact

**Developer:** Krishna

- 📧 Email: krishna@example.com
- 💼 LinkedIn: [linkedin.com/in/krishna-dev](https://linkedin.com/in/krishna-dev)
- 🐙 GitHub: [@krishna](https://github.com/krishna)
- 🌐 Website: [www.rakshasetu.com](https://www.rakshasetu.com)

**Project Link:** [https://github.com/krishna/rakshasetu](https://github.com/krishna/rakshasetu)

---

## 🙏 Acknowledgments

### Special Thanks To

- **Google AI Team** - For Gemini API access and documentation
- **Android Community** - For valuable resources and support
- **Beta Testers** - For feedback and bug reports
- **Open Source Contributors** - For libraries and tools

### Resources & Inspiration

- [Android Developer Documentation](https://developer.android.com/)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Material Design Guidelines](https://material.io/design)
- [Google AI Studio](https://makersuite.google.com/)
- Women Safety Organizations in India

### Libraries Used

- [Kotlin Coroutines](https://github.com/Kotlin/kotlinx.coroutines)
- [Google Play Services](https://developers.google.com/android/guides/overview)
- [Material Components](https://github.com/material-components/material-components-android)
- [OkHttp](https://square.github.io/okhttp/)

---

## 📊 Project Statistics

![GitHub stars](https://img.shields.io/github/stars/krishna/rakshasetu?style=social)
![GitHub forks](https://img.shields.io/github/forks/krishna/rakshasetu?style=social)
![GitHub issues](https://img.shields.io/github/issues/krishna/rakshasetu)
![GitHub pull requests](https://img.shields.io/github/issues-pr/krishna/rakshasetu)
![GitHub contributors](https://img.shields.io/github/contributors/krishna/rakshasetu)

---

## 📈 Activity

![Alt](https://repobeats.axiom.co/api/embed/your-repo-id.svg "Repobeats analytics image")

---

## ⭐ Star History

[![Star History Chart](https://api.star-history.com/svg?repos=krishna/rakshasetu&type=Date)](https://star-history.com/#krishna/rakshasetu&Date)

---

## 🌟 Show Your Support

If you like this project, please consider:

- ⭐ Starring the repository
- 🐛 Reporting bugs
- 💡 Suggesting new features
- 📢 Sharing with others
- 🤝 Contributing to the code

**Made with ❤️ in India**

---

<div align="center">

### "Your Safety, Our Priority"

**RakshaSetu - Empowering Safety Through Technology**

[⬆ Back to Top](#️-rakshasetu---women-safety-application)

</div>
