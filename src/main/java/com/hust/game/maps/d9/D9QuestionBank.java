package com.hust.game.maps.d9;

import com.hust.game.util.AssetLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * D9QuestionBank - Load và quản lý câu hỏi từ JSON
 */
public class D9QuestionBank {
    private static final String[] REQUIRED_SETS = {
            "oop_floor_1",
            "oop_floor_2",
            "oop_floor_3",
            "oop_floor_4",
            "oop_floor_5",
            "oop_floor_6",
            "oop_final"
    };

    private String rawJson;

    public static class Question {
        public String id;
        public String topic;
        public String difficulty;
        public String question;
        public String[] options;
        public int answerIndex;
        public String explanation;

        public Question(String id, String topic, String difficulty, String question,
                        String[] options, int answerIndex, String explanation) {
            this.id = id;
            this.topic = topic;
            this.difficulty = difficulty;
            this.question = question;
            this.options = options;
            this.answerIndex = answerIndex;
            this.explanation = explanation;
        }
    }

    public D9QuestionBank(String jsonPath) {
        try {
            rawJson = AssetLoader.readString(jsonPath);
        } catch (IOException e) {
            System.err.println("[D9] Warning: failed to load question bank '" + jsonPath + "': " + e.getMessage());
            rawJson = "";
        }
        validateQuestionBank();
    }

    public List<Question> getQuestionsForSet(String setName, int count) {
        List<Question> questions = parseQuestionsForSet(setName, true);

        if (questions.size() <= count) {
            if (questions.size() < count) {
                System.err.println("[D9] Warning: question set " + setName + " has "
                        + questions.size() + " usable questions; requested " + count + ".");
            }
            return questions;
        }

        List<Question> shuffled = new ArrayList<>(questions);
        java.util.Collections.shuffle(shuffled);
        return shuffled.subList(0, count);
    }

    public boolean hasQuestionsForSet(String setName) {
        return extractArrayForKey(setName) != null;
    }

    private String extractArrayForKey(String key) {
        String patternText = "\"" + Pattern.quote(key) + "\"\\s*:\\s*\\[";
        Pattern pattern = Pattern.compile(patternText);
        Matcher matcher = pattern.matcher(rawJson);
        if (!matcher.find()) {
            return null;
        }

        int start = matcher.end() - 1;
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int i = start; i < rawJson.length(); i++) {
            char c = rawJson.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\' && inString) {
                escaped = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                continue;
            }
            if (inString) {
                continue;
            }

