import os
import glob

replacements = {
    "GreenPrimary": "MaterialTheme.colorScheme.primary",
    "DarkGreen": "MaterialTheme.colorScheme.onPrimaryContainer",
    "GreenAccent": "MaterialTheme.colorScheme.primaryContainer",
    "OrangeAccent": "MaterialTheme.colorScheme.tertiary",
    "BackgroundSoft": "MaterialTheme.colorScheme.background",
    "CardWhite": "MaterialTheme.colorScheme.surface",
    "TextPrimary": "MaterialTheme.colorScheme.onSurface",
    "TextSecondary": "MaterialTheme.colorScheme.onSurfaceVariant",
}

files = glob.glob('app/src/main/java/com/example/*.kt')
for filepath in files:
    with open(filepath, 'r') as file:
        content = file.read()
    
    # We must be careful not to replace it if it's imported, but removing imports is also good.
    # We can just remove the imports entirely.
    for k in replacements.keys():
        content = content.replace(f"import com.example.ui.theme.{k}\n", "")
    
    for k, v in replacements.items():
        # Match as whole word if possible, but simple replace works for these exact variable names
        # since they are unique.
        import re
        content = re.sub(rf'\b{k}\b', v, content)
        
    with open(filepath, 'w') as file:
        file.write(content)

print("Colors Replaced")
