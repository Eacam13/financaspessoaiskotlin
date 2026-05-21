# 💰 Finanças Pessoais

Aplicativo Android nativo desenvolvido em **Kotlin** para gerenciamento de finanças pessoais. Utilize esta ferramenta para acompanhar suas despesas, receitas e controlar melhor seu orçamento.

## 📋 Características

- ✅ Gerenciamento de receitas e despesas
- ✅ Interface moderna com Jetpack Compose
- ✅ Armazenamento local com Room Database
- ✅ Requisições HTTP com Retrofit
- ✅ Testes unitários e de UI automatizados
- ✅ Arquitetura escalável e testável

## 🛠️ Tecnologias

- **Linguagem**: Kotlin 100%
- **UI**: Jetpack Compose & Material Design 3
- **Banco de Dados**: Room Database
- **Networking**: Retrofit + OkHttp
- **Serialização**: Moshi
- **Corrotinas**: Kotlin Coroutines
- **Arquitetura**: MVVM com ViewModel
- **Testes**: JUnit, Roborazzi, Robolectric

## 📋 Requisitos

- Android Studio (versão recente)
- Android SDK 24+ (compileSdk 36)
- Java 11+

## 🚀 Como Executar Localmente

### 1. Abrir o Projeto
```bash
1. Abra o Android Studio
2. Clique em "Open" (Abrir)
3. Selecione o diretório do projeto
4. Aguarde o Android Studio sincronizar o Gradle
```

### 2. Configurar Variáveis de Ambiente

Crie um arquivo `.env` na raiz do projeto:

```env
# Adicione suas chaves de API conforme necessário
# Exemplo: GEMINI_API_KEY=sua_chave_aqui
```

Veja o arquivo `.env.example` para um template de configuração.

### 3. Configurar Build Local (Debug)

Se necessário, remova ou ajuste a configuração de assinatura de debug em `app/build.gradle.kts`:

```kotlin
// Se tiver problemas, pode remover ou comentar:
// signingConfig = signingConfigs.getByName("debugConfig")
```

### 4. Executar o App

```bash
# Em um emulador ou dispositivo físico conectado
1. Clique em "Run" (ou pressione Shift + F10)
2. Selecione o emulador ou dispositivo
3. Aguarde a compilação e instalação
```

## 📁 Estrutura do Projeto

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/aistudio/financaspessoais/
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   └── test/
│       └── Testes unitários
├── build.gradle.kts
└── ...
```

## 🧪 Testes

O projeto inclui testes automatizados:

```bash
# Executar testes unitários
./gradlew test

# Executar testes de UI com Compose
./gradlew connectedAndroidTest

# Gerar snapshots com Roborazzi
./gradlew testDebug
```

## 🔒 Configuração de Release

Para gerar um build de release, você precisará:

1. Configurar as variáveis de ambiente:
   - `KEYSTORE_PATH`: Caminho para o keystore
   - `STORE_PASSWORD`: Senha do keystore
   - `KEY_PASSWORD`: Senha da chave

2. Executar:
   ```bash
   ./gradlew assembleRelease
   ```

## 🤝 Contribuindo

Sinta-se livre para abrir issues e pull requests com melhorias!

## 📝 Licença

Este projeto é licenciado sob a licença MIT.

## 📞 Contato

Para dúvidas ou sugestões, abra uma [issue](https://github.com/Eacam13/financaspessoaiskotlin/issues).

---

**Desenvolvido com ❤️ em Kotlin**
