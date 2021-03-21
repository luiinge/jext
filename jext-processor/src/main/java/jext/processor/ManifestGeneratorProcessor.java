package jext.processor;

import static java.util.stream.Collectors.*;
import java.io.File;
import java.util.*;
import javax.annotation.processing.*;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import org.apache.maven.model.*;
import org.apache.maven.model.building.*;
import org.apache.maven.model.building.ModelProblem.Severity;
import org.apache.maven.model.interpolation.StringVisitorModelInterpolator;

public class ManifestGeneratorProcessor {

    private ProcessingHelper helper;

    public boolean process(ProcessingEnvironment processingEnv, RoundEnvironment roundEnv) {
        boolean generateManifest = Boolean.parseBoolean(
            processingEnv.getOptions().getOrDefault("generateManifest", "true")
        );
        if (generateManifest) {
            helper = new ProcessingHelper(processingEnv);
            generateManifest();
        }
        return true;
    }



    public void generateManifest() {

        Model pomModel = interpolatedPomModel();
        if (pomModel == null) {
            helper.log(Diagnostic.Kind.ERROR, "Error reading pom.xml");
            return;
        }
        String groupId = pomModel.getGroupId();
        String artifactId = pomModel.getArtifactId();
        String version = pomModel.getVersion();
        List<String> dependencies = pomModel.getDependencies().stream()
            .filter(dependency -> !"test".equals(dependency.getScope()))
            .map(this::coordinates)
            .collect(toList());

        if (groupId == null) {
            helper.log(Diagnostic.Kind.ERROR, "Property project.groupId is not defined in pom.xml");
        } else if (artifactId == null) {
            helper.log(Diagnostic.Kind.ERROR, "Property project.artifactId is not defined in pom.xml");
        } else if (version == null) {
            helper.log(Diagnostic.Kind.ERROR, "Property project.version is not defined in pom.xml");
        }
        if (groupId == null || artifactId == null || version == null) {
            return;
        }

        try {
            writeManifest(groupId,artifactId,version, dependencies);
        } catch (Exception e) {
            helper.log(Diagnostic.Kind.ERROR, "Cannot write MANIFEST.MF : {}", e.getMessage());
        }
    }



    public Model interpolatedPomModel() {

        File pomFile = new File("pom.xml");
        DefaultModelBuilder modelBuilder = new DefaultModelBuilderFactory().newInstance();
        var result = modelBuilder.buildRawModel(
            pomFile,
            ModelBuildingRequest.VALIDATION_LEVEL_STRICT,
            false
        );
        if (result.hasErrors()) {
            result.getProblems().forEach(problem -> helper.log(Kind.ERROR,problem.getMessage()));
            return null;
        }
        Model rawModel = result.get();

        var request = new DefaultModelBuildingRequest()
            .setRawModel(rawModel)
            .setProcessPlugins(false)
            .setTwoPhaseBuilding(false)
        ;
       return new StringVisitorModelInterpolator().interpolateModel(
           rawModel,
           pomFile,
           request,
           this::logModelProblem
       );

    }



    private void logModelProblem(ModelProblemCollectorRequest problem) {
        Kind kind = (problem.getSeverity() == Severity.WARNING ? Kind.WARNING : Kind.ERROR);
        String message = problem.getMessage();
        helper.log(kind, message);
    }



    private void writeManifest(
        String groupId,
        String artifactId,
        String version,
        List<String> dependencies
    ) {
        List<String> manifest = new ArrayList<>();
        manifest.add("Manifest-Version: 1.0");
        manifest.add("Archiver-Version: Plexus Archiver");
        manifest.add("Plugin-Vendor: "+groupId);
        manifest.add("Plugin-Name: "+artifactId);
        manifest.add("Plugin-Version: "+version);
        manifest.add("Plugin-Dependencies: "+dependencies.stream().collect(joining(" \n ")));
        helper.writeResource("META-INF/MANIFEST.MF",manifest);
    }



    private String coordinates(Dependency dependency) {
        return dependency.getGroupId()+":"+dependency.getArtifactId()+":"+dependency.getVersion();
    }




}
