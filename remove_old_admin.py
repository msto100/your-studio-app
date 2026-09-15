import re

with open('app/src/main/java/com/example/ui/components/StudioComponents.kt', 'r') as f:
    text = f.read()

# I will replace the logoClickCount logic in StudioHeader
old_header_logic = """    var logoClickCount by remember { mutableIntStateOf(0) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showAdminModal by remember { mutableStateOf(false) }"""

new_header_logic = """    // Admin logic removed from header, moved to ProfileScreen"""
text = text.replace(old_header_logic, new_header_logic)

# Replace the clickable modifier on Row
old_row = """            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    logoClickCount++
                    if (logoClickCount >= 5) {
                        showPinDialog = true
                        logoClickCount = 0
                    }
                }
            ) {"""
new_row = """            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {"""
text = text.replace(old_row, new_row)


# I need to remove the pin dialog and SecretAdminModal calls from StudioHeader
dialogs_start = "    if (showPinDialog) {"
dialogs_end = "fun SecretAdminModal(onDismiss: () -> Unit) {"

# Try to find and replace everything from showPinDialog to SecretAdminModal including SecretAdminModal body
# Actually, it's easier to just strip the dialogs by exact match if possible, or use regex.
text = re.sub(r'    if \(showPinDialog\) \{.*?    \}\n\n    if \(showAdminModal\) \{.*?    \}\n\}\n\n@Composable\nfun SecretAdminModal.*$', '}\n', text, flags=re.DOTALL)


with open('app/src/main/java/com/example/ui/components/StudioComponents.kt', 'w') as f:
    f.write(text)

print("Removed old admin modal from StudioComponents")
