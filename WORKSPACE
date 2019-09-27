load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

# Load rules scala annex
RULES_SCALA_ANNEX_COMMIT = "9d6eaa6befa43b7703118cdb6f6acc273192aa0c"

RULES_SCALA_ANNEX_SHA256 = "60a626fa859ad4ae96e16b8ef72fce8ebd58d1592712a0e8c5ffde38542fb447"

http_archive(
    name = "rules_scala_annex",
    sha256 = RULES_SCALA_ANNEX_SHA256,
    strip_prefix = "rules_scala-{}".format(RULES_SCALA_ANNEX_COMMIT),
    url = "https://github.com/higherkindness/rules_scala/archive/{}.zip".format(RULES_SCALA_ANNEX_COMMIT),
)

http_archive(
    name = "rules_jvm_external",
    sha256 = "515ee5265387b88e4547b34a57393d2bcb1101314bcc5360ec7a482792556f42",
    strip_prefix = "rules_jvm_external-2.1",
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/2.1.zip",
)

http_archive(
    name = "io_bazel_rules_docker",
    sha256 = "87fc6a2b128147a0a3039a2fd0b53cc1f2ed5adb8716f50756544a572999ae9a",
    strip_prefix = "rules_docker-0.8.1",
    urls = ["https://github.com/bazelbuild/rules_docker/archive/v0.8.1.tar.gz"],
)

load("@rules_scala_annex//rules/scala:workspace.bzl", "scala_register_toolchains", "scala_repositories")

scala_repositories()

scala_register_toolchains()

# Load bazel skylib and google protobuf
git_repository(
    name = "bazel_skylib",
    commit = "3721d32c14d3639ff94320c780a60a6e658fb033",
    remote = "https://github.com/bazelbuild/bazel-skylib.git",
    shallow_since = "1553102012 +0100",
)

http_archive(
    name = "com_google_protobuf",
    sha256 = "0963c6ae20340ce41f225a99cacbcba8422cebe4f82937f3d9fa3f5dd7ae7342",
    strip_prefix = "protobuf-9f604ac5043e9ab127b99420e957504f2149adbe",
    urls = ["https://github.com/google/protobuf/archive/9f604ac5043e9ab127b99420e957504f2149adbe.zip"],
)

load("@com_google_protobuf//:protobuf_deps.bzl", "protobuf_deps")

protobuf_deps()

# Install external dependencies
load("@rules_jvm_external//:defs.bzl", "maven_install")
load("//scala:dependencies.bzl", "artifacts")

maven_install(
    artifacts = artifacts,
    fetch_sources = True,
    repositories = [
        "https://repo1.maven.org/maven2/",
        "https://jcenter.bintray.com",
    ],
)

# Install compiler plugins
load("@bazel_tools//tools/build_defs/repo:jvm.bzl", "jvm_maven_import_external")

jvm_maven_import_external(
    name = "kind_projector_2_12",
    artifact = "org.spire-math:kind-projector_2.12:0.9.6",
    licenses = ["notice"],
    server_urls = ["http://central.maven.org/maven2"],
)

jvm_maven_import_external(
    name = "paradise_2_12",
    artifact = "org.scalameta:paradise_2.12.8:3.0.0-M11",
    licenses = ["notice"],
    server_urls = ["http://central.maven.org/maven2"],
)

jvm_maven_import_external(
    name = "better_monadic_for_2_12",
    artifact = "com.olegpy:better-monadic-for_2.12:0.3.0-M4",
    licenses = ["notice"],
    server_urls = ["http://central.maven.org/maven2"],
)
