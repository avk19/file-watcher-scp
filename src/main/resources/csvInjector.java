import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CsvWriterExample {
    public static void main(String[] args) {
        // Example data
        List<String> data = Arrays.asList("John", "=SUM(A1:B1)", "Doe", "*=", "*+", "*-", "*@");

        // CSV file path
        String filePath = "example.csv";

        try (FileWriter writer = new FileWriter(filePath)) {
            // Convert and write data to CSV file
            writer.append(String.join(",", data.stream().map(CsvWriterExample::escapeSpecialCharacters).collect(Collectors.toList())))
                  .append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Function to escape special characters, Excel formulas, and specific combinations
    private static String escapeSpecialCharacters(String input) {
        if (Objects.isNull(input)) {
            return "\"\"";
        }

        // Escape double quotes by doubling them
        String escapedValue = input.replace("\"", "\"\"");

        // Check if the value starts with specific combinations and prepend an apostrophe
        if (escapedValue.matches("^[*][=+@-].*")) {
            escapedValue = "'" + escapedValue;
        }

        return "\"" + escapedValue + "\"";
    }
}
