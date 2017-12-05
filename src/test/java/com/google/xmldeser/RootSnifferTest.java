package com.google.xmldeser;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class RootSnifferTest {
    @Test
    public void test_RootSniffer() throws Exception {
        String xmlInput = "<Root><Messages><Message>Hello</Message><Message>World</Message></Messages></Root>";
        InputStream is = new ByteArrayInputStream(xmlInput.getBytes(StandardCharsets.UTF_8));
        RootSniffingXMLStreamReader sr = new RootSniffingXMLStreamReader(XMLInputFactory.newFactory().createXMLStreamReader(is));
        XmlMapper xmlMapper = new XmlMapper();
        Map map = (Map) xmlMapper.readValue(sr, Object.class);
        Assert.assertEquals( sr.getLocalNameForRootElement(), "Root");
    }

    @Test
    public void test_BothRootSnifferAndArrayInference() throws Exception {
        String xmlInput = "<Root><Messages><Message>Hello</Message><Message>World</Message></Messages></Root>";
        InputStream is = new ByteArrayInputStream(xmlInput.getBytes(StandardCharsets.UTF_8));
        RootSniffingXMLStreamReader sr = new RootSniffingXMLStreamReader(XMLInputFactory.newFactory().createXMLStreamReader(is));
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.registerModule(new SimpleModule().addDeserializer(Object.class, new ArrayInferringUntypedObjectDeserializer()));
        Map map = (Map) xmlMapper.readValue(sr, Object.class);
        Assert.assertEquals( sr.getLocalNameForRootElement(), "Root");
        Object messages = map.get("Messages");
        Assert.assertTrue( messages instanceof Map, "map");
        Object list = ((Map)messages).get("Message");
        Assert.assertTrue( list instanceof List, "list");
        Assert.assertEquals( ((List)list).get(0), "Hello");
        Assert.assertEquals( ((List)list).get(1), "World");
    }
}
