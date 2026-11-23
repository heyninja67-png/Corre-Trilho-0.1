# Corre Trilho — Android WebView

Aplicativo Android em tela cheia que abre o site **https://subwayrunblodvt.lovable.app/** dentro de um WebView.

## O que já está configurado

- **Nome do aplicativo:** Corre Trilho
- **Package / Application ID:** `com.corretrilhos.byvictorjv`
- **Tela de abertura:** animação própria inspirada na tela de carregamento enviada como referência
- **Marca:** usa a imagem de logo enviada no projeto (`logo_corretrilhos.jpg`)
- **Tempo da abertura:** 6 segundos; depois o aplicativo abre o site automaticamente
- **Tela cheia:** esconde as barras de status e navegação (modo imersivo)
- **WebView:** JavaScript, DOM Storage, cookies e aceleração de hardware habilitados
- **Botão voltar:** retorna para a página anterior do site antes de sair do aplicativo
- **Falha de conexão:** mostra uma tela simples com opção para tentar carregar novamente
- **GitHub Actions:** compila o APK automaticamente e publica o arquivo como artefato

## Como gerar o APK pelo GitHub

1. Extraia este ZIP.
2. Envie todos os arquivos e pastas para um repositório GitHub (a pasta `.github/workflows` precisa ser enviada também).
3. Abra a aba **Actions** do repositório e habilite os workflows, caso o GitHub solicite.
4. Faça um `push` para `main`/`master`, ou abra **Actions → Build Corre Trilho APK → Run workflow**.
5. Quando a execução terminar com sucesso, abra a execução e baixe o artefato **Corre-Trilho-APK**. Dentro dele estará `Corre-Trilho.apk`.

### Criar uma Release com link de APK

Depois de enviar o projeto ao GitHub, crie e envie uma tag:

```bash
git tag v1.0.0
git push origin v1.0.0
```

O workflow compila o APK e anexa `Corre-Trilho.apk` à Release dessa tag. Assim você terá uma página de Release com o arquivo para download.

## Compilação local

O workflow instala Gradle 8.13, JDK 17 e Android SDK API 36 automaticamente. Em um PC/ambiente com essas ferramentas instaladas, o comando é:

```bash
gradle --no-daemon :app:assembleDebug
```

O APK será criado em:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Observações importantes

- O aplicativo é um **WebView**: o jogo/site continua hospedado no Lovable. É necessária conexão com a internet para abrir o conteúdo.
- Alterações feitas no site publicado aparecem no aplicativo sem precisar recompilar o APK.
- O workflow gera um APK **Debug assinado com a chave de depuração automática do Android Gradle Plugin**, adequado para instalar e testar. Para publicação na Play Store, será necessário configurar uma chave de assinatura Release própria.
- O ZIP contém o projeto-fonte e o workflow; o APK é gerado pelo GitHub Actions após o envio do projeto.


## Versão 1.0.2

- Inicialização da tela de abertura antes da configuração do modo imersivo.
- WebView criado somente com o contexto da Activity.
- Tratamento de falhas na inicialização com mensagem visível na tela.
- Modo de tela cheia simplificado para maior compatibilidade entre versões do Android.
- Target SDK ajustado para 34.
