package org.jabref.gui.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import javafx.scene.Parent;
import javafx.scene.Scene;

import org.jabref.gui.JabRefFrame;
import org.jabref.model.strings.StringUtil;
import org.jabref.model.util.FileUpdateMonitor;
import org.jabref.preferences.JabRefPreferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Installs the style file and provides live reloading.
 * <p>
 * The live reloading has to be turned on by setting the <code>-Djabref.theme.css</code> property.
 * There two possible modes:
 * (1) When only <code>-Djabref.theme.css</code> is specified, then the standard <code>Base.css</code> that is found will be watched
 * and on changes in that file, the style-sheet will be reloaded and changes are immediately visible.
 * (2) When a path to a css file is passed to <code>-Djabref.theme.css</code>, then the given style is loaded in addition to the base css file.
 * Changes in the specified css file lead to an immediate redraw of the interface.
 * <p>
 * When working from an IDE, this usually means that the <code>Base.css</code> is located in the build folder.
 * To use the css-file that is located in the sources directly, the full path can be given as value for the "VM option":
 * <code>-Djabref.theme.css="/path/to/src/Base.css"</code>
 */
public class ThemeLoader {

    public static final String DEFAULT_MAIN_CSS = "Base.css";
    private static final String DEFAULT_PATH_MAIN_CSS = JabRefFrame.class.getResource(DEFAULT_MAIN_CSS).toExternalForm();
    private static final Logger LOGGER = LoggerFactory.getLogger(ThemeLoader.class);
    private String cssToLoad = System.getProperty("jabref.theme.css");
    private final FileUpdateMonitor fileUpdateMonitor;

    public ThemeLoader(FileUpdateMonitor fileUpdateMonitor, JabRefPreferences jabRefPreferences) {
        this.fileUpdateMonitor = Objects.requireNonNull(fileUpdateMonitor);

        if (!StringUtil.isNullOrEmpty(cssToLoad)) {
            LOGGER.info("using css from system " + cssToLoad);
            return;
        }

        // otherwise load css from preference
        String cssFileName = jabRefPreferences.get(JabRefPreferences.FX_THEME);
        if (cssFileName != null) {
            try {
                cssToLoad = JabRefFrame.class.getResource(cssFileName).toExternalForm();
                LOGGER.info("using css " + cssToLoad);
            } catch (Exception e) {
                LOGGER.warn("can't get css file path of " + cssFileName);
            }
        }
    }


    /**
     * Installs the base css file as a stylesheet in the given scene.
     * Changes in the css file lead to a redraw of the scene using the new css file.
     */
    public void installBaseCss(Scene scene, JabRefPreferences preferences) {
        if (!StringUtil.isNullOrEmpty(cssToLoad)) {
            addAndWatchForChanges(scene, cssToLoad, 0);
        } else {
            LOGGER.warn("using the last default css " + DEFAULT_PATH_MAIN_CSS);
            addAndWatchForChanges(scene, DEFAULT_PATH_MAIN_CSS, 0);
        }

        preferences.getFontSize().ifPresent(size -> scene.getRoot().setStyle("-fx-font-size: " + size + "pt;"));
    }

    private void addAndWatchForChanges(Scene scene, String cssUrl, int index) {
        // avoid repeat add
        if (scene.getStylesheets().contains(cssUrl)) return;

        scene.getStylesheets().add(index, cssUrl);

        try {
            // If -Djabref.theme.css is defined and the resources are not part of a .jar bundle,
            // we watch the file for changes and turn on live reloading
            if (!cssUrl.startsWith("jar:")) {
                Path cssFile = Paths.get(new URL(cssUrl).toURI());
                LOGGER.info("Enabling live reloading of " + cssFile);
                fileUpdateMonitor.addListenerForFile(cssFile, () -> {
                    LOGGER.info("Reload css file " + cssFile);
                    DefaultTaskExecutor.runInJavaFXThread(() -> {
                                scene.getStylesheets().remove(cssUrl);
                                scene.getStylesheets().add(index, cssUrl);
                            }
                    );
                });
            }
        } catch (URISyntaxException | IOException e) {
            LOGGER.error("Could not watch css file for changes " + cssUrl, e);
        }
    }

    /**
     * @deprecated you should never need to add css to a control, add it to the scene containing the control
     */
    @Deprecated
    public void installBaseCss(Parent control) {
        control.getStylesheets().add(0, DEFAULT_PATH_MAIN_CSS);
    }
}
