package com.squareup.javapoet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;
import static com.squareup.javapoet.Util.checkArgument;
import static com.squareup.javapoet.Util.checkNotNull;;

public class LambdaSpec {

    /**
     * The available format modes a lambda function can have.
     * {@code VISIBLE_TYPES} example: (int x, int y) -> x + y;
     * {@code BODY_NEWLINE} example:
     * (x, y) ->
     * x + y;
     * {@code BODY_NEWLINE} + {@code BODY_INDENT} example:
     * (x, y) -> {
     *  int z = 3; return x + y + z;
     * }
    */
    private enum LambdaMode {
        VISIBLE_TYPES,
        BODY_INDENT,
        BODY_NEWLINE
    }

    /** The inputs of the function. */
    public List<ParameterSpec> inputs = new ArrayList<>();

    /** The body of the function. */
    public CodeBlock body;

    /** @see #LambdaSpec.LambdaMode LambdaMode.*/
    public Set<LambdaMode> modes = new LinkedHashSet<>();

    private LambdaSpec(Builder builder) {
        this.inputs = checkNotNull(builder.inputs, "inputs == null");
        validateInputs(this.inputs);
        this.body = checkNotNull(builder.body, "body == null");
        this.modes = checkNotNull(builder.modes, "modes == null");
    }

    /**
     * Emits the left hand side of the lambda function, meaning
     * the inputs.
     * @param codeWriter the writer used to append the constructed
     * inputs.
     * @throws IOException
     */
    void emitInputs(CodeWriter codeWriter) throws IOException {
        boolean emitTypes = false;
        for (LambdaMode mode : this.modes) {
            // handle all related modes the user specified.
            // add cases when needed.
            switch (mode) {
                case VISIBLE_TYPES:
                    emitTypes = true;
                    break;
            }
        }

        boolean placeParenthesis = inputs.size() > 1 || emitTypes || inputs.isEmpty();

        codeWriter.emit(placeParenthesis ? "(" : "");

        int paramsPlaced = 0;

        // the inputs of the lambda (left side)
        for (ParameterSpec inputParameter : inputs) {
            if (emitTypes) {
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
        boolean indented = false;
        boolean newLine = false;
        for (LambdaMode mode : this.modes) {
            // handle all related modes the user specified.
            // add cases when needed.
            switch (mode) {
                case BODY_INDENT:
                    indented = true;
                    break;
                case BODY_NEWLINE:
                    newLine = true;
                    break;
            }
        }

        String bodySide = this.body.toString();

        // true if the function has more than 1 statement in its body
        boolean multiStatementBody = bodySide.contains(";");

        codeWriter.emit(multiStatementBody ? "{" : "");

        codeWriter.emit(newLine ? "\n" : "");
        if (indented) {
            codeWriter.indent();
            codeWriter.emit(body);
            codeWriter.unindent();
        } else {
            codeWriter.emit(body);
        }
        codeWriter.emit(newLine ? "\n" : "");

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
            codeWriter.emit(" -> ");
            emitBody(codeWriter);
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
                !inputParameter.modifiers.stream().anyMatch(p -> !p.equals(Modifier.FINAL)),
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
        builder.modes.addAll(modes);
        return builder;
    }

    public static final class Builder {
        public List<ParameterSpec> inputs = new ArrayList<>();
        public CodeBlock body;
        public Set<LambdaMode> modes = new LinkedHashSet<>();

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

        /** Makes the body of the function appear on the new line. */
        public Builder bodyOnNewLine() {
            this.addMode(LambdaMode.BODY_NEWLINE);
            return this;
        }

        /** Makes the body of the function appear indented. */
        public Builder indentedBody() {
            this.addMode(LambdaMode.BODY_INDENT);
            return this;
        }

        /** Makes the inputs of the function appear with their types. */
        public Builder visibleTypes() {
            this.addMode(LambdaMode.VISIBLE_TYPES);
            return this;
        }

        /** Adds an input to the function. */
        public Builder addInput(ParameterSpec... parameters) {
            for (ParameterSpec parameter : parameters) {
                this.inputs.add(parameter);
            }
            return this;
        }

        // private: the user should not be responsible for the enum usage.
        private Builder addMode(LambdaMode... modes) {
            for (LambdaMode mode : modes) {
                this.modes.add(mode);
            }
            return this;
        }

        public LambdaSpec build() {
            return new LambdaSpec(this);
        }
    }
}
