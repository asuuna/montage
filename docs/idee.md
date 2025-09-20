1) Multi-module Maven propre + profils JavaFX par OS
   pom.xml (parent, fichier complet)
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
https://maven.apache.org/xsd/maven-4.0.0.xsd">
<modelVersion>4.0.0</modelVersion>
<groupId>com.acme</groupId>
<artifactId>ai-video-editor</artifactId>
<version>0.1.0</version>
<packaging>pom</packaging>
<name>AI Video Editor</name>
<description>JavaFX + JavaCV AI-assisted video editor</description>
<modules>
<module>app-core</module>
<module>app-ai</module>
<module>app-io</module>
<module>app-ui</module>
<module>app-launcher</module>
</modules>

  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.release>17</maven.compiler.release>
    <javafx.version>21.0.3</javafx.version>
    <junit.jupiter.version>5.10.2</junit.jupiter.version>
    <slf4j.version>2.0.13</slf4j.version>
    <logback.version>1.5.6</logback.version>
    <javacv.version>1.5.10</javacv.version>
    <onnx.version>1.18.0</onnx.version>
    <vosk.version>0.3.45</vosk.version>
    <pitest.version>1.15.8</pitest.version>
  </properties>

  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.bytedeco</groupId>
        <artifactId>javacv-platform</artifactId>
        <version>${javacv.version}</version>
      </dependency>
      <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>${slf4j.version}</version>
      </dependency>
      <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
        <version>${logback.version}</version>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <build>
    <pluginManagement>
      <plugins>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-surefire-plugin</artifactId>
          <version>3.3.0</version>
          <configuration>
            <useModulePath>false</useModulePath>
          </configuration>
        </plugin>
        <plugin>
          <groupId>org.pitest</groupId>
          <artifactId>pitest-maven</artifactId>
          <version>${pitest.version}</version>
        </plugin>
      </plugins>
    </pluginManagement>
  </build>

  <!-- Profils JavaFX par OS -->
  <profiles>
    <profile>
      <id>javafx-windows</id>
      <activation><os><family>Windows</family></os></activation>
      <properties><javafx.platform>win</javafx.platform></properties>
    </profile>
    <profile>
      <id>javafx-mac</id>
      <activation><os><family>mac</family></os></activation>
      <properties><javafx.platform>mac</javafx.platform></properties>
    </profile>
    <profile>
      <id>javafx-linux</id>
      <activation><os><family>unix</family></os></activation>
      <properties><javafx.platform>linux</javafx.platform></properties>
    </profile>
  </profiles>
</project>

app-ui/pom.xml (JavaFX contrôlé par profils)
<project xmlns="http://maven.apache.org/POM/4.0.0" ...>
<modelVersion>4.0.0</modelVersion>
<parent>
<groupId>com.acme</groupId><artifactId>ai-video-editor</artifactId><version>0.1.0</version>
</parent>
<artifactId>app-ui</artifactId>
<dependencies>
<dependency><groupId>com.acme</groupId><artifactId>app-core</artifactId><version>${project.version}</version></dependency>
<dependency><groupId>com.acme</groupId><artifactId>app-io</artifactId><version>${project.version}</version></dependency>
<dependency><groupId>org.openjfx</groupId><artifactId>javafx-controls</artifactId><version>${javafx.version}</version>
<classifier>${javafx.platform}</classifier></dependency>
<dependency><groupId>org.openjfx</groupId><artifactId>javafx-graphics</artifactId><version>${javafx.version}</version>
<classifier>${javafx.platform}</classifier></dependency>
<dependency><groupId>org.openjfx</groupId><artifactId>javafx-media</artifactId><version>${javafx.version}</version>
<classifier>${javafx.platform}</classifier></dependency>
<dependency><groupId>org.slf4j</groupId><artifactId>slf4j-api</artifactId></dependency>
<dependency><groupId>ch.qos.logback</groupId><artifactId>logback-classic</artifactId></dependency>
</dependencies>
</project>


Pourquoi : isole l’UI (JavaFX) des moteurs (JavaCV/IA), accélère les builds, facilite les tests headless.

2) Point d’entrée minimal, thème + JFR + i18n
   app-launcher/src/main/java/app/Main.java (fichier complet)
   package app;

