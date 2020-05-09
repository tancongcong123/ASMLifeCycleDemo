package com.apricity.plugin

import com.android.build.gradle.AppExtension
import com.apricity.plugin.bean.SettingParams
import org.gradle.api.Plugin
import org.gradle.api.Project

class LifeCyclePlugin implements Plugin<Project>{

    @Override
    void apply(Project project) {
        project.extensions.create('trackParams', SettingParams)
        GlobalConfig.setProject(project)
        println "=="+GlobalConfig.getParams().name+"=="

        def android = project.extensions.getByType(AppExtension)
        printf "==register asm transform=="
        LifeCycleTransform lifeCycleTransform = new LifeCycleTransform()
        android.registerTransform(lifeCycleTransform)
    }
}