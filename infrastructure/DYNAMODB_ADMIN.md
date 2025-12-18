# DynamoDB Admin でLocalStackのデータベースを確認する方法

## 概要

DynamoDB Adminは、LocalStackのDynamoDBテーブルをブラウザから簡単に確認・編集できるツールです。

## 前提条件

- Node.jsがインストールされていること
- LocalStackが起動していること

## セットアップ

### 1. DynamoDB Adminのインストール

```bash
npm install -g dynamodb-admin
```

### 2. LocalStackの起動確認

```bash
docker ps
```

`cooking-app-localstack` コンテナが起動していることを確認してください。

## 使用方法

### DynamoDB Adminの起動

```bash
set DYNAMO_ENDPOINT=http://localhost:4566
set AWS_REGION=ap-northeast-1
dynamodb-admin
```

または1行で：

```bash
set DYNAMO_ENDPOINT=http://localhost:4566 & set AWS_REGION=ap-northeast-1 & dynamodb-admin
```

### ブラウザでアクセス

起動後、以下のURLにアクセスします：

```
http://localhost:8001
```

### 停止方法

ターミナルで `Ctrl+C` を押して停止します。

## 機能

- **テーブル一覧表示**: トップページに全テーブルが表示されます
- **データ閲覧**: テーブル名をクリックして中身を確認
- **データ追加**: 「Create item」ボタンから新規データを追加
- **データ編集**: 各行の編集アイコンをクリック
- **データ削除**: 各行の削除アイコンをクリック
- **検索・フィルタ**: Scan/Query機能でデータを絞り込み

## 確認できるテーブル

- `cooking-app-users-local`: ユーザー情報テーブル

## トラブルシューティング

### テーブルが表示されない場合

1. LocalStackが起動しているか確認
   ```bash
   docker ps
   ```

2. DynamoDBエンドポイントが正しいか確認
   ```bash
   curl http://localhost:4566/_localstack/health
   ```

3. テーブルが存在するか確認
   ```bash
   aws dynamodb list-tables --endpoint-url=http://localhost:4566 --region ap-northeast-1
   ```

### ポート8001が使用中の場合

別のポートで起動できます：

```bash
set DYNAMO_ENDPOINT=http://localhost:4566
set AWS_REGION=ap-northeast-1
set PORT=8002
dynamodb-admin
```
