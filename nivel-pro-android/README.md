# Nível Pro — Nivelador Digital Vertical (Android Nativo)

Aplicativo Android nativo em **Kotlin + Jetpack Compose** projetado para verificação de prumo vertical (postes, tubos, paredes e estruturas metálicas), compatível com **Xiaomi Poco X7 Pro** (Android 15 / HyperOS 2, `compileSdk 35`, `minSdk 26`).

## Como gerar o APK instalável

### Opção 1: Via Android Studio (Recomendado)
1. Abra o **Android Studio** (versão Ladybug 2024.2+ ou mais recente).
2. Clique em **Open** e selecione a pasta `nivel-pro-android`.
3. Aguarde o **Gradle Sync** concluir (o Android Studio baixará o Gradle 8.9 e as dependências do Jetpack Compose automaticamente).
4. Conecte seu **Xiaomi Poco X7 Pro** via cabo USB (com **Depuração USB** ativada nas Opções do Desenvolvedor) e clique em **Run** (`Shift + F10`) para instalar direto, **OU**:
5. Para gerar o arquivo `.apk` instalável:
   - Menu superior: **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
   - Ao finalizar, clique em **locate** na notificação.
   - O arquivo estará em:
     `app/build/outputs/apk/debug/app-debug.apk` (ou `app/build/outputs/apk/release/app-release.apk` ao selecionar Build Variant `release`).

### Opção 2: Via Linha de Comando (Terminal)
Se possuir Java 17 (JDK 17) e Android SDK instalados localmente:

```bash
cd nivel-pro-android
gradle wrapper --gradle-version 8.9   # apenas se o gradlew ainda não estiver presente
./gradlew assembleDebug             # gera app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease           # gera app/build/outputs/apk/release/app-release.apk
./gradlew testDebugUnitTest         # executa os testes unitários de física em PlumbMathTest
```

## Instalação no Xiaomi Poco X7 Pro
1. Copie o `app-debug.apk` (ou `app-release.apk`) para o Poco X7 Pro.
2. Abra o arquivo pelo Gerenciador de Arquivos e autorize **Instalar apps desconhecidos** para o gerenciador.
3. Dica importante de uso em campo: a ilha de câmeras do Poco X7 Pro cria um pequeno ressalto na traseira; para apoiar em postes, tubos ou paredes, utilize preferencialmente uma das **bordas laterais planas** do aparelho ou realize a **Calibração de Zero** junto a um nível físico de referência.
