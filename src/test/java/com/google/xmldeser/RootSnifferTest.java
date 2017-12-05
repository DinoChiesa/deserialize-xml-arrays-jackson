package com.google.xmldeser;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class RootSnifferTest {
    @Test
    public void test_RootSniffer() throws Exception {
        String xmlInput = "<Root><Message>Hello</Message></Root>";
        InputStream is = new ByteArrayInputStream(xmlInput.getBytes(StandardCharsets.UTF_8));
        RootSniffingXMLStreamReader sr = new RootSniffingXMLStreamReader(XMLInputFactory.newFactory().createXMLStreamReader(is));
        XmlMapper xmlMapper = new XmlMapper();
        Map map = (Map) xmlMapper.readValue(sr, Object.class);
        Assert.assertEquals( sr.getLocalNameForRootElement(), "Root");
    }
}
