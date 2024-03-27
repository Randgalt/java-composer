[![Maven Build](https://github.com/Randgalt/java-composer/actions/workflows/ci.yml/badge.svg)](https://github.com/Randgalt/java-composer/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.soabase.java-composer/java-composer.svg?sort=date)](https://search.maven.org/search?q=g:io.soabase.java-composer%20a:java-composer)

# Java Composer

This is a soft-fork of [JavaPoet](https://github.com/square/javapoet). JavaPoet appears to have been abandoned and is missing 
support for post-Java 8 features. This repository exists solely to add those missing features until a time when JavaPoet 
chooses to reactivate.

Please see [JavaPoet](https://github.com/square/javapoet) for documentation, licensing, etc.

# Changes From JavaPoet

### March 26, 2024
- Require Java 17
- Add record support from https://github.com/square/javapoet/pull/981
- Support sealed/non-sealed/permits
