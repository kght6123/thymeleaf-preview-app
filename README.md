# Thymeleaf Preview Tool

Thymeleafテンプレートをモックデータで即座にプレビューできるスタンドアロンツール。

## Features

- **Hot Reload** - サーバー再起動なしでテンプレート/JSON/CSSの変更を即時反映
- **Mock Data Injection** - JSONファイルでテンプレート変数を定義
- **Custom Dialects** - 外部JARからカスタムExpression Objectを動的ロード
- **Template Catalog** - テンプレート一覧の検索・閲覧UI
- **Static Asset Serving** - CSS/JS/画像の配信
- **Security** - パストラバーサル攻撃防止

## Quick Start

### サンプルで試す

```bash
# ビルド
./mvnw package -DskipTests

# サンプルで実行
java -jar target/thymeleaf-preview-1.0.0-SNAPSHOT.jar \
  --preview.templates-root=samples/templates \
  --preview.defs-root=samples/defs

# ブラウザでアクセス
# Catalog: http://localhost:8080/catalog
# Preview: http://localhost:8080/preview?tpl=index.html
```

### 自分のテンプレートで使う

```bash
java -jar target/thymeleaf-preview-1.0.0-SNAPSHOT.jar \
  --preview.templates-root=/path/to/your/templates \
  --preview.defs-root=/path/to/your/defs
```

### Docker

```bash
# イメージビルド
docker build -t thymeleaf-preview:latest .

# 実行
docker run -p 8080:8080 \
  -v $(pwd)/samples/templates:/templates \
  -v $(pwd)/samples/defs:/defs \
  thymeleaf-preview:latest
```

### カスタムダイアレクトを使う

thymeleaf-preview-appは外部JARからカスタムThymeleafダイアレクトを動的にロードできます。これにより、プロジェクト固有の Expression Object（`#myUtil`, `#request` など）をプレビュー環境で利用可能にします。

#### サンプルダイアレクトで試す

```bash
# 1. thymeleaf-preview-app をビルド
./mvnw package -DskipTests

# 2. サンプルダイアレクトをビルド
cd samples/sample-dialect
mvn package -DskipTests
cd ../..

# 3. ダイアレクト付きで起動
java -Dloader.path=samples/sample-dialect/target/sample-preview-dialect-1.0.0-SNAPSHOT.jar \
  -jar target/thymeleaf-preview-1.0.0-SNAPSHOT.jar \
  --preview.templates-root=samples/templates \
  --preview.defs-root=samples/defs

# 4. ブラウザでダイアレクトデモを確認
# http://localhost:8080/preview?tpl=dialect-demo.html
```

サンプルダイアレクトは以下の Expression Object を提供します：

| Expression Object | 説明 | 使用例 |
|-------------------|------|--------|
| `#request` | HTTPリクエストモック | `${#request.getHeader('Accept-Language')}` |
| `#utils` | ユーティリティ関数 | `${#utils.formatCurrency(1234.56)}` |

#### 独自ダイアレクトの作成

`samples/sample-dialect/` をテンプレートとして独自のダイアレクトを作成できます。詳細は [samples/sample-dialect/README.md](samples/sample-dialect/README.md) を参照してください。

**重要なポイント：**

1. **ServiceLoader登録** - `META-INF/services/org.thymeleaf.dialect.IDialect` にダイアレクトクラス名を記載
2. **loader.path指定** - 起動時に `-Dloader.path=<jar-path>` でJARを指定
3. **設定はJSONで** - `global.json` や各テンプレート用JSONの `dialects` セクションで設定

```json
{
  "dialects": {
    "request": {
      "headers": { "X-Custom": "value" }
    },
    "utils": {
      "locale": "ja-JP",
      "translations": { "hello": "こんにちは" }
    }
  }
}
```

## Endpoints

| Endpoint | Description |
|----------|-------------|
| `/catalog` | テンプレート一覧（検索・ページネーション付き） |
| `/preview?tpl=<path>` | テンプレートプレビュー |
| `/preview?tpl=<path>&fragment=<name>` | フラグメントプレビュー |
| `/assets/**` | 静的アセット配信 |

## Directory Structure

```
templates/
├── index.html
├── pages/
│   ├── about.html
│   └── contact.html
├── components/
│   ├── header.html
│   └── footer.html
└── assets/
    ├── css/
    │   └── style.css
    └── js/
        └── main.js

defs/
├── global.json          # 全テンプレート共通のデータ
├── index.json           # index.html 用データ
└── pages/
    ├── about.json       # pages/about.html 用データ
    └── contact.json     # pages/contact.html 用データ
```

## JSON Definition Format

```json
{
    "css": ["/assets/css/style.css"],
    "js": ["/assets/js/main.js"],
    "fixtures": {
        "title": "Page Title",
        "items": [
            { "name": "Item 1" },
            { "name": "Item 2" }
        ]
    },
    "dialects": {
        "request": { "headers": { "X-Lang": "ja" } },
        "utils": { "locale": "ja-JP" }
    }
}
```

| フィールド | 説明 |
|-----------|------|
| `css` | プレビューに注入するCSSファイルパス |
| `js` | プレビューに注入するJSファイルパス |
| `fixtures` | テンプレート変数（`${title}`, `${items}` など） |
| `dialects` | カスタムダイアレクト用設定（Expression Object に渡される） |

## Sample Files

`samples/` ディレクトリにサンプルファイルが含まれています：

- `samples/templates/` - サンプルテンプレート
- `samples/defs/` - サンプルJSON定義
- `samples/sample-dialect/` - カスタムダイアレクトのサンプル実装

## Development

```bash
# テスト実行
./mvnw test

# ビルド
./mvnw package
```

## Requirements

- Java 17+
- Maven 3.8+

## License

MIT
