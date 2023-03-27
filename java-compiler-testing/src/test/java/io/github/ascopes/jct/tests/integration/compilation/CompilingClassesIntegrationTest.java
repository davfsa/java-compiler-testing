/*
 * Copyright (C) 2022 - 2023, the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.ascopes.jct.tests.integration.compilation;

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.junit.JavacCompilerTest;
import io.github.ascopes.jct.tests.integration.AbstractIntegrationTest;
import io.github.ascopes.jct.workspaces.Workspaces;
import org.junit.jupiter.api.DisplayName;

/**
 * Integration tests that test the generic compilation.
 *
 * @author Ashley Scopes
 */
public class CompilingClassesIntegrationTest extends AbstractIntegrationTest {
  @DisplayName("Headers are created in NATIVE_HEADER_OUTPUT")
  @JavacCompilerTest
  void headersCreated(JctCompiler<?, ?> compiler) {
    // Given
    try (var workspace = Workspaces.newWorkspace()) {
      workspace
          .createSourcePathPackage()
          .copyContentsFrom(resourcesDirectory().resolve("nativeclass"));

      // When
      var compilation = compiler.compile(workspace);

      // Then
      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings();

      assertThatCompilation(compilation)
          .classOutput()
          .packages()
          .fileExists("com", "example", "nativeclass", "NativeClass.class")
          .isNotEmptyFile();

      assertThatCompilation(compilation)
          .generatedHeaders()
          .packages()
          .fileExists("com_example_nativeclass_NativeClass.h")
          .isNotEmptyFile()
          .content()
          .contains("JNIEXPORT jint JNICALL Java_com_example_nativeclass_NativeClass_add");
    }
  }

  @DisplayName("Adding class path package is added to CLASS_PATH")
  @JavacCompilerTest
  void classPathCreated(JctCompiler<?, ?> compiler) {
    // Given
    try (
        var firstWorkspace = Workspaces.newWorkspace();
        var secondWorkspace = Workspaces.newWorkspace()
    ) {
      // Setup class path package
      firstWorkspace
          .createSourcePathPackage()
          .copyContentsFrom(resourcesDirectory().resolve("mathutils.add"));

      var firstCompilation = compiler.compile(firstWorkspace);

      assertThatCompilation(firstCompilation)
          .isSuccessfulWithoutWarnings();

      assertThatCompilation(firstCompilation)
          .classOutput()
          .packages()
          .fileExists("mathutils", "MathUtils.class")
          .isNotEmptyFile();

      // Setup second workspace
      secondWorkspace
          .createSourcePathPackage()
          .copyContentsFrom(resourcesDirectory().resolve("calculator.simple"));
      secondWorkspace.addClassPathPackage(firstWorkspace.getClassOutputPackages().get(0).getPath());

      // When
      var compilation = compiler.compile(secondWorkspace);

      // Then
      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings();

      assertThatCompilation(compilation)
          .classOutput()
          .packages()
          .fileExists("com", "example", "calculator", "Calculator.class")
          .isNotEmptyFile();

      assertThatCompilation(compilation)
          .classPath()
          .fileExists("mathutils","MathUtils.class");
    }
  }

