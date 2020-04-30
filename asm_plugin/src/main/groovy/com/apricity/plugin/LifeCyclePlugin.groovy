package com.apricity.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

public class LifeCyclePlugin implements Plugin<Project>{

    @Override
    void apply(Project project) {
        println "==LifeCyclePlugin.apply=="

        def android = project.extensions.getByType(AppExtension)
        printf "==register asm transform=="
        LifeCycleTransform lifeCycleTransform = new LifeCycleTransform()
        android.registerTransform(lifeCycleTransform)
    }
}