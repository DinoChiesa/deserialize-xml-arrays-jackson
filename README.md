# demo: reading arbitrary XML with Jackson

This demonstrates how to read arbitrary XML into a java.util.Map using fasterxml jackson.
There's no pre-compiled marked-up class required.

## The Implementation

The magic depends on an extension of Jackson's builtin
UntypedObjectDeserializer.  The one included here is savvy enough to
check for existence of the key in the map being generated during
deserialization. If the key has already been seen, the deserializer
class infers that this element name should de-serialize into an array.

There is no explicit support for serialization, for writing java.util.Map into XML.
That may work, but it's not an intended capability of this module.


## Build and run tests

```
mvn clean test
```

This will look into the [tests/resources](./src/tests/resources) directory for .xml files, and deserialize each one.
Every XML file should have a companion .json file, which shows the expected json serialization of the map. A missing .json file will cause a test failure. an Extraneous .json file will not.


## Dependencies

* jackson-core
* jackson-databind
* jackson-dataformat-xml

Notice there is no dependency on jackson-annotations.


## License

The  material in this repo is Copyright 2017 Google Inc.
and is licensed under the [Apache 2.0 License](LICENSE).




