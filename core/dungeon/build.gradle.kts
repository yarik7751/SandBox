plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    testImplementation(libs.junit)
}

tasks.test {
    // мультиагентный A* солвера держит в памяти сотни тысяч состояний
    maxHeapSize = "4g"
    // ручная сборка JSON-пака уровней: -Dgenpack=true (см. DungeonPackGeneratorTest)
    systemProperty("genpack", System.getProperty("genpack", "false"))
    systemProperty("pack.from", System.getProperty("pack.from", ""))
    systemProperty("pack.to", System.getProperty("pack.to", ""))
}
