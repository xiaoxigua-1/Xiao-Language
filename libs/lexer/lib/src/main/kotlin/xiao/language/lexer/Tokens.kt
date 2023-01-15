package xiao.language.lexer

enum class Tokens {
    Identifier,
    Keyword,
    Literal,
    RawLiteral,
    Number,
    Symbol,
    Whitespace,
    Unknown,
    EOF,
}