import re
with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()
if "SplashRoute" in content:
    print("Found SplashRoute")
