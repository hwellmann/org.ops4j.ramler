package org.ops4j.ramler.common.model;

/**
 * Common constants relevant for all generators.
 *
 * @author hwellmann
 *
 */
public class CommonConstants {

    /**
     * Name of value property of annotation types.
     */
    public static final String VALUE = "value";

    /**
     * Name of Ramler {@code typeArgs} annotation.
     */
    public static final String TYPE_ARGS = "typeArgs";

    /**
     * Name of Ramler {@code typeVar} annotation.
     */
    public static final String TYPE_VAR = "typeVar";

    /**
     * Name of Ramler {@code typeVars} annotation.
     */
    public static final String TYPE_VARS = "typeVars";

    /**
     * Name of built-in RAML object type.
     */
    public static final String OBJECT = "object";

    /**
     * Name of built-in RAML integer type.
     */
    public static final String INTEGER = "integer";

    /**
     * Name of built-in RAML string type.
     */
    public static final String STRING = "string";

    /**
     * Name of built-in RAML boolean type.
     */
    public static final String BOOLEAN = "boolean";

    private CommonConstants() {
        throw new UnsupportedOperationException();
    }
}
