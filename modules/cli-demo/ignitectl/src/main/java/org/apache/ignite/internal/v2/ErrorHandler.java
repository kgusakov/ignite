package org.apache.ignite.internal.v2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

public class ErrorHandler implements CommandLine.IExecutionExceptionHandler {
    Logger logger = LoggerFactory.getLogger(ErrorHandler.class);

    @Override public int handleExecutionException(Exception ex, CommandLine cmd,
        CommandLine.ParseResult parseResult) throws Exception {
        if (ex instanceof IgniteCLIException)
            cmd.getErr().println(cmd.getColorScheme().errorText(ex.getMessage()));
        else {
            cmd.getErr().println(cmd.getColorScheme().errorText(ex.getMessage()));
            logger.error("", ex);
        }
        return cmd.getExitCodeExceptionMapper() != null
            ? cmd.getExitCodeExceptionMapper().getExitCode(ex)
            : cmd.getCommandSpec().exitCodeOnExecutionException();
    }
}
