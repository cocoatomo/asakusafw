/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.vocabulary.flow.graph;

import java.text.MessageFormat;

/**
 * フローの境界属性。
 */
public enum FlowBoundary implements FlowElementAttribute {

    /**
     * ステージ境界を表す。
     */
    STAGE,

    /**
     * シャッフル境界を表す。
     */
    SHUFFLE,

    /**
     * 境界でない。
     */
    DEFAULT,

    ;
    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}.{1}",
                getDeclaringClass().getSimpleName(),
                name());
    }
}
