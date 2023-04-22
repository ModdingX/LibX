package org.moddingx.libx.render.target;

/**
 * Thrown when a {@link RenderJob} fails to render correctly.
 */
public class RenderJobFailedException extends RuntimeException {

    /**
     * The reason for the failure.
     */
    public final Reason reason;
    
    public RenderJobFailedException(Reason reason) {
        super(reason.name());
        this.reason = reason;
    }
    
    public RenderJobFailedException(String message) {
        this(Reason.GENERIC, message);
    }

    public RenderJobFailedException(String message, Throwable cause) {
        this(Reason.GENERIC, message, cause);
    }

    public RenderJobFailedException(Reason reason, String message) {
        super(reason == Reason.GENERIC ? message : reason.name() + ": " + message);
        this.reason = reason;
    }

    public RenderJobFailedException(Reason reason, String message, Throwable cause) {
        super(reason == Reason.GENERIC ? message : reason.name() + ": " + message, cause);
        this.reason = reason;
    }

    /**
     * A reason for the {@link RenderJob} to fail.
     */
    public enum Reason {

        /**
         * Generic reason.
         */
        GENERIC,

        /**
         * The image size requested by the {@link RenderJob} exceeds the maximum texture size of
         * the current OpenGL implementation.
         */
        TEXTURE_TOO_LARGE
    }
}
