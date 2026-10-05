package com.repolens.backend.analyzer.language;

import java.io.File;

public interface LanguageAnalyzer {

    String getLanguage();

    LanguageMetrics analyze(File repositoryDirectory);
}