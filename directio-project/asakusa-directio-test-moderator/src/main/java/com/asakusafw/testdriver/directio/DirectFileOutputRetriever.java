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
package com.asakusafw.testdriver.directio;

import java.io.IOException;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.testdriver.core.BaseExporterRetriever;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.ExporterRetriever;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.vocabulary.directio.DirectFileOutputDescription;

/**
 * An implementation of {@link ExporterRetriever} for {@link DirectFileOutputDescription}.
 * @since 0.2.5
 */
public class DirectFileOutputRetriever extends BaseExporterRetriever<DirectFileOutputDescription> {

    static final Logger LOG = LoggerFactory.getLogger(DirectFileOutputRetriever.class);

    @Override
    public void truncate(DirectFileOutputDescription description, TestContext context) throws IOException {
        DirectIoTestHelper helper = new DirectIoTestHelper(context, description.getBasePath());
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Truncating Direct I/O output: {0}", //$NON-NLS-1$
                    description.getClass().getName()));
        }
        helper.truncate();
    }

    @Override
    public <V> ModelOutput<V> createOutput(
            DataModelDefinition<V> definition,
            DirectFileOutputDescription description,
            TestContext context) throws IOException {
        throw new UnsupportedOperationException(MessageFormat.format(
                Messages.getString("DirectFileOutputRetriever.errorPrepareOutput"), //$NON-NLS-1$
                description.getClass().getName()));
    }

    @Override
    public <V> DataModelSource createSource(
            DataModelDefinition<V> definition,
            DirectFileOutputDescription description,
            TestContext context) throws IOException {
        DirectIoTestHelper helper = new DirectIoTestHelper(context, description.getBasePath());
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Retrieving Direct I/O output: {0}", //$NON-NLS-1$
                    description.getClass().getName()));
        }
        V object = definition.toObject(definition.newReflection().build());
        ModelInput<? super V> input = helper.openInput(definition.getModelClass(), description);
        return new DataModelSource() {
            @Override
            public DataModelReflection next() throws IOException {
                if (input.readTo(object)) {
                    return definition.toReflection(object);
                }
                return null;
            }
            @Override
            public void close() throws IOException {
                input.close();
            }
        };
    }
}
