package io.github.noeppi_noeppi.libx.impl.config.gui.editor;

import io.github.noeppi_noeppi.libx.config.gui.ConfigEditor;
import io.github.noeppi_noeppi.libx.impl.config.gui.screen.ConfigScreenManager;

// Internal interface for editors. Can be used to create editors
// that rely on a ConfigScreenManager
// If these editors are used normally, they should present as
// unsupported editor.
public interface InternalAwareEditor<T> extends ConfigEditor<T> {
    
    ConfigEditor<T> withManager(ConfigScreenManager manager);
}
