package com.asascience.ncsos;

import com.asascience.ncsos.service.Parser;
import java.io.*;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.asascience.ncsos.outputformatter.OutputFormatter;

import static org.junit.Assert.*;

import com.asascience.ncsos.util.XMLDomUtils;
import org.jdom.Element;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import ucar.nc2.dataset.NetcdfDataset;

@RunWith(Parameterized.class)
public class DSNetworkTest extends NcSOSTest {

    private Element currentFile;
    public DSNetworkTest(Element file){
    	this.currentFile = file;
    }

    public static void setUpClass() throws Exception {
        NcSOSTest.setUpClass();

        // Modify the outputs
        outputDir += "DescribeSensor-Network" + NcSOSTest.systemSeparator;
        exampleDir += "DescribeSensor-Network" + NcSOSTest.systemSeparator;

        // Create output directories if they don't exist
        new File(outputDir).mkdirs();
        new File(exampleDir).mkdirs();

        kvp.put("outputFormat", URLEncoder.encode("text/xml;subtype=\"sensorML/1.0.1/profiles/ioos_sos/1.0\"", "UTF-8"));
        kvp.put("request", "DescribeSensor");
        kvp.put("procedure", "urn:ioos:network:ncsos:all");
    }

   	// Create the parameters for the test constructor
    @Parameters
    public static Collection<Object[]> testCases() throws Exception {
    	setUpClass();
        Object[][] data = new Object[fileElements.size()][1];
        int curIndex = 0;
        for (Element e : fileElements) {
            data[curIndex][0] = e;
            curIndex++;
        }
    	return Arrays.asList(data);
    }

    @Test
    public void testNetworkDescribeSensor() {
        File   file     = new File("resources" + systemSeparator + "datasets" + systemSeparator + this.currentFile.getAttributeValue("path"));
        String fullPath = file.getAbsolutePath();
        String feature  = this.currentFile.getAttributeValue("feature");
        String output   = new File(outputDir + systemSeparator + file.getName() + ".xml").getAbsolutePath();

        System.out.println();
        System.out.println("------ " + file + " (" + feature + ") ------");

        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(file.getAbsolutePath());
            Parser parser = new Parser();
            Writer writer = new CharArrayWriter();

            OutputFormatter outputFormat = (OutputFormatter) parser.enhanceGETRequest(dataset, this.getQueryString(), fullPath).get("outputHandler");
            outputFormat.writeOutput(writer);

            // Write to disk
            System.out.println("------ Saving output: " + output +" ------");
            NcSOSTest.fileWriter(output, writer);

            // Now we need to load the resulting XML and do some actual tests.
            Element root = XMLDomUtils.loadFile(output).getRootElement();

        } catch (IOException ex) {
            assertTrue(ex.getMessage(), false);
        } finally {
            System.out.println("------ END " + file + " END ------");
        }
    	
    }
    
}
