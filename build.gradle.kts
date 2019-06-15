plugins {
    kotlin("jvm") version "1.3.31" 
}

repositories {
    jcenter() 
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib")) 
    testImplementation("junit:junit:4.12")
}
