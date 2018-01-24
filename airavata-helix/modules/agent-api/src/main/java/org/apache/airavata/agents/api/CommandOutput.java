package org.apache.airavata.agents.api;

import java.io.OutputStream;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public interface CommandOutput {

        /**
         * Gets standard error as a output stream.
         * @return Command error as a stream.
         */
        OutputStream getStandardError();

        /**
         * The command exit code.
         * @param code The program exit code
         */
        void exitCode(int code);

        /**
         * Return the exit code of the command execution.
         * @return exit code
         */
        int getExitCode();

}
