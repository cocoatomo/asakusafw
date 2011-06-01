/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.testdriver;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.batch.BatchClass;
import com.asakusafw.compiler.batch.BatchDriver;
import com.asakusafw.compiler.flow.ExternalIoCommandProvider.CommandContext;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.testing.BatchInfo;
import com.asakusafw.compiler.testing.DirectBatchCompiler;
import com.asakusafw.compiler.testing.DirectFlowCompiler;
import com.asakusafw.compiler.testing.JobflowInfo;
import com.asakusafw.testdriver.core.Difference;
import com.asakusafw.testdriver.core.TestDataPreparator;
import com.asakusafw.testdriver.core.TestResultInspector;
import com.asakusafw.testdriver.core.VerifyContext;
import com.asakusafw.vocabulary.batch.BatchDescription;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;

/**
 * バッチ用のテストドライバクラス。
 */
public class BatchTester extends TestDriverBase {

    static final Logger LOG = LoggerFactory.getLogger(BatchTester.class);

    private final Map<String, JobFlowTester> jobFlowMap = new HashMap<String, JobFlowTester>();

    /**
     * コンストラクタ。
     *
     * @param callerClass 呼出元クラス
     */
    public BatchTester(Class<?> callerClass) {
        super(callerClass);
    }

    /**
     * バッチに含まれるジョブフローを指定する。
     *
     * @param name ジョブフロー名。ジョブフロークラスのアノテーションnameの値を指定する。
     * @return ジョブフローテストドライバ。
     */
    public JobFlowTester jobflow(String name) {
        JobFlowTester driver = jobFlowMap.get(name);
        if (driver == null) {
            driver = new JobFlowTester(driverContext.getCallerClass());
            jobFlowMap.put(name, driver);
        }
        return driver;
    }

