package com.apricity.plugin

import com.apricity.plugin.bean.SettingParams
import org.gradle.api.Project

class GlobalConfig {

    private static Project project

    static Project getProject() {
        return project
    }

    static void setProject(Project project) {
        GlobalConfig.@project = project
    }

    static SettingParams getParams(){
        return project.trackParams
    }

    static boolean getIsOpenTrack(){
        return getParams().isOpenTrack
    }
}