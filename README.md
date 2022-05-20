# Nameless-Plugin
The official Minecraft plugin for NamelessMC v2. For compiled files see the [spigot resource page](https://www.spigotmc.org/resources/nameless-plugin-for-v2.59032/)

## Features
* Register command
* Report command
* User information command
* Validate command
* Notifications command
* Permissions
* Mvdw and PlaceholderAPI placeholders for number of notifications
* Server data sender (the plugin can send detailed information about the minecraft servers and the players online to the website)
* Group sync
* Whitelist registered users

## Installation
1. Install the plugin jar file in the `plugins` folder
2. Restart the server
3. Modify `config.yaml`: enter API URL and server id.
4. Run `/nlpl reload`

## Translations
<a href="http://translate.namelessmc.com/engage/namelessmc/">
<img src="http://translate.namelessmc.com/widgets/namelessmc/-/spigot-plugin/multi-auto.svg" alt="Translation status" />
</a>

## Compiling

Requirements: Maven, Git, JDK 11, JDK 17

On Debian/Ubuntu: `apt install maven git openjdk-11-jdk openjdk-17-jdk`

```sh
git clone https://github.com/Derkades/Derkutils
cd Derkutils
git checkout spigot-1.13 # important!
mvn clean install # Uses JDK 11
cd ..

git clone https://github.com/NamelessMC/Nameless-Java-API
cd Nameless-Java-API
mvn clean install # Uses JDK 11
cd ..

git clone https://github.com/NamelessMC/Nameless-Plugin
cd Nameless-Plugin
mvn clean package # Uses JDK 11 and 17
# find jar in {bungeecord,paper,spigot,sponge7,velocity}/target/*
```

Building the entire project can take quite a long time. During development, you might want to build a single module only:
```sh
mvn package -pl velocity -am
```

## Discord
[![discord](https://discord.com/api/guilds/246705793066467328/widget.png?style=shield)](https://discord.gg/nameless)

## v1
The legacy NamelessPlugin for v1 is available for download [on spigot](https://www.spigotmc.org/resources/official-namelessplugin.42698/). The source is available in the v1-Pre-1.1 branch.
