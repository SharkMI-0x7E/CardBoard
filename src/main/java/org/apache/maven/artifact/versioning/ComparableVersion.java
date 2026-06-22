package org.apache.maven.artifact.versioning;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ComparableVersion implements Comparable<ComparableVersion> {

    private final String value;
    private final List<Token> tokens;

    public ComparableVersion(String version) {
        this.value = version == null ? "" : version.trim();
        this.tokens = parse(this.value);
    }

    @Override
    public int compareTo(ComparableVersion other) {
        if (other == null) {
            return 1;
        }

        int max = Math.max(this.tokens.size(), other.tokens.size());
        for (int i = 0; i < max; i++) {
            Token left = i < this.tokens.size() ? this.tokens.get(i) : Token.ZERO;
            Token right = i < other.tokens.size() ? other.tokens.get(i) : Token.ZERO;
            int cmp = left.compareTo(right);
            if (cmp != 0) {
                return cmp;
            }
        }

        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ComparableVersion)) {
            return false;
        }
        return compareTo((ComparableVersion) obj) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(normalize(value));
    }

    @Override
    public String toString() {
        return value;
    }

    private static List<Token> parse(String version) {
        List<Token> parsed = new ArrayList<>();
        if (version == null || version.isEmpty()) {
            parsed.add(Token.ZERO);
            return parsed;
        }

        StringBuilder current = new StringBuilder();
        Token.Kind currentKind = null;

        for (int i = 0; i < version.length(); i++) {
            char c = version.charAt(i);
            if (c == '.' || c == '-' || c == '_') {
                flush(parsed, current, currentKind);
                current.setLength(0);
                currentKind = null;
                continue;
            }

            Token.Kind kind = Character.isDigit(c) ? Token.Kind.NUMBER : Token.Kind.TEXT;
            if (currentKind != null && kind != currentKind) {
                flush(parsed, current, currentKind);
                current.setLength(0);
            }

            currentKind = kind;
            current.append(c);
        }

        flush(parsed, current, currentKind);

        if (parsed.isEmpty()) {
            parsed.add(Token.ZERO);
        }

        return parsed;
    }

    private static void flush(List<Token> parsed, StringBuilder current, Token.Kind kind) {
        if (current == null || current.length() == 0 || kind == null) {
            return;
        }
        if (kind == Token.Kind.NUMBER) {
            parsed.add(Token.number(current.toString()));
        } else {
            parsed.add(Token.text(current.toString()));
        }
    }

    private static String normalize(String version) {
        if (version == null) {
            return "";
        }
        return version.trim().replace('_', '.').replace('-', '.');
    }

    private static final class Token implements Comparable<Token> {
        static final Token ZERO = new Token(Kind.NUMBER, 0, "0");

        enum Kind {
            NUMBER,
            TEXT
        }

        private final Kind kind;
        private final int number;
        private final String text;

        private Token(Kind kind, int number, String text) {
            this.kind = kind;
            this.number = number;
            this.text = text;
        }

        static Token number(String value) {
            try {
                return new Token(Kind.NUMBER, Integer.parseInt(value), value);
            } catch (NumberFormatException ex) {
                return new Token(Kind.NUMBER, 0, value);
            }
        }

        static Token text(String value) {
            return new Token(Kind.TEXT, 0, value.toLowerCase());
        }

        @Override
        public int compareTo(Token other) {
            if (other == null) {
                return 1;
            }
            if (this.kind != other.kind) {
                return this.kind == Kind.NUMBER ? 1 : -1;
            }
            if (this.kind == Kind.NUMBER) {
                return Integer.compare(this.number, other.number);
            }
            return this.text.compareTo(other.text);
        }
    }
}
