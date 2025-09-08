/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.javapoet;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.squareup.javapoet.Util.checkArgument;
import static com.squareup.javapoet.Util.checkNotNull;
import static com.squareup.javapoet.Util.checkState;

/** A generated field declaration. */
public final class FieldSpec {
  public final TypeName type;
  public final String name;
  public final CodeBlock javadoc;
  public final List<AnnotationSpec> annotations;
  public final Set<Modifier> modifiers;
  public final CodeBlock initializer;

  private FieldSpec(Builder builder) {
    this.type = checkNotNull(builder.type, "type == null");
    this.name = checkNotNull(builder.name, "name == null");
    this.javadoc = builder.javadoc.build();
    this.annotations = Util.immutableList(builder.annotations);
    this.modifiers = Util.immutableSet(builder.modifiers);
    this.initializer = (builder.initializer == null)
        ? CodeBlock.builder().build()
        : builder.initializer;
  }

  /**
   * Checks if this field has the modifier that is specified.
   * @param modifier the modifier to check for
   * @return true if the field has the modifier, false otherwise
   */
  public boolean hasModifier(Modifier modifier) {
    return modifiers.contains(modifier);
  }

  /**
   * Uses the given CodeWriter to emit this field .
   * @param codeWriter the code writer used to emit the field
   * @param implicitModifiers the set of the field's implicit modifiers
   * @throws IOException if an I/O error occurs
   */
  void emit(CodeWriter codeWriter, Set<Modifier> implicitModifiers) throws IOException {
    codeWriter.emitJavadoc(javadoc);
    codeWriter.emitAnnotations(annotations, false);
    codeWriter.emitModifiers(modifiers, implicitModifiers);
    codeWriter.emit("$T $L", type, name);
    if (!initializer.isEmpty()) {
      codeWriter.emit(" = ");
      codeWriter.emit(initializer);
    }
    codeWriter.emit(";\n");
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
      emit(codeWriter, Collections.emptySet());
      return out.toString();
    } catch (IOException e) {
      throw new AssertionError();
    }
  }

  /**
   * Returns a new builder for a {@link FieldSpec}.
   * @param type the type of the field
   * @param name the name of the field
   * @param modifiers the modifiers to apply to the field
   * @return a new FieldSpec builder
   */
  public static Builder builder(TypeName type, String name, Modifier... modifiers) {
    checkNotNull(type, "type == null");
    checkArgument(SourceVersion.isName(name), "not a valid name: %s", name);
    return new Builder(type, name)
        .addModifiers(modifiers);
  }

  /**
   * Returns a new builder for a FieldSpec.
   * @param type the type of the field
   * @param name the name of the field
   * @param modifiers the modifiers to apply to the field
   * @return a new FieldSpec builder
   */
  public static Builder builder(Type type, String name, Modifier... modifiers) {
    return builder(TypeName.get(type), name, modifiers);
  }

  /**
   * Creates and returns a new builder using the values of an already
   * existing FieldSpec
   * @return the new builder
   */
  public Builder toBuilder() {
    Builder builder = new Builder(type, name);
    builder.javadoc.add(javadoc);
    builder.annotations.addAll(annotations);
    builder.modifiers.addAll(modifiers);
    builder.initializer = initializer.isEmpty() ? null : initializer;
    return builder;
  }

  public static final class Builder {
    private final TypeName type;
    private final String name;

    private final CodeBlock.Builder javadoc = CodeBlock.builder();
    private CodeBlock initializer = null;

    public final List<AnnotationSpec> annotations = new ArrayList<>();
    public final List<Modifier> modifiers = new ArrayList<>();

    private Builder(TypeName type, String name) {
      this.type = type;
      this.name = name;
    }

    /**
     * Adds the Javadoc comment to the builder of the field.
     * @param format the format of the Javadoc comment
     * @param args the arguments to replace the format's placehoders
     * @return this builder
     */
    public Builder addJavadoc(String format, Object... args) {
      javadoc.add(format, args);
      return this;
    }

    /**
     * Adds the Javadoc comment to the builder of the field.
     * @param block the body of the Javadoc comment
     * @return this builder
     */
    public Builder addJavadoc(CodeBlock block) {
      javadoc.add(block);
      return this;
    }

    /**
     * Adds the annotations to the builder of the FieldSpec.
     * @param annotationSpecs the annotations to be added
     * @return this builder
     */
    public Builder addAnnotations(Iterable<AnnotationSpec> annotationSpecs) {
      checkArgument(annotationSpecs != null, "annotationSpecs == null");
      for (AnnotationSpec annotationSpec : annotationSpecs) {
        this.annotations.add(annotationSpec);
      }
      return this;
    }

    /**
     * Used to add a single annotation to the builder of the FieldSpec.
     * @param annotationSpec the annotation to be added
     * @return this builder
     */
    public Builder addAnnotation(AnnotationSpec annotationSpec) {
      this.annotations.add(annotationSpec);
      return this;
    }

    /**
     * Used to add a single annotation to the builder of the FieldSpec.
     * @param annotation the ClassName representing the annotation's class
     * @return this builder
     */
    public Builder addAnnotation(ClassName annotation) {
      this.annotations.add(AnnotationSpec.builder(annotation).build());
      return this;
    }

    /**
     * Used to add a single annotation to the builder of the FieldSpec.
     * @param annotation the Class of the annotation
     * @return this builder
     */
    public Builder addAnnotation(Class<?> annotation) {
      return addAnnotation(ClassName.get(annotation));
    }

    /**
     * Adds modifiers to the builder of the FieldSpec
     * @param modifiers the modifiers to be added
     * @return this builder
     */
    public Builder addModifiers(Modifier... modifiers) {
      Collections.addAll(this.modifiers, modifiers);
      return this;
    }

    /**
     * Adds the initializer of the field
     * @param format the format of the initializer
     * @param args the arguments to replace the format's placeholders
     * @return this builder
     */
    public Builder initializer(String format, Object... args) {
      return initializer(CodeBlock.of(format, args));
    }

    /**
     * Adds the initializer of the field
     * @param codeBlock the code block of the initializer
     * @return this builder
     */
    public Builder initializer(CodeBlock codeBlock) {
      checkState(this.initializer == null, "initializer was already set");
      this.initializer = checkNotNull(codeBlock, "codeBlock == null");
      return this;
    }
    
    /**
     * Builds the FieldSpec instance
     * @return the built FieldSpec
     */
    public FieldSpec build() {
      return new FieldSpec(this);
    }
  }
}
