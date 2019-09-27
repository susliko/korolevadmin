load("@rules_scala_annex//rules:scala.bzl", "scala_library")
load("//scala:dependencies.bzl", "lib_dependencies")
load("//scala:scalacopts.bzl", "scalacopts")

scala_library(
    name = "utils",
    srcs = glob(["scala/**"]),
    scala = "//scala:2_12",
    scalacopts = scalacopts,
    visibility = [
        "//visibility:public",
    ],
    deps = lib_dependencies + ["@maven//:org_scala_lang_scala_reflect_2_12_8"],
)
