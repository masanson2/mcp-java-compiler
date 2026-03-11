package it.masanson.mcpjavacompiler.model;

import java.util.List;

public class ProjectInspectionResult {

    private String buildTool;
    private boolean hasPom;
    private String packaging;
    private List<String> modules;
    private boolean singleModule;
    private String recommendedCompileCommand;
    private String recommendedTestCompileCommand;

    public ProjectInspectionResult() {
    }

    public ProjectInspectionResult(String buildTool, boolean hasPom, String packaging, List<String> modules,
            boolean singleModule, String recommendedCompileCommand, String recommendedTestCompileCommand) {
        this.buildTool = buildTool;
        this.hasPom = hasPom;
        this.packaging = packaging;
        this.modules = modules;
        this.singleModule = singleModule;
        this.recommendedCompileCommand = recommendedCompileCommand;
        this.recommendedTestCompileCommand = recommendedTestCompileCommand;
    }

    public static ProjectInspectionResult missingPom() {
        return new ProjectInspectionResult("unknown", false, null, List.of(), false, null, null);
    }

    public String getBuildTool() {
        return buildTool;
    }

    public void setBuildTool(String buildTool) {
        this.buildTool = buildTool;
    }

    public boolean isHasPom() {
        return hasPom;
    }

    public void setHasPom(boolean hasPom) {
        this.hasPom = hasPom;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    public List<String> getModules() {
        return modules;
    }

    public void setModules(List<String> modules) {
        this.modules = modules;
    }

    public boolean isSingleModule() {
        return singleModule;
    }

    public void setSingleModule(boolean singleModule) {
        this.singleModule = singleModule;
    }

    public String getRecommendedCompileCommand() {
        return recommendedCompileCommand;
    }

    public void setRecommendedCompileCommand(String recommendedCompileCommand) {
        this.recommendedCompileCommand = recommendedCompileCommand;
    }

    public String getRecommendedTestCompileCommand() {
        return recommendedTestCompileCommand;
    }

    public void setRecommendedTestCompileCommand(String recommendedTestCompileCommand) {
        this.recommendedTestCompileCommand = recommendedTestCompileCommand;
    }
}
