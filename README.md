# WhatsThatLink?

> **Before you click, know the risk.**

WhatsThatLink? is an Android cybersecurity prototype designed to help protect users from phishing links received through WhatsApp.

It detects URLs appearing in WhatsApp notifications, analyzes the detected URL using a Random Forest machine-learning model and VirusTotal threat intelligence, and provides a risk assessment to help the user decide whether they should open the link.

---

## The Problem

Phishing attacks frequently rely on malicious links sent through messaging platforms.

WhatsApp is particularly important in this context because users regularly receive links through conversations and notifications, and a malicious URL can appear legitimate at first glance.

A user may therefore click a link before having an opportunity to assess whether it is trustworthy.

Existing security tools may require users to manually copy and scan a URL, which introduces friction and relies on the user remembering to perform the check.

**WhatsThatLink? addresses this problem by detecting URLs directly from WhatsApp notifications and analyzing them automatically before the user opens the link.**

---

## Existing Systems and the Gap

Several existing approaches can help users identify suspicious or malicious links, but they generally require the user to take an action after receiving the link.

### Manual URL Scanners

Tools such as VirusTotal allow users to submit a URL and receive information about whether security engines have flagged it.

However, the user must first **copy the URL, open the scanner, paste the URL, and initiate the scan**. This creates friction and relies on the user recognizing that a link may be dangerous.

### Browser and Search-Engine Security Warnings

Modern browsers and search engines can warn users about known malicious or deceptive websites.

However, these protections generally become relevant when the user is **opening or navigating to the URL**, rather than proactively analyzing the link when it first arrives in a WhatsApp notification.

### Security Chatbots and AI Assistants

Security-focused chatbots and AI assistants can help users analyze suspicious links when a user manually provides the URL or asks whether a link is safe.

While useful, this still requires the user to **copy, paste, or submit the link themselves**. It does not automatically monitor incoming WhatsApp notifications for URLs.

### Threat-Intelligence Services

Services such as VirusTotal provide valuable threat intelligence by checking URLs against multiple security engines and databases.

WhatsThatLink? uses VirusTotal as one of its intelligence sources rather than replacing it. The difference is that WhatsThatLink? initiates the analysis automatically when a URL is detected in a WhatsApp notification.

---

## What Makes WhatsThatLink? Different?

WhatsThatLink? focuses on **proactive, WhatsApp-specific URL detection**.

Instead of requiring the user to manually submit a link, the system detects and analyzes URLs automatically when they appear in WhatsApp notifications.

### Existing approach

```text
Receive link
     |
     v
Recognize it may be suspicious
     |
     v
Copy URL
     |
     v
Open scanner / chatbot
     |
     v
Paste URL
     |
     v
Request analysis
```

### WhatsThatLink?

```text
WhatsApp notification
        |
        v
URL automatically detected
        |
        v
URL automatically analyzed
        |
        +----------------------+
        |                      |
        v                      v
Random Forest            VirusTotal
        |                      |
        +----------+-----------+
                   |
                   v
             Risk Assessment
                   |
                   v
          Warning notification
```

The key difference is **where the security check happens in the user's workflow**.

WhatsThatLink? is designed to intervene **between receiving a WhatsApp notification and clicking the link**, rather than requiring the user to manually submit the URL after receiving it.

WhatsThatLink? specifically:

- Monitors **WhatsApp notifications only**
- Extracts URLs automatically from those notifications
- Ignores notifications from other applications
- Combines machine-learning classification with VirusTotal threat intelligence
- Provides a risk assessment before the user opens the link
- Does not automatically open detected URLs
- Keeps the final decision with the user

The goal is not to replace existing security services, but to provide an **additional proactive layer of protection at the point where a potentially malicious link is first received**.

---

## Our Solution

WhatsThatLink? acts as an additional layer of protection between receiving a WhatsApp message and clicking its URL.

When a WhatsApp notification containing a URL is received:

1. WhatsThatLink? detects the WhatsApp notification.
2. It extracts the URL.
3. The URL is sent to the analysis backend.
4. A Random Forest phishing classifier evaluates the URL.
5. VirusTotal provides an additional threat-intelligence signal.
6. The results are combined into a risk assessment.
7. WhatsThatLink? presents a warning to the user.

