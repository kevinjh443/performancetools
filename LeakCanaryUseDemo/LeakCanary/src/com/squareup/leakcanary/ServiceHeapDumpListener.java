/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.leakcanary;

import static com.squareup.leakcanary.internal.LeakCanaryInternals.setEnabled;
import android.content.Context;

import com.squareup.leakcanary.internal.HeapAnalyzerService;
import com.squareup.leakcanary.watcher.HeapDump;
import com.squareup.leakcanary.watcher.Preconditions;

public final class ServiceHeapDumpListener implements HeapDump.Listener {

  private final Context context;
  private final Class<? extends AbstractAnalysisResultService> listenerServiceClass;

  public ServiceHeapDumpListener(Context context,
      Class<? extends AbstractAnalysisResultService> listenerServiceClass) {
    setEnabled(context, listenerServiceClass, true);
    setEnabled(context, HeapAnalyzerService.class, true);
    this.listenerServiceClass = Preconditions.checkNotNull(listenerServiceClass, "listenerServiceClass");
    this.context = Preconditions.checkNotNull(context, "context").getApplicationContext();
  }

  @Override public void analyze(HeapDump heapDump) {
	  Preconditions.checkNotNull(heapDump, "heapDump");
    HeapAnalyzerService.runAnalysis(context, heapDump, listenerServiceClass);
  }
}
