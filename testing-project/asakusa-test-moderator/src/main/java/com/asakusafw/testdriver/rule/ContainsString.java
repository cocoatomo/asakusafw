/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.testdriver.rule;

import java.text.MessageFormat;

/**
 * Accepts iff actual contains expected string.
 * @since 0.2.0
 */
public class ContainsString implements ValuePredicate<String> {

    @Override
    public boolean accepts(String expected, String actual) {
        if (expected == null || actual == null) {
            throw new IllegalArgumentException();
        }
        return actual.indexOf(expected) >= 0;
    }

    @Override
    public String describeExpected(String expected, String actual) {
        if (expected == null) {
            return "(error)"; //$NON-NLS-1$
        }
        return MessageFormat.format(
                Messages.getString("ContainsString.message"), //$NON-NLS-1$
                Util.format(expected));
    }
}
