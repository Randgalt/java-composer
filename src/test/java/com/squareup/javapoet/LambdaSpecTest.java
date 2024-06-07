package com.squareup.javapoet;

import javax.lang.model.element.Modifier;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

public class LambdaSpecTest {

    /** Ensure that void inputs result in exception. */
    @Test public void ensureInputTypeError() {
        // parameter with void type - should be rejected
        ParameterSpec p1 = ParameterSpec.builder(TypeName.VOID, "x").build();

        try {
            // check that void type will cause errors
            LambdaSpec.builder("2 * x").addInput(p1).build();

            fail("Managed to create a lambda with void type input.");
        } catch (IllegalArgumentException expected) {
            assertThat(expected).hasMessageThat().isEqualTo(
                "lambda input parameters cannot be of void type"
            );
        }
    }

    /** Ensure that not all type inputs are allowed. */
    @Test public void ensureInputModifierError() {
        // parameter with modifier different than final - should be rejected
        ParameterSpec p1 = ParameterSpec.builder(TypeName.INT, "x")
            .addModifiers(Modifier.PUBLIC).build();

        try {
            // check that input will cause errors
            LambdaSpec.builder("2 * x").addInput(p1).build();

            fail("Managed to create a lambda with input of public access.");
        } catch (IllegalArgumentException expected) {
            assertThat(expected).hasMessageThat().isEqualTo(
                "lambda input parameters can only be final"
            );
        }
    }

    @Test public void ensureMethodSpecCompatibility() {
        // lambda inputs
        ParameterSpec p1 = ParameterSpec.builder(TypeName.INT, "x").build();
        ParameterSpec p2 = ParameterSpec.builder(TypeName.DOUBLE, "y").build();
    
        // lambda body that considers input values
        CodeBlock body1 = CodeBlock.of("int $3N = 3; return $1N + $2N + $3N;", "x", "y", "z");
    
        // lambda body that does not consider input values
        CodeBlock body2 = CodeBlock.of("int $1N = 3; int $2N = 5; return $1N + $2N;", "x", "y");

        // lambdas of different nature
        LambdaSpec lambda1 = LambdaSpec.builder(body1).addInput(p1, p2).visibleTypes().build();
        LambdaSpec lambda2 = LambdaSpec.builder("x + y").addInput(p2).build();
        LambdaSpec lambda3 = LambdaSpec.builder(body2).build();
        LambdaSpec lambda4 = LambdaSpec.builder("5 + 7").build();
        LambdaSpec lambda5 = LambdaSpec.builder("method1(); method2();").build();
        LambdaSpec lambda6 = LambdaSpec.builder("x + 5").addInput(p1).visibleTypes().build();

        MethodSpec method = MethodSpec.methodBuilder("method")
        .addCode("methodCall(")
        .addLambda(lambda1).addCode(", ") // lambda with multiple inputs and (CodeBlock) body and visible types
        .addLambda(lambda2).addCode(", ") // lambda with single input and (String) body
        .addLambda(lambda3).addCode(", ") // lambda with no inputs and (CodeBlock) body
        .addLambda(lambda4).addCode(", ") // lambda with no inputs and (String) body
        .addLambda(lambda5).addCode(", ") // lambda with multiple statements
        .addLambda(lambda6).addCode(");\n") // lambda with single input of visible type
        .build();

        assertThat(method.toString()).isEqualTo(""
          + "void method() {\n"
          +  "  methodCall("
          +    "(int x, double y) -> {int z = 3; return x + y + z;}, "
          +    "y -> x + y, "
          +    "() -> {int x = 3; int y = 5; return x + y;}, "
          +    "() -> 5 + 7, "
          +    "() -> {method1(); method2();}, "
          +    "(int x) -> x + 5"
          +  ");\n"
          + "}\n"
        );
    }

    @Test public void ensureCodeBlockCompatibility() {
        // lambda body that does not consider input values
        CodeBlock body = CodeBlock.of("int $1N = 3; int $2N = 5; return $1N + $2N;", "x", "y");
    
        // lambda expression that does not consider input values
        CodeBlock body2 = CodeBlock.of("5 + 3");
    
        LambdaSpec lambda1 = LambdaSpec.builder(body).build();
        LambdaSpec lambda2 = LambdaSpec.builder(body2).build();
        LambdaSpec lambda3 = LambdaSpec.builder(body).bodyOnNewLine().indentedBody().build();

        CodeBlock codeWithLambda = CodeBlock.builder()
        .add("methodCall(")
        .addLambda(lambda1).add(", ") // producer lambda
        .addLambda(lambda2).add(");\n")
        .add("Producer<Integer> pr = ")
        .addLambda(lambda3)
        .build();
    
        MethodSpec method = MethodSpec.methodBuilder("method")
        .addCode(codeWithLambda)
        .build();

        assertThat(method.toString()).isEqualTo(""
          + "void method() {\n"
          +  "  methodCall("
          +  "() -> {int x = 3; int y = 5; return x + y;}, "
          +   "() -> 5 + 3"
          +  ");\n"
          +  "  Producer<Integer> pr = () -> {\n"
          +  "    int x = 3; int y = 5; return x + y;\n"
          +  "  }\n"
          + "}\n"
        );
    }
    
}
