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
package com.asakusafw.operator.builtin;

import java.util.List;

import javax.lang.model.element.Modifier;

import com.asakusafw.operator.Constants;
import com.asakusafw.operator.OperatorDriver;
import com.asakusafw.operator.builtin.DslBuilder.ElementRef;
import com.asakusafw.operator.builtin.DslBuilder.TypeRef;
import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.model.JavaName;
import com.asakusafw.operator.model.OperatorDescription;
import com.asakusafw.operator.model.OperatorDescription.Node;
import com.asakusafw.operator.model.OperatorDescription.Reference;

/**
 * {@link OperatorDriver} for {@code Branch} annotation.
 */
public class BranchOperatorDriver implements OperatorDriver {

    @Override
    public ClassDescription getAnnotationTypeName() {
        return Constants.getBuiltinOperatorClass("Branch"); //$NON-NLS-1$
    }

    @Override
    public OperatorDescription analyze(Context context) {
        DslBuilder dsl = new DslBuilder(context);
        if (dsl.method().modifiers().contains(Modifier.ABSTRACT)) {
            dsl.method().error(Messages.getString("BranchOperatorDriver.errorAbstract")); //$NON-NLS-1$
        }
        boolean enumResult = dsl.result().type().isEnum();
        if (enumResult == false) {
            dsl.method().error(Messages.getString("BranchOperatorDriver.errorReturnNotEnumType")); //$NON-NLS-1$
        }
        for (ElementRef p : dsl.parameters()) {
            TypeRef type = p.type();
            if (type.isDataModel()) {
                if (dsl.getInputs().isEmpty()) {
                    dsl.addInput(p.document(), p.name(), p.type().mirror(), p.reference());
                } else {
                    p.error(Messages.getString("BranchOperatorDriver.errorInputTooMany")); //$NON-NLS-1$
                }
            } else if (type.isExtra()) {
                dsl.consumeExtraParameter(p);
            } else {
                p.error(Messages.getString("BranchOperatorDriver.errorParameterUnsupportedType")); //$NON-NLS-1$
            }
        }
        if (dsl.getInputs().isEmpty() == false && enumResult) {
            List<ElementRef> constants = dsl.result().type().enumConstants();
            if (constants.isEmpty()) {
                dsl.result().error(Messages.getString("BranchOperatorDriver.errorReturnEmptyEnumType")); //$NON-NLS-1$
            } else {
                Node input = dsl.getInputs().get(0);
                for (ElementRef constant : constants) {
                    JavaName name = JavaName.of(constant.name());
                    dsl.addOutput(
                            constant.document(),
                            name.toMemberName(),
                            input.getType(),
                            Reference.special(constant.name()));
                }
            }
        }
        return dsl.toDescription();
    }
}
