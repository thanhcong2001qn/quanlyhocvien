package com.dacs.quanlyhocvien.Services.Generators;

import com.dacs.quanlyhocvien.Embedding.SemanticAliasResolver;
import com.dacs.quanlyhocvien.Enums.PromptConstants;
import com.dacs.quanlyhocvien.Exceptions.ChatbotException;
import com.dacs.quanlyhocvien.Services.Clients.GeminiApiClient;
import com.dacs.quanlyhocvien.Services.Extractors.AliasExtractionService;
import com.dacs.quanlyhocvien.Utils.DatabaseMapping;
import com.dacs.quanlyhocvien.Utils.DatabaseSchemaExtractor;
import com.dacs.quanlyhocvien.Utils.PromptLoader;
import com.dacs.quanlyhocvien.Utils.SmartEntityExtractor;
import com.dacs.quanlyhocvien.models.AliasResolutionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class SqlQueryGenerator {

    private static final Logger logger = Logger.getLogger(SqlQueryGenerator.class.getName());

    @Autowired
    private GeminiApiClient geminiApiClient;

    @Autowired
    private DatabaseSchemaExtractor schemaExtractor;

    @Autowired
    private SmartEntityExtractor smartEntityExtractor;

    @Autowired
    private AliasExtractionService aliasExtractionService;

    @Autowired
    private SemanticAliasResolver semanticAliasResolver;

    private AliasResolutionResult lastAliasResolution;
    private String cachedSchema = null;

    public String generateIntentBasedSql(String userQuestion) {
        try {
            if (cachedSchema == null) {
                cachedSchema = schemaExtractor.getCompleteSchema();
            }

            // ✅ Tách alias meaningful và mở rộng ngữ nghĩa
            AliasResolutionResult aliasResolution = semanticAliasResolver.resolveAll(userQuestion);
            this.lastAliasResolution = aliasResolution; // lưu lại nếu bạn dùng sau này

            Map<String, String> aliasToCanonical = aliasResolution.getColumns().stream()
                    .collect(Collectors.toMap(
                            AliasResolutionResult.ColumnMapping::alias,
                            AliasResolutionResult.ColumnMapping::canonical,
                            (a, b) -> b, // merge conflict nếu có
                            LinkedHashMap::new
                    ));

            List<String> keywords = new ArrayList<>(aliasToCanonical.values());

            List<String> involvedTables = keywords.stream()
                    .map(DatabaseMapping::getTableName)
                    .distinct()
                    .filter(table -> cachedSchema.toLowerCase().contains("create table " + table.toLowerCase()))
                    .toList();

            String prompt = buildAdvancedSqlPrompt(userQuestion, keywords, involvedTables);

            String generatedSql = geminiApiClient.getResponse(prompt);
            logger.info("🔥 SQL sinh ra từ Gemini: " + generatedSql);

            if (generatedSql == null || generatedSql.trim().isEmpty() || generatedSql.trim().equalsIgnoreCase("undefined")) {
                throw new ChatbotException("ERR_SQL_UNDEFINED", "⚠️ Không thể tạo câu truy vấn từ câu hỏi bạn vừa nhập.", "Gemini trả về SQL không hợp lệ");
            }

            String cleanedSql = cleanSqlQuery(generatedSql);

            if (!containsValidTable(cleanedSql, cachedSchema)) {
                throw new ChatbotException("ERR_INVALID_TABLE", "🧾 Có vẻ bảng bạn yêu cầu không tồn tại trong hệ thống.", "Bảng không hợp lệ trong SQL: " + cleanedSql);
            }

            return cleanedSql;

        } catch (ChatbotException ce) {
            throw ce;
        } catch (Exception e) {
            throw new ChatbotException("ERR_SQL_GENERATION", "⚠️ Không thể tạo câu truy vấn từ câu hỏi bạn vừa nhập.", e.getMessage());
        }
    }

    private String buildAdvancedSqlPrompt(String question, List<String> keywords, List<String> involvedTables) {
        String currentYear = String.valueOf(LocalDate.now().getYear());
        String promptTemplate = PromptLoader.loadPrompt(PromptConstants.SQL_GENERATION_PROMPT);
        String fewshots = PromptLoader.loadPrompt("few_shot_examples.txt");

        String keywordInfo = keywords.isEmpty()
                ? ""
                : "\nCác từ khóa bạn cần tập trung là: " + String.join(", ", keywords);

        String tableInfo = involvedTables.isEmpty()
                ? ""
                : "\nCác bảng dữ liệu cần chú ý: " + String.join(", ", involvedTables);

        return promptTemplate
                .replace("{SCHEMA}", cachedSchema)
                .replace("{MAPPING}", DatabaseMapping.getMappingInfo())
                .replace("{CURRENT_YEAR}", currentYear)
                .replace("{QUESTION}", question + tableInfo)
                + "\n\n" + fewshots;
    }

    private String cleanSqlQuery(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new ChatbotException("ERR_SQL_EMPTY", "⚠️ Không tạo được câu truy vấn nào từ câu hỏi của bạn.", "SQL được sinh ra là null hoặc rỗng");
        }

        sql = sql.replaceAll("```sql\\s*", "")
                .replaceAll("```", "")
                .replaceAll("[\\r\\n]+", " ")
                .replaceAll("\\s+", " ")
                .replaceAll("\\s*,\\s*", ", ")
                .replaceAll("(?i)--.*", "")
                .replaceAll("\\s*=\\s*", " = ")
                .replaceAll(";", "")
                .trim();
        sql = sql.replaceAll("STRFTIME\\('%Y-%m',\\s*(\\w+)\"", "DATE_FORMAT($1, '%Y-%m')");
        return sql;
    }

    private boolean containsValidTable(String sql, String schema) {
        sql = sql.toLowerCase();

        List<String> tableNames = extractTableNamesFromSchema(schema);

        int fromIndex = sql.indexOf("from ");
        if (fromIndex == -1) return false;

        String[] tokens = sql.substring(fromIndex + 5).split(" ");
        String possibleTable = tokens[0].replaceAll("[^a-zA-Z0-9_]", "");

        return tableNames.contains(possibleTable);
    }

    private List<String> extractTableNamesFromSchema(String schema) {
        List<String> tables = new ArrayList<>();
        Pattern pattern = Pattern.compile("create table (\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(schema);
        while (matcher.find()) {
            tables.add(matcher.group(1).toLowerCase());
        }
        return tables;
    }

    public Set<String> extractRelevantColumnsFromSql(String sql) {
        Set<String> columns = new LinkedHashSet<>();
        if (sql == null || sql.isBlank()) return columns;

        sql = sql.replaceAll("\\s+", " ").trim();

        Pattern funcPattern = Pattern.compile(
                "\\b[a-zA-Z_][a-zA-Z0-9_]*\\s*\\(\\s*([a-zA-Z_][a-zA-Z0-9_\\.]+)\\s*(,|\\))",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcherFunc = funcPattern.matcher(sql);
        while (matcherFunc.find()) {
            String col = stripAlias(matcherFunc.group(1));
            if (isValidColumn(col)) columns.add(col);
        }

        Pattern directPattern = Pattern.compile(
                "(where|and|or|having)\\s+([a-zA-Z_][a-zA-Z0-9_\\.]+)\\s*(=|!=|<|>|<=|>=|like|in|between)",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcherDirect = directPattern.matcher(sql);
        while (matcherDirect.find()) {
            String col = stripAlias(matcherDirect.group(2));
            if (isValidColumn(col)) columns.add(col);
        }

        Pattern betweenPattern = Pattern.compile(
                "\\b([a-zA-Z_][a-zA-Z0-9_\\.]+)\\s+(between|is null|is not null)",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcherBetween = betweenPattern.matcher(sql);
        while (matcherBetween.find()) {
            String col = stripAlias(matcherBetween.group(1));
            if (isValidColumn(col)) columns.add(col);
        }

        return columns;
    }

    private String stripAlias(String col) {
        col = col.trim();
        int dot = col.lastIndexOf('.');
        return dot >= 0 ? col.substring(dot + 1) : col;
    }

    private boolean isValidColumn(String col) {
        if (col == null || col.isBlank()) return false;
        String lower = col.toLowerCase();

        return !(lower.matches("'[^']*'") || lower.matches("\\d+") ||
                lower.equals("true") || lower.equals("false") || lower.startsWith("'%"));
    }

    public AliasResolutionResult getLastAliasResolution() {
        return this.lastAliasResolution;
    }
}
