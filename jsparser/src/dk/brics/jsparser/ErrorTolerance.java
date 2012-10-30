package dk.brics.jsparser;

/**
 * What to do if a syntax error occurs.
 */
public enum ErrorTolerance {
    /**
     * Throw exception.
     */
    FAIL,

    /**
     * Try to make sense of what's happening.
     */
    COMPENSATE
}