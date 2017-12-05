# demo: reading arbitrary XML with Jackson

This demonstrates how to read arbitrary XML into a java.util.Map using fasterxml jackson.
There's no pre-compiled marked-up class required.

## Background

There are two challenges to deserializing arbitrary XML with Jackson.

1. XML has a root element, while a JSON object does not. The Jackson library was designed to handle JSON  primarily, so when deserializing XML into a java.util.Map, jackskon loses or discards the root element.

2. The UntypedObjectDeserializer in Jackson does not map multiple child elements with the same name into a list or array. 


As an example of item 1, if you deserialize with jackson's default UntypedObjectDeserializer, given this XML input:
```xml
<Root>
  <Parameters>
    <Parameter name='A'>valueA</Parameter> <!-- level 2 -->
  </Parameters>
</Root>
```

...you will get this output (shown in json form):
```json
{
  "Parameters" : {
    "Parameter" : {
      "name" : "A",
      "" : "valueA"
    }
  }
}
```

Notice the Root element is gone.



As an example of item 2, if you deserialize with jackson's default UntypedObjectDeserializer,
if you add a second Parameter element, like this:
```xml
<Root>
  <Parameters>
    <Parameter name='A'>valueA</Parameter> <!-- level 2 -->
    <Parameter name='B'>valueB</Parameter>
  </Parameters>
</Root>
```

...the second child element named Parameter
overwrites the previous one. This is the JSON output:
```json
{
  "Parameters" : {
    "Parameter" : {
      "name" : "B",
      "" : "valueB"
    }
  }
}
```


With the ArrayInferringUntypedObjectDeserializer class, the result for the first input is the same, but the result for the second input is:
```json
{
  "Parameters" : {
    "Parameter" : [
      {
        "name" : "A",
        "" : "valueA"
      },{
        "name" : "B",
        "" : "valueB"
      }
    ]
  }
}
```


Example usage of both classes:

```java
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
```

## The Implementation

The array inference depends on an extension of Jackson's builtin
UntypedObjectDeserializer. The one included here checks for existence of the key in the
map being generated during deserialization. If the key is present, the deserializer
class infers that this element name should de-serialize into an array.

There is no explicit support for serialization - for writing java.util.Map into XML.
That may work, but it's not an intended capability of this module.


## Build and run tests

```
mvn clean test
```

This will look into the [tests/resources](./src/tests/resources) directory for .xml
files, and deserialize each one.  Every XML file should have a companion .json file,
which shows the expected json serialization of the map. A missing .json file will cause
a test failure. an Extraneous .json file will not.


## Dependencies

* jackson-core
* jackson-databind
* jackson-dataformat-xml

Notice there is no dependency on jackson-annotations.


## License

The  material in this repo is Copyright 2017 Google Inc.
and is licensed under the [Apache 2.0 License](LICENSE).



