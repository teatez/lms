plugins {
    kotlin("jvm") version "1.3.31" 
}

repositories {
    jcenter() 
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib"))
    compile(kotlin("reflect"))
    testImplementation("junit:junit:4.12")
}
