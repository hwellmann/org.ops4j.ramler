/*
 * Copyright 2019 OPS4J Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.ramler.openapi;

import org.junit.jupiter.api.Test;

public class DiscriminatorTest extends AbstractOpenApiTest {

    @Override
    public String getBasename() {
        return "discriminator";
    }

    @Test
    public void shouldFindSchemas() {
        assertSchemas("Employee", "Manager", "Person");
    }

    @Test
    public void shouldFindPersonProperties() {
        expectSchema("Person");
        assertProperties("firstname", "lastname", "objectType");
        assertDiscriminator("objectType");
    }

    @Test
    public void shouldFindEmployeeProperties() {
        expectSchemaWithAllOf("Employee", "Person");
        assertProperties("department");
    }

    @Test
    public void shouldFindManagerProperties() {
        expectSchemaWithAllOf("Manager", "Employee");
        assertProperties("numEmployees");
    }
}
