# Configuração de compilação

| Item | Valor |
|---|---|
| Android Gradle Plugin | 8.13.2 |
| Gradle | 8.13 |
| JDK | 17 |
| compileSdk | 36 |
| targetSdk | 36 |
| minSdk | 23 (Android 6.0) |
| Application ID | com.corretrilhos.byvictorjv |

O workflow usa `gradle/actions/setup-gradle` para provisionar Gradle e `android-actions/setup-android` para instalar o SDK necessário. Não depende de um `gradle-wrapper.jar` armazenado no ZIP.
