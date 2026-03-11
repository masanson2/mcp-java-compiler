package it.masanson.mcpjavacompiler.service;

import it.masanson.mcpjavacompiler.model.ProjectInspectionResult;
import it.masanson.mcpjavacompiler.security.CommandPolicy;
import it.masanson.mcpjavacompiler.security.PathPolicy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@ApplicationScoped
public class ProjectInspectionService {

    private final PathPolicy pathPolicy;
    private final CommandPolicy commandPolicy;

    @Inject
    public ProjectInspectionService(PathPolicy pathPolicy, CommandPolicy commandPolicy) {
        this.pathPolicy = pathPolicy;
        this.commandPolicy = commandPolicy;
    }

    public ProjectInspectionResult inspect(String projectPathValue) {
        Path projectPath = pathPolicy.resolveAndValidate(projectPathValue);
        Path pomPath = projectPath.resolve("pom.xml");
        if (!Files.exists(pomPath)) {
            return ProjectInspectionResult.missingPom();
        }

        Document pom = readPom(pomPath);
        Element projectElement = pom.getDocumentElement();

        List<String> modules = readModules(projectElement);
        boolean singleModule = modules.isEmpty();
        String packaging = readDirectChildText(projectElement, "packaging");
        if (packaging == null || packaging.isBlank()) {
            packaging = "jar";
        }

        return new ProjectInspectionResult(
                "maven",
                true,
                packaging,
                modules,
                singleModule,
                commandPolicy.render(commandPolicy.buildCompileCommand(false, false)),
                commandPolicy.render(commandPolicy.buildCompileCommand(true, false)));
    }

    private Document readPom(Path pomPath) {
        try {
            String xml = Files.readString(pomPath);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            factory.setExpandEntityReferences(false);
            factory.setNamespaceAware(false);
            return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        } catch (IOException | SAXException | RuntimeException | javax.xml.parsers.ParserConfigurationException e) {
            throw new IllegalArgumentException("Cannot parse pom.xml in projectPath.", e);
        }
    }

    private List<String> readModules(Element projectElement) {
        Node modulesNode = findDirectChild(projectElement, "modules");
        if (modulesNode == null) {
            return List.of();
        }

        List<String> modules = new ArrayList<>();
        for (int i = 0; i < modulesNode.getChildNodes().getLength(); i++) {
            Node child = modulesNode.getChildNodes().item(i);
            if (child instanceof Element element && "module".equals(element.getTagName())) {
                String value = element.getTextContent();
                if (value != null && !value.isBlank()) {
                    modules.add(value.trim());
                }
            }
        }
        return List.copyOf(modules);
    }

    private String readDirectChildText(Element element, String tagName) {
        Node child = findDirectChild(element, tagName);
        return child == null ? null : child.getTextContent().trim();
    }

    private Node findDirectChild(Element parent, String tagName) {
        for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
            Node child = parent.getChildNodes().item(i);
            if (child instanceof Element element && tagName.equals(element.getTagName())) {
                return element;
            }
        }
        return null;
    }
}
