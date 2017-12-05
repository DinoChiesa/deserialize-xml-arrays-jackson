package com.google.xmldeser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class BasicXmlDeserializationTest {
    private final static String testDataDir = "src/test/resources";
    private XmlMapper xmlMapper;
    private ObjectMapper objectMapper;
    private static boolean wantStrictComparison = true;
    private static boolean wantVerbose = false;
    
    @BeforeClass
    public void beforeClass() throws Exception {
        xmlMapper = new XmlMapper();
        // use this deserializer to intelligently infer arrays when multiple child elements of the same name occur
        xmlMapper.registerModule(new SimpleModule().addDeserializer(Object.class, new ArrayInferringUntypedObjectDeserializer()));
        objectMapper = new ObjectMapper();
    }
    
    @DataProvider(name = "batch1")
    public Object[][] getDataForBatch1() throws Exception {

        // @DataProvider requires the output to be a Object[][]. The inner
        // Object[] is the set of params that get passed to the test method.
        // So, if you want to pass just one param to the constructor, then
        // each inner Object[] must have length 1.

        // ObjectMapper om = new ObjectMapper();
        // om.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //
        //  Path currentRelativePath = Paths.get("");
        //  String s = currentRelativePath.toAbsolutePath().toString();
        //  System.out.println("Current relative path is: " + s);

        // read in all the *.xml files in the test/resources directory
        File testDir = new File(testDataDir);
        if (!testDir.exists()) {
            throw new IllegalStateException("no test directory found at "+ testDir.getCanonicalPath());
        }
        File[] files = testDir.listFiles();
        if (files.length == 0) {
            throw new IllegalStateException("no tests found.");
        }
        int c=0;
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        for (File file : files) {
            String name = file.getName();
            if (name.endsWith(".xml") && !name.startsWith(".#")) {
                String jsonFileName = file.getCanonicalPath().replaceAll(".xml$",".json");
                File jsonFile = new File(jsonFileName);
                list.add(new Object[] { file, jsonFile });
            }
        }
        return list.toArray(new Object[list.size()][]);
    }

    @Test
    public void insureReasonableNumberOfConfigurationDrivenTests() throws Exception {
        Object[][] tests = getDataForBatch1();
        if (wantVerbose)
            System.out.printf("** Found %d test XML files in dir %s\n", tests.length, testDataDir);
        Assert.assertTrue( tests.length > 5, "more than 5 tests");
    }

    @Test(dataProvider = "batch1")
    public void test_Configs(File xmlFile, File jsonFile) throws Exception {
        String cpath = xmlFile.getCanonicalPath();
        String xml = new String(Files.readAllBytes(Paths.get(cpath)), StandardCharsets.UTF_8);
        if (wantVerbose)
            System.out.printf("%s\nINPUT:\n%s\n", cpath, xml);
        XMLStreamReader sr = XMLInputFactory.newFactory().createXMLStreamReader(new FileInputStream(cpath));
        Map map = (Map) xmlMapper.readValue(sr, Object.class);
        
        Assert.assertTrue( jsonFile.exists(), "file " + jsonFile.getCanonicalPath());
        String expectedJson = new String(Files.readAllBytes(Paths.get(jsonFile.getCanonicalPath())), StandardCharsets.UTF_8);
        String actualJson = objectMapper.writeValueAsString(map);
        if (wantVerbose) {
            System.out.printf("ACTUAL OUTPUT:\n%s\n", actualJson);
            System.out.printf("EXPECTED OUTPUT:\n%s\n", expectedJson);
        }
        JSONAssert.assertEquals(cpath, actualJson, expectedJson, wantStrictComparison);
    }
    
}
