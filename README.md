# Thymeleaf Preview Tool

Thymeleafテンプレートをモックデータで即座にプレビューできるスタンドアロンツール。

## Features

- **Hot Reload** - サーバー再起動なしでテンプレート/JSON/CSSの変更を即時反映
- **Mock Data Injection** - JSONファイルでテンプレート変数を定義
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
    "fixtures": {
        "title": "Page Title",
        "items": [
            { "name": "Item 1" },
            { "name": "Item 2" }
        ]
    }
}
```

`fixtures` 内のデータがテンプレート変数として利用可能：
- `${title}` → "Page Title"
- `${items}` → リストとして th:each で使用可能

## Sample Files

`samples/` ディレクトリにサンプルファイルが含まれています：

- `samples/templates/` - サンプルテンプレート
- `samples/defs/` - サンプルJSON定義

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
