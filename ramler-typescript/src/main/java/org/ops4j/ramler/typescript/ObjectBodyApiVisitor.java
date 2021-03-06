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

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.ops4j.ramler.common.model.CommonConstants.OBJECT;
import static org.ops4j.ramler.java.JavaConstants.TYPE_ARGS;
import static org.ops4j.ramler.java.JavaConstants.TYPE_VARS;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ops4j.ramler.common.helper.NameFactory;
import org.ops4j.ramler.common.model.Annotations;
import org.ops4j.ramler.common.model.ApiVisitor;
import org.raml.v2.api.model.v10.datamodel.ArrayTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.ObjectTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.trimou.util.ImmutableMap;

/**
 * Creates the exported interface declaration of a TypeScript module corresponding to a RAML object
 * type.
 *
 * @author Harald Wellmann
 *
 */
public class ObjectBodyApiVisitor implements ApiVisitor {

    private TypeScriptGeneratorContext context;
    private NameFactory nameFactory;

    /**
     * Creates a visitor with the given generator context.
     *
     * @param context
     *            generator context
     */
    public ObjectBodyApiVisitor(TypeScriptGeneratorContext context) {
        this.context = context;
        this.nameFactory = new TypeScriptNameFactory();
    }

    @Override
    public void visitObjectTypeStart(ObjectTypeDeclaration type) {
        List<String> baseClasses = type.parentTypes()
            .stream()
            .filter(t -> !t.name()
                .equals(OBJECT))
            .map(t -> this.typeWithArgs(type, t))
            .collect(toList());

        List<String> typeVars = Annotations.getStringAnnotations(type, TYPE_VARS);

        Map<String, Object> contextObject = ImmutableMap.of("name", type.name(), "baseClasses",
            baseClasses, "typeVars", typeVars);

        context.getMustache("objectStart")
            .render(context.getOutput(), contextObject);
    }

    @Override
    public void visitObjectTypeEnd(ObjectTypeDeclaration type) {
        context.getMustache("objectEnd")
            .render(context.getOutput(), Collections.emptyMap());
    }

    @Override
    public void visitObjectTypeProperty(ObjectTypeDeclaration type, TypeDeclaration property) {
        if (property instanceof ArrayTypeDeclaration) {
            generateArrayProperty((ArrayTypeDeclaration) property);
        }
        else {
            generateProperty(property);
        }
    }

    /**
     * @param type
     * @param property
     */
    private void generateArrayProperty(ArrayTypeDeclaration property) {
        String fieldName = nameFactory.buildVariableName(property);
        String itemTypeName = context.getApiModel()
            .getItemType(property);
        String typeVar = Annotations.findTypeVar(property);
        String tsItemType;
        if (typeVar != null) {
            tsItemType = typeVar;
        }
        else {
            tsItemType = itemTypeName;
        }
        Map<String, Object> contextObject = ImmutableMap.of("name", fieldName, "tsPropType",
            tsItemType + "[]", "optional", !property.required());
        context.getMustache("property")
            .render(context.getOutput(), contextObject);
    }

    /**
     * @param type
     * @param property
     */
    private void generateProperty(TypeDeclaration property) {
        String fieldName = nameFactory.buildVariableName(property);
        String tsPropType;
        String typeVar = Annotations.findTypeVar(property);
        if (typeVar != null) {
            tsPropType = typeVar;
        }
        else {
            tsPropType = propertyTypeWithArgs(property);
        }
        Map<String, Object> contextObject = ImmutableMap.of("name", fieldName, "tsPropType",
            tsPropType, "optional", !property.required());
        context.getMustache("property")
            .render(context.getOutput(), contextObject);
    }

    /**
     * @param property
     * @return
     */
    private String propertyTypeWithArgs(TypeDeclaration property) {
        String tsPropType;
        tsPropType = context.getTypeScriptPropertyType(property);
        List<String> typeArgs = Annotations.getStringAnnotations(property, TYPE_ARGS);
        if (!typeArgs.isEmpty()) {
            StringBuilder builder = new StringBuilder(tsPropType);
            builder.append("<");
            builder.append(typeArgs.stream()
                .collect(joining(", ")));
            builder.append(">");
            tsPropType = builder.toString();
        }
        return tsPropType;
    }

    private String typeWithArgs(TypeDeclaration annotated, TypeDeclaration type) {
        String tsPropType;
        tsPropType = context.getTypeScriptType(type);
        List<String> typeArgs = Annotations.getStringAnnotations(annotated, TYPE_ARGS);
        if (!typeArgs.isEmpty()) {
            StringBuilder builder = new StringBuilder(tsPropType);
            builder.append("<");
            builder.append(typeArgs.stream()
                .collect(joining(", ")));
            builder.append(">");
            tsPropType = builder.toString();
        }
        return tsPropType;
    }

}
