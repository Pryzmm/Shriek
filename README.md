# VoiceLib

Minecraft text to speech library

# Implementation

```java
repositories {
    maven {
        name = "Modrinth"
        url = "https://api.modrinth.com/maven"
    }
}

dependencies {
    modImplementation "maven.modrinth:voicelib:1.0.1"
}
```

# Usage

VoiceLibApi is the class for the API.
The following are the methods you will be using
```java
import org.modogthedev.client.event.EventHandler;
EventHandler.setModel();
// This will set the language model, defined in https://alphacephei.com/vosk/models
// Language models vary in size; the default is vosk-model-small-en-us-0.15, which is ~40 MB compressed, while others like vosk-model-ru-0.10 are ~2.5GB compressed
// Changing models will restart a restart to download, or a call from EventHandler.getOrCreatePath();

VoiceLibApi.registerServerPlayerSpeechListener(Consumer<ServerPlayerTalkEvent> consumer)
// This method registers a ServerPLayerTalkEvent Consumer. Whenever a player speaks,
// This event will be fired. The ServerPlayerTalkEvent provides the player, and a string of what they said.

VoiceLibApi.registerClientSpeechListener(Consumer<ClientTalkEvent> consumer)
// This method registers a ClientTalkEvent Consumer. This method is only fired on the client.
// Whenever the user speaks, this will be fired.

VoiceLibApi.setPrintToChat(boolean printToChat)
// This is only on the client. This sets whether or not to 
// print client speak events to chat. (Default False)

VoiceLibApi.setPrintToConsole(boolean printToConsole)
// Same as printToChat but for the console instead. (Default False)
```
All methods have java docs for information on their usages.

# Example
This method makes it so if any player on the server says "ouch" they light on fire
```java
VoiceLibApi.registerServerPlayerSpeechListener((serverPlayerTalkEvent -> {
    if (serverPlayerTalkEvent.getText().contains("ouch"))
        serverPlayerTalkEvent.getPlayer().igniteForSeconds(2);
}));
```
You can also see this in VoiceLibExample

# Security

There are a few things to note with this mod,
- The mod automatically downloads a vosk model from the internet on the first launch, it is about 40MB
- This mod by default, constantly records and sends all text data to the server. This means a bad actor could listen in on your conversations (But only in text as audio is not sent to the server)
- The push to mute key will turn OFF your microphone whilst held
- Other mods can forcefully enable always on recording, they can also disable it (See VoiceLibClient)
