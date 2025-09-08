package com.squareup.javapoet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.Modifier;
import static com.squareup.javapoet.Util.checkArgument;
import static com.squareup.javapoet.Util.checkNotNull;;

public class LambdaSpec {

    /**
     * The characters that will be placed *before*
     * the *arrow* of the lambda.
    */
    private final String beforeArrow;

    /** The inputs of the function. */
    private final List<ParameterSpec> inputs;

    /**
     * The characters that will be placed *before*
     * the *body* of the lambda (and before the
     * curly bracket in such cases).
    */
    private final String beforeBody;

    /** The body of the function. */
    private final CodeBlock body;

    /**
     * The characters that will be placed *after*
     * the *body* of the lambda (and after the
     * curly bracket in such cases).
    */
    private final String afterBody;

    /** The visibility status of the input parameters */
    private boolean visibleTypes;

    private LambdaSpec(Builder builder) {
        this.inputs = checkNotNull(builder.inputs, "inputs == null");
        validateInputs(this.inputs);
        this.body = checkNotNull(builder.body, "body == null");
        this.beforeArrow = checkNotNull(builder.beforeArrow, "beforeArrow == null");
        this.beforeBody = checkNotNull(builder.beforeBody, "beforeBody == null");
        this.afterBody = checkNotNull(builder.afterBody, "afterBody == null");
        this.visibleTypes = builder.visibleTypes;
    }

    /**
     * Emits the left hand side of the lambda function, meaning
     * the inputs.
     * @param codeWriter the writer used to append the constructed
     * inputs.
     * @throws IOException
     */
    void emitInputs(CodeWriter codeWriter) throws IOException {
        boolean placeParenthesis = inputs.size() > 1 || visibleTypes || inputs.isEmpty();

        codeWriter.emit(placeParenthesis ? "(" : "");

        int paramsPlaced = 0;

        // the inputs of the lambda (left side)
        for (ParameterSpec inputParameter : inputs) {
            if (visibleTypes) {
                codeWriter.emitAnnotations(inputParameter.annotations, true);
                codeWriter.emitModifiers(inputParameter.modifiers);
                codeWriter.emit(inputParameter.type.toString() + " ");
            }
            codeWriter.emit(inputParameter.name);
            if (++paramsPlaced < inputs.size())
                codeWriter.emit(", ");
        }

        codeWriter.emit(placeParenthesis ? ")" : "");
    }

    /**
     * Emits the right hand side of the lambda function, meaning
     * the body.
     * @param codeWriter the writer used to append the constructed
     * body.
     * @throws IOException
     */
    void emitBody(CodeWriter codeWriter) throws IOException {
        String bodySide = this.body.toString();

        // true if the function has more than 1 statement in its body
        boolean multiStatementBody = bodySide.contains(";");

        codeWriter.emit(multiStatementBody ? "{" : "");
        codeWriter.emit(body);
        codeWriter.emit(multiStatementBody ? "}" : "");
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (getClass() != o.getClass()) return false;
        return toString().equals(o.toString());
    }

    @Override public int hashCode() {
        return toString().hashCode();
    }

    @Override public String toString() {
        StringBuilder out = new StringBuilder();
        try {
            CodeWriter codeWriter = new CodeWriter(out);
            emitInputs(codeWriter);
            codeWriter.emit(this.beforeArrow);
            codeWriter.emit("->");
            codeWriter.emit(this.beforeBody);
            emitBody(codeWriter);
            codeWriter.emit(this.afterBody);
            return out.toString();
        } catch (IOException e) {
            throw new AssertionError();
        }
    }

    /**
     * Validates a list of parameters as lambda inputs.
     * @param inputParameters the parameters of the lambda.
     */
    private static void validateInputs(List<ParameterSpec> inputParameters) {
        for (ParameterSpec inputParameter : inputParameters) {
            checkArgument(
                !inputParameter.type.equals(TypeName.VOID),
                "lambda input parameters cannot be of void type"
            );
            checkArgument(
                inputParameter.modifiers.stream().allMatch(p -> p.equals(Modifier.FINAL)),
                "lambda input parameters can only be final"
            );
        }
    }

    public static Builder builder(List<ParameterSpec> inputs, CodeBlock body) {
        return new Builder(inputs, body);
    }

    public static Builder builder(List<ParameterSpec> inputs, String body) {
        return builder(inputs, CodeBlock.of(body));
    }

    public static Builder builder(CodeBlock body) {
        return new Builder(body);
    }

    public static Builder builder(String body) {
        return builder(CodeBlock.of(body));
    }

    public Builder toBuilder() {
        return toBuilder(inputs, body);
    }

    Builder toBuilder(List<ParameterSpec> inputs, CodeBlock body) {
        Builder builder = new Builder(inputs, body);
        return builder;
    }

    public static final class Builder {
        public String beforeArrow = " ";
        public List<ParameterSpec> inputs = new ArrayList<>();
        public String beforeBody = " ";
        public CodeBlock body;
        public String afterBody = "";
        public boolean visibleTypes;

        private Builder(List<ParameterSpec> inputs, CodeBlock body) {
            this.inputs = inputs;
            this.body = body;
        }

        private Builder(List<ParameterSpec> inputs, String body) {
            this(inputs, CodeBlock.of(body));
        }

        private Builder(CodeBlock body) {
            this.body = body;
        }

        private Builder(String body) {
            this(CodeBlock.of(body));
        }

        /** Adds an input to the function. */
        public Builder addInput(ParameterSpec... parameters) {
            Collections.addAll(this.inputs, parameters);
            return this;
        }

        public Builder beforeArrow(String code) {
            this.beforeArrow = code;
            return this;
        }

        public Builder beforeBody(String code) {
            this.beforeBody = code;
            return this;
        }

        public Builder afterBody(String code) {
            this.afterBody = code;
            return this;
        }

        public Builder visibleTypes() {
            this.visibleTypes = true;
            return this;
        }

        public LambdaSpec build() {
            return new LambdaSpec(this);
        }
    }
}