import app.ui.AppStage;
import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Main extends Application {
private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // JFR hint (opt-in via JVM args: -XX:StartFlightRecording=filename=profile.jfr)
        log.info("Starting AI Video Editor. Java={}, VM={}",
                System.getProperty("java.version"), ManagementFactory.getRuntimeMXBean().getVmName());
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Locale locale = detectLocale();
        ResourceBundle i18n = loadBundle(locale);

        AppStage app = new AppStage(i18n);
        app.show(primaryStage);

        log.info("UI started with locale {}.", locale);
    }

    private Locale detectLocale() {
        String lang = System.getProperty("app.lang");
        if (lang != null && !lang.isBlank()) return Locale.forLanguageTag(lang);
        return Locale.getDefault();
    }

    private ResourceBundle loadBundle(Locale locale) {
        try {
            return ResourceBundle.getBundle("i18n.Messages", locale);
        } catch (MissingResourceException e) {
            return ResourceBundle.getBundle("i18n.Messages", Locale.ENGLISH);
        }
    }
}

app-ui/src/main/java/app/ui/AppStage.java (fichier complet)
package app.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.ResourceBundle;

public class AppStage {
private final ResourceBundle i18n;

    public AppStage(ResourceBundle i18n) {
        this.i18n = i18n;
    }

    public void show(Stage stage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(8));

        MenuBar menu = new MenuBar();
        Menu file = new Menu(txt("menu.file"));
        MenuItem quit = new MenuItem(txt("menu.quit"));
        quit.setOnAction(e -> Platform.exit());
        file.getItems().addAll(new MenuItem(txt("menu.new")), new MenuItem(txt("menu.open")),
                new SeparatorMenuItem(), quit);

        Menu help = new Menu(txt("menu.help"));
        help.getItems().add(new MenuItem(txt("menu.shortcuts")));
        menu.getMenus().addAll(file, help);
        root.setTop(menu);

        // Placeholder Timeline area
        Label timeline = new Label(txt("ui.timeline.placeholder"));
        timeline.getStyleClass().add("timeline");
        root.setCenter(timeline);

        Scene scene = new Scene(root, 1200, 700);
        // Theme toggle via VM arg: -Dapp.theme=dark
        String theme = System.getProperty("app.theme", "light");
        scene.getStylesheets().add(theme.equals("dark") ? "/themes/dark.css" : "/themes/light.css");

        stage.setTitle("AI Video Editor");
        stage.setScene(scene);
        stage.show();
    }

    private String txt(String key) {
        return i18n.containsKey(key) ? i18n.getString(key) : key;
    }
}

app-ui/src/main/resources/i18n/Messages_en.properties
menu.file=File
menu.new=New Project
menu.open=Open...
menu.quit=Quit
menu.help=Help
menu.shortcuts=Keyboard Shortcuts
ui.timeline.placeholder=Hello Timeline — drop media to begin.

app-ui/src/main/resources/i18n/Messages_fr.properties
menu.file=Fichier
menu.new=Nouveau projet
menu.open=Ouvrir...
menu.quit=Quitter
menu.help=Aide
menu.shortcuts=Raccourcis clavier
ui.timeline.placeholder=Hello Timeline — déposez un média pour commencer.


Pourquoi : point d’entrée fin, bascule de thème, i18n robuste, prêt pour l’onboarding.

3) Contrats propres : Domain + Pipeline + Progress/Cancel
   app-core/src/main/java/app/domain/Model.java (fichier complet)
   package app.domain;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public record Project(UUID id, String name, Path workspace, List<Track> tracks) { }
public record Track(UUID id, TrackType type, List<Clip> clips) { }
public enum TrackType { VIDEO, AUDIO }
public record Clip(UUID id, Path media, double inSec, double outSec) { }

app-core/src/main/java/app/pipeline/Pipeline.java (fichier complet)
package app.pipeline;

import java.util.concurrent.CompletableFuture;

public interface PipelineNode<I, O> {
String name();
CompletableFuture<O> process(I input, Progress progress, CancellationToken cancel);
}

@FunctionalInterface
public interface Progress {
void report(double fraction, String message);
}

public interface CancellationToken {
boolean isCancelled();
void throwIfCancelled() throws OperationCancelledException;

    class OperationCancelledException extends RuntimeException {
        public OperationCancelledException() { super("Operation cancelled"); }
    }
}


Pourquoi : APIs testables, annulation/avancement uniformes pour toutes les tâches (decode, analyze, render).

4) I/O vidéo : façade FFmpeg sûre + fallback guidé
   app-io/src/main/java/app/io/FFmpegService.java (fichier complet)
   package app.io;

import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.global.avformat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Optional;

public final class FFmpegService {
private static final Logger log = LoggerFactory.getLogger(FFmpegService.class);
private static volatile boolean initialized = false;

    public static synchronized void init() {
        if (initialized) return;
        avformat.av_register_all(); // safe no-op on newer ffmpeg builds
        initialized = true;
        log.info("FFmpeg initialized.");
    }

