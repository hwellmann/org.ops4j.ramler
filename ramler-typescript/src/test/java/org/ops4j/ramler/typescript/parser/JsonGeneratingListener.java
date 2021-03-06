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
package org.ops4j.ramler.typescript.parser;

import java.io.StringWriter;

import javax.json.Json;
import javax.json.stream.JsonGenerator;

import org.ops4j.ramler.typescript.parser.TypeScriptParser.ArrayTypeContext;
import org.ops4j.ramler.typescript.parser.TypeScriptParser.BaseTypeContext;
import org.ops4j.ramler.typescript.parser.TypeScriptParser.EnumDeclContext;
import org.ops4j.ramler.typescript.parser.TypeScriptParser.EnumMemberContext;
import org.ops4j.ramler.typescript.parser.TypeScriptParser.ExportsContext;
import org.ops4j.ramler.typescript.parser.TypeScriptParser.ExtendsDeclContext;
import org.ops4j.ramler.typescript.parser.TypeScriptParser.IdentifiersContext;
import org.ops4j.ramler.typescript.parser.TypeScriptParser.ImportDeclContext;
import org.ops4j.ramler.typescript.parser.TypeScriptParser.ImportsContext;
import org.ops4j.ramler.typescript.parser.TypeScriptParser.InterfaceDeclContext;
import org.ops4j.ramler.typescript.parser.TypeScriptParser.MemberContext;
import org.ops4j.ramler.typescript.parser.TypeScriptParser.MembersContext;
import org.ops4j.ramler.typescript.parser.TypeScriptParser.ModuleContext;
import org.ops4j.ramler.typescript.parser.TypeScriptParser.ParamTypeContext;
import org.ops4j.ramler.typescript.parser.TypeScriptParser.SimpleTypeContext;
import org.ops4j.ramler.typescript.parser.TypeScriptParser.TypeAliasContext;
import org.ops4j.ramler.typescript.parser.TypeScriptParser.TypeDeclContext;
import org.ops4j.ramler.typescript.parser.TypeScriptParser.TypeRefElemContext;
import org.ops4j.ramler.typescript.parser.TypeScriptParser.TypeRefsContext;
import org.ops4j.ramler.typescript.parser.TypeScriptParser.TypeVarsContext;
import org.ops4j.ramler.typescript.parser.TypeScriptParser.UnionTypeContext;
import org.ops4j.ramler.typescript.parser.TypeScriptParser.VariantContext;
import org.trimou.util.ImmutableMap;

/**
 * @author Harald Wellmann
 *
 */
public class JsonGeneratingListener extends TypeScriptBaseListener {

    private StringWriter stringWriter;

    private JsonGenerator generator;

    public String getJson() {
        return stringWriter.toString();
    }

    @Override
    public void enterModule(ModuleContext ctx) {
        stringWriter = new StringWriter();
        generator = Json
            .createGeneratorFactory(ImmutableMap.of(JsonGenerator.PRETTY_PRINTING, true))
            .createGenerator(stringWriter);
        generator.writeStartObject();
    }

    @Override
    public void exitModule(ModuleContext ctx) {
        generator.writeEnd();
        generator.close();
    }

    @Override
    public void enterImports(ImportsContext ctx) {
        generator.writeStartArray("imports");
    }

    @Override
    public void exitImports(ImportsContext ctx) {
        generator.writeEnd();
    }

    @Override
    public void enterExports(ExportsContext ctx) {
        generator.writeStartArray("exports");
    }

    @Override
    public void exitExports(ExportsContext ctx) {
        generator.writeEnd();
    }

    @Override
    public void enterImportDecl(ImportDeclContext ctx) {
        generator.writeStartObject();
        generator.write("module", ctx.STRING()
            .getText());
        generator.writeStartArray("identifiers");
    }

    @Override
    public void exitImportDecl(ImportDeclContext ctx) {
        generator.writeEnd();
        generator.writeEnd();
    }

    @Override
    public void enterIdentifiers(IdentifiersContext ctx) {
        ctx.ID()
            .forEach(id -> generator.write(id.getText()));
    }

    @Override
    public void enterInterfaceDecl(InterfaceDeclContext ctx) {
        generator.writeStartObject();
        generator.write("discriminator", "interface");

    }

