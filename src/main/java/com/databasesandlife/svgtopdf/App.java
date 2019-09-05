package com.databasesandlife.svgtopdf;

import org.apache.batik.apps.rasterizer.DestinationType;
import org.apache.batik.apps.rasterizer.SVGConverter;
import org.apache.batik.apps.rasterizer.SVGConverterException;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class App {
    
    private static Document readSvg() throws Exception {
        Class c = App.class;
        String resourceName = c.getPackageName().replaceAll("\\.", "/") + "/input.svg";
        try (InputStream stream = c.getClassLoader().getResourceAsStream(resourceName)) {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);     // See https://stackoverflow.com/a/49800040
            return dbf.newDocumentBuilder().parse(stream);
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }
    
    private static byte[] convertSvgToPdf(Document svgXmlDoc) throws SVGConverterException {
        try {
            File svgFile = File.createTempFile("graphic-", ".svg");
            svgFile.deleteOnExit();
            TransformerFactory tFac = TransformerFactory.newInstance();
            Transformer transformer = tFac.newTransformer();
            DOMSource source2 = new DOMSource(svgXmlDoc);
            try (FileOutputStream fOut = new FileOutputStream(svgFile)) {
                transformer.transform(source2, new StreamResult(fOut));
            }
    
            File outputFile = File.createTempFile("result-", ".pdf");
            outputFile.deleteOnExit();
            SVGConverter converter = new SVGConverter();
            converter.setDestinationType(DestinationType.PDF);
            converter.setSources(new String[] { svgFile.getPath() });
            converter.setDst(outputFile);
            converter.execute();
            
            return FileUtils.readFileToByteArray(outputFile);
        }
        catch (IOException | TransformerException e) { throw new RuntimeException(e); }
    }
    
    // Slightly contrived example to show conversion of a DOM representation of an SVG, to a byte[] PDF
    public static void main(String[] args) throws Exception {
        Document svgXmlDoc = readSvg();
        byte[] pdf = convertSvgToPdf(svgXmlDoc);
        FileUtils.writeByteArrayToFile(new File("output.pdf"), pdf);
    }
}
