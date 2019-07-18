/*
 * Copyright 2015 Ben Manes. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package parser;

import com.google.common.io.Closeables;

import java.io.*;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A skeletal implementation that reads the trace file line by line as textual data.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public abstract class TextTraceReader extends AbstractTraceReader implements TraceReader {

  public TextTraceReader(String filePath) {
    super(filePath);
  }

  /** Returns a stream of each line in the trace file. */
  protected Stream<String> lines() throws IOException {
    InputStream input = readFile();
    Reader reader = new InputStreamReader(input, UTF_8);
    return new BufferedReader(reader).lines().map(String::trim)
        .onClose(() -> Closeables.closeQuietly(input));
  }
}
