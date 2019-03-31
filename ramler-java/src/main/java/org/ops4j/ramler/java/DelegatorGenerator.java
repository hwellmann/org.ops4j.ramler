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
package org.ops4j.ramler.java;

import java.util.stream.Stream;

import org.ops4j.ramler.common.exc.Exceptions;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.codemodel.JTypeVar;
import com.sun.codemodel.JVar;

/**
 * Creates a delegator class for a given model class. The delegator class has a protected
 * {@code delegate} member of the target type and accessor methods delegating to the target.
 * <p>
 * Users can extend the delegator class, e.g. to implement custom builder methods for complex use
 * cases.
 *
 * @author Harald Wellmann
 *
 */
public class DelegatorGenerator {

    private JavaGeneratorContext context;

    /**
     * Creates a delegator generator with the given context.
     *
     * @param context
     *            generator context
     */
    public DelegatorGenerator(JavaGeneratorContext context) {
        this.context = context;
    }

    /**
     * Generates a delegator for the given class.
     *
     * @param delegateClass
     *            delegate class
     */
    public void generateDelegator(JDefinedClass delegateClass) {
        JPackage pkg = context.getDelegatorPackage();
        try {
            JDefinedClass delegator = pkg
                ._class(delegateClass.name() + context.getConfig().getDelegatorSuffix());
            context.annotateAsGenerated(delegator);

            JClass delegateType = delegateClass;

            if (delegateClass.typeParams().length > 0) {
                Stream.of(delegateClass.typeParams()).map(JTypeVar::name)
                    .forEach(delegator::generify);
                delegateType = delegateClass.narrow(delegateClass.typeParams());
            }

            JFieldVar delegate = delegator.field(JMod.PROTECTED, delegateType,
                context.getConfig().getDelegateFieldName());
            delegate.init(JExpr._new(delegateType));

            JDefinedClass klass = delegateClass;
            while (klass != null) {
                buildDelegatingMethods(klass, delegator, delegate);
                if (klass._extends() instanceof JDefinedClass) {
                    klass = (JDefinedClass) klass._extends();
                }
                else {
                    klass = null;
                }
            }
        }
        catch (JClassAlreadyExistsException exc) {
            throw Exceptions.unchecked(exc);
        }
    }

    private void buildDelegatingMethods(JDefinedClass delegateClass, JDefinedClass delegator,
        JFieldVar delegate) {
        for (JMethod method : delegateClass.methods()) {
            buildDelegatingMethod(delegator, method, delegate);
        }
    }

    private void buildDelegatingMethod(JDefinedClass delegator, JMethod method,
        JFieldVar delegate) {
        if (isSetter(method)) {
            buildDelegatingSetter(delegator, method, delegate);
        }
        else if (isGetter(method)) {
            buildDelegatingGetter(delegator, method, delegate);
        }
    }

    private void buildDelegatingGetter(JDefinedClass delegator, JMethod method,
        JFieldVar delegate) {
        if (delegator.getMethod(method.name(), new JType[0]) == null) {
            JMethod delegatingMethod = delegator.method(JMod.PUBLIC, method.type(), method.name());
            delegatingMethod.body()._return(delegate.invoke(method.name()));
        }
    }

    private void buildDelegatingSetter(JDefinedClass delegator, JMethod method,
        JFieldVar delegate) {
        JType type = method.listParamTypes()[0];
        if (delegator.getMethod(method.name(), new JType[] { type }) == null) {
            JMethod delegatingMethod = delegator.method(JMod.PUBLIC, method.type(), method.name());
            JVar p1 = delegatingMethod.param(type, method.listParams()[0].name());
            delegatingMethod.body().invoke(delegate, delegatingMethod.name()).arg(p1);
        }
    }

    private boolean isSetter(JMethod method) {
        return method.name().matches("set[A-Z]\\w*");
    }

    private boolean isGetter(JMethod method) {
        return method.name().matches("(get|is)[A-Z]\\w*");
    }
}