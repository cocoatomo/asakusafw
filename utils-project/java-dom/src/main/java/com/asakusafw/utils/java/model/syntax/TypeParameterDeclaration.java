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
package com.asakusafw.utils.java.model.syntax;

import java.util.List;

/**
 * An interface which represents type parameter declarations.
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:4.4] Type Variables} </li>
 *   </ul> </li>
 * </ul>
 */
public interface TypeParameterDeclaration
        extends Model {

    /**
     * Returns the type variable name.
     * @return the type variable name
     */
    SimpleName getName();

    /**
     * Returns the bound types.
     * @return the bound types
     */
    List<? extends Type> getTypeBounds();
}
