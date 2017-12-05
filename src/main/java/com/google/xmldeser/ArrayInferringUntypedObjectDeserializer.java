package com.google.xmldeser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ArrayInferringUntypedObjectDeserializer is an deserializer for Jackson that
 * infers arrays when multiple child elements of the same name occur in the
 * XML. When deserializing JSON, an array is explicit, and Jackson handles that
 * appropriately. But Jackson can also deserialize XML, and deserializing into
 * an array from XML is not always explicit.  This class is a naive
 * implementation that infers an array when there are multiple child elements of
 * the same name in the source XML. This class does not support "wrapper"
 * elements to denote arrays.
 *
 * For example, with the default UntypedObjectDeserializer, given this XML input:
 * <pre>
 * {@code
 * <Parameters>
 *   <Parameter name='A'>valueA</Parameter>
 * </Parameters>
 * }
 * </pre>
 *
 * output:
 * <pre>
 * {@code
 * [Parameters] -> (Map)
 *   [Parameter] -> (Map)
 *     [name] -> [A]
 *     [] -> [valueA]
 * }
 * </pre>
 *
 * and with this input:
 * <pre>
 * {@code
 * <Parameters>
 *   <Parameter name='A'>valueA</Parameter>
 *   <Parameter name='B'>valueB</Parameter>
 * </Parameters>
 * }
 * </pre>
 *
 * output:
 * <pre>
 * {@code
 * [Parameters] -> (Map)
 *   [Parameter] -> (Map)
 *     [name] -> [B]
 *     [] -> [valueB]
 * }
 * </pre>
 *
 * As you can see in the latter case, the second child element named Parameter
 * overwrites the previous one.
 *
 * With THIS deserializer, the result for the first input is the same. The result for the second input is:
 * <pre>
 * {@code
 * [Parameters] -> (Map)
 *   [Parameter] -> (List)[
 *     {
 *       [name] -> [A]
 *       [] -> [valueA]
 *     },
 *     {
 *       [name] -> [B]
 *       [] -> [valueB]
 *     }
 *   ]
 * }
 * </pre>
 *
 * Example usage:
 * <pre>
 * {@code
 * XmlMapper mapper = new XmlMapper();
 * mapper.registerModule(new SimpleModule().addDeserializer(Object.class, new ArrayInferringUntypedObjectDeserializer()));
 * XMLStreamReader sr = XMLInputFactory.newFactory().createXMLStreamReader(new FileInputStream(path));
 * Map m = (Map) mapper.readValue(sr, Object.class);
 * }
 * </pre>
 */
public class ArrayInferringUntypedObjectDeserializer extends UntypedObjectDeserializer {

    @Override
    protected Map<String,Object> mapObject(JsonParser jp, DeserializationContext ctxt) throws IOException
    {
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.START_OBJECT) {
            t = jp.nextToken();
        }
        if (t == JsonToken.END_OBJECT) { // empty map, eg {}
            // empty LinkedHashMap might work; but caller may want to modify... so better just give small modifiable.
            return new LinkedHashMap<String,Object>(2);
        }
        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
        do {
            String fieldName = jp.getCurrentName();
            jp.nextToken();
            result.put(fieldName, handleMultipleValue(result, fieldName, deserialize(jp, ctxt)));
        } while (jp.nextToken() != JsonToken.END_OBJECT);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Object handleMultipleValue(Map<String, Object> map, String key, Object value) {
        if (!map.containsKey(key)) {
            return value;
        }

        Object originalValue = map.get(key);
        if (originalValue instanceof List) {
            ((List) originalValue).add(value);
            return originalValue;
        }
        else {
            ArrayList newValue = new ArrayList();
            newValue.add(originalValue);
            newValue.add(value);
            return newValue;
        }
    }

}