    @Override
    public void exitInterfaceDecl(InterfaceDeclContext ctx) {
        generator.writeEnd();
    }

    @Override
    public void enterTypeAlias(TypeAliasContext ctx) {
        generator.writeStartObject();
        generator.write("discriminator", "alias");
        generator.write("name", ctx.ID()
            .getText());
        generator.writeStartObject("type");
    }

    @Override
    public void exitTypeAlias(TypeAliasContext ctx) {
        generator.writeEnd();
        generator.writeEnd();
    }

    @Override
    public void enterTypeDecl(TypeDeclContext ctx) {
        generator.writeStartObject("type");
        generator.write("name", ctx.ID()
            .getText());
    }

    @Override
    public void exitTypeDecl(TypeDeclContext ctx) {
        generator.writeEnd();
    }

    @Override
    public void enterEnumDecl(EnumDeclContext ctx) {
        generator.writeStartObject();
        generator.write("discriminator", "enum");
        generator.write("name", ctx.ID()
            .getText());
        generator.writeStartArray("members");
    }

    @Override
    public void exitEnumDecl(EnumDeclContext ctx) {
        generator.writeEnd();
        generator.writeEnd();
    }

    @Override
    public void enterEnumMember(EnumMemberContext ctx) {
        generator.writeStartObject();
        generator.write("name", ctx.ID()
            .getText());
        if (ctx.STRING() != null) {
            generator.write("value", ctx.STRING()
                .getText());
        }
        generator.writeEnd();
    }

    @Override
    public void enterTypeVars(TypeVarsContext ctx) {
        generator.writeStartArray("vars");
    }

    @Override
    public void exitTypeVars(TypeVarsContext ctx) {
        generator.writeEnd();
    }

    @Override
    public void enterExtendsDecl(ExtendsDeclContext ctx) {
        generator.writeStartArray("extends");
    }

    @Override
    public void exitExtendsDecl(ExtendsDeclContext ctx) {
        generator.writeEnd();
    }

    @Override
    public void enterSimpleType(SimpleTypeContext ctx) {
        generator.write("discriminator", "simple");
        generator.write("name", ctx.ID()
            .getText());
    }

    @Override
    public void enterArrayType(ArrayTypeContext ctx) {
        generator.write("discriminator", "array");
        generator.write("name", ctx.ID()
            .getText());
    }

    @Override
    public void enterParamType(ParamTypeContext ctx) {
        generator.write("discriminator", "param");
        generator.write("name", ctx.ID()
            .getText());
    }

    @Override
    public void enterUnionType(UnionTypeContext ctx) {
        generator.write("discriminator", "union");
        generator.writeStartArray("variants");
    }

    @Override
    public void exitUnionType(UnionTypeContext ctx) {
        generator.writeEnd();
    }

    @Override
    public void enterVariant(VariantContext ctx) {
        generator.writeStartObject();
    }

    @Override
    public void exitVariant(VariantContext ctx) {
        generator.writeEnd();
    }

    @Override
    public void enterTypeRefs(TypeRefsContext ctx) {
        generator.writeStartArray("types");
    }

    @Override
    public void exitTypeRefs(TypeRefsContext ctx) {
        generator.writeEnd();
    }

    @Override
    public void enterTypeRefElem(TypeRefElemContext ctx) {
        generator.writeStartObject();
    }

    @Override
    public void exitTypeRefElem(TypeRefElemContext ctx) {
        generator.writeEnd();
    }

    @Override
    public void enterBaseType(BaseTypeContext ctx) {
        generator.writeStartObject();
    }

    @Override
    public void exitBaseType(BaseTypeContext ctx) {
        generator.writeEnd();
    }

    @Override
    public void enterMembers(MembersContext ctx) {
        generator.writeStartArray("members");
    }

    @Override
    public void exitMembers(MembersContext ctx) {
        generator.writeEnd();
    }

    @Override
    public void enterMember(MemberContext ctx) {
        generator.writeStartObject()
            .write("name", ctx.ID()
                .getText())
            .writeStartObject("type");
    }

    @Override
    public void exitMember(MemberContext ctx) {
        generator.writeEnd();
        generator.writeEnd();
    }
}
