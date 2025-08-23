# Custom Accessibility in zMantra

## What is Accessibility in Android?
Accessibility in Android ensures that apps are usable by everyone, including people with disabilities such as **blindness, low vision, or motor impairments**.  
The most common built-in accessibility service is **TalkBack**, which provides spoken feedback and allows blind users to interact with the device using gestures.

📖 Learn more:  
- [Android Accessibility Service (Docs)](https://developer.android.com/guide/topics/ui/accessibility/service)  
- [GeeksforGeeks Tutorial on Accessibility Service](https://www.geeksforgeeks.org/android/how-to-create-an-accessibility-service-in-android-with-example/)

---

## What is Custom Accessibility?
A **Custom Accessibility Service** is when developers build their own accessibility layer on top of Android’s APIs to create an interaction style that better fits their app.  
Instead of relying only on TalkBack’s gestures, developers can override or simplify the interaction model.

---

## Why Custom Accessibility in zMantra?
In TalkBack, every action normally requires **two taps**:
1. **First tap** → Focus/select the item.  
2. **Second tap** → Activate it.  

This ensures blind users don’t accidentally trigger something.  
But in **game modes like Tap Mode**, this would make gameplay **slower and less engaging**.

### Our Custom Accessibility Approach:
- We designed a **custom accessibility service** for zMantra where:  
  - **Single tap** directly triggers the game action.  
  - TalkBack-style spoken feedback is still provided.  
  - This makes the gameplay **faster, more natural, and fun**, while still being accessible.  

---

## Example: Tap Mode
- **With TalkBack ON** → To record an answer, the user needs to tap twice.  
- **With Custom Accessibility ON** → Only one tap is needed to record the answer.  

This customization improves **usability and responsiveness** for blind players while keeping accessibility intact.

---

## Benefits of Custom Accessibility
- **Simplifies gameplay** → fewer steps, more direct interaction.  
- **Still accessible** → speech output and feedback remain.  
- **Inclusive design** → optimized for blind learners who want speed and efficiency.  