    /**
     * バッチのテストを実行し、テスト結果を検証します。
     * @param batchDescriptionClass ジョブフロークラスのクラスオブジェクト
     * @throws RuntimeException テストの実行に失敗した場合
     */
    public void runTest(Class<? extends BatchDescription> batchDescriptionClass) {
        try {
            runTestInternal(batchDescriptionClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void runTestInternal(Class<? extends BatchDescription> batchDescriptionClass) throws IOException {
        LOG.info("テストを開始しています: {}", driverContext.getCallerClass().getName());

        // 初期化
        LOG.info("バッチをコンパイルしています: {}", batchDescriptionClass.getName());
        initializeClusterDirectory(driverContext.getClusterWorkDir());
        ClassLoader classLoader = this.getClass().getClassLoader();

        // バッチコンパイラの実行
        BatchDriver batchDriver = BatchDriver.analyze(batchDescriptionClass);
        assertFalse(batchDriver.getDiagnostics().toString(), batchDriver.hasError());
        BatchClass batchClass = batchDriver.getBatchClass();

        String batchId = batchClass.getConfig().name();
        File compileWorkDir = new File(driverContext.getCompileWorkBaseDir(), batchId
                + System.getProperty("file.separator") + driverContext.getExecutionId());
        if (compileWorkDir.exists()) {
            FileUtils.forceDelete(compileWorkDir);
        }

        File compilerOutputDir = new File(compileWorkDir, "output");
        File compilerLocalWorkingDir = new File(compileWorkDir, "build");

        BatchInfo batchInfo = DirectBatchCompiler.compile(batchDescriptionClass, "test.batch",
                Location.fromPath(driverContext.getClusterWorkDir() + "/" + driverContext.getExecutionId(), '/'),
                compilerOutputDir, compilerLocalWorkingDir,
                Arrays.asList(new File[] { DirectFlowCompiler.toLibraryPath(batchDescriptionClass) }),
                batchDescriptionClass.getClassLoader(), driverContext.getOptions());

        // ジョブフロー名の検査
        for (String flowId : jobFlowMap.keySet()) {
            if (batchInfo.findJobflow(flowId) == null) {
                throw new IllegalStateException(MessageFormat.format(
                        "ジョブフロー{1}はバッチ{0}に定義されていません",
                        driverContext.getCallerClass().getName(),
                        flowId));
            }
        }

        // バッチ実行前のtruncate
        LOG.info("テストデータを初期化しています: {}", driverContext.getCallerClass().getName());
        for (Map.Entry<String, JobFlowTester> entry : jobFlowMap.entrySet()) {
            String flowId = entry.getKey();
            JobFlowTester driver = entry.getValue();
            JobflowInfo jobflowInfo = batchInfo.findJobflow(flowId);
            assert jobflowInfo != null;

            LOG.debug("ジョブフローの入出力を初期化しています: {}", flowId);
            TestDataPreparator preparator = new TestDataPreparator(classLoader);
            for (JobFlowDriverInput<?> input : driver.inputs) {
                LOG.debug("入力{}を初期化しています", input.getName());
                ImporterDescription importerDescription = jobflowInfo.findImporter(input.getName());
                if (importerDescription == null) {
                    throw new IllegalStateException(MessageFormat.format(
                            "入力{2}はジョブフロー{0}#{1}に定義されていません",
                            batchDescriptionClass.getName(),
                            flowId,
                            input.getName()));
                }
                preparator.truncate(input.getModelType(), importerDescription);
            }
            for (JobFlowDriverOutput<?> output : driver.outputs) {
                LOG.debug("出力{}を初期化しています", output.getName());
                ExporterDescription exporterDescription = jobflowInfo.findExporter(output.getName());
                if (exporterDescription == null) {
                    throw new IllegalStateException(MessageFormat.format(
                            "出力{2}はジョブフロー{0}#{1}に定義されていません",
                            batchDescriptionClass.getName(),
                            flowId,
                            output.getName()));
                }
                preparator.truncate(output.getModelType(), exporterDescription);
            }
        }

        // バッチに含まれるジョブフローを実行
        for (Map.Entry<String, JobFlowTester> entry : jobFlowMap.entrySet()) {
            String flowId = entry.getKey();
            JobFlowTester driver = entry.getValue();
            JobflowInfo jobflowInfo = batchInfo.findJobflow(flowId);
            assert jobflowInfo != null;

            // ジョブフローのjarをImporter/Exporterが要求するディレクトリにコピー
            String jobFlowJarName = "jobflow-" + flowId + ".jar";
            File srcFile = new File(compilerOutputDir, "lib/" + jobFlowJarName);
            File destDir = driverContext.getJobflowPackageLocation(batchId);
            FileUtils.copyFileToDirectory(srcFile, destDir);

            CommandContext context = driverContext.getCommandContext();

            Map<String, String> dPropMap = createHadoopProperties(context);

            TestExecutionPlan plan = createExecutionPlan(jobflowInfo, context, dPropMap);
            savePlan(compileWorkDir, plan);

            // テストデータの配置
            LOG.info("ジョブフローのテストデータを配置しています: {}#{}", batchDescriptionClass.getName(), flowId);
            if (driver != null) {
                TestDataPreparator preparator = new TestDataPreparator(classLoader);
                for (JobFlowDriverInput<?> input : driver.inputs) {
                    if (input.sourceUri != null) {
                        LOG.debug("入力{}を配置しています: {}", input.getName(), input.getSourceUri());
                        ImporterDescription importerDescription = jobflowInfo.findImporter(input.getName());
                        input.setImporterDescription(importerDescription);
                        preparator.prepare(input.getModelType(), importerDescription, input.getSourceUri());
                    }
                }
                for (JobFlowDriverOutput<?> output : driver.outputs) {
                    if (output.sourceUri != null) {
                        LOG.debug("出力{}を配置しています: {}", output.getName(), output.getSourceUri());
                        ExporterDescription exporterDescription = jobflowInfo.findExporter(output.getName());
                        output.setExporterDescription(exporterDescription);
                        preparator.prepare(output.getModelType(), exporterDescription, output.getSourceUri());
                    }
                }
            }

            // コンパイル結果のジョブフローを実行
            LOG.info("ジョブフローを実行しています: {}#{}", batchDescriptionClass.getName(), flowId);
            VerifyContext verifyContext = new VerifyContext();
            executePlan(plan, jobflowInfo.getPackageFile());
            verifyContext.testFinished();

            // 実行結果の検証
            LOG.info("ジョブフローの実行結果を検証しています: {}#{}", batchDescriptionClass.getName(), flowId);
            if (driver != null) {
                TestResultInspector inspector = new TestResultInspector(this.getClass().getClassLoader());
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("%n"));
                boolean failed = false;
                for (JobFlowDriverOutput<?> output : driver.outputs) {
                    if (output.expectedUri != null) {
                        LOG.debug("出力{}を検証しています: {}", output.getName(), output.getExpectedUri());
                        ExporterDescription exporterDescription = jobflowInfo.findExporter(output.getName());
                        output.setExporterDescription(exporterDescription);
                        List<Difference> diffList = inspect(output, verifyContext, inspector);
                        if (diffList.isEmpty() == false) {
                            LOG.warn("{}#{}の出力{}には{}個の差異があります", new Object[] {
                                    batchDescriptionClass.getName(),
                                    flowId,
                                    output.getName(),
                                    diffList.size(),
                            });
                        }
                        for (Difference difference : diffList) {
                            failed = true;
                            sb.append(String.format("%s: %s%n",
                                    output.getModelType().getSimpleName(),
                                    difference));
                        }
                    }
                }
                if (failed) {
                    throw new AssertionError(sb);
                }
            }
        }
    }
}
