package vfrolenko;
import org.jetbrains.annotations.NotNull;

public record Token(TokenType type, String value, int line, int column) {

    @Override
    @NotNull
    public String toString() {
        return String.format("Token{type=%-20s value=%-15s line=%d, col=%d}",
                type, "'" + value + "'", line, column);
    }
}