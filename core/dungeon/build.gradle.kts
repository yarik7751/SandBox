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
}
