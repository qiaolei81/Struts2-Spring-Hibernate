package rml.util;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class JacksonJsonUtilTest {

    /** Reset the static singleton mapper between tests to avoid shared-state interference. */
    @Before
    public void resetSingleton() throws Exception {
        Field f = JacksonJsonUtil.class.getDeclaredField("mapper");
        f.setAccessible(true);
        f.set(null, null);
    }

    // --- getMapperInstance ---

    @Test
    public void getMapperInstance_createNewFalse_returnsSameInstance() {
        ObjectMapper first  = JacksonJsonUtil.getMapperInstance(false);
        ObjectMapper second = JacksonJsonUtil.getMapperInstance(false);
        assertNotNull(first);
        assertSame("Should return the same singleton", first, second);
    }

    @Test
    public void getMapperInstance_createNewTrue_returnsNewInstance() {
        ObjectMapper first  = JacksonJsonUtil.getMapperInstance(false);
        ObjectMapper second = JacksonJsonUtil.getMapperInstance(true);
        assertNotSame("createNew=true should produce a new instance", first, second);
    }

    // --- beanToJson ---

    @Test
    public void beanToJson_simpleMap_producesExpectedJson() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("key", "value");
        String json = JacksonJsonUtil.beanToJson(map);
        assertNotNull(json);
        assertTrue(json.contains("\"key\""));
        assertTrue(json.contains("\"value\""));
    }

    @Test
    public void beanToJson_integerValue_encodedCorrectly() throws Exception {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("count", 42);
        String json = JacksonJsonUtil.beanToJson(map);
        assertTrue(json.contains("42"));
    }

    @Test
    public void beanToJson_nullValue_encodedAsNull() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("k", null);
        String json = JacksonJsonUtil.beanToJson(map);
        assertTrue(json.contains("null"));
    }

    @Test
    public void beanToJson_withCreateNewTrue_producesValidJson() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("a", "b");
        String json = JacksonJsonUtil.beanToJson(map, true);
        assertNotNull(json);
        assertTrue(json.startsWith("{"));
    }

    // --- jsonToBean ---

    @Test
    public void jsonToBean_validJson_deserializesCorrectly() throws Exception {
        String json = "{\"name\":\"Alice\",\"age\":30}";
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) JacksonJsonUtil.jsonToBean(json, Map.class);
        assertEquals("Alice", result.get("name"));
        assertEquals(30, result.get("age"));
    }

    @Test
    public void jsonToBean_withCreateNewTrue_deserializesCorrectly() throws Exception {
        String json = "{\"x\":1}";
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) JacksonJsonUtil.jsonToBean(json, Map.class, true);
        assertEquals(1, result.get("x"));
    }

    // --- round-trip ---

    @Test
    public void roundTrip_beanToJsonAndBack_preservesData() throws Exception {
        Map<String, String> original = new HashMap<String, String>();
        original.put("city", "Beijing");
        original.put("country", "China");
        String json = JacksonJsonUtil.beanToJson(original);
        @SuppressWarnings("unchecked")
        Map<String, String> restored = (Map<String, String>) JacksonJsonUtil.jsonToBean(json, Map.class);
        assertEquals(original.get("city"),    restored.get("city"));
        assertEquals(original.get("country"), restored.get("country"));
    }

    // --- error cases ---

    @Test(expected = Exception.class)
    public void jsonToBean_malformedJson_throwsException() throws Exception {
        JacksonJsonUtil.jsonToBean("not-json", Map.class);
    }
}
