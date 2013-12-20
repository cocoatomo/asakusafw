/*
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.gradle.plugins

import com.asakusafw.gradle.plugins.internal.AsakusafwInternalPluginConvention
import org.gradle.api.*

/**
 * Base class of Asakusa Framework Gradle Plugin.
 */
class AsakusafwBasePlugin implements Plugin<Project> {

    private Project project
    private AntBuilder ant

    void apply(Project project) {
        this.project = project
        this.ant = project.ant

        configureProject()
    }

    private void configureProject() {
        configureExtentionProperties()
        configureRepositories()
    }

    private void configureExtentionProperties() {
        project.extensions.create('asakusafwInternal', AsakusafwInternalPluginConvention)
    }

    private void configureRepositories() {
        project.repositories {
            mavenCentral()
            maven { url "http://asakusafw.s3.amazonaws.com/maven/releases" }
            maven { url "http://asakusafw.s3.amazonaws.com/maven/snapshots" }
        }
    }
}

