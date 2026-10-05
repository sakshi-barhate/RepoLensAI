package com.repolens.backend.analyzer.language;

public class LanguageMetrics {

    private int classes;
    private int interfaces;
    private int functions;
    private int structs;
    private int packages;
    private int modules;
    private int headers;
    private int linesOfCode;

    public LanguageMetrics() {
    }

    public int getClasses() {
        return classes;
    }

    public void setClasses(int classes) {
        this.classes = classes;
    }

    public int getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(int interfaces) {
        this.interfaces = interfaces;
    }

    public int getFunctions() {
        return functions;
    }

    public void setFunctions(int functions) {
        this.functions = functions;
    }

    public int getStructs() {
        return structs;
    }

    public void setStructs(int structs) {
        this.structs = structs;
    }

    public int getPackages() {
        return packages;
    }

    public void setPackages(int packages) {
        this.packages = packages;
    }

    public int getModules() {
        return modules;
    }

    public void setModules(int modules) {
        this.modules = modules;
    }

    public int getHeaders() {
        return headers;
    }

    public void setHeaders(int headers) {
        this.headers = headers;
    }

    public int getLinesOfCode() {
        return linesOfCode;
    }

    public void setLinesOfCode(int linesOfCode) {
        this.linesOfCode = linesOfCode;
    }
}