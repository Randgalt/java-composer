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
