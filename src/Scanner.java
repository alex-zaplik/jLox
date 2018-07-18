import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Scanner {
    private final String src;
    private final List<Token> tokens = new ArrayList<>();

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    TokenType.AND);
        keywords.put("class",  TokenType.CLASS);
        keywords.put("else",   TokenType.ELSE);
        keywords.put("false",  TokenType.FALSE);
        keywords.put("for",    TokenType.FOR);
        keywords.put("fun",    TokenType.FUN);
        keywords.put("if",     TokenType.IF);
        keywords.put("nil",    TokenType.NIL);
        keywords.put("or",     TokenType.OR);
        keywords.put("print",  TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super",  TokenType.SUPER);
        keywords.put("this",   TokenType.THIS);
        keywords.put("true",   TokenType.TRUE);
        keywords.put("var",    TokenType.VAR);
        keywords.put("while",  TokenType.WHILE);
    }

    private int start = 0, current = 0, line = 1;

    public Scanner(String src) {
        this.src = src;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            // Single character tokens
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case '-': addToken(TokenType.MINUS); break;
            case '+': addToken(TokenType.PLUS); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '*': addToken(TokenType.STAR); break;

            // Single or double character tokens
            case '!': addToken(match('=') ? TokenType.BANG_EQUAL    : TokenType.BANG); break;
            case '=': addToken(match('=') ? TokenType.EQUAL_EQUAL   : TokenType.EQUAL); break;
            case '<': addToken(match('=') ? TokenType.LESS_EQUAL    : TokenType.LESS); break;
            case '>': addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER); break;

            //
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {
                    multilineComment();
                } else {
                    addToken(TokenType.SLASH);
                }
                break;

            // Ignored whitespace
            case ' ':
            case '\r':
            case '\t':
                break;

            // New line
            case '\n':
                line++;
                break;

            // String literal
            case '"': string(); break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character \'" + c + "\'.");
                }

                break;
        }
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {{
            if (peek() == '\n') line++;
            advance();
        }}

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string literal");
            return;
        }

        advance();

        String text = src.substring(start + 1, current - 1);
        addToken(TokenType.STRING, text);
    }

    private void multilineComment() {
        while (peek() != '*' && peekNext() != '/' && !isAtEnd()) {{
            if (peek() == '\n') line++;
            advance();
        }}

        if (isAtEnd()) {
            Lox.error(line, "Unterminated multiline comment");
            return;
        }

        advance();
        advance();
    }

    private void number() {
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
        }

        double num = Double.parseDouble(src.substring(start, current));
        addToken(TokenType.NUMBER, num);
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = src.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = TokenType.IDENTIFIER;

        addToken(type);
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return src.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= src.length()) return '\0';
        return src.charAt(current + 1);
    }

    private char advance() {
        return src.charAt(current++);
    }

    private boolean match(char c) {
        if (isAtEnd() || src.charAt(current) != c) return false;

        current++;
        return true;
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isDigit(c) || isAlpha(c);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = src.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean isAtEnd() {
        return current >= src.length();
    }
}
