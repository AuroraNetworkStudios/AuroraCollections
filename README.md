# AuroraCollections

Highly customizable and feature-rich Hypixel like collection leveling plugin for Paper servers.
Let your players level up collections by collecting multiple items from the same type and give them rewards like money, 
items, permissions, or even custom rewards.


## Developer API

### Maven

```xml
<repository>
    <id>auroramc</id>
    <url>https://repo.auroramc.gg/releases/</url>
</repository>
```

```xml
<dependency>
    <groupId>gg.auroramc</groupId>
    <artifactId>AuroraCollections</artifactId>
    <version>{VERSION}</version>
    <scope>provided</scope>
</dependency>
```
### Gradle

**Groovy DSL:**
```gradle
repositories {
    maven {
        url "https://repo.auroramc.gg/releases/"
    }
}

dependencies {
    compileOnly 'gg.auroramc:AuroraCollections:{VERSION}'
}
```

**Kotlin DSL:**
```Gradle Kotlin DSL
repositories { 
    maven("https://repo.auroramc.gg/releases/")
}

dependencies { 
    compileOnly("gg.auroramc:AuroraCollections:{VERSION}")
}
```