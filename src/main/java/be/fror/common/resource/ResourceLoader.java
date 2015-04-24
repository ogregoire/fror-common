/*
 * Copyright 2015 Olivier Grégoire <https://github.com/ogregoire>.
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
package be.fror.common.resource;

import com.google.common.io.ByteSource;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 *
 * @author Olivier Grégoire &lt;https://github.com/ogregoire&gt;
 */
public interface ResourceLoader<T> {

  /**
   * Loads a resource from <tt>source</tt> but throws an {@link UncheckedIOException} instead of an
   * {@link IOException}.
   *
   * <p>
   * This method is the same as calling:
   * 
   * <pre>
   * <code>try {
   *   return this.load(source);
   * } catch (IOException ex) {
   *   throw new UncheckedIOException(ex);
   * }</code>
   * </pre>
   * 
   * @param <T>
   * @param source
   * @return
   * @throws UncheckedIOException
   */
  public default <T> T uncheckedLoad(ByteSource source) throws UncheckedIOException {
    try {
      return this.load(source);
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  /**
   * 
   * @param <T>
   * @param source
   * @return
   * @throws IOException 
   */
  public <T> T load(ByteSource source) throws IOException;

}
