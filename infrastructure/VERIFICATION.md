# ローカル環境セットアップ検証ガイド

このガイドでは、ローカル開発環境が正しくセットアップされているか確認します。

## 前提条件の確認

### 1. Docker Desktopの起動

**Windows**:
1. スタートメニューから「Docker Desktop」を起動
2. タスクバーにDockerアイコンが表示され、「Docker Desktop is running」と表示されるまで待つ

**確認コマンド**:
```bash
docker --version
docker ps
```

期待される出力:
```
Docker version 28.1.1, build 4eba377
CONTAINER ID   IMAGE     COMMAND   CREATED   STATUS    PORTS     NAMES
```

## LocalStackのセットアップ

### 1. LocalStackの起動

```bash
cd infrastructure
docker-compose up -d
```

期待される出力:
```
[+] Running 2/2
 ✔ Network infrastructure_default  Created
 ✔ Container cooking-app-localstack  Started
```

### 2. LocalStackの起動確認

```bash
docker ps
```

期待される出力:
```
CONTAINER ID   IMAGE                          COMMAND                  STATUS         PORTS
xxxxx          localstack/localstack:latest   "docker-entrypoint.sh"   Up 10 seconds  0.0.0.0:4566->4566/tcp
```

### 3. LocalStackのログ確認

```bash
docker logs cooking-app-localstack
```

以下のメッセージが表示されればOK:
```
Ready.
```

### 4. LocalStackのヘルスチェック

```bash
curl http://localhost:4566/_localstack/health
```

期待される出力（JSON形式）:
```json
{
  "services": {
    "dynamodb": "running",
    "s3": "running",
    "cognito-idp": "running",
    "apigateway": "running"
  }
}
```

## AWSリソースの確認

### 1. DynamoDBテーブルの確認

**AWS CLIを使用**:
```bash
aws dynamodb list-tables --endpoint-url http://localhost:4566
```

期待される出力:
```json
{
  "TableNames": [
    "cooking-app-users-local",
    "cooking-app-recipes-local",
    "cooking-app-schedules-local",
    "cooking-app-shopping-lists-local",
    "cooking-app-reviews-local"
  ]
}
```

**PowerShellを使用**:
```powershell
Invoke-RestMethod -Uri "http://localhost:4566/dynamodb" -Method POST `
  -Headers @{"Content-Type"="application/x-amz-json-1.0"; "X-Amz-Target"="DynamoDB_20120810.ListTables"} `
  -Body '{}'
```

### 2. S3バケットの確認

```bash
aws s3 ls --endpoint-url http://localhost:4566
```

期待される出力:
```
2025-11-28 16:00:00 cooking-app-images-local
```

### 3. 特定のテーブル詳細を確認

```bash
aws dynamodb describe-table \
  --table-name cooking-app-users-local \
  --endpoint-url http://localhost:4566
```

## バックエンドの起動確認

### 1. ローカルプロファイルでバックエンドを起動

```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=local'
```

または、PowerShellの場合:
```powershell
cd backend
powershell -Command "& .\gradlew.bat bootRun --args='--spring.profiles.active=local'"
```

### 2. 起動ログの確認

以下のログが表示されればOK:
```
Started CookingAppApplication in X.XXX seconds
Tomcat started on port 8080 (http)
```

### 3. ヘルスチェック

```bash
curl http://localhost:8080/actuator/health
```

または、ブラウザで http://localhost:8080 にアクセス

## トラブルシューティング

### Docker Desktopが起動しない

**解決策**:
1. Docker Desktopを再起動
2. Windowsを再起動
3. Docker Desktopを再インストール

### LocalStackが起動しない

**解決策1: ポートの競合を確認**
```bash
netstat -ano | findstr :4566
```

ポート4566が使用されている場合、そのプロセスを終了するか、docker-compose.ymlのポート番号を変更

**解決策2: コンテナを削除して再起動**
```bash
docker-compose down
docker rm -f cooking-app-localstack
docker-compose up -d
```

**解決策3: Dockerイメージを再取得**
```bash
docker-compose down
docker rmi localstack/localstack:latest
docker-compose up -d
```

### DynamoDBテーブルが作成されない

**手動で初期化スクリプトを実行**:
```bash
docker exec cooking-app-localstack /etc/localstack/init/ready.d/01-create-dynamodb-tables.sh
docker exec cooking-app-localstack /etc/localstack/init/ready.d/02-create-s3-bucket.sh
```

### AWS CLIが見つからない

**AWS CLIのインストール**:
- Windows: https://aws.amazon.com/cli/
- または、LocalStackコンテナ内で実行:
```bash
docker exec cooking-app-localstack awslocal dynamodb list-tables
```

### バックエンドがLocalStackに接続できない

**確認事項**:
1. LocalStackが起動しているか: `docker ps`
2. application-local.ymlの設定が正しいか
3. Spring Bootのプロファイルが`local`になっているか

**ログで確認**:
```
aws.dynamodb.endpoint: http://localhost:4566
```

## 検証チェックリスト

- [ ] Docker Desktopが起動している
- [ ] `docker ps`でLocalStackコンテナが表示される
- [ ] `curl http://localhost:4566/_localstack/health`が成功する
- [ ] DynamoDBテーブルが5つ作成されている
- [ ] S3バケット`cooking-app-images-local`が作成されている
- [ ] バックエンドが`local`プロファイルで起動する
- [ ] バックエンドのログにエラーがない

すべてチェックできたら、ローカル環境のセットアップは完了です！🎉

## 次のステップ

ローカル環境が正常に動作したら、次のタスク（ユーザー管理機能の実装）に進むことができます。
