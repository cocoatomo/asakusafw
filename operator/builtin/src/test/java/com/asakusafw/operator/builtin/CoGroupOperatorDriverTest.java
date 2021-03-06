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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Map;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVariable;

import org.junit.Test;

import com.asakusafw.operator.description.Descriptions;
import com.asakusafw.operator.model.OperatorDescription;
import com.asakusafw.operator.model.OperatorDescription.Node;
import com.asakusafw.operator.model.OperatorElement;
import com.asakusafw.vocabulary.attribute.BufferType;
import com.asakusafw.vocabulary.flow.processor.InputBuffer;
import com.asakusafw.vocabulary.operator.CoGroup;

/**
 * Test for {@link CoGroupOperatorDriver}.
 */
public class CoGroupOperatorDriverTest extends OperatorDriverTestRoot {

    /**
     * Creates a new instance.
     */
    public CoGroupOperatorDriverTest() {
        super(new CoGroupOperatorDriver());
    }

    /**
     * annotation.
     */
    @Test
    public void annotationTypeName() {
        assertThat(driver.getAnnotationTypeName(), is(Descriptions.classOf(CoGroup.class)));
    }

    /**
     * simple case.
     */
    @Test
    public void simple() {
        compile(new Action("com.example.Simple") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();
                assertThat(description.getInputs().size(), is(1));
                assertThat(description.getOutputs().size(), is(1));
                assertThat(description.getArguments().size(), is(0));

                Node input = description.getInputs().get(0);
                assertThat(input.getName(), is("in"));
                assertThat(input.getType(), is(sameType("com.example.Model")));

                Node output = description.getOutputs().get(0);
                assertThat(output.getName(), is("out"));
                assertThat(output.getType(), is(sameType("com.example.Proceeded")));
            }
        });
    }

    /**
     * w/ multiple inputs.
     */
    @Test
    public void input_multiple() {
        compile(new Action("com.example.WithInputMany") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();
                assertThat(description.getInputs().size(), is(3));
                assertThat(description.getOutputs().size(), is(1));
                assertThat(description.getArguments().size(), is(0));

                assertThat(description.getInputs().get(0).getName(), is("in0"));
                assertThat(description.getInputs().get(1).getName(), is("in1"));
                assertThat(description.getInputs().get(2).getName(), is("in2"));
            }
        });
    }

    /**
     * w/ multiple output.
     */
    @Test
    public void output_multiple() {
        compile(new Action("com.example.WithOutputMany") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();
                assertThat(description.getInputs().size(), is(1));
                assertThat(description.getOutputs().size(), is(3));
                assertThat(description.getArguments().size(), is(0));

                assertThat(description.getOutputs().get(0).getName(), is("out0"));
                assertThat(description.getOutputs().get(1).getName(), is("out1"));
                assertThat(description.getOutputs().get(2).getName(), is("out2"));
            }
        });
    }

    /**
     * w/ arguments.
     */
    @Test
    public void with_argument() {
        compile(new Action("com.example.WithArgument") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();
                assertThat(description.getInputs().size(), is(1));
                assertThat(description.getOutputs().size(), is(1));
                assertThat(description.getArguments().size(), is(2));

                Node input = description.getInputs().get(0);
                assertThat(input.getName(), is("in"));
                assertThat(input.getType(), is(sameType("com.example.Model")));

                Node output = description.getOutputs().get(0);
                assertThat(output.getName(), is("out"));
                assertThat(output.getType(), is(sameType("com.example.Proceeded")));

                Map<String, Node> arguments = toMap(description.getArguments());
                assertThat(arguments.get("stringArg"), is(notNullValue()));
                assertThat(arguments.get("stringArg").getType(), is(sameType(String.class)));
                assertThat(arguments.get("intArg"), is(notNullValue()));
                assertThat(arguments.get("intArg").getType(), is(kindOf(TypeKind.INT)));
            }
        });
    }

    /**
     * w/ type parameters.
     */
    @Test
    public void with_projective() {
        compile(new Action("com.example.WithProjective") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();
                assertThat(description.getInputs().size(), is(1));
                assertThat(description.getOutputs().size(), is(1));
                assertThat(description.getArguments().size(), is(0));

                TypeVariable t = getTypeVariable(target.getDeclaration(), "T");

                Node input = description.getInputs().get(0);
                assertThat(input.getName(), is("in"));
                assertThat(input.getType(), is(sameType(t)));

                Node output = description.getOutputs().get(0);
                assertThat(output.getName(), is("out"));
                assertThat(output.getType(), is(sameType(t)));
            }
        });
    }

    /**
     * w/ table inputs.
     */
    @Test
    public void with_view() {
        compile(new Action("com.example.WithView") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();
                assertThat(description.getInputs().size(), is(2));
                assertThat(description.getOutputs().size(), is(1));
                assertThat(description.getArguments().size(), is(0));

                Node input = description.getInputs().get(0);
                assertThat(input.getName(), is("in"));
                assertThat(input.getType(), is(sameType("com.example.Model")));
                assertThat(input.getAttributes(), not(hasItem(isView())));

                Node side = description.getInputs().get(1);
                assertThat(side.getName(), is("side"));
                assertThat(side.getType(), is(sameType("com.example.Model")));
                assertThat(side.getAttributes(), hasItem(flatView()));

                Node output = description.getOutputs().get(0);
                assertThat(output.getName(), is("out"));
                assertThat(output.getType(), is(sameType("com.example.Proceeded")));
            }
        });
    }

    /**
     * w/ table inputs.
     */
    @Test
    public void with_table() {
        compile(new Action("com.example.WithTable") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();
                assertThat(description.getInputs().size(), is(2));
                assertThat(description.getOutputs().size(), is(1));
                assertThat(description.getArguments().size(), is(0));

                Node input = description.getInputs().get(0);
                assertThat(input.getName(), is("in"));
                assertThat(input.getType(), is(sameType("com.example.Model")));
                assertThat(input.getAttributes(), not(hasItem(isView())));

                Node side = description.getInputs().get(1);
                assertThat(side.getName(), is("side"));
                assertThat(side.getType(), is(sameType("com.example.Model")));
                assertThat(side.getAttributes(), hasItem(groupView("=content")));

                Node output = description.getOutputs().get(0);
                assertThat(output.getName(), is("out"));
                assertThat(output.getType(), is(sameType("com.example.Proceeded")));
            }
        });
    }

    /**
     * w/ many table inputs.
     */
    @Test
    public void with_table_many() {
        compile(new Action("com.example.WithTableMany") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();
                assertThat(description.getInputs().size(), is(4));
                assertThat(description.getOutputs().size(), is(1));
                assertThat(description.getArguments().size(), is(0));

                Node input = description.getInputs().get(0);
                assertThat(input.getName(), is("in"));
                assertThat(input.getType(), is(sameType("com.example.Model")));
                assertThat(input.getAttributes(), not(hasItem(isView())));

                Node side0 = description.getInputs().get(1);
                assertThat(side0.getName(), is("side0"));
                assertThat(side0.getType(), is(sameType("com.example.Model")));
                assertThat(side0.getAttributes(), hasItem(groupView("=content")));

                Node side1 = description.getInputs().get(2);
                assertThat(side1.getName(), is("side1"));
                assertThat(side1.getType(), is(sameType("com.example.Model")));
                assertThat(side1.getAttributes(), hasItem(groupView("=content")));

                Node side2 = description.getInputs().get(3);
                assertThat(side2.getName(), is("side2"));
                assertThat(side2.getType(), is(sameType("com.example.Model")));
                assertThat(side2.getAttributes(), hasItem(groupView()));

                Node output = description.getOutputs().get(0);
                assertThat(output.getName(), is("out"));
                assertThat(output.getType(), is(sameType("com.example.Proceeded")));
            }
        });
    }

    /**
     * w/ iterable inputs.
     */
    @Test
    public void with_iterable() {
        compile(new Action("com.example.WithIterable") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();
                assertThat(description.getInputs().size(), is(1));
                assertThat(description.getOutputs().size(), is(1));
                assertThat(description.getArguments().size(), is(0));

                Node input = description.getInputs().get(0);
                assertThat(input.getName(), is("in"));
                assertThat(input.getType(), is(sameType("com.example.Model")));

                Node output = description.getOutputs().get(0);
                assertThat(output.getName(), is("out"));
                assertThat(output.getType(), is(sameType("com.example.Proceeded")));
            }
        });
    }

    /**
     * w/ buffer types.
     */
    @Test
    public void with_buffer_type() {
        compile(new Action("com.example.WithBufferType") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();
                assertThat(description.getInputs().size(), is(3));
                assertThat(description.getOutputs().size(), is(1));
                assertThat(description.getArguments().size(), is(0));
                assertThat(description.getAttributes(), hasItem(Descriptions.valueOf(InputBuffer.EXPAND)));

                Node in0 = description.getInputs().get(0);
                assertThat(in0.getName(), is("in0"));
                assertThat(in0.getType(), is(sameType("com.example.Model")));
                assertThat(in0.getAttributes(), hasItem(Descriptions.valueOf(BufferType.HEAP)));

                Node in1 = description.getInputs().get(1);
                assertThat(in1.getName(), is("in1"));
                assertThat(in1.getType(), is(sameType("com.example.Model")));
                assertThat(in1.getAttributes(), hasItem(Descriptions.valueOf(BufferType.SPILL)));

                Node in2 = description.getInputs().get(2);
                assertThat(in2.getName(), is("in2"));
                assertThat(in2.getType(), is(sameType("com.example.Model")));
                assertThat(in2.getAttributes(), hasItem(Descriptions.valueOf(BufferType.VOLATILE)));
            }
        });
    }

    /**
     * w/ buffer types.
     */
    @Test
    public void with_buffer_type_parent() {
        compile(new Action("com.example.WithBufferTypeParent") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();
                assertThat(description.getInputs().size(), is(3));
                assertThat(description.getOutputs().size(), is(1));
                assertThat(description.getArguments().size(), is(0));
                assertThat(description.getAttributes(), hasItem(Descriptions.valueOf(InputBuffer.ESCAPE)));

                Node in0 = description.getInputs().get(0);
                assertThat(in0.getName(), is("in0"));
                assertThat(in0.getType(), is(sameType("com.example.Model")));
                assertThat(in0.getAttributes(), hasItem(Descriptions.valueOf(BufferType.SPILL)));

                Node in1 = description.getInputs().get(1);
                assertThat(in1.getName(), is("in1"));
                assertThat(in1.getType(), is(sameType("com.example.Model")));
                assertThat(in1.getAttributes(), hasItem(Descriptions.valueOf(BufferType.SPILL)));

                Node in2 = description.getInputs().get(2);
                assertThat(in2.getName(), is("in2"));
                assertThat(in2.getType(), is(sameType("com.example.Model")));
                assertThat(in2.getAttributes(), hasItem(Descriptions.valueOf(BufferType.VOLATILE)));
            }
        });
    }

    /**
     * violates method is not abstract.
     */
    @Test
    public void violate_not_abstract() {
        violate("com.example.ViolateNotAbstract");
    }

    /**
     * violates method returns void.
     */
    @Test
    public void violate_return_void() {
        violate("com.example.ViolateReturnVoid");
    }

    /**
     * violates method must be have one or more inputs.
     */
    @Test
    public void violate_with_input() {
        violate("com.example.ViolateWithInput");
    }

    /**
     * violates method must be have one or more outputs.
     */
    @Test
    public void violate_with_output() {
        violate("com.example.ViolateWithOutput");
    }

    /**
     * violates method input.
     */
    @Test
    public void violate_input_with_key() {
        violate("com.example.ViolateInputWithKey");
    }

    /**
     * violates method input must be a model.
     */
    @Test
    public void violate_input_with_model() {
        violate("com.example.ViolateInputWithModel");
    }

    /**
     * violates method output must be a model.
     */
    @Test
    public void violate_output_with_model() {
        violate("com.example.ViolateOutputWithModel");
    }

    /**
     * violates method has only valid parameters.
     */
    @Test
    public void violate_valid_parameter() {
        violate("com.example.ViolateValidParameter");
    }

    /**
     * violates method output type is inferable.
     */
    @Test
    public void violate_output_inferable() {
        violate("com.example.ViolateOutputInferable");
    }

    /**
     * violates each group has the same type.
     */
    @Test
    public void violate_key_with_same_type() {
        violate("com.example.ViolateKeyWithSameType");
    }

    /**
     * violates each group has same number of properties.
     */
    @Test
    public void violate_key_group_not_larger() {
        violate("com.example.ViolateKeyGroupNotLarger");
    }

    /**
     * violates each group has same number of properties.
     */
    @Test
    public void violate_key_group_not_smaller() {
        violate("com.example.ViolateKeyGroupNotSmaller");
    }

    /**
     * violates parameter order.
     */
    @Test
    public void violate_input_before_output() {
        violate("com.example.ViolateInputBeforeOutput");
    }

    /**
     * violates parameter order.
     */
    @Test
    public void violate_input_before_side() {
        violate("com.example.ViolateInputBeforeSide");
    }

    /**
     * violates parameter order.
     */
    @Test
    public void violate_input_before_argument() {
        violate("com.example.ViolateInputBeforeArgument");
    }

    /**
     * violates parameter order.
     */
    @Test
    public void violate_side_before_output() {
        violate("com.example.ViolateSideBeforeOutput");
    }

    /**
     * violates parameter order.
     */
    @Test
    public void violate_output_before_argument() {
        violate("com.example.ViolateOutputBeforeArgument");
    }

    /**
     * violates input w/ "once" must be declared as iterable.
     */
    @Test
    public void violate_input_once_iterable() {
        violate("com.example.ViolateInputOnceIterable");
    }

    /**
     * violates flat views SHOULD not have any key annotations (warning).
     */
    @Test
    public void violate_flat_view_without_key() {
        violate("com.example.ViolateFlatViewWithoutKey");
    }

    /**
     * violates group views must have key annotation.
     */
    @Test
    public void violate_group_view_with_key() {
        violate("com.example.ViolateGroupViewWithKey");
    }
}
