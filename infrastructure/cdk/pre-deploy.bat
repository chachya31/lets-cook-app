@echo off
echo ========================================
echo CDK デプロイ前チェック
echo ========================================
echo.

echo [1/3] バックエンドのJARファイルを確認中...
if exist "..\..\backend\build\libs\backend-1.0.0.jar" (
    echo ✓ backend-1.0.0.jar が見つかりました
) else (
    echo ✗ backend-1.0.0.jar が見つかりません
    echo.
    echo バックエンドをビルドしますか？ (Y/N)
    set /p BUILD_BACKEND=
    if /i "%BUILD_BACKEND%"=="Y" (
        echo バックエンドをビルド中...
        cd ..\..\backend
        call gradlew.bat clean build shadowJar
        cd ..\infrastructure\cdk
        echo ✓ ビルド完了
    ) else (
        echo エラー: デプロイにはJARファイルが必要です
        echo 以下のコマンドでビルドしてください:
        echo   cd backend
        echo   gradlew.bat clean build shadowJar
        exit /b 1
    )
)
echo.

echo [2/3] Node.js依存関係を確認中...
if exist "node_modules" (
    echo ✓ node_modules が見つかりました
) else (
    echo node_modules が見つかりません。インストール中...
    call npm install
    echo ✓ インストール完了
)
echo.

echo [3/3] AWS認証情報を確認中...
aws sts get-caller-identity >nul 2>&1
if %errorlevel% equ 0 (
    echo ✓ AWS認証情報が設定されています
) else (
    echo ✗ AWS認証情報が設定されていません
    echo 以下のコマンドで設定してください:
    echo   aws configure
    exit /b 1
)
echo.

echo ========================================
echo ✓ すべてのチェックが完了しました
echo ========================================
echo.
echo デプロイを開始しますか？ (Y/N)
set /p START_DEPLOY=
if /i "%START_DEPLOY%"=="Y" (
    echo.
    echo デプロイ中...
    call cdk deploy CookingAppStack-Prod
) else (
    echo デプロイをキャンセルしました
    echo 手動でデプロイする場合:
    echo   cdk deploy CookingAppStack-Prod
)
