# Oxygen Voice Assistant - README

## Ye code kya karta hai
- `SetupActivity` - pehli baar khulti hai, permissions maangti hai, service start karti hai, phir apna icon home screen se HIDE kar deti hai.
- `OxygenService` - background me continuous chalta hai, "oxygen" wake word sunta hai, sunte hi Dynamic Island dikhata hai.
- `DynamicIsland` - iPhone jaisa transparent pill overlay, animation ke sath show/hide hota hai.
- `CommandProcessor` - torch on/off, app open, time batana, reminder set karna, wifi/data panel kholna.
- `Jokes` - har normal command ke baad ek joke sunata hai (jaisa tumne bola tha).

## Zaroori Limitations (ye samajhna important hai)
1. **Mobile Data on/off**: Android 10+ (Q) se koi bhi normal app (bina root) directly mobile data on/off nahi kar sakti - ye Google ki security restriction hai. Isliye code seedha Wifi/Network settings panel khol deta hai jaha 1 tap me on/off ho jata hai. Root phone pe hi full auto-control possible hai.
2. **Battery Optimization**: Xiaomi/Vivo/Oppo/Realme jaise phones apne aap background apps ko band kar dete hain. Install karne ke baad Settings > Battery > Oxygen > "No restriction" / "Autostart allow" zaroor karo, warna kuch der baad wake-word sunna band ho jayega.
3. **True always-on low-power wake word**: is code me Android ka built-in SpeechRecognizer use hua hai (free hai lekin thoda battery leta hai aur internet chahiye kabhi kabhi). Agar tumhe Google Assistant jaisa bilkul offline/low-power wake word chahiye, to Picovoice Porcupine SDK (free tier hai, apna keyword "Hey Oxygen" train kar sakte ho: https://picovoice.ai/) is service ke andar plug kar sakte ho - structure already taiyar hai bas `startListeningLoop()` ko Porcupine ke callback se replace karna hoga.
4. **Icon hide**: Android me app ko bilkul "invisible/system app" jaisa banana bina root ke possible nahi - lekin humne jo kiya (launcher activity disable karna) wahi standard tarika hai jo asli apps bhi use karte hain. Agar kabhi wapas icon chahiye ho to ADB se:
   `adb shell pm enable com.oxygen.assistant/.SetupActivity`

## Permissions jo manually bhi allow karni padengi
- Microphone
- "Draw over other apps" (overlay)
- Notification
- Battery: "no restriction"
