$env:JAVA_HOME=(Get-Command java).Path -replace '\\bin\\java.exe$',''; .\mvnw test
