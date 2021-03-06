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
package com.asakusafw.compiler.flow.processor.flow;

import com.asakusafw.compiler.flow.processor.ConvertFlowProcessor;
import com.asakusafw.compiler.flow.processor.operator.ConvertFlowFactory;
import com.asakusafw.compiler.flow.processor.operator.ConvertFlowFactory.WithParameter;
import com.asakusafw.compiler.flow.testing.external.Ex1MockExporterDescription;
import com.asakusafw.compiler.flow.testing.external.Ex1MockImporterDescription;
import com.asakusafw.compiler.flow.testing.external.Ex2MockExporterDescription;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.vocabulary.flow.Export;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Import;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.JobFlow;
import com.asakusafw.vocabulary.flow.Out;


/**
 * test for {@link ConvertFlowProcessor}.
 */
@JobFlow(name = "testing")
public class ConvertFlowWithParameter extends FlowDescription {

    private In<Ex1> in1;

    private Out<Ex1> out1;

    private Out<Ex2> out2;

    /**
     * Creates a new instance.
     * @param in1 input
     * @param out1 output1
     * @param out2 output2
     */
    public ConvertFlowWithParameter(
            @Import(name = "e1", description = Ex1MockImporterDescription.class)
            In<Ex1> in1,
            @Export(name = "e1", description = Ex1MockExporterDescription.class)
            Out<Ex1> out1,
            @Export(name = "e2", description = Ex2MockExporterDescription.class)
            Out<Ex2> out2) {
        this.in1 = in1;
        this.out1 = out1;
        this.out2 = out2;
    }

    @Override
    protected void describe() {
        ConvertFlowFactory f = new ConvertFlowFactory();
        WithParameter op = f.withParameter(in1, 10);
        out1.add(op.original);
        out2.add(op.out);
    }
}
