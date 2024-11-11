// Copyright (C) 2020 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.gerrit.acceptance.testsuite.change;

import java.util.function.Function;

/**
 * Builder for the file specification of line/range comments. Used by {@link TestCommentCreation}
 * and {@link TestRobotCommentCreation}.
 */
public class FileBuilder<T> {
  private final Function<String, T> nextStepProvider;

  public FileBuilder(Function<String, T> nextStepProvider) {
    this.nextStepProvider = nextStepProvider;
  }

  /** File on which the comment should be added. */
  public T ofFile(String file) {
    return nextStepProvider.apply(file);
  }
}