            if (c == '[') {
                depth++;
            } else if (c == ']') {
                depth--;
                if (depth == 0) {
                    return rawJson.substring(start + 1, i);
                }
            }
        }
        return null;
    }

    private List<String> splitArrayObjects(String arrayText) {
        List<String> objects = new ArrayList<>();
        int depth = 0;
        int start = -1;
        boolean inString = false;
        boolean escaped = false;
        for (int i = 0; i < arrayText.length(); i++) {
            char c = arrayText.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\' && inString) {
                escaped = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                continue;
            }
            if (inString) {
                continue;
            }

            if (c == '{') {
                if (depth == 0) {
                    start = i;
                }
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    objects.add(arrayText.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return objects;
    }

    private Question parseQuestion(String text) {
        String id = extractString(text, "id");
        String topic = extractString(text, "topic");
        String difficulty = extractString(text, "difficulty");
        String question = extractString(text, "question");
        String explanation = extractString(text, "explanation");
        String[] options = extractStringArray(text, "options");
        int answerIndex = extractInt(text, "answerIndex", 0);

        if (id == null || question == null || options == null) {
            System.err.println("[D9] Warning: invalid question entry skipped.");
            return null;
        }
        if (options.length < 4) {
            System.err.println("[D9] Warning: question " + id + " has fewer than 4 options.");
        }
        if (options.length > 4) {
            System.err.println("[D9] Warning: question " + id + " has more than 4 options; D9 UI shows the first 4 buttons.");
        }
        if (answerIndex < 0 || answerIndex >= options.length) {
            System.err.println("[D9] Warning: question " + id + " has invalid answerIndex=" + answerIndex + ".");
            return null;
        }
        return new Question(id, topic, difficulty, question, options, answerIndex, explanation);
    }

    private void validateQuestionBank() {
        for (String setName : REQUIRED_SETS) {
            if (!hasQuestionsForSet(setName)) {
                System.err.println("[D9] Warning: required question set missing: " + setName);
                continue;
            }
            List<Question> questions = parseQuestionsForSet(setName, false);
            if (questions.isEmpty()) {
                System.err.println("[D9] Warning: required question set has no usable questions: " + setName);
            } else {
                System.out.println("[D9] Question set loaded: " + setName + " (" + questions.size() + " questions)");
            }
        }
    }

    private List<Question> parseQuestionsForSet(String setName, boolean warnMissing) {
        List<Question> questions = new ArrayList<>();
        String arrayText = extractArrayForKey(setName);
        if (arrayText == null) {
            if (warnMissing) {
                System.err.println("[D9] Warning: missing question set: " + setName);
            }
            return questions;
        }

        List<String> objects = splitArrayObjects(arrayText);
        for (String objectText : objects) {
            Question question = parseQuestion(objectText);
            if (question != null) {
                questions.add(question);
            }
        }
        return questions;
    }

    private String extractString(String text, String key) {
        int valueStart = findValueStart(text, key);
        if (valueStart < 0 || valueStart >= text.length() || text.charAt(valueStart) != '"') {
            return null;
        }
        return parseJsonString(text, valueStart);
    }

    private String[] extractStringArray(String text, String key) {
        int valueStart = findValueStart(text, key);
        if (valueStart < 0 || valueStart >= text.length() || text.charAt(valueStart) != '[') {
            return null;
        }

        int start = valueStart;
        int depth = 0;
        int endIndex = -1;
        boolean inString = false;
        boolean escaped = false;
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\' && inString) {
                escaped = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                continue;
            }
            if (inString) {
                continue;
            }

            if (c == '[') {
                depth++;
            } else if (c == ']') {
                depth--;
                if (depth == 0) {
                    endIndex = i;
                    break;
                }
            }
        }

        if (endIndex < 0) {
            return null;
        }

        String arrayContent = text.substring(start + 1, endIndex);
        List<String> values = new ArrayList<>();
        for (int i = 0; i < arrayContent.length(); i++) {
            if (arrayContent.charAt(i) == '"') {
                String value = parseJsonString(arrayContent, i);
                if (value != null) {
                    values.add(value);
                    i = skipJsonString(arrayContent, i);
                }
            }
        }
        return values.toArray(new String[0]);
    }

    private int extractInt(String text, String key, int defaultValue) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    private int findValueStart(String text, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:");
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return -1;
        }
        int index = matcher.end();
        while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
            index++;
        }
        return index;
    }

    private String parseJsonString(String text, int quoteIndex) {
        if (quoteIndex < 0 || quoteIndex >= text.length() || text.charAt(quoteIndex) != '"') {
            return null;
        }

        StringBuilder value = new StringBuilder();
        boolean escaped = false;
        for (int i = quoteIndex + 1; i < text.length(); i++) {
            char c = text.charAt(i);
            if (escaped) {
                switch (c) {
                    case '"':
                    case '\\':
                    case '/':
                        value.append(c);
                        break;
                    case 'b':
                        value.append('\b');
                        break;
                    case 'f':
                        value.append('\f');
                        break;
                    case 'n':
                        value.append('\n');
                        break;
                    case 'r':
                        value.append('\r');
                        break;
                    case 't':
                        value.append('\t');
                        break;
                    default:
                        value.append(c);
                        break;
                }
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                return value.toString();
            } else {
                value.append(c);
            }
        }
        return null;
    }

    private int skipJsonString(String text, int quoteIndex) {
        boolean escaped = false;
        for (int i = quoteIndex + 1; i < text.length(); i++) {
            char c = text.charAt(i);
            if (escaped) {
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                return i;
            }
        }
        return text.length() - 1;
    }
}
