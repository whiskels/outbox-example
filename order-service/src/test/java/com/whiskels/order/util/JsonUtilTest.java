package com.whiskels.order.util;

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonUtilTest {

    @Test
    public void testToJson() {
        TestObject testObject = new TestObject();
        testObject.setId(1);
        testObject.setName("Test Name");

        String jsonString = JsonUtil.toJson(testObject);

        String expectedJson = "{\"id\":1,\"name\":\"Test Name\"}";
        assertEquals(expectedJson, jsonString);
    }

    @Test
    public void testToJsonWithException() {
        assertThrows(InvalidDefinitionException.class, () -> JsonUtil.toJson(new Object()));
    }

    static class TestObject {
        private int id;
        private String name;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}