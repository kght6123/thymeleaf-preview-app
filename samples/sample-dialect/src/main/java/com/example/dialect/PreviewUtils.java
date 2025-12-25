package com.example.dialect;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * Utility expression object for common template operations.
 *
 * This expression object provides helper methods for formatting,
 * string manipulation, and other common tasks in templates.
 *
 * Usage in templates:
 *   ${#utils.formatCurrency(1234.56)}          -> "1,234.56"
 *   ${#utils.formatNumber(1234567)}            -> "1,234,567"
 *   ${#utils.truncate('Long text here', 8)}   -> "Long tex..."
 *   ${#utils.urlEncode('hello world')}         -> "hello+world"
 *   ${#utils.defaultIfEmpty(value, 'N/A')}     -> value or "N/A"
 *   ${#utils.translate('greeting')}            -> translated text
 *
 * Configuration in global.json or page-specific JSON:
 * {
 *   "dialects": {
 *     "utils": {
 *       "locale": "en-US",
 *       "currencySymbol": "$",
 *       "translations": {
 *         "greeting": "Hello",
 *         "farewell": "Goodbye"
 *       }
 *     }
 *   }
 * }
 */
public class PreviewUtils {

    private final Locale locale;
    private final String currencySymbol;
    private final Map<String, Object> translations;

    @SuppressWarnings("unchecked")
    public PreviewUtils(Map<String, Object> config) {
        String localeStr = getStringConfig(config, "locale", "en-US");
        this.locale = Locale.forLanguageTag(localeStr.replace("_", "-"));
        this.currencySymbol = getStringConfig(config, "currencySymbol", "");

        Object translationsObj = config.get("translations");
        if (translationsObj instanceof Map) {
            this.translations = (Map<String, Object>) translationsObj;
        } else {
            this.translations = Collections.emptyMap();
        }
    }

    /**
     * Formats a number as currency.
     * @param value the numeric value
     * @return formatted currency string
     */
    public String formatCurrency(Number value) {
        if (value == null) {
            return "";
        }
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        String formatted = formatter.format(value);
        if (!currencySymbol.isEmpty()) {
            // Replace default currency symbol with configured one
            formatted = formatted.replaceFirst("[^0-9.,\\s]+", currencySymbol);
        }
        return formatted;
    }

    /**
     * Formats a number with grouping separators.
     * @param value the numeric value
     * @return formatted number string
     */
    public String formatNumber(Number value) {
        if (value == null) {
            return "";
        }
        NumberFormat formatter = NumberFormat.getNumberInstance(locale);
        return formatter.format(value);
    }

    /**
     * Formats a number with specified decimal places.
     * @param value the numeric value
     * @param decimalPlaces number of decimal places
     * @return formatted number string
     */
    public String formatDecimal(Number value, int decimalPlaces) {
        if (value == null) {
            return "";
        }
        StringBuilder pattern = new StringBuilder("#,##0");
        if (decimalPlaces > 0) {
            pattern.append(".");
            for (int i = 0; i < decimalPlaces; i++) {
                pattern.append("0");
            }
        }
        DecimalFormat formatter = new DecimalFormat(pattern.toString());
        return formatter.format(value);
    }

    /**
     * Truncates a string to specified length with ellipsis.
     * @param value the string to truncate
     * @param maxLength maximum length (including ellipsis)
     * @return truncated string
     */
    public String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        if (maxLength <= 3) {
            return value.substring(0, maxLength);
        }
        return value.substring(0, maxLength - 3) + "...";
    }

    /**
     * URL encodes a string.
     * @param value the string to encode
     * @return URL encoded string
     */
    public String urlEncode(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * Returns the value if not empty, otherwise returns the default.
     * @param value the value to check
     * @param defaultValue the default value
     * @return value or defaultValue
     */
    public String defaultIfEmpty(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value;
    }

    /**
     * Checks if a value is empty (null or blank string).
     * @param value the value to check
     * @return true if empty
     */
    public boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String) {
            return ((String) value).trim().isEmpty();
        }
        return false;
    }

    /**
     * Checks if a value has content (not null and not blank).
     * @param value the value to check
     * @return true if has value
     */
    public boolean hasValue(Object value) {
        return !isEmpty(value);
    }

    /**
     * Translates a key using the configured translations.
     * @param key the translation key
     * @return translated text or the key if not found
     */
    public String translate(String key) {
        Object translation = translations.get(key);
        return translation != null ? translation.toString() : key;
    }

    /**
     * Translates a key with fallback.
     * @param key the translation key
     * @param fallback the fallback text if key not found
     * @return translated text or fallback
     */
    public String translate(String key, String fallback) {
        Object translation = translations.get(key);
        return translation != null ? translation.toString() : fallback;
    }

    /**
     * Replaces newlines with HTML br tags.
     * @param value the string to process
     * @return string with newlines replaced by br tags
     */
    public String nl2br(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\r\n", "<br>")
                    .replace("\n", "<br>")
                    .replace("\r", "<br>");
    }

    /**
     * Creates an array from varargs (useful for Thymeleaf iteration).
     * @param items the items
     * @return array of items
     */
    public Object[] toArray(Object... items) {
        return items;
    }

    private String getStringConfig(Map<String, Object> config, String key, String defaultValue) {
        Object value = config.get(key);
        return value != null ? value.toString() : defaultValue;
    }
}
