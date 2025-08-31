package com.devnem0y.tg_bot_assistant_ai.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormattingManager {

    public static boolean hasUnclosedFormattingAdvanced(String text) {
        if (text == null) return false;

        // Игнорируем экранированные символы
        String processedText = text.replace("\\*", "")
                .replace("\\_", "")
                .replace("\\~", "")
                .replace("\\`", "")
                .replace("\\|", "")
                .replace("\\[", "")
                .replace("\\]", "")
                .replace("\\(", "")
                .replace("\\)", "");

        return hasUnclosedFormatting(processedText);
    }

    public static boolean hasUnclosedFormatting(String text) {
        // Проверяем незакрытые блоки кода (тройные кавычки)
        int codeBlockCount = countOccurrences(text, "```");
        if (codeBlockCount % 2 != 0) return true;

        // Проверяем незакрытые жирные тексты (**)
        int boldCount = countOccurrences(text, "**");
        if (boldCount % 2 != 0) return true;

        // Проверяем незакрытые курсивные тексты (*)
        int italicCount = countOccurrences(text, "*");
        if (italicCount % 2 != 0) return true;

        // Проверяем незакрытые подчеркнутые тексты (__)
        int underlineCount = countOccurrences(text, "__");
        if (underlineCount % 2 != 0) return true;

        // Проверяем незакрытые зачеркнутые тексты (~~)
        int strikethroughCount = countOccurrences(text, "~~");
        if (strikethroughCount % 2 != 0) return true;

        // Проверяем незакрытые inline code блоки (`)
        int inlineCodeCount = countOccurrences(text, "`");
        if (inlineCodeCount % 2 != 0) return true;

        // Проверяем незакрытые спойлеры (||)
        int spoilerCount = countOccurrences(text, "||");
        if (spoilerCount % 2 != 0) return true;

        // Проверяем незакрытые цитаты (>>> для многострочных)
        int blockQuoteCount = countOccurrences(text, ">>>");
        if (blockQuoteCount % 2 != 0) return true;

        // Проверяем незакрытые однострочные цитаты (>>)
        int singleLineQuoteCount = countOccurrences(text, ">>");
        if (singleLineQuoteCount % 2 != 0) return true;

        // Проверяем незакрытые ссылки с форматированием [текст](ссылка)
        if (hasUnclosedLinks(text)) return true;

        return false;
    }

    // Вспомогательный метод для подсчета вхождений подстроки
    private static int countOccurrences(String text, String substring) {
        if (text == null || substring == null || substring.isEmpty()) {
            return 0;
        }

        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }

    // Вспомогательный метод для проверки незакрытых ссылок
    private static boolean hasUnclosedLinks(String text) {
        int openBracketCount = countOccurrences(text, "[");
        int closeBracketCount = countOccurrences(text, "]");
        int openParenCount = countOccurrences(text, "(");
        int closeParenCount = countOccurrences(text, ")");

        // Проверяем соответствие квадратных скобок и круглых скобок
        if (openBracketCount != closeBracketCount) return true;
        if (openParenCount != closeParenCount) return true;

        // Дополнительная проверка: после закрывающей квадратной скобки должна идти открывающая круглая
        Pattern linkPattern = Pattern.compile("\\[.*?\\]\\(.*?\\)");
        Matcher matcher = linkPattern.matcher(text);
        String remainingText = text;

        while (matcher.find()) {
            remainingText = remainingText.replace(matcher.group(), "");
        }

        // Если остались незакрытые скобки после удаления корректных ссылок
        return remainingText.contains("[") || remainingText.contains("]") ||
                remainingText.contains("(") || remainingText.contains(")");
    }
}
