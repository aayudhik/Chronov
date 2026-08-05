import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

replacement = """
                        val isPinEnabled = appContainer.privacyManager.isPinEnabled.collectAsState().value
                        val isBiometricEnabled = appContainer.privacyManager.isBiometricEnabled.collectAsState().value
                        
                        val startDest = if (isOnboardingCompleted == false) {
                            "onboarding1"
                        } else if (!isSignedIn!!) {
                            "auth_landing"
                        } else if (isPinEnabled || isBiometricEnabled) {
                            "lock_screen"
                        } else {
                            "home"
                        }
                        ChronovaScaffold(startDestination = startDest)
"""

content = re.sub(r'val startDest = if \(isOnboardingCompleted == false\) \{.*?\n                        \} else \{.*?\n                        \}\n                        ChronovaScaffold\(startDestination = startDest\)', replacement.strip(), content, flags=re.DOTALL)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
