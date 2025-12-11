# 開発環境セットアップガイド

このドキュメントでは、macOSとWindows環境での開発環境構築の違いを説明します。

## � 重要: Windows ユーザーへ

**Windows では Git Bash の使用を強く推奨します！**

Git Bash を使用することで:
- ✅ macOS と同じコマンドが使える
- ✅ Unix スタイルのパス (`/c/Program Files/...`) が使える
- ✅ チーム全体でコマンドを統一できる
- ✅ トラブルシューティングが簡単

[Git for Windows](https://gitforwindows.org/) をインストールして Git Bash を使用してください。

## 📋 目次

- [共通の前提条件](#共通の前提条件)
- [macOS環境](#macos環境)
- [Windows環境](#windows環境)
- [プラットフォーム別の違い](#プラットフォーム別の違い)

---

## 共通の前提条件

### ✅ すべての環境で必要

| ツール | 用途 | 備考 |
|--------|------|------|
| **Docker Desktop** | LocalStack実行 | macOS/Windows両方必須 |
| **Node.js 18+** | フロントエンド開発 | macOS/Windows両方必須 |
| **Git** | バージョン管理 | macOS/Windows両方必須 |
| **Kiro IDE** または **VS Code** | 開発環境 | macOS/Windows両方必須 |

### ✅ IDE拡張機能（共通）

`.vscode/extensions.json`に定義された拡張機能は**すべての環境で推奨**:
- Extension Pack for Java
- Spring Boot Extension Pack
- Gradle for Java
- ESLint, Prettier
- GitLens, SonarLint

---

## macOS環境

### 1. Java 21のインストール

**方法1: SDKMAN（推奨）**
```bash
# SDKMANのインストール
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Java 21のインストール
sdk install java 21.0.9-amzn

# Gradleのインストール
sdk install gradle 8.10.2
```

**方法2: Homebrew**
```bash
brew install openjdk@21
echo 'export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home' >> ~/.zshrc
source ~/.zshrc
```

### 2. Gradle Wrapperの生成

```bash
cd backend
gradle wrapper
chmod +x gradlew
```

### 3. 設定ファイルの修正

`backend/gradle.properties`を確認:
```properties
# macOSでは以下の行をコメントアウトまたは削除
# org.gradle.java.home=C:\\Program Files\\Java\\jdk-23
```

### 4. 実行コマンド

```bash
# バックエンド実行
cd backend
./gradlew bootRun --args='--spring.profiles.active=local'

# フロントエンド実行
cd frontend
npm install
npm run dev

# LocalStack実行
cd infrastructure
docker-compose up -d
```

---

## Windows環境

### 前提: Git Bashの使用を推奨

Windowsでは**Git Bash**の使用を推奨します。Git Bashを使用することで、macOSと同じコマンドが使えます。

- [Git for Windows](https://gitforwindows.org/)をインストール
- Git Bashを起動して以下のコマンドを実行

### 1. Java 21のインストール

**方法1: Oracle JDK（推奨）**
1. [Oracle JDK 21](https://www.oracle.com/java/technologies/downloads/#java21)をダウンロード
2. インストーラーを実行
3. 環境変数`JAVA_HOME`を設定:
   - システム環境変数に追加: `C:\Program Files\Java\jdk-21`
   - Git Bashの場合、`~/.bashrc`に追加:
   ```bash
   export JAVA_HOME="/c/Program Files/Java/jdk-21"
   export PATH="$JAVA_HOME/bin:$PATH"
   ```

**方法2: Chocolatey（PowerShell）**
```powershell
choco install openjdk21
```

### 2. Gradle Wrapperの確認

リポジトリに`gradlew.bat`が含まれているため、**生成不要**です。

### 3. 設定ファイルの確認

`backend/gradle.properties`を確認:
```properties
# Windowsの場合、Java Homeを設定（オプション）
org.gradle.java.home=C:\\Program Files\\Java\\jdk-21
```

### 4. 実行コマンド（Git Bash）

```bash
# バックエンド実行
cd backend
./gradlew.bat bootRun --args='--spring.profiles.active=local'

# フロントエンド実行
cd frontend
npm install
npm run dev

# LocalStack実行
cd infrastructure
docker-compose up -d
```

**注意**: Git Bashでは`./gradlew.bat`のように`./`を付けて実行します。

---

## プラットフォーム別の違い

### 🔧 必須の違い

| 項目 | macOS | Windows (Git Bash) | 理由 |
|------|-------|-------------------|------|
| **Gradleスクリプト** | `./gradlew` | `./gradlew.bat` | シェルスクリプト vs バッチファイル |
| **Gradle Wrapper生成** | ✅ 必要 | ❌ 不要 | リポジトリに`gradlew.bat`のみ存在 |
| **実行権限付与** | ✅ 必要 (`chmod +x`) | ❌ 不要 | Git Bashでは不要 |
| **Java Home設定** | ❌ 不要（SDKMAN自動） | ⚠️ 推奨 | 環境変数設定 |

### 📝 オプションの違い

| 項目 | macOS | Windows (Git Bash) | 備考 |
|------|-------|-------------------|------|
| **gradle.properties** | Java Home削除推奨 | Java Home設定推奨 | 環境による |
| **パッケージマネージャー** | Homebrew/SDKMAN | Chocolatey/Scoop | どちらも任意 |
| **シェル** | zsh/bash | Git Bash (bash) | **Git Bash推奨でコマンド統一** |

### ✅ 共通（プラットフォーム非依存）

以下は**すべての環境で同じ**:

| 項目 | 説明 |
|------|------|
| **`.vscode/` 設定** | launch.json, settings.json, extensions.json |
| **Docker Compose** | `docker-compose up -d` |
| **npm コマンド** | `npm install`, `npm run dev` |
| **IDE デバッグ** | Run and Debug パネルの使用方法 |
| **ポート番号** | Backend: 8080, Frontend: 3000, LocalStack: 4566 |
| **環境変数** | `spring.profiles.active=local` |

---

## 🚀 クイックスタート

### macOS

```bash
# 1. Java & Gradle
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 21.0.9-amzn
sdk install gradle 8.10.2

# 2. Gradle Wrapper生成
cd backend && gradle wrapper && chmod +x gradlew && cd ..

# 3. 起動
cd infrastructure && docker-compose up -d && cd ..
cd backend && ./gradlew bootRun &
cd frontend && npm install && npm run dev
```

### Windows (Git Bash)

```bash
# 1. Java インストール（Oracle JDKを手動インストール）
# https://www.oracle.com/java/technologies/downloads/#java21

# 2. 環境変数設定（~/.bashrcに追加）
echo 'export JAVA_HOME="/c/Program Files/Java/jdk-21"' >> ~/.bashrc
echo 'export PATH="$JAVA_HOME/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc

# 3. 起動
cd infrastructure && docker-compose up -d && cd ..
cd backend && ./gradlew.bat bootRun &
cd frontend && npm install && npm run dev
```

**注意**: Windows (PowerShell/cmd)を使用する場合は`gradlew.bat`を直接実行してください。

---

## 🐛 トラブルシューティング

### macOS

**問題**: `./gradlew: Permission denied`
```bash
chmod +x backend/gradlew
```

**問題**: Java not found
```bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk use java 21.0.9-amzn
```

### Windows (Git Bash)

**問題**: `./gradlew.bat: Permission denied`
```bash
# Git Bashでは通常発生しないが、発生した場合
chmod +x backend/gradlew.bat
```

**問題**: JAVA_HOME not set
```bash
# ~/.bashrcに追加
echo 'export JAVA_HOME="/c/Program Files/Java/jdk-21"' >> ~/.bashrc
echo 'export PATH="$JAVA_HOME/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc

# 確認
echo $JAVA_HOME
java -version
```

**問題**: PowerShell/cmdを使用している場合
```bash
# Git Bashに切り替えることを推奨
# または、PowerShellの場合:
# gradlew.bat bootRun
```

---

## 📚 参考リンク

- [SDKMAN (macOS)](https://sdkman.io/)
- [Chocolatey (Windows)](https://chocolatey.org/)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Node.js](https://nodejs.org/)

---

## ✨ まとめ

| 作業 | macOS | Windows (Git Bash) | 共通 |
|------|-------|-------------------|------|
| Java インストール | SDKMAN推奨 | Oracle JDK推奨 | - |
| Gradle Wrapper | 生成必要 | 不要 | - |
| 実行コマンド | `./gradlew` | `./gradlew.bat` | **Git Bashでほぼ同じ** |
| IDE設定 | - | - | ✅ 完全に共通 |
| Docker | - | - | ✅ 完全に共通 |
| npm | - | - | ✅ 完全に共通 |

**重要**: 
- `.vscode/`の設定ファイルは**すべての環境で共通**なので、チーム全体で同じ開発体験が得られます！
- **Windows では Git Bash を使用することで、macOS とほぼ同じコマンドで開発できます！**