**The application does not automatically open detected URLs.**

The user remains in control of whether they proceed.

---

## Important Privacy and Scope

WhatsThatLink? is specifically designed to monitor **WhatsApp notifications only**.

### It does:

- Read notifications generated by WhatsApp
- Look for URLs inside those WhatsApp notifications
- Analyze detected URLs

### It does NOT:

- Read notifications from Instagram
- Read notifications from Telegram
- Read SMS notifications
- Read Gmail notifications
- Read notifications from other applications
- Read unrelated notifications
- Automatically open detected links

The notification listener filters for the WhatsApp package, meaning notifications from other applications are ignored.

The application only acts when a URL is detected in a WhatsApp notification.

---

## How It Works

WhatsThatLink? receives a WhatsApp notification containing a URL. The notification listener detects the WhatsApp notification and extracts the URL. The URL is then sent to the Flask backend, where it is analyzed by the Random Forest model and checked against VirusTotal. The results are used to generate a risk assessment, which is returned to the Android application and shown to the user as a warning.

```text
WhatsApp
    |
    v
WhatsApp Notification
    |
    v
WhatsThatLink?
Notification Listener
    |
    v
URL Extraction
    |
    v
Flask REST API
    |
    +------------------+
    |                  |
    v                  v
Random Forest       VirusTotal
Classifier          API
    |                  |
    +--------+---------+
             |
             v
      Risk Assessment
             |
             v
      User Warning
```

---

## Key Features

- WhatsApp notification URL detection
- URL feature extraction
- Random Forest phishing classification
- Phishing probability score
- Legitimate probability score
- VirusTotal threat intelligence
- HIGH / MEDIUM / LOW risk classification
- Warning notifications
- Scan history
- Android user interface
- Error handling when the analysis service is unavailable
- Does not automatically open suspicious links

---

## Machine Learning

The phishing detection model was trained on **235,795 URL samples**.

The model uses URL characteristics including:

- URL length
- Domain length
- Number of subdomains
- Number of digits
- Digit ratio
- Number of letters
- Letter ratio
- TLD length
- Special characters
- Obfuscation characteristics
- Whether the domain is an IP address

### Model

**Random Forest Classifier**

### Test Results

| Metric | Score |
| ------ | ----- |
| Accuracy | 98.84% |
| Precision | 99.33% |
| Recall | 97.94% |
| F1 Score | 98.63% |

### Confusion Matrix

```text
                 Predicted
              Phishing  Legitimate

Phishing        19,774      415
Legitimate         133   26,837
```

---

## VirusTotal Integration

The machine-learning model is supplemented with VirusTotal threat intelligence.

VirusTotal results can provide information such as:

- Malicious detections
- Suspicious detections
- Harmless detections
- Undetected results

Example response:

```json
{
    "prediction": "PHISHING",
    "phishing_probability": 0.995,
    "legitimate_probability": 0.005,
    "risk": "HIGH"
}
```

VirusTotal credentials are kept on the backend and are not included in the Android application.

---

## Technology Stack

### Android

- Kotlin
- Jetpack Compose
- Android NotificationListenerService

### Backend

- Python
- Flask
- pandas
- scikit-learn
- joblib

### Machine Learning

- Random Forest
- URL-based feature extraction

### Threat Intelligence

- VirusTotal API

---

## Prototype Status

**WhatsThatLink? is currently a working prototype/demo and is not deployed as a public production service.**

The prototype consists of:

- An Android application
- A Flask backend
- A trained Random Forest model
- VirusTotal integration
- A demo video contained in the repo

Because the backend is currently running in a development environment rather than a public cloud deployment, the APK is intended primarily for prototype evaluation and demonstration.

A full public deployment is planned as a future improvement.

---

## How Judges Can Evaluate the Project

### Option 1 — Watch the Demo Video

A complete demonstration video is provided with the submission.

The video demonstrates:

