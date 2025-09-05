package org.modogthedev.client.event;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.modogthedev.VoiceLib;
import org.modogthedev.VoiceLibClient;
import org.modogthedev.VoiceLibConstants;
import org.modogthedev.api.VoiceLibApi;
import org.modogthedev.api.events.ClientTalkEvent;
import org.modogthedev.networking.VoiceLibPackets;
import org.modogthedev.networking.packets.PlayerSpeakPacket;
import org.modogthedev.speech.MicrophoneHandler;
import org.modogthedev.speech.SpeechRecognizer;
import org.vosk.Model;

import javax.sound.sampled.AudioFormat;
import java.io.*;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class EventHandler {
    /**
     * The following variables are used to store the speech recognizer
     */
    private static MicrophoneHandler microphoneHandler;

    /**
     * The following variables are used to store the microphone handler
     */
    private static SpeechRecognizer speechRecognizer;

    /**
     * The following variables are used to store the last recognized result
     */
    private static String lastResult = "";

    /**
     * The following variables are used to store the thread that listens to the microphone
     */
    private static Thread listenThread;
    private static boolean recordingLastTick = false;
    private static String modelType = "vosk-model-small-en-us-0.15";

    /**
     * This method is used to register the response processing for the game start event
     */
    public static void register() {
        ClientTickEvent.CLIENT_PRE.register(minecraft -> handleStartClientTickEvent());
        ClientTickEvent.CLIENT_POST.register(minecraft -> handleEndClientTickEvent());
        ClientLifecycleEvent.CLIENT_STARTED.register(minecraft -> handelClientStartEvent());
        ClientLifecycleEvent.CLIENT_STOPPING.register(minecraft -> handleClientStopEvent());
    }

    /**
     * Sets the Model to be downloaded.
     * Different models can be used to change the language, although the
     * mod would also have to be configured for that language.
     * I do NOT recommend changing this as the other models are more than 1GB
     * See <a href="https://alphacephei.com/vosk/models">Vosk Models</a>
     * The default is "vosk-model-small-en-us-0.15"
     * @param model The model name to be downloaded from <a href="https://alphacephei.com/vosk/models">Vosk</a>
     */
    public static void setModel(String model) {
        modelType = model;
    }

    /**
     * Sets the path for the Model to be downloaded.
     * @param path The path to be downloaded to
     */
    public static void setModelPath(Path path) {
        VoiceLibConstants.acousticModelPath = path;
    }

    private static void listenThreadTask() {
        while (true) {
            try {
                if (speechRecognizer == null) {         // wait 10 seconds and try to initialize the speech recognizer again
//                    if (Minecraft.getInstance().player != null) {
//                        Minecraft.getInstance().player.sendSystemMessage(Component.literal("§cAcoustic Model Load Failed"));
//                    }
                    // listenThread.wait(10000);
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ie) {
                        continue;
                    }

                    speechRecognizer = new SpeechRecognizer(new Model(getOrCreatePath()), VoiceLibConstants.sampleRate);
                } else if (microphoneHandler == null) {  // wait 10 seconds and try to initialize the microphone handler again
                    listenThread.wait(10000);
                    microphoneHandler = new MicrophoneHandler(new AudioFormat(VoiceLibConstants.sampleRate, 16, 1, true, false));
                    microphoneHandler.startListening();  // Try to restart the microphone
                } else {                                 // If the speech recognizer and the microphone handler are initialized successfully
                    String tmp = speechRecognizer.getStringMsg(microphoneHandler.readData());
                    if (!tmp.isEmpty() && !tmp.equals(lastResult) &&
                            VoiceLibClient.recordingSpeech) {   // Read audio data from the microphone and send it to the speech recognizer for recognition
                        if (VoiceLibConstants.encoding_repair) {
                            lastResult = SpeechRecognizer.repairEncoding(tmp, VoiceLibConstants.srcEncoding, VoiceLibConstants.dstEncoding);
                        } else {                                        // default configuration without encoding repair
                            lastResult = tmp;                           // restore the recognized text
                        }
                    }
                }
            } catch (Exception e) {
                VoiceLib.LOGGER.error(e.getMessage());
            }
        }
    }

    private static void unzip(Path path, Charset charset) throws IOException {
        Path destFolderPath = VoiceLibConstants.acousticModelPath;

        try (ZipFile zipFile = new ZipFile(path.toFile(), ZipFile.OPEN_READ, charset)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Path entryPath = destFolderPath.resolve(entry.getName());
                if (entryPath.normalize().startsWith(destFolderPath.normalize())) {
                    if (entry.isDirectory()) {
                        Files.createDirectories(entryPath);
                    } else {
                        Files.createDirectories(entryPath.getParent());
                        try (InputStream in = zipFile.getInputStream(entry)) {
                            try (OutputStream out = new FileOutputStream(entryPath.toFile())) {
                                IOUtils.copy(in, out);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            VoiceLib.LOGGER.error("Failed to unzip file: {}", e.getMessage());
        }
    }

    // This has been made publicly accessible for the ability to download other models, rather than the default.
    // The function name was also changed for a better understanding of functionality
    public static String getOrCreatePath() {
        String path = "";
        try {
            Path downloadPath = VoiceLibConstants.acousticModelPath;
            File file = new File(downloadPath+"\\"+modelType);
            path = new File(downloadPath+"\\"+modelType).getAbsoluteFile().toString();
            if (!file.exists()) {
                VoiceLib.LOGGER.info("Downloading voice model...");
                File download = new File("vosk.zip");
                FileUtils.copyURLToFile(URI.create("https://alphacephei.com/vosk/models/" + modelType + ".zip").toURL(), download);
                if (download.exists()) {
                    unzip(download.getAbsoluteFile().toPath(), Charset.defaultCharset());
                } else
                    VoiceLib.LOGGER.error("Failed to download, check for a mod update! (Downloaded zip file does not exist)");
                boolean finishedDeletion = download.delete();
                if (!finishedDeletion) {
                    VoiceLib.LOGGER.error("Failed to delete temporary Vosk file!");
                } else {
                    VoiceLib.LOGGER.info("Download complete, loading Vosk...");
                }
            }
        } catch (IOException e) {
            VoiceLib.LOGGER.error("Failed to download: {}", e.getMessage());
        }
        return path;
    }

    private static boolean voskInitialized = false;
    private static Timer retryTimer = null;

    private static void handelClientStartEvent() {
        VoiceLib.LOGGER.info("Loading Vosk model '{}' from '{}'   ...", VoiceLibConstants.acousticModelPath, getOrCreatePath());
        if (!tryInitializeVosk()) {
            scheduleVoskRetry();
        }
        initializeMicrophone();
        if (listenThread == null) {
            listenThread = new Thread(EventHandler::listenThreadTask);
            listenThread.start();
        }
    }

    private static boolean tryInitializeVosk() {
        try {
            Class<?> modelClass = Class.forName("org.vosk.Model");
            VoiceLib.LOGGER.info("Vosk Model class found successfully");
            Constructor<?> constructor = modelClass.getConstructor(String.class);
            Object model = constructor.newInstance(getOrCreatePath());
            speechRecognizer = new SpeechRecognizer((Model) model, VoiceLibConstants.sampleRate);
            VoiceLib.LOGGER.info("Vosk model loaded successfully!");
            voskInitialized = true;
            return true;
        } catch (ClassNotFoundException e) {
            VoiceLib.LOGGER.error("Vosk classes not found on classpath: {}", e.getMessage());
            return false;
        } catch (UnsatisfiedLinkError e) {
            VoiceLib.LOGGER.error("Vosk native libraries not found: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            VoiceLib.LOGGER.error("Failed to load Vosk (will retry): {} - {}", e.getClass().getSimpleName(), e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static void scheduleVoskRetry() {
        if (retryTimer != null) {
            retryTimer.cancel();
        }

        retryTimer = new Timer("VoskRetryTimer", true);
        retryTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!voskInitialized && tryInitializeVosk()) {
                    retryTimer.cancel();
                    retryTimer = null;
                } else if (!voskInitialized) {
                    scheduleVoskRetry();
                }
            }
        }, 5000);
    }

    private static void initializeMicrophone() {
        try {
            microphoneHandler = new MicrophoneHandler(new AudioFormat(VoiceLibConstants.sampleRate, 16, 1, true, false));
            microphoneHandler.startListening();
            VoiceLib.LOGGER.info("Microphone handler initialized successfully!");
        } catch (Exception e2) {
            VoiceLib.LOGGER.error("Failed to initialize microphone: {}", e2.getMessage());
        }

        if (VoiceLibConstants.encoding_repair) {
            VoiceLib.LOGGER.warn("(test function) Trt to resolve error encoding from {} to {}...", VoiceLibConstants.srcEncoding, VoiceLibConstants.dstEncoding);
        }
    }
    public static void resetListener() {
        handleClientStopEvent();
    }

    private static void handleClientStopEvent() {
        listenThread.interrupt();                 // Stop the thread that listens to the microphone
        microphoneHandler.stopListening();        // Stop listening to the microphone
        speechRecognizer = null;
        microphoneHandler = null;
        listenThread = null;                      // Clear the thread
    }

    private static void handleEndClientTickEvent() {
        if (VoiceLibClient.recordingSpeech &&           // If the user presses the key V
                microphoneHandler != null &&                                   // If the microphone initialization is successful
                !lastResult.isEmpty()) {
            if (VoiceLibClient.printToChat)// If the recognized text is not empty
                VoiceLib.LOGGER.info("{}{}", VoiceLibConstants.prefix, lastResult);
            if (Minecraft.getInstance().player != null) {
                if (VoiceLibClient.printToChat)
                    Minecraft.getInstance().player.sendSystemMessage(Component.literal(VoiceLibConstants.prefix + lastResult));
                VoiceLibPackets.sendToServer(new PlayerSpeakPacket(lastResult));
                VoiceLibApi.fireClientTalkEvent(new ClientTalkEvent() {
                    @Override
                    public String getText() {
                        return lastResult;
                    }
                });
            }
            lastResult = "";                                                   // Clear the recognized text
        }
    }

    private static void handleStartClientTickEvent() {
//        if (listenThread == null)
//            handelClientStartEvent();
        if (VoiceLibClient.recordingSpeech) {
            if (!recordingLastTick)
                VoiceLib.LOGGER.info("Resumed microphone listener...");
            recordingLastTick = true;
        } else {
            if (recordingLastTick) {
                VoiceLib.LOGGER.info("Paused microphone listener...");
                recordingLastTick = false;
            }
            if (!lastResult.isEmpty()) {
                lastResult = "";
            }
        }
    }

}
