def library(org, version, artifacts):
    return [org + ":" + a + "_2.12:" + version for a in artifacts]

def jlibrary(org, version, artifacts):
    return [org + ":" + a + ":" + version for a in artifacts]

artifacts = (
    library("io.monix", "3.0.0-RC2", ["monix"]) +
    library("io.circe", "0.12.0-M3", ["circe-core", "circe-parser", "circe-generic", "circe-generic-extras"]) +
    library("io.circe", "0.9.0-M3", ["circe-derivation"]) +
    library("com.github.fomkin", "0.12.1", ["korolev", "korolev-server", "korolev-server-akkahttp", "korolev-cats-effect-support", "korolev-slf4j-support"]) +
    library("ru.tinkoff", "0.1", ["tofu-memo", "tofu-logging", "tofu-env"]) +
    library("org.manatki", "0.8.1", ["derevo-core", "derevo-circe", "derevo-tschema"]) +
    library("ru.tinkoff", "0.11.0-beta9", ["typed-schema"]) +
    library("com.chuusai", "2.3.3", ["shapeless"]) +
    library("com.propensive", "0.11.0", ["magnolia"]) +
    library("org.typelevel", "1.1.0", ["cats-core"]) +
    library("org.typelevel", "2.0.0", ["cats-effect"]) +
    library("com.beachape", "1.5.13", ["enumeratum", "enumeratum-circe"]) +
    library("com.github.julien-truffaut", "1.5.1-cats", ["monocle-core", "monocle-macro"]) +
    library("com.iheart", "1.4.6", ["ficus"]) +
    library("com.github.t3hnar", "4.0", ["scala-bcrypt"]) +
    library("de.heikoseeberger", "1.26.0", ["akka-http-circe"]) +
    library("co.fs2", "2.0.0", ["fs2-core", "fs2-io"])
)

fs2 = [
    "@maven//:co_fs2_fs2_core_2_12",
    "@maven//:co_fs2_fs2_io_2_12",
]

monix = [
    "@maven//:io_monix_monix_2_12",
    "@maven//:io_monix_monix_eval_2_12",
    "@maven//:io_monix_monix_reactive_2_12",
    "@maven//:io_monix_monix_execution_2_12",
]

korolev = [
    "@maven//:com_github_fomkin_korolev_2_12",
    "@maven//:com_github_fomkin_korolev_server_2_12",
    "@maven//:com_github_fomkin_korolev_server_akkahttp_2_12",
    "@maven//:com_github_fomkin_korolev_cats_effect_support_2_12",
    "@maven//:com_github_fomkin_korolev_slf4j_support_2_12",
    "@maven//:com_github_fomkin_levsha_core_2_12",
    "@maven//:com_github_fomkin_levsha_events_2_12",
    "@maven//:com_github_fomkin_korolev_async_2_12",
]

circe = [
    "@maven//:io_circe_circe_core_2_12",
    "@maven//:io_circe_circe_parser_2_12",
    "@maven//:io_circe_circe_generic_2_12",
    "@maven//:io_circe_circe_generic_extras_2_12",
    "@maven//:io_circe_circe_derivation_2_12",
]

tofu = [
    "@maven//:ru_tinkoff_tofu_env_2_12",
    "@maven//:ru_tinkoff_tofu_memo_2_12",
    "@maven//:ru_tinkoff_tofu_logging_2_12",
    "@maven//:ru_tinkoff_tofu_logging_derivation_2_12",
    "@maven//:ru_tinkoff_tofu_logging_structured_2_12",
    "@maven//:ru_tinkoff_tofu_core_2_12",
]

derevo = [
    "@maven//:org_manatki_derevo_core_2_12",
    "@maven//:org_manatki_derevo_circe_2_12",
    "@maven//:org_manatki_derevo_tschema_2_12",
]

shapeless = [
    "@maven//:com_chuusai_shapeless_2_12",
]

cats = [
    "@maven//:org_typelevel_cats_core_2_12",
    "@maven//:org_typelevel_cats_effect_2_12",
    "@maven//:org_typelevel_cats_kernel_2_12",
    "@maven//:org_typelevel_cats_tagless_macros_2_12",
    "@maven//:org_typelevel_cats_tagless_core_2_12",
]

propensive = [
    "@maven//:com_propensive_mercator_2_12",
    "@maven//:com_propensive_magnolia_2_12",
]

typed_schema = [
    "@maven//:ru_tinkoff_typed_schema_2_12",
    "@maven//:ru_tinkoff_typed_schema_swagger_2_12",
    "@maven//:ru_tinkoff_typed_schema_typedsl_2_12",
    "@maven//:ru_tinkoff_typed_schema_akka_http_2_12",
    "@maven//:ru_tinkoff_typed_schema_param_2_12",
]

enumeratum = [
    "@maven//:com_beachape_enumeratum_2_12",
    "@maven//:com_beachape_enumeratum_circe_2_12",
]

monocle = [
    "@maven//:com_github_julien_truffaut_monocle_core_2_12",
    "@maven//:com_github_julien_truffaut_monocle_macro_2_12",
]

config = [
    "@maven//:com_iheart_ficus_2_12",
    "@maven//:com_typesafe_config",
]

bcrypt = [
    "@maven//:com_github_t3hnar_scala_bcrypt_2_12",
    "@maven//:de_svenkubiak_jBCrypt",
]

akka = [
    "@maven//:de_heikoseeberger_akka_http_circe_2_12",
    "@maven//:com_typesafe_akka_akka_stream_2_12",
    "@maven//:com_typesafe_akka_akka_actor_2_12",
    "@maven//:com_typesafe_akka_akka_http_2_12",
    "@maven//:com_typesafe_akka_akka_http_core_2_12",
]

lib_dependencies = (monix + korolev + circe + tofu + derevo + shapeless + cats +
                    propensive + typed_schema + enumeratum + monocle + config +
                    bcrypt + akka + fs2)
