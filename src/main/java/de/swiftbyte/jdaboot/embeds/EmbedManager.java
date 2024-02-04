package de.swiftbyte.jdaboot.embeds;

import de.swiftbyte.jdaboot.annotation.embed.Embed;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Modifier;

/**
 * The EmbedManager class is responsible for managing embeds in the application.
 * It uses reflection to find fields annotated with @Embed and sets them to a new instance of TemplateEmbed created with the @Embed annotation.
 *
 * @since alpha.4
 */
@Slf4j
public class EmbedManager {

    /**
     * Constructor for EmbedManager. Initializes the manager with the specified main class.
     *
     * @param mainClass The main class of your project.
     * @since alpha.4
     */
    public EmbedManager(Class<?> mainClass) {

        Reflections reflections = new Reflections(mainClass.getPackageName().split("\\.")[0], Scanners.FieldsAnnotated);

        reflections.getFieldsAnnotatedWith(Embed.class).forEach(field -> {

            Embed embedAnnotation = field.getAnnotation(Embed.class);

            if (Modifier.isStatic(field.getModifiers())) {
                try {
                    field.setAccessible(true);
                    field.set(null, new TemplateEmbed(embedAnnotation));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else {
                log.warn("An embed must be a static field. Skipping field " + field + "...");
            }
        });
    }
}