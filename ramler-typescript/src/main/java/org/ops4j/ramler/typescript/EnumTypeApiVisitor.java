/*
 * Copyright 2017 OPS4J Contributors
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
package org.ops4j.ramler.typescript;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ops4j.ramler.common.model.ApiVisitor;
import org.ops4j.ramler.common.model.EnumValue;
import org.ops4j.ramler.java.JavaNameFactory;
import org.raml.v2.api.model.v10.datamodel.StringTypeDeclaration;
import org.trimou.util.ImmutableMap;

/**
 * Generates a TypeScript enum class for a RAML enumeration type.
 *
 * @author Harald Wellmann
 *
 */
public class EnumTypeApiVisitor implements ApiVisitor {

    private TypeScriptGeneratorContext context;

    private StringBuilder output;

    private List<EnumSymbol> enumSymbols = new ArrayList<>();

    /**
     * Represents an enumeration member.
     *
     */
    static class EnumSymbol {

        private String symbol;

        private String value;

        /**
         * Creates an enumeration member with the given symbol and value.
         *
         * @param symbol
         *            member symbol
         * @param value
         *            optional value
         */
        EnumSymbol(String symbol, String value) {
            this.symbol = symbol;
            this.value = value;
        }

        /**
         * Gets the symbol of this member.
         *
         * @return the symbol
         */
        public String getSymbol() {
            return symbol;
        }

        /**
         * Gets the valu of this member
         *
         * @return the value, or null
         */
        public String getValue() {
            return value;
        }
    }

    /**
     * Creates a new visitor with the given generation context.
     *
     * @param context
     *            generation context
     */
    public EnumTypeApiVisitor(TypeScriptGeneratorContext context) {
        this.context = context;
        this.output = context.startOutput();
    }

    @Override
    public void visitEnumTypeEnd(StringTypeDeclaration type) {
        String name = type.name();

        Map<String, Object> contextObject = ImmutableMap.of("name", name, "enumValues",
            enumSymbols);

        context.getMustache("enum")
            .render(output, contextObject);

        context.writeToFile(output.toString(), type.name());
        enumSymbols.clear();
    }

    @Override
    public void visitEnumValue(StringTypeDeclaration type, EnumValue enumValue) {
        EnumSymbol enumSymbol = new EnumSymbol(
            JavaNameFactory.buildConstantName(enumValue.getName()),
            enumValue.getName());
        enumSymbols.add(enumSymbol);
    }
}
