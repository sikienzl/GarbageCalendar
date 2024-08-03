package kienzle.garbage;
/*
 * Copyright 2024 Siegfried Kienzle
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.awt.Color;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import kienzle.type.Type;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GarbageCan {
    private String colour;
    private Type type;

    public GarbageCan() {}

    public GarbageCan(String colour, Type type) {
        this.colour = colour;
        this.type = type;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public String getColour() {
        return this.colour;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public Color getColorObject() {
        try {
            switch (this.colour.toLowerCase()) {
                case "yellow":
                    return Color.YELLOW;
                case "blue":
                    return new Color(0, 0, 255); // RGB für Blau
                case "brown":
                    return new Color(139, 69, 19);
                case "black":
                    return Color.BLACK;
                default:
                    return null; // Farbe nicht gefunden
            }


            //return Color.decode(this.colour);
        } catch (NumberFormatException e) {
            return Color.WHITE; // Standardfarbe, wenn die Farbe ungültig ist
        }
    }

    public static void saveToXML(List<GarbageCan> garbageCans, String filename) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();

        // Root element
        Element rootElement = doc.createElement("GarbageCans");
        doc.appendChild(rootElement);

        for (GarbageCan gc : garbageCans) {
            Element garbageCanElement = doc.createElement("GarbageCan");

            Element colourElement = doc.createElement("Colour");
            colourElement.appendChild(doc.createTextNode(gc.getColour()));
            garbageCanElement.appendChild(colourElement);

            Element typeElement = doc.createElement("Type");
            typeElement.appendChild(doc.createTextNode(gc.getType().toString()));
            garbageCanElement.appendChild(typeElement);

            rootElement.appendChild(garbageCanElement);
        }

        // Write the content into an XML file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(filename));

        transformer.transform(source, result);
    }

    public static List<GarbageCan> loadFromXML(String filename) throws Exception {
        List<GarbageCan> garbageCans = new ArrayList<>();

        File xmlFile = new File(filename);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("GarbageCan");

        for (int i = 0; i < nList.getLength(); i++) {
            Element element = (Element) nList.item(i);

            String colour = element.getElementsByTagName("Colour").item(0).getTextContent();
            Type type = Type.valueOf(element.getElementsByTagName("Type").item(0).getTextContent());

            garbageCans.add(new GarbageCan(colour, type));
        }

        return garbageCans;
    }
}
