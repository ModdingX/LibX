package org.moddingx.libx.datagen.provider.sandbox;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.CommonButtonData;
import net.minecraft.server.dialog.CommonDialogData;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.DialogAction;
import net.minecraft.server.dialog.Input;
import net.minecraft.server.dialog.action.Action;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.server.dialog.input.InputControl;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;
import org.moddingx.libx.datagen.provider.RegistryProviderBase;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * SandBox provider for {@link Dialog dialogs}.
 *
 * This provider must run in the {@link DatagenStage#REGISTRY_SETUP registry setup} stage.
 */
public abstract class DialogProviderBase extends RegistryProviderBase {

    protected DialogProviderBase(DatagenContext ctx) {
        super(ctx, DatagenStage.REGISTRY_SETUP);
    }

    @Override
    public final String getName() {
        return this.mod.modid + " dialogs";
    }

    /**
     * Registers the given {@link Dialog}.
     *
     * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
     * added to the registry. {@link RegistryProviderBase} does this automatically if the result is stored in a
     * {@code public}, non-{@code static} field inside the provider.
     */
    public Holder<Dialog> dialog(Dialog dialog) {
        return this.registries.writableRegistry(Registries.DIALOG).createIntrusiveHolder(dialog);
    }

    /**
     * Returns a new builder for the {@link CommonDialogData common data} that every dialog type requires.
     */
    public DialogDataBuilder data(Component title) {
        return new DialogDataBuilder(title);
    }

    /**
     * Creates a button without an action. Closing behavior is defined by the dialog type the button is used in.
     */
    public static ActionButton button(Component label) {
        return button(buttonData(label), null);
    }

    /**
     * Creates a button that runs the given action when clicked.
     */
    public static ActionButton button(Component label, Action action) {
        return button(buttonData(label), action);
    }

    /**
     * Creates a button that runs the given action when clicked. Pass {@code null} for a button without action.
     */
    public static ActionButton button(CommonButtonData button, @Nullable Action action) {
        return new ActionButton(button, Optional.ofNullable(action));
    }

    /**
     * Creates button data with the default width of {@value CommonButtonData#DEFAULT_WIDTH}.
     */
    public static CommonButtonData buttonData(Component label) {
        return new CommonButtonData(label, CommonButtonData.DEFAULT_WIDTH);
    }

    /**
     * Creates button data with the given width.
     */
    public static CommonButtonData buttonData(Component label, int width) {
        return new CommonButtonData(label, width);
    }

    /**
     * Creates button data with a tooltip and the given width.
     */
    public static CommonButtonData buttonData(Component label, Component tooltip, int width) {
        return new CommonButtonData(label, Optional.of(tooltip), width);
    }

    /**
     * Creates a plain text dialog body with the default width of {@value PlainMessage#DEFAULT_WIDTH}.
     */
    public static PlainMessage message(Component contents) {
        return new PlainMessage(contents, PlainMessage.DEFAULT_WIDTH);
    }

    /**
     * Creates a plain text dialog body with the given width.
     */
    public static PlainMessage message(Component contents, int width) {
        return new PlainMessage(contents, width);
    }

    public static class DialogDataBuilder {

        private final Component title;
        private final List<DialogBody> body;
        private final List<Input> inputs;
        @Nullable
        private Component externalTitle;
        private boolean canCloseWithEscape;
        private boolean pause;
        private DialogAction afterAction;

        private DialogDataBuilder(Component title) {
            this.title = title;
            this.body = new ArrayList<>();
            this.inputs = new ArrayList<>();
            this.externalTitle = null;
            this.canCloseWithEscape = true;
            this.pause = true;
            this.afterAction = DialogAction.CLOSE;
        }

        /**
         * Sets the title used where the dialog is referenced from the outside, for example in a dialog list.
         * Defaults to the regular title.
         */
        public DialogDataBuilder externalTitle(Component externalTitle) {
            this.externalTitle = externalTitle;
            return this;
        }

        /**
         * Sets whether the dialog can be closed by pressing escape. Defaults to {@code true}.
         */
        public DialogDataBuilder canCloseWithEscape(boolean canCloseWithEscape) {
            this.canCloseWithEscape = canCloseWithEscape;
            return this;
        }

        /**
         * Sets whether the game is paused while the dialog is open. Defaults to {@code true}. A pausing dialog
         * must use an {@link #afterAction(DialogAction) after action} that unpauses the game again.
         */
        public DialogDataBuilder pause(boolean pause) {
            this.pause = pause;
            return this;
        }

        /**
         * Sets what happens after a button of this dialog has been clicked. Defaults to
         * {@link DialogAction#CLOSE}.
         */
        public DialogDataBuilder afterAction(DialogAction afterAction) {
            this.afterAction = afterAction;
            return this;
        }

        /**
         * Adds a plain text paragraph to the dialog body.
         */
        public DialogDataBuilder body(Component contents) {
            return this.body(message(contents));
        }

        /**
         * Adds elements to the dialog body.
         */
        public DialogDataBuilder body(DialogBody... body) {
            this.body.addAll(Arrays.asList(body));
            return this;
        }

        /**
         * Adds an input control to the dialog. Its value can be referenced by actions of this dialog through
         * the given key.
         */
        public DialogDataBuilder input(String key, InputControl control) {
            return this.input(new Input(key, control));
        }

        /**
         * Adds input controls to the dialog.
         */
        public DialogDataBuilder input(Input... inputs) {
            this.inputs.addAll(Arrays.asList(inputs));
            return this;
        }

        /**
         * Builds the {@link CommonDialogData}.
         */
        public CommonDialogData build() {
            if (this.pause && !this.afterAction.willUnpause()) {
                throw new IllegalStateException("Dialog pauses the game but its after action '" + this.afterAction.getSerializedName() + "' does not unpause it.");
            }
            return new CommonDialogData(
                    this.title,
                    Optional.ofNullable(this.externalTitle),
                    this.canCloseWithEscape,
                    this.pause,
                    this.afterAction,
                    List.copyOf(this.body),
                    List.copyOf(this.inputs)
            );
        }
    }
}
