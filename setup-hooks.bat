@echo off
REM Git Hooks セットアップスクリプト (Windows)

echo Setting up Git hooks...

REM Git hooksのパスを設定
git config core.hooksPath .githooks

REM 実行権限を付与（Git Bashで実行する場合）
if exist ".githooks\pre-commit" (
    echo Git hooks configured successfully!
    echo.
    echo To enable pre-commit checks, run:
    echo   git config core.hooksPath .githooks
    echo.
    echo Pre-commit hook will check:
    echo   - No System.out.println in backend production code
    echo   - Backend builds successfully
    echo   - ESLint passes for frontend
) else (
    echo Error: .githooks/pre-commit not found
    exit /b 1
)

echo.
echo Done!
pause