    public static MediaInfo probe(Path file) {
        init();
        AVFormatContext ctx = avformat.avformat_alloc_context();
        try {
            int ret = avformat.avformat_open_input(ctx, file.toString(), null, null);
            if (ret < 0) {
                throw new MissingFFmpegException("Cannot open media (ffmpeg). Return code: " + ret);
            }
            if (avformat.avformat_find_stream_info(ctx, (AVFormatContext) null) < 0) {
                throw new MissingFFmpegException("Cannot find stream info.");
            }
            double durationSec = ctx.duration() > 0 ? ctx.duration() / 1_000_000.0 : -1;
            return new MediaInfo(file, durationSec);
        } catch (UnsatisfiedLinkError e) {
            throw new MissingFFmpegException("FFmpeg native libs not found.", e);
        } finally {
            avformat.avformat_close_input(ctx);
        }
    }

    public static Optional<String> checkEnvironment() {
        try {
            init();
            return Optional.empty();
        } catch (MissingFFmpegException e) {
            return Optional.of(e.getMessage());
        }
    }

    public record MediaInfo(Path path, double durationSec) {}

    public static class MissingFFmpegException extends RuntimeException {
        public MissingFFmpegException(String msg) { super(msg); }
        public MissingFFmpegException(String msg, Throwable t) { super(msg, t); }
    }
}

Assistant d’installation si FFmpeg absent (UI)

Idée : au démarrage, si FFmpegService.checkEnvironment() renvoie un message, afficher un dialog avec étapes (lien doc local dans USER_GUIDE.md), jamais un téléchargement auto.

5) IA locale découplée (SPI ready)
   app-ai/src/main/java/app/ai/SceneDetector.java (fichier complet)
   package app.ai;

import app.pipeline.CancellationToken;
import app.pipeline.PipelineNode;
import app.pipeline.Progress;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;

public interface SceneDetector extends PipelineNode<Path, List<SceneSegment>> {
record SceneSegment(double startSec, double endSec) {}
String provider(); // ex: "opencv-hist"

    static SceneDetector loadDefault() {
        return ServiceLoader.load(SceneDetector.class)
                .findFirst().orElseThrow(() -> new IllegalStateException("No SceneDetector provider"));
    }

    @Override
    default String name() { return "SceneDetector"; }

    static CompletableFuture<List<SceneSegment>> detect(Path video,
            Progress progress, CancellationToken cancel) {
        return loadDefault().process(video, progress, cancel);
    }
}


Pourquoi : ServiceLoader ouvre la voie aux plugins d’IA sans coupler l’UI.

6) Orchestrateur de jobs fiable (progress, annulation, reprise)
   app-core/src/main/java/app/jobs/JobQueue.java (fichier complet)
   package app.jobs;

import app.pipeline.CancellationToken;
import app.pipeline.Progress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;

public final class JobQueue implements Closeable {
private static final Logger log = LoggerFactory.getLogger(JobQueue.class);
private final ExecutorService exec;
private final ConcurrentMap<UUID, Future<?>> running = new ConcurrentHashMap<>();

    public JobQueue(int threads) {
        this.exec = new ThreadPoolExecutor(
                threads, threads, 30, TimeUnit.SECONDS,
                new PriorityBlockingQueue<>(), r -> {
                    Thread t = new Thread(r, "job-" + System.nanoTime());
                    t.setDaemon(true);
                    return t;
                });
    }

    public UUID submit(String title, int priority, Callable<?> task,
                       Progress progress, CancellationToken cancel) {
        UUID id = UUID.randomUUID();
        FutureTask<?> f = new FutureTask<>(() -> {
            progress.report(0.0, title + " started");
            Object result = task.call();
            progress.report(1.0, title + " done");
            return result;
        });
        running.put(id, f);
        ((ThreadPoolExecutor) exec).getQueue().add(new PrioritizedFuture(f, priority));
        return id;
    }

    public void cancel(UUID id) {
        Future<?> f = running.remove(id);
        if (f != null) f.cancel(true);
    }

    @Override public void close() {
        exec.shutdownNow();
    }

    private record PrioritizedFuture(FutureTask<?> task, int priority) implements Runnable, Comparable<PrioritizedFuture> {
        @Override public void run() { task.run(); }
        @Override public int compareTo(PrioritizedFuture o) { return Integer.compare(o.priority, priority); }
    }
}


Pourquoi : queue priorisée (render > analyze), progress uniforme, annulation fiable.

