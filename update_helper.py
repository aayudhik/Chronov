import re

with open("app/src/main/java/com/example/data/local/DatabaseEncryptionHelper.kt", "r") as f:
    content = f.read()

content = content.replace("SQLiteDatabase.openDatabase(\n                originalFile.absolutePath,\n                \n                null,\n                SQLiteDatabase.OPEN_READONLY\n            ).close()",
"SQLiteDatabase.openDatabase(originalFile.absolutePath, String(passphrase), null, SQLiteDatabase.OPEN_READONLY).close()")
content = content.replace("SQLiteDatabase.openDatabase(originalFile.absolutePath, String(passphrase), null, SQLiteDatabase.OPEN_READONLY).close()", 
"SQLiteDatabase.openDatabase(originalFile.absolutePath, String(passphrase), null, SQLiteDatabase.OPEN_READONLY).close()")

with open("app/src/main/java/com/example/data/local/DatabaseEncryptionHelper.kt", "w") as f:
    f.write(content)
