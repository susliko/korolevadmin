load("@rules_scala_annex//rules:scala.bzl", "scala_binary", "scala_library")
load("@io_bazel_rules_docker//container:container.bzl", "container_push")
load("//scala:dependencies.bzl", "lib_dependencies")
load("//scala:scalacopts.bzl", "scalacopts")

scala_library(
    name = "app_lib",
    srcs = glob([
        "src/main/scala/**",
    ]),
    resources = glob([
        "src/main/resources/**",
    ]),
    scala = "//scala:2_12",
    scalacopts = scalacopts,
    deps = lib_dependencies + [
        "//common",
        "//access",
        "//core",
        "//data",
        "//modules",
        "//operations",
        "//rendering",
        "//schema",
        "//utils",
    ],
)

scala_binary(
    name = "app",
    jvm_flags = ["-Dapp.config=override.conf"],
    main_class = "ru.tinkoff.traveladmin.Main",
    scala = "//scala:2_12",
    deps = [":app_lib"],
)
