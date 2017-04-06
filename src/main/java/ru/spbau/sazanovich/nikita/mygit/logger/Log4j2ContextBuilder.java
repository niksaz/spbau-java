package ru.spbau.sazanovich.nikita.mygit.logger;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.*;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class which builds a context with one logger with a file appender in a given directory.
 */
public class Log4j2ContextBuilder {

    /**
     * Creates a context with one logger and a RollingFile appender.
     *
     * @param name name of the context's configuration
     * @param directory directory where to store logs
     * @return a context
     */
    @NotNull
    public static LoggerContext createContext(@NotNull String name, @NotNull Path directory) {
        final ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.setConfigurationName(name);
        builder.setStatusLevel(Level.OFF);
        final LayoutComponentBuilder layoutBuilder =
                builder
                        .newLayout("PatternLayout")
                        .addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable");
        final ComponentBuilder<?> triggeringPolicy =
                builder
                        .newComponent("Policies")
                        .addComponent(
                                builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", "1MB"));
        final ComponentBuilder<?> rolloverStrategy =
                builder
                        .newComponent("DefaultRolloverStrategy")
                        .addAttribute("max", 3);
        final Path logsPath = Paths.get(directory.toAbsolutePath().toString(), "logs");
        final AppenderComponentBuilder appenderBuilder =
                builder
                        .newAppender("file", "ROLLINGFILE")
                        .addAttribute("fileName", Paths.get(logsPath.toString(), "mygit0.log").toString())
                        .addAttribute("filePattern", Paths.get(logsPath.toString(), "mygit%i.log").toString())
                        .add(layoutBuilder)
                        .addComponent(triggeringPolicy)
                        .addComponent(rolloverStrategy);
        builder.add(appenderBuilder);
        final RootLoggerComponentBuilder rootLogger =
                builder
                        .newRootLogger(Level.TRACE)
                        .add(builder.newAppenderRef("file"))
                        .addAttribute("additivity", false);
        builder.add(rootLogger);
        return Configurator.initialize(builder.build());
    }

    private Log4j2ContextBuilder() {}
}
