/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.test.test;

import junit.framework.AssertionFailedError;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.test.ESTestCase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ESTestCaseTests extends ESTestCase {

    public void testExpectThrows() {
        IllegalArgumentException e = expectThrows(IllegalArgumentException.class, () -> {
            throw new IllegalArgumentException("bad arg");
        });
        assertEquals("bad arg", e.getMessage());

        try {
            expectThrows(IllegalArgumentException.class, () -> {
               throw new IllegalStateException("bad state");
            });
            fail("expected assertion error");
        } catch (AssertionFailedError assertFailed) {
            assertEquals("Unexpected exception type, expected IllegalArgumentException", assertFailed.getMessage());
            assertNotNull(assertFailed.getCause());
            assertEquals("bad state", assertFailed.getCause().getMessage());
        }

        try {
            expectThrows(IllegalArgumentException.class, () -> {});
            fail("expected assertion error");
        } catch (AssertionFailedError assertFailed) {
            assertNull(assertFailed.getCause());
            assertEquals("Expected exception IllegalArgumentException", assertFailed.getMessage());
        }
    }

    public void testShuffleXContent() throws IOException {
        Map<String, Object> randomStringObjectMap = randomStringObjectMap(5);
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.map(randomStringObjectMap);
        XContentBuilder shuffleXContent = shuffleXContent(builder, Collections.emptySet());
        XContentParser parser = XContentFactory.xContent(shuffleXContent.bytes()).createParser(shuffleXContent.bytes());
        Map<String, Object> resultMap = parser.map();
        assertEquals("both maps should contain the same mappings", randomStringObjectMap, resultMap);
        assertNotEquals("Both builders string representations should be different", builder.string(), shuffleXContent.string());
    }

    private static Map<String, Object> randomStringObjectMap(int depth) {
        Map<String, Object> result = new HashMap<>();
        int entries = randomInt(10);
        for (int i = 0; i < entries; i++) {
            String key = randomAsciiOfLengthBetween(5, 15);
            int suprise = randomIntBetween(0, 4);
            switch (suprise) {
            case 0:
                result.put(key, randomUnicodeOfCodepointLength(20));
                break;
            case 1:
                result.put(key, randomInt(100));
                break;
            case 2:
                result.put(key, randomDoubleBetween(-100.0, 100.0, true));
                break;
            case 3:
                result.put(key, randomBoolean());
                break;
            case 4:
                List<String> stringList = new ArrayList<>();
                int size = randomInt(5);
                for (int s = 0; s < size; s++) {
                    stringList.add(randomUnicodeOfCodepointLength(20));
                }
                result.put(key, stringList);
                break;
            default:
                throw new IllegalArgumentException("unexpected random option: " + suprise);
            }
        }
        if (depth > 0) {
            result.put(randomAsciiOfLengthBetween(5, 15), randomStringObjectMap(depth - 1));
        }
        return result;
    }
}
