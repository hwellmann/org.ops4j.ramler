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
package org.ops4j.ramler.typescript;

import static java.util.stream.Collectors.joining;
import static org.ops4j.ramler.java.JavaConstants.TYPE_ARGS;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ops4j.ramler.common.model.Annotations;
import org.ops4j.ramler.common.model.ApiVisitor;
import org.ops4j.ramler.java.Names;
import org.raml.v2.api.model.v10.bodies.Response;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.raml.v2.api.model.v10.methods.Method;
import org.trimou.util.ImmutableMap;

/**
 * Creates the import statements of a TypeScript module corresponding to a RAML resource.
 *
 * @author Harald Wellmann
 *
 */
public class MethodApiVisitor implements ApiVisitor {

    private TypeScriptGeneratorContext context;

    static class Parameter {

        private String name;

        private String type;

        Parameter(String name, String type) {
            this.name = name;
            this.type = type;
        }

        /**
         * Gets the name.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the type.
         *
         * @return the type
         */
        public String getType() {
            return type;
        }
    }

    /**
     * Creates a visitor with the given generator context.
     *
     * @param context
     *            generator context
     */
    public MethodApiVisitor(TypeScriptGeneratorContext context) {
        this.context = context;
    }

    @Override
    public void visitMethodStart(Method method) {
        List<TypeDeclaration> bodies = method.body();
        TypeDeclaration body = bodies.isEmpty() ? null : bodies.get(0);
        String name = buildMethodName(method, -1);
        Response response = method.responses()
            .get(0);
        String returnType = "void";
        List<TypeDeclaration> responseBody = response.body();
        if (!responseBody.isEmpty()) {
            returnType = typeWithArgs(responseBody.get(0));
        }
        List<Parameter> parameters = new ArrayList<>();
        addBodyParameters(body, parameters);
        addPathParameters(method, parameters);
        addQueryParameters(method, parameters);

        Map<String, Object> contextObject = ImmutableMap.of("name", name,
            "returnType", returnType,
            "parameters", parameters);
        context.getMustache("method")
            .render(context.getOutput(), contextObject);
    }

    private void addBodyParameters(TypeDeclaration body, List<Parameter> parameters) {
        if (body != null) {
            parameters.add(new Parameter("body", typeWithArgs(body)));
        }
    }

    private void addPathParameters(Method method, List<Parameter> parameters) {
        context.getApiModel()
            .findAllUriParameters(method)
            .stream()
            .map(p -> new Parameter(p.name(), typeWithArgs(p)))
            .forEach(parameters::add);
    }

    private void addQueryParameters(Method method, List<Parameter> parameters) {
        method.queryParameters()
            .stream()
            .map(p -> new Parameter(p.name(), typeWithArgs(p)))
            .forEach(parameters::add);
    }

    private String typeWithArgs(TypeDeclaration property) {
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

    private String buildMethodName(Method method, int bodyIndex) {
        String methodName = buildMethodName(method);
        if (bodyIndex > 0) {
            TypeDeclaration responseType = method.responses()
                .get(0)
                .body()
                .get(bodyIndex);
            String codeName = Annotations.findCodeName(responseType);
            if (codeName == null) {
                methodName += Integer.toString(bodyIndex);
            }
            else {
                methodName = codeName;
            }
        }
        return methodName;
    }

    private String buildMethodName(Method method) {
        String name = Annotations.findCodeName(method);
        if (name == null) {
            name = method.displayName()
                .value();
        }
        if (name == null) {
            name = method.method();
        }
        return Names.buildVariableName(name);
    }

}