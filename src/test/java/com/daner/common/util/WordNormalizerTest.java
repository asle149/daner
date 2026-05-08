package com.daner.common.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.text.Normalizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WordNormalizerTest {

    @Nested
    class HappyPath {

        @Test
        void trims_leading_and_trailing_whitespace() {
            assertThat(WordNormalizer.normalize("  퇴근  ")).isEqualTo("퇴근");
            assertThat(WordNormalizer.normalize("\t퇴근\n")).isEqualTo("퇴근");
        }

        @Test
        void lowercases_english() {
            assertThat(WordNormalizer.normalize("Hello")).isEqualTo("hello");
            assertThat(WordNormalizer.normalize("LOVE")).isEqualTo("love");
        }

        @Test
        void nfc_normalizes_decomposed_hangul_to_composed() {
            String decomposed = Normalizer.normalize("퇴근", Normalizer.Form.NFD);
            assertThat(decomposed).isNotEqualTo("퇴근");

            assertThat(WordNormalizer.normalize(decomposed)).isEqualTo("퇴근");
        }

        @Test
        void accepts_exactly_10_korean_chars() {
            String tenKorean = "가나다라마바사아자차";
            assertThat(WordNormalizer.normalize(tenKorean)).isEqualTo(tenKorean);
        }

        @Test
        void accepts_exactly_20_english_chars() {
            String twentyEnglish = "abcdefghijklmnopqrst";
            assertThat(WordNormalizer.normalize(twentyEnglish)).isEqualTo(twentyEnglish);
        }

        @Test
        void mixed_korean_english_uses_korean_limit() {
            String tenMixed = "abc일이삼사오육칠";
            assertThat(WordNormalizer.normalize(tenMixed)).isEqualTo(tenMixed);
        }
    }

    @Nested
    class Rejection {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        void empty_or_whitespace_only_input_is_rejected(String input) {
            assertThatThrownBy(() -> WordNormalizer.normalize(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("단어를 입력해주세요");
        }

        @ParameterizedTest
        @ValueSource(strings = {"안녕 하세요", "hello world", "퇴근\t후", "도파\n민"})
        void internal_whitespace_is_rejected(String input) {
            assertThatThrownBy(() -> WordNormalizer.normalize(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("띄어쓰기");
        }

        @Test
        void korean_longer_than_10_is_rejected() {
            String elevenKorean = "가나다라마바사아자차카";
            assertThatThrownBy(() -> WordNormalizer.normalize(elevenKorean))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("10자");
        }

        @Test
        void english_longer_than_20_is_rejected() {
            String twentyOneEnglish = "abcdefghijklmnopqrstu";
            assertThatThrownBy(() -> WordNormalizer.normalize(twentyOneEnglish))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("20자");
        }

        @Test
        void mixed_input_over_10_chars_is_rejected_under_korean_limit() {
            String elevenMixed = "abc일이삼사오육칠팔";
            assertThatThrownBy(() -> WordNormalizer.normalize(elevenMixed))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("10자");
        }
    }

    @Nested
    class Idempotency {

        @Test
        void normalize_is_idempotent() {
            String first = WordNormalizer.normalize("  Hello  ");
            String second = WordNormalizer.normalize(first);

            assertThat(second).isEqualTo(first);
        }

        @Test
        void same_word_with_different_whitespace_returns_same_value() {
            assertThat(WordNormalizer.normalize("퇴근"))
                    .isEqualTo(WordNormalizer.normalize("  퇴근  "))
                    .isEqualTo(WordNormalizer.normalize("\t퇴근\n"));
        }

        @Test
        void casing_difference_collapses() {
            assertThat(WordNormalizer.normalize("Love"))
                    .isEqualTo(WordNormalizer.normalize("love"))
                    .isEqualTo(WordNormalizer.normalize("LOVE"));
        }
    }
}
