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
 * An interface which represents class declarations.
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:8.1] Class Declaration} </li>
 *   </ul> </li>
 * </ul>
 */
public interface ClassDeclaration
        extends TypeDeclaration {

    /**
     * Returns the type parameter declarations.
     * @return the type parameter declarations
     */
    List<? extends TypeParameterDeclaration> getTypeParameters();

    /**
     * Returns the super class.
     * @return super class, or {@code null} if there is no explicit super class
     */
    Type getSuperClass();

    /**
     * Returns the super interface types.
     * @return the super interface types
     */
    List<? extends Type> getSuperInterfaceTypes();
}
