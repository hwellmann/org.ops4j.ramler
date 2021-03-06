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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.ops4j.ramler.common.model.ApiTraverser;
import org.ops4j.ramler.common.model.ApiVisitor;
import org.ops4j.ramler.common.model.EnumValue;
import org.ops4j.ramler.common.model.Metatype;
import org.ops4j.ramler.java.JavaNameFactory;
import org.raml.v2.api.model.v10.datamodel.ArrayTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.NumberTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.ObjectTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.StringTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.UnionTypeDeclaration;
import org.trimou.util.ImmutableMap;

/**
 * Creates a TypeScript model for a given RAML model. Visits all top-level types and delegates to
 * type-specific visitors.
 * <p>
 * Code generation is based on Trimou templates.
 *
 * @author Harald Wellmann
 *
 */
public class ModelCreatingApiVisitor implements ApiVisitor {

    private TypeScriptGeneratorContext context;
    private EnumTypeApiVisitor enumVisitor;

    /**
     * Creates a visitor with the given generator context.
     *
     * @param context
     *            generator context
     */
    public ModelCreatingApiVisitor(TypeScriptGeneratorContext context) {
        this.context = context;
        this.enumVisitor = new EnumTypeApiVisitor(context);
    }

    @Override
    public void visitObjectTypeStart(ObjectTypeDeclaration type) {
        ObjectCreatingApiVisitor visitor = new ObjectCreatingApiVisitor(context);
        ApiTraverser traverser = new ApiTraverser(context.getApiModel());
        traverser.traverse(type, visitor);
    }

    @Override
    public void visitUnionType(UnionTypeDeclaration type) {
        String declaredName = context.getApiModel()
            .getDeclaredName(type);
        if (declaredName == null) {
            return;
        }

        Map<String, String> imports = new LinkedHashMap<>();
        for (TypeDeclaration variant : type.of()) {
            addTypeToImports(imports, variant.name());
        }
        createUnionType(type, imports);
    }

    @Override
    public void visitNumberType(NumberTypeDeclaration type) {
        createTypeAlias(type, "number");
    }

    @Override
    public void visitStringType(StringTypeDeclaration type) {
        createTypeAlias(type, "string");
    }

    @Override
    public void visitEnumValue(StringTypeDeclaration type, EnumValue enumValue) {
        enumVisitor.visitEnumValue(type, enumValue);
    }

    @Override
    public void visitEnumTypeEnd(StringTypeDeclaration type) {
        enumVisitor.visitEnumTypeEnd(type);
    }

    @Override
    public void visitArrayType(ArrayTypeDeclaration type) {
        if (context.getApiModel()
            .getDeclaredName(type) == null) {
            return;
        }
        String itemTypeName = context.getApiModel()
            .getItemType(type);
        Map<String, String> imports = new LinkedHashMap<>();
        addTypeToImports(imports, itemTypeName);

        createTypeAlias(type, itemTypeName + "[]", imports);
    }

    private void addTypeToImports(Map<String, String> imports, String typeName) {
        String tsType = typeName;
        while (tsType.endsWith("[]")) {
            tsType = tsType.substring(0, tsType.length() - 2);
        }
        if (!Metatype.isBuiltIn(tsType)) {
            String tsFile = JavaNameFactory.buildLowerKebabCaseName(tsType);
            imports.put(tsType, tsFile);
        }
    }

    private void createTypeAlias(TypeDeclaration type, String targetType) {
        createTypeAlias(type, targetType, Collections.emptyMap());
    }

    /**
     * @param type
     */
    private void createTypeAlias(TypeDeclaration type, String targetType,
        Map<String, String> imports) {
        StringBuilder output = context.startOutput();

        renderImports(imports, output);

        context.getMustache("typeAlias")
            .render(output, ImmutableMap.of("name", type.name(), "tsType", targetType));

        context.writeToFile(output.toString(), type.name());
    }

    private void createUnionType(UnionTypeDeclaration type, Map<String, String> imports) {
        StringBuilder output = context.startOutput();

        renderImports(imports, output);

        context.getMustache("union")
            .render(output, ImmutableMap.of("name", type.name(), "type", type));

        context.writeToFile(output.toString(), type.name());
    }

    private void renderImports(Map<String, String> imports, StringBuilder output) {
        if (!imports.isEmpty()) {
            imports.forEach((k, v) -> context.getMustache("import")
                .render(output, ImmutableMap.of("tsType", k, "tsFile", v)));
            output.append("\n");
        }
    }
}
