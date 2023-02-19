package com.github.msemitkin.financie.telegram.util;

import java.util.Set;

public class MarkdownUtil {
    private static final Set<String> CHARACTERS_TO_ESCAPE = Set.of(
        "_", "[", "]", "(", ")", "~", ">", "#", "+", "-", "=", "|", "{", "}", ".", "!"
    );

    private MarkdownUtil() {
    }

    public static String escapeMarkdownV2(String text) {
        return CHARACTERS_TO_ESCAPE.stream()
            .reduce(text, (result, character) -> result.replace(character, "\\" + character));
    }
}
