load("@rules_scala_annex//rules:scala.bzl", "scala_library")
load("//scala:dependencies.bzl", "lib_dependencies")
load("//scala:scalacopts.bzl", "scalacopts")

scala_library(
    name = "data",
    srcs = glob(["scala/**"]),
    scala = "//scala:2_12",
    scalacopts = scalacopts,
    visibility = [
        "//visibility:public",
    ],
    deps = lib_dependencies + ["//utils"],
)
