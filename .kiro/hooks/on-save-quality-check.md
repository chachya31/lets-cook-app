# Code Quality Check on Save

このフックはファイル保存時に自動的にコード品質をチェックします。

## トリガー
- イベント: ファイル保存時
- 対象ファイル: `*.java`, `*.ts`, `*.tsx`

## チェック内容

### Javaファイル
- `@Slf4j`の使用チェック（禁止）
- `System.out.println()`の使用チェック（禁止）
- `System.getProperty()`の使用チェック（禁止）
- `Logger`の直接定義を推奨

### TypeScriptファイル
- `axios`のimportチェック（禁止）
- `fetch`の直接使用チェック（apiClient.ts使用を推奨）
- `console.log()`の使用チェック（警告）

## アクション
保存されたファイルのコーディング規約違反をチェックし、違反があれば警告を表示します。

---

**設定方法**: 
Kiro IDEのコマンドパレット（Ctrl+Shift+P / Cmd+Shift+P）から「Open Kiro Hook UI」を選択し、以下の設定を追加してください：

```
Trigger: On Message Sent
Message Pattern: (保存されたファイルをチェック|check saved file|code review)
Action: Send Message to Agent
Message: 
最後に保存されたファイルをコーディング規約に従ってレビューしてください。

チェック項目：
- Javaファイル: @Slf4j使用禁止、Logger直接定義を使用
- Javaファイル: System.getProperty()禁止、@Valueアノテーション使用
- Javaファイル: System.out.println()禁止、log.info()使用
- TypeScriptファイル: axios禁止、fetch API使用
- TypeScriptファイル: API呼び出しはapiClient.tsのヘルパー関数を使用
- すべて: デバッグコード（console.log, System.out.println）の削除
- すべて: 未使用のimport削除

違反があれば具体的に指摘し、修正案を提示してください。
```
