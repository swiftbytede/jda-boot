package de.swiftbyte.jdaboot;

import de.swiftbyte.jdaboot.annotation.JDABootConfiguration;
import de.swiftbyte.jdaboot.cli.ConsoleCommandManager;
import de.swiftbyte.jdaboot.configuration.ConfigProvider;
import de.swiftbyte.jdaboot.configuration.ConfigValueManager;
import de.swiftbyte.jdaboot.embeds.EmbedManager;
import de.swiftbyte.jdaboot.event.EventManager;
import de.swiftbyte.jdaboot.interactions.buttons.ButtonManager;
import de.swiftbyte.jdaboot.interactions.commands.CommandManager;
import de.swiftbyte.jdaboot.scheduler.SchedulerManager;
import de.swiftbyte.jdaboot.variables.TranslationProvider;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Manages the configuration for the JDABoot framework.
 * It applies the configuration specified by the {@link JDABootConfiguration} annotation and initializes various managers.
 *
 * @see JDABootConfiguration
 * @see ConfigProvider
 * @see TranslationProvider
 * @see CommandManager
 * @see ConsoleCommandManager
 * @see ButtonManager
 * @see ConfigValueManager
 * @see EventManager
 * @see EmbedManager
 * @see SchedulerManager
 * @since alpha.4
 */
@Slf4j
public class JDABootConfigurationManager {

    @Getter(AccessLevel.PROTECTED)
    private static List<GatewayIntent> intents;

    @Getter(AccessLevel.PROTECTED)
    private static List<CacheFlag> enabledCacheFlags;

    @Getter(AccessLevel.PROTECTED)
    private static List<CacheFlag> disabledCacheFlags;

    @Getter(AccessLevel.PROTECTED)
    private static MemberCachePolicy memberCachePolicy;


    /**
     * The configuration provider used to retrieve configuration values.
     *
     * @since alpha.4
     */
    @Getter
    @Setter
    private static ConfigProvider configProvider;

    /**
     * The translation provider used to retrieve translations.
     *
     * @since alpha.4
     */
    @Getter
    @Setter
    private static TranslationProvider translationProvider;


    @Getter(AccessLevel.PROTECTED)
    private static CommandManager commandManager;

    @Getter(AccessLevel.PUBLIC)
    private static ButtonManager buttonManager;


    private static boolean consoleCommandsEnabled;


    /**
     * Applies the configuration specified by the {@link JDABootConfiguration} annotation.
     *
     * @param mainClass The main class of the application.
     * @since alpha.4
     */
    protected static void configure(Class<?> mainClass) {
        JDABootConfiguration jdaBootConfiguration = mainClass.getAnnotation(JDABootConfiguration.class);
        if (jdaBootConfiguration == null) {
            jdaBootConfiguration = JDABoot.class.getAnnotation(JDABootConfiguration.class);
        }
        applyConfiguration(jdaBootConfiguration);
    }

    /**
     * Applies the configuration specified by the {@link JDABootConfiguration} annotation.
     *
     * @param jdaBootConfiguration The AutoConfiguration annotation to apply.
     * @since alpha.4
     */
    private static void applyConfiguration(@NotNull JDABootConfiguration jdaBootConfiguration) {

        configProvider = (ConfigProvider) JDABootObjectManager.getOrInitialiseObject(jdaBootConfiguration.configProviderChain()[0]);
        for (int i = 1; i < jdaBootConfiguration.configProviderChain().length; i++) {
            configProvider.addConfigProviderToChain((ConfigProvider) JDABootObjectManager.getOrInitialiseObject(jdaBootConfiguration.configProviderChain()[i]));
        }

        String configProfile = configProvider.getString("profile", jdaBootConfiguration.configProfile());
        log.info("Using configuration profile: '" + configProfile + "'");
        configProvider.setConfigProfile(configProfile);
        configProvider.reload();

        try {
            translationProvider = jdaBootConfiguration.translationProvider().getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            log.error("Failed to instantiate translation provider", e);
            System.exit(1);
        }

        intents = List.of(jdaBootConfiguration.intents());
        enabledCacheFlags = List.of(jdaBootConfiguration.enabledCacheFlags());
        disabledCacheFlags = List.of(jdaBootConfiguration.disabledCacheFlags());
        memberCachePolicy = jdaBootConfiguration.memberCachePolicy().getJDAUtilsMemberCachePolicy();

        consoleCommandsEnabled = jdaBootConfiguration.enableConsoleCommands();
    }

    /**
     * Initializes various managers.
     *
     * @param mainClass The main class of the application.
     * @param jda       The JDA instance.
     * @since alpha.4
     */
    protected static void initialiseManagers(Class<?> mainClass, JDA jda) {
        commandManager = new CommandManager(jda, mainClass);
        buttonManager = new ButtonManager(jda, mainClass);

        new EventManager(jda, mainClass);
        new ConfigValueManager(mainClass);
        new EmbedManager(mainClass);
        new SchedulerManager(mainClass);

        if (consoleCommandsEnabled) new ConsoleCommandManager(mainClass);
    }
}
