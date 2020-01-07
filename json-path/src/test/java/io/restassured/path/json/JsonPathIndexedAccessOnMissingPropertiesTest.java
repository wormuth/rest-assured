/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.restassured.path.json;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Explanatory tests for issue
 * 
 * @author andree.wormuth
 */
public class JsonPathIndexedAccessOnMissingPropertiesTest {

    private final static String JSON = "{\n" + 
            "    \"items\": [\n" + 
            "        {\n" + 
            "            \"a\": [\n" + 
            "                \"item1_a_v1\",\n" + 
            "                \"item1_a_v2\"\n" + 
            "            ],\n" + 
            "            \"b\": [\n" + 
            "                \"item1_b_v1\",\n" + 
            "                \"item1_b_v2\"\n" + 
            "                ]\n" + 
            "        },\n" + 
            "        {\n" + 
            "            \"a\": [\n" + 
            "                \"item2_a_v1\",\n" + 
            "                \"item2_a_v2\"\n" + 
            "                ]\n" + 
            "        },\n" + 
            "        {\n" + 
            "            \"c\": [\n" + 
            "                {\n" + 
            "                    \"data\": [\n" + 
            "                        \"item3_c_v1\",\n" + 
            "                        \"item3_c_v2\"\n" + 
            "                    ]\n" + 
            "                }\n" + 
            "            ]\n" + 
            "        }\n" + 
            "    ]\n" + 
            "}";

    @Test
    public void explanation() {
        // Item 1: contains both property a and b:

        // get all values of a of first item - passing as expected:
        assertThat( new JsonPath( JSON ).get( "items[0].a" ), contains( "item1_a_v1", "item1_a_v2" ) );

        // get one of these - passing as expected:
        assertThat( new JsonPath( JSON ).get( "items[0].a[0]" ), equalTo( "item1_a_v1" ) );

        // "alternative" navigation in nested lists is no problem - passing as expected:
        assertThat( new JsonPath( JSON ).get( "items[0].a[0]" ), equalTo( new JsonPath( JSON ).get( "items.a[0][0]" ) ) );

        // same for property b - each passing as expected:
        assertThat( new JsonPath( JSON ).get( "items[0].b" ), contains( "item1_b_v1", "item1_b_v2" ) );
        assertThat( new JsonPath( JSON ).get( "items[0].b[0]" ), equalTo( "item1_b_v1" ) );
        assertThat( new JsonPath( JSON ).get( "items[0].b[0]" ), equalTo( new JsonPath( JSON ).get( "items.b[0][0]" ) ) );
        //

        // Item 2: only property a exists, b is missing:

        // Same tests for property a - each passing as expected:
        assertThat( new JsonPath( JSON ).get( "items[1].a" ), contains( "item2_a_v1", "item2_a_v2" ) );
        assertThat( new JsonPath( JSON ).get( "items[1].a[0]" ), equalTo( "item2_a_v1" ) );
        assertThat( new JsonPath( JSON ).get( "items[1].a[0]" ), equalTo( new JsonPath( JSON ).get( "items.a[1][0]" ) ) );

        // Tests for missing property b on Item 2:

        // b is missing, so null is delivered - passing as expected:
        assertThat( new JsonPath( JSON ).get( "items[1].b" ), nullValue() );

        // b is missing, so trying to access b.c should deliver null - passing as expected:
        assertThat( new JsonPath( JSON ).get( "items[1].b.c" ), nullValue() );

        // indexed access on b fails with an IllegalArgumentException:
        // Cannot invoke method getAt() on null object:
        // assertThat( new JsonPath( JSON ).get( "items[1].b[0]" ), nullValue() );

        // Test for property c: the interesting part
        // Item 3 has a rather complicated structure:
        // "c" is an array of objects having a property "data" being an itself array

        // accessing the data as usual:
        assertThat( new JsonPath( JSON ).get( "items[2].c[0].data[0]" ), equalTo( "item3_c_v1" ) );
        // "alternative" notation works as well:
        assertThat( new JsonPath( JSON ).get( "items[2].c.data[0][0]" ),
                equalTo( new JsonPath( JSON ).get( "items[2].c[0].data[0]" ) ) );

        // now trying these 2 "c" paths for another property "d" - which does not exist:

        // the "normal" notation leads to the infamous IllegalArgumentException:
        // Cannot invoke method getAt() on null object:
        // assertThat( new JsonPath( JSON ).get( "items[2].d[0].data[0]" ), nullValue() );

        // whereas the "alternative" notation works as I hoped:
        // property does not exist? So I'll get null instead of exception:
        assertThat( new JsonPath( JSON ).get( "items[2].d.data[0][0]" ), nullValue() );
    }

    @Test
    public void no_exception_on_indexed_access_to_missing_property() {
        // indexed access on b fails with an IllegalArgumentException:
        // Cannot invoke method getAt() on null object:

        // I would expect a null value instead:
        assertThat( new JsonPath( JSON ).get( "items[1].b" ), nullValue() );    // passes
        assertThat( new JsonPath( JSON ).get( "items[1].b.c" ), nullValue() );  // passes
        assertThat( new JsonPath( JSON ).get( "items[1].b[0]" ), nullValue() ); // does not pass, but should
    }
    
    @Test
    public void no_exception_on_indexed_access_to_missing_array_property() {
        // same here:
        // the "normal" notation leads to the infamous IllegalArgumentException,
        // whereas the "alternative" notation works well for my needs:
        assertThat( new JsonPath( JSON ).get( "items[2].d.data[0][0]" ), nullValue() ); // passes
        assertThat( new JsonPath( JSON ).get( "items[2].d[0].data[0]" ), nullValue() ); // does not pass, but should
    }

}