1. A WhatsApp message containing a URL
2. WhatsThatLink? detecting the URL
3. The URL being analyzed
4. Machine-learning phishing detection
5. VirusTotal threat intelligence
6. Risk classification
7. The resulting warning shown to the user

### Demo Video

**[Watch the WhatsThatLink? Demo](INSERT_VIDEO_LINK_HERE)**

### Option 2 — Install the Android Prototype

The Android prototype APK can be provided as part of the submission.

#### Requirements

- Android device
- WhatsApp
- Notification Access enabled for WhatsThatLink?
- Access to the prototype backend during testing

#### Installation

1. Download the `WhatsThatLink.apk` file from the project submission.
2. Install the APK on an Android device.
3. Open WhatsThatLink?.
4. Grant **Notification Access** when prompted.
5. Ensure the Android device can communicate with the prototype backend.
6. Open WhatsApp.
7. Receive a message containing a URL.
8. WhatsThatLink? detects the URL and performs the analysis.
9. View the resulting risk assessment.

> **Note:** Because this is a prototype and the backend is not publicly deployed, the APK does not currently function as a completely standalone internet service. The demo video therefore provides the most straightforward way for judges to evaluate the complete system.

---

## Suggested Test

For demonstration purposes, a URL can be sent through a WhatsApp message and allowed to trigger the detection process.

Example:

```text
https://example.com/login?id=12345
```

**Do not open suspicious URLs simply for testing.**

The purpose of the test is to demonstrate URL detection and analysis, not to visit the website.

---

## Architecture

```text
+---------------------+
|      WhatsApp       |
|     Notification    |
+----------+----------+
           |
           v
+----------------------+
| WhatsThatLink Android|
| NotificationListener |
+----------+-----------+
           |
           | URL
           v
+---------------------+
|    Flask REST API   |
+----------+----------+
           |
      +----+----+
      |         |
      v         v
+----------+ +------------+
| Random   | | VirusTotal |
| Forest   | |    API     |
+----+-----+ +-----+------+
     |             |
     +------+------+
            |
            v
   +-----------------+
   | Risk Assessment |
   +--------+--------+
            |
            v
   +-----------------+
   | User Warning    |
   +-----------------+
```

---

## Project Structure

```text
WhatsThatLink/
|
+-- android/
|   +-- app/
|   +-- gradle/
|   +-- build.gradle.kts
|   +-- settings.gradle.kts
|
+-- backend/
|   +-- src/
|   |   +-- api.py
|   |   +-- predict.py
|   |   +-- features.py
|   |
|   +-- model/
|   |   +-- phishing_model.joblib
|   |
|   +-- requirements.txt
|
+-- demo/
|   +-- WhatsThatLink-demo.mp4
|
+-- README.md
+-- .gitignore
```

---

## Security Considerations

WhatsThatLink? follows several security principles:

- Detected URLs are not automatically opened.
- VirusTotal API credentials remain on the backend.
- API credentials are not included in the Android application.
- Local configuration and secrets should not be committed to GitHub.
- The application only processes WhatsApp notifications.
- Notifications from other applications are ignored.
- The application only acts on URLs detected in WhatsApp notifications.

A LOW risk result does not guarantee that a URL is completely safe.

---

## Limitations

- The current version is a prototype and is not publicly deployed.
- The prototype backend may require local network connectivity during testing.
- Model performance depends on the characteristics represented in the training dataset.
- VirusTotal results depend on available threat intelligence.
- No machine-learning classifier can guarantee detection of every malicious URL.
- Network connectivity can affect real-time analysis.

---

## Future Improvements

Future versions could include:

- Public cloud deployment
- HTTPS communication
- Additional threat-intelligence providers
- Domain reputation analysis
- Domain age checking
- SSL certificate analysis
- Blacklist checking
- On-device machine-learning inference
- Support for additional messaging platforms
- Automated model retraining
- Improved phishing detection models

---

## Disclaimer

WhatsThatLink? is a cybersecurity research and demonstration prototype.

It is designed to assist users in identifying potentially suspicious URLs and should not be treated as an absolute guarantee of website safety.

---

## Author

**Hawatif Abdisalam**

BSc Computer Science  
University of Nairobi

---

## License

MIT License
