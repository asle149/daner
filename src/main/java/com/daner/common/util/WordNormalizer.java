package com.daner.common.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public final class WordNormalizer {

    public static final int MAX_LENGTH_WITH_KOREAN = 10;
    public static final int MAX_LENGTH_WITHOUT_KOREAN = 20;

    private static final Pattern WHITESPACE = Pattern.compile("\\s");

    private WordNormalizer() {
    }

    public static String normalize(String input) {
        if (input == null) {
            throw new IllegalArgumentException("단어를 입력해주세요.");
        }
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("단어를 입력해주세요.");
        }
        if (WHITESPACE.matcher(trimmed).find()) {
            throw new IllegalArgumentException("단어에 띄어쓰기를 포함할 수 없어요.");
        }
        String composed = Normalizer.normalize(trimmed, Normalizer.Form.NFC);
        String lowered = composed.toLowerCase();
        validateLength(lowered);
        return lowered;
    }

    private static void validateLength(String word) {
        boolean hasKorean = word.codePoints().anyMatch(WordNormalizer::isKorean);
        int length = word.codePointCount(0, word.length());
        int max = hasKorean ? MAX_LENGTH_WITH_KOREAN : MAX_LENGTH_WITHOUT_KOREAN;
        if (length > max) {
            throw new IllegalArgumentException(hasKorean
                    ? "한글이 포함된 단어는 10자까지 입력할 수 있어요."
                    : "단어는 20자까지 입력할 수 있어요.");
        }
    }

    private static boolean isKorean(int codePoint) {
        return (codePoint >= 0xAC00 && codePoint <= 0xD7A3)
                || (codePoint >= 0x1100 && codePoint <= 0x11FF)
                || (codePoint >= 0x3130 && codePoint <= 0x318F);
    }
}
