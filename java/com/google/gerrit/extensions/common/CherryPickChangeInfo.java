// Copyright (C) 2018 The Android Open Source Project
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

package com.google.gerrit.extensions.common;

/**
 * {@link ChangeInfo} that is returned when a change was cherry-picked.
 *
 * <p>This class used to define additional fields to {@link ChangeInfo}, but those were pulled up
 * into {@link ChangeInfo}. Now this class is no longer needed, but we need to keep it for backwards
 * compatibility of the Java API.
 */
public class CherryPickChangeInfo extends ChangeInfo {}
