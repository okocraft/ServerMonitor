pluginManagement {
    includeBuild("build-logic")
}

rootProject.name = "ServerMonitor"

sequenceOf(
        "core",
        "paper",
        "velocity"
).forEach {
    include("server-monitor-$it")
    project(":server-monitor-$it").projectDir = file(it)
}
