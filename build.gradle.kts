plugins {
    kotlin("jvm") version "1.9.20"
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    `maven-publish`
}

group = "com.github.bonsai"
version = project.findProperty("version") as String

repositories {
    mavenCentral()
    maven("https://repo.essential.gg/repository/maven-public/")
    maven(url = "https://libraries.minecraft.net")
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
}

loom {
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.bonsai"
            artifactId = "RenderLib"
            version = project.findProperty("version") as String
            from(getComponents().getByName("java"))
        }
    }
}