/*
 * Copyright 2018 Google LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.tools.jib.builder.steps;

import com.google.cloud.tools.jib.builder.BuildStepType;
import com.google.cloud.tools.jib.builder.ProgressEventDispatcher;
import com.google.common.base.Preconditions;
import java.io.Closeable;
import java.util.function.Consumer;
import javax.annotation.Nullable;

/**
 * Contains a {@link ProgressEventDispatcher}. This class is mutable and should only be used within
 * a local context.
 *
 * <p>This class is necessary because the total BLOb size (allocation units) is not known until the
 * response headers are received, only after which can the {@link ProgressEventDispatcher} be
 * created.
 */
class ProgressEventDispatcherContainer implements Consumer<Long>, Closeable {

  private final ProgressEventDispatcher.Factory progressEventDispatcherFactory;
  private final String description;
  private final BuildStepType type;
  @Nullable private ProgressEventDispatcher progressEventDispatcher;

  ProgressEventDispatcherContainer(
      ProgressEventDispatcher.Factory progressEventDispatcherFactory,
      String description,
      BuildStepType type) {
    this.progressEventDispatcherFactory = progressEventDispatcherFactory;
    this.description = description;
    this.type = type;
  }

  @Override
  public void accept(Long byteCount) {
    Preconditions.checkNotNull(progressEventDispatcher);
    progressEventDispatcher.dispatchProgress(byteCount);
  }

  @Override
  public void close() {
    Preconditions.checkNotNull(progressEventDispatcher);
    progressEventDispatcher.close();
  }

  void initializeWithBlobSize(long blobSize) {
    Preconditions.checkState(progressEventDispatcher == null);
    progressEventDispatcher = progressEventDispatcherFactory.create(type, description, blobSize);
  }
}