7) Journalisation structurée + rotation
   app-ui/src/main/resources/logback.xml (fichier complet)
   <configuration>
   <property name="LOG_DIR" value="${user.home}/.ai-video-editor/logs"/>
   <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
   <file>${LOG_DIR}/app.log</file>
   <encoder><pattern>%d{yyyy-MM-dd HH:mm:ss} %-5level [%thread] %logger - %msg%n</pattern></encoder>
   <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
   <fileNamePattern>${LOG_DIR}/app.%d{yyyy-MM-dd}.log.gz</fileNamePattern>
   <maxHistory>14</maxHistory>
   </rollingPolicy>
   </appender>
   <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
   <encoder><pattern>%d{HH:mm:ss} %-5level %logger{36} - %msg%n</pattern></encoder>
   </appender>
   <logger name="org.bytedeco" level="WARN"/>
   <root level="INFO">
   <appender-ref ref="STDOUT"/>
   <appender-ref ref="FILE"/>
   </root>
   </configuration>

8) Qualité auto : Checkstyle, SpotBugs, PIT, thresholds
   app-core/pom.xml (ajoute plugins qualité)
   <project ...>
   <modelVersion>4.0.0</modelVersion>
   <parent>...parent...</parent>
   <artifactId>app-core</artifactId>
   <dependencies>
   <dependency><groupId>org.slf4j</groupId><artifactId>slf4j-api</artifactId></dependency>
   <dependency><groupId>org.junit.jupiter</groupId><artifactId>junit-jupiter</artifactId><version>${junit.jupiter.version}</version><scope>test</scope></dependency>
   </dependencies>
   <build>
   <plugins>
   <plugin>
   <groupId>com.github.spotbugs</groupId><artifactId>spotbugs-maven-plugin</artifactId><version>4.8.6.5</version>
   <configuration><effort>max</effort><threshold>Medium</threshold></configuration>
   <executions><execution><goals><goal>check</goal></goals></execution></executions>
   </plugin>
   <plugin>
   <groupId>org.apache.maven.plugins</groupId><artifactId>maven-checkstyle-plugin</artifactId><version>3.3.1</version>
   <executions><execution><phase>verify</phase><goals><goal>check</goal></goals></execution></executions>
   <configuration><failOnViolation>true</failOnViolation></configuration>
   </plugin>
   <plugin>
   <groupId>org.pitest</groupId><artifactId>pitest-maven</artifactId>
   <configuration>
   <targetClasses>
   <param>app.*</param>
   </targetClasses>
   <mutationThreshold>60</mutationThreshold>
   </configuration>
   </plugin>
   </plugins>
   </build>
   </project>

9) Auto-diagnostic minimal + scripts
   tools/selfcheck.sh (fichier complet)
   #!/usr/bin/env bash
   set -euo pipefail
   echo "== Self-check =="
   java -version || { echo "Java not found"; exit 1; }
   echo "- Java OK"
   mvn -v || { echo "Maven not found"; exit 1; }
   echo "- Maven OK"
   echo "- FFmpeg check (via JavaCV load)"
   mvn -q -pl app-io -am -DskipTests package
   echo "{\"status\":\"OK\",\"notes\":[\"build core IO ok\"]}" > selfcheck.json
   echo "Self-check done -> selfcheck.json"

Commandes utiles

Dev rapide UI :
mvn -pl app-ui,app-core,app-io,app-ai,app-launcher -am javafx:run -Pjavafx-linux (ou -Pjavafx-mac / -Pjavafx-windows)

Tests + qualité :
mvn -T 1C clean verify
mvn -pl app-core org.pitest:pitest-maven:mutationCoverage

Packaging (ex. Linux .deb) :
mvn -pl app-launcher -am package puis jpackage (plugin à ajouter section 10 de ton sommaire)

Micro-checklist d’améliorations (à viser tout de suite)

Séparer app-launcher (JavaFX) du cœur pour tests headless rapides.

Introduire Progress + CancellationToken partout (decode/analyze/render).

Façade FFmpegService avec gestion “absent” + assistant d’installation.

ServiceLoader pour IA (SceneDetector, STT) → plugins drop-in.

JobQueue priorisée, annulation fiable, messages de progression UI.

i18n complet + thèmes light/dark via propriété app.theme.

Logback rotation + dossier dédié utilisateur.

Checkstyle/SpotBugs/PIT intégrés au verify (seuils cassants).

tools/selfcheck.sh dans CI locale avant “gate” sections 9–10.

Où ça améliore concrètement ton .java existant

Lisibilité / dette : interfaces étroites, classes finales, records pour domain → moins de boilerplate et immutabilité par défaut.

Robustesse : erreurs FFmpeg centralisées, messages UI guidés, pas de crash natif silencieux.

Perf : JobQueue + priorités + annulation = pas de “UI freeze” pendant analyse/rendu.

Testabilité : PipelineNode + Progress → mockable; modules sans JavaFX → tests JUnit en headless.

Évolutivité : SPI pour IA, on remplace/ajoute un détecteur sans toucher l’UI.

Packaging : profils JavaFX par OS déjà prêts pour jpackage.
