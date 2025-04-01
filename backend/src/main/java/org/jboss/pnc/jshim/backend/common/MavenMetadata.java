package org.jboss.pnc.jshim.backend.common;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lombok.extern.slf4j.Slf4j;

/**
 * Helper class to parse maven-metadata.xml, used by the maven repository
 */
@Slf4j
public class MavenMetadata {

    public static List<String> getVersions(String url) throws Exception {

        File tempFile = Files.createTempFile("maven-version-", ".xml").toFile();
        URL downloadUrlUrl = new URL(url);
        FileUtils.copyURLToFile(downloadUrlUrl, tempFile);

        return getVersions(tempFile);
    }

    public static List<String> getVersions(File file) throws Exception {

        // Create a DocumentBuilder
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Parse the XML file
        Document document = builder.parse(file);

        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        NodeList nodes = (NodeList) xpath
                .evaluate("/metadata/versioning/versions/version", document, XPathConstants.NODESET);

        List<String> versions = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            versions.add(node.getTextContent());
        }

        return versions;
    }
}