  @DisplayName("Adding upgrade class path package is added to UPGRADE_CLASS_PATH")
  @JavacCompilerTest(minVersion = 9)
  void upgradeClassPathCreated(JctCompiler<?, ?> compiler) {
    // Given
    try (
        var firstWorkspace = Workspaces.newWorkspace();
        var secondWorkspace = Workspaces.newWorkspace();
        var thirdWorkspace = Workspaces.newWorkspace()
    ) {
      // Setup class path package
      firstWorkspace
          .createSourcePathPackage()
          .copyContentsFrom(resourcesDirectory().resolve("mathutils.add"));

      var firstCompilation = compiler.compile(firstWorkspace);

      assertThatCompilation(firstCompilation)
          .isSuccessfulWithoutWarnings();

      assertThatCompilation(firstCompilation)
          .classOutput()
          .packages()
          .fileExists("mathutils", "MathUtils.class")
          .isNotEmptyFile();

      // Setup upgrade class path package
      secondWorkspace
          .createSourcePathPackage()
          .copyContentsFrom(resourcesDirectory().resolve("mathutils.sub"));

      var secondCompilation = compiler.compile(secondWorkspace);

      assertThatCompilation(secondCompilation)
          .isSuccessfulWithoutWarnings();

      assertThatCompilation(secondCompilation)
          .classOutput()
          .packages()
          .fileExists("mathutils", "MathUtils.class")
          .isNotEmptyFile();

      // Setup third workspace
      thirdWorkspace
          .createSourcePathPackage()
          .copyContentsFrom(resourcesDirectory().resolve("calculator.advanced"));
      thirdWorkspace.addClassPathPackage(firstWorkspace.getClassOutputPackages().get(0).getPath());
      thirdWorkspace.addUpgradeModulePathModule("mathutils", secondWorkspace.getClassOutputPackages().get(0).getPath());

      // When
      var thirdCompilation = compiler.compile(thirdWorkspace);

      // Then
      assertThatCompilation(thirdCompilation)
          .isSuccessfulWithoutWarnings();

      assertThatCompilation(thirdCompilation)
          .classOutput()
          .packages()
          .fileExists("com", "example", "calculator", "Calculator.class")
          .isNotEmptyFile();

      assertThatCompilation(thirdCompilation)
          .classPath()
          .fileExists("mathutils","MathUtils.class");
    }
  }

  @DisplayName("Adding patch class path package is added to PATCH_CLASS_PATH")
  @JavacCompilerTest()
  void patchClassPathCreated(JctCompiler<?, ?> compiler) {
    // Given
    try (
        var firstWorkspace = Workspaces.newWorkspace();
        var secondWorkspace = Workspaces.newWorkspace();
        var thirdWorkspace = Workspaces.newWorkspace()
    ) {
      // Setup class path package
      firstWorkspace
          .createSourcePathPackage()
          .copyContentsFrom(resourcesDirectory().resolve("mathutils.add"));

      var firstCompilation = compiler.compile(firstWorkspace);

      assertThatCompilation(firstCompilation)
          .isSuccessfulWithoutWarnings();

      assertThatCompilation(firstCompilation)
          .classOutput()
          .packages()
          .fileExists("mathutils", "MathUtils.class")
          .isNotEmptyFile();

      // Setup upgrade class path package
      secondWorkspace
          .createSourcePathPackage()
          .copyContentsFrom(resourcesDirectory().resolve("mathutils.sub"));

      var secondCompilation = compiler.compile(secondWorkspace);

      assertThatCompilation(secondCompilation)
          .isSuccessfulWithoutWarnings();

      assertThatCompilation(secondCompilation)
          .classOutput()
          .packages()
          .fileExists("mathutils", "MathUtils.class")
          .isNotEmptyFile();

      // Setup third workspace
      thirdWorkspace
          .createSourcePathPackage()
          .copyContentsFrom(resourcesDirectory().resolve("calculator.advanced"));
      thirdWorkspace.addClassPathPackage(firstWorkspace.getClassOutputPackages().get(0).getPath());
      thirdWorkspace.addPatchModulePathModule("mathutils", secondWorkspace.getClassOutputPackages().get(0).getPath());

      // When
      var thirdCompilation = compiler.compile(thirdWorkspace);

      // Then
      assertThatCompilation(thirdCompilation)
          .isSuccessfulWithoutWarnings();

      assertThatCompilation(thirdCompilation)
          .classOutput()
          .packages()
          .fileExists("com", "example", "calculator", "Calculator.class")
          .isNotEmptyFile();

      assertThatCompilation(thirdCompilation)
          .classPath()
          .fileExists("mathutils","MathUtils.class");
    }
  }
}
