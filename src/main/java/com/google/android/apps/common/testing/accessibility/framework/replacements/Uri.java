/*
 * Copyright (C) 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.android.apps.common.testing.accessibility.framework.replacements;

import com.google.errorprone.annotations.Immutable;
import java.net.URI;

/** Used as a local replacement for Android's {@link android.net.Uri} */
@Immutable
public final class Uri {

  private final URI uri;

  public Uri(String rfc2396UriString) {
    uri = URI.create(rfc2396UriString);
  }

  /**
   * @see android.net.Uri#isAbsolute()
   */
  public boolean isAbsolute() {
    return uri.isAbsolute();
  }

  /**
   * @see android.net.Uri#isRelative()
   */
  public boolean isRelative() {
    return !isAbsolute();
  }
}
