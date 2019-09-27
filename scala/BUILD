load(
    "@rules_scala_annex//rules:scala.bzl",
    "configure_bootstrap_scala",
    "configure_zinc_scala",
    "scala_library",
)
load("@rules_scala_annex//rules:providers.bzl", "reconfigure_deps_configuration")

compiler_classpath_2_12_8 = [
    "@annex//:org_scala_lang_scala_compiler",
    "@annex//:org_scala_lang_scala_library",
    "@annex//:org_scala_lang_scala_reflect",
]

runtime_classpath_2_12_8 = [
    "@annex//:org_scala_lang_scala_library",
]

configure_bootstrap_scala(
    name = "bootstrap_2_12_8",
    compiler_classpath = compiler_classpath_2_12_8,
    runtime_classpath = runtime_classpath_2_12_8,
    version = "2.12.8",
    visibility = ["//visibility:public"],
)

scala_library(
    name = "compiler_bridge_2_12_8",
    srcs = [
        "@compiler_bridge_2_12//:src",
    ],
    scala = ":bootstrap_2_12_8",
    visibility = ["//visibility:public"],
    deps = compiler_classpath_2_12_8 + [
        "@annex//:org_scala_sbt_compiler_interface",
        "@annex//:org_scala_sbt_util_interface",
    ],
)

configure_zinc_scala(
    name = "zinc",
    compiler_bridge = ":compiler_bridge_2_12_8",
    compiler_classpath = compiler_classpath_2_12_8,
    global_plugins = [
        "@kind_projector_2_12",
        "@paradise_2_12",
        "@better_monadic_for_2_12",
    ],
    runtime_classpath = runtime_classpath_2_12_8,
    version = "2.12.8",
    visibility = ["//visibility:public"],
)

reconfigure_deps_configuration(
    name = "2_12",
    provider = ":zinc",
    used = "off",
    visibility = ["//visibility:public"],
)
