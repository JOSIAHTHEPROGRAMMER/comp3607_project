package comp3607_project.compile;

import comp3607_project.compile.CompileErrorClassifier.Category;
import comp3607_project.compile.CompileErrorClassifier.Classification;

/**
 * One classification rule: if the message contains any fragment, it maps to
 * code + category.
 */
final class ClassificationRule {
    private final String code;
    private final Category category;
    private final String[] fragments;

    ClassificationRule(String code, Category category, String... fragments) {
        this.code = code;
        this.category = category;
        this.fragments = fragments;
    }

    boolean matches(String lowerCaseMessage) {
        for (String fragment : fragments) {
            if (lowerCaseMessage.contains(fragment)) {
                return true;
            }
        }
        return false;
    }

    Classification toClassification() {
        return new Classification(code, category);
    }
}