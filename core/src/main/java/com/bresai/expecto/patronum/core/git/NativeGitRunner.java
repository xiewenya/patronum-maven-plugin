package com.bresai.expecto.patronum.core.git;

import pl.project13.core.GitCommitIdExecutionException;
import pl.project13.core.log.LoggerBridge;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static java.lang.String.format;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/11/26
 * @content:
 */
public class NativeGitRunner implements GitRunner {

    final File dotGitDirectory;

    final long nativeGitTimeoutInMs;

    final File canonical;

    private transient ProcessRunner runner;

    private LoggerBridge log;

    private static NativeGitRunner nativeGitRunner;

    public static synchronized NativeGitRunner of(@Nonnull File dotGitDirectory, long nativeGitTimeoutInMs, @Nonnull LoggerBridge loggerBridge){
        if (nativeGitRunner == null){
            nativeGitRunner = new NativeGitRunner(dotGitDirectory, nativeGitTimeoutInMs, loggerBridge);
        }

        return nativeGitRunner;
    }

    private NativeGitRunner(@Nonnull File dotGitDirectory, long nativeGitTimeoutInMs, @Nonnull LoggerBridge loggerBridge) {
        this.log = loggerBridge;
        this.dotGitDirectory = dotGitDirectory;
        this.nativeGitTimeoutInMs = nativeGitTimeoutInMs;
        try {
            this.canonical = dotGitDirectory.getCanonicalFile();
        } catch (IOException ex) {
            throw new RuntimeException(new GitCommitIdExecutionException("Passed a invalid directory, not a GIT repository: " + dotGitDirectory, ex));
        }

    }

    public File getDotGitDirectory() {
        return dotGitDirectory;
    }

    public long getNativeGitTimeoutInMs() {
        return nativeGitTimeoutInMs;
    }

    public File getCanonical() {
        return canonical;
    }

    public LoggerBridge getLog() {
        return log;
    }

    /**
     * Runs a maven command and returns {@code true} if output was non empty.
     * Can be used to short cut reading output from command when we know it may be a rather long one.
     * Return true if the result is empty.
     **/
    @Override
    public boolean tryCheckEmptyRunGitCommand(File directory, long nativeGitTimeoutInMs, String gitCommand) {
        try {
            String env = System.getenv("GIT_PATH");
            String exec = env == null ? "git" : env;
            String command = String.format("%s %s", exec, gitCommand);

            return getRunner().runEmpty(directory, nativeGitTimeoutInMs, command);
        } catch (IOException ex) {
            log.error("Failed to run git command", ex);
            // Error means "non-empty"
            return false;
            // do nothing...
        }
    }

    @Override
    public String runQuietGitCommand(File directory, long nativeGitTimeoutInMs, String gitCommand) {
        final String env = System.getenv("GIT_PATH");
        final String exec = env == null ? "git" : env;
        final String command = String.format("%s %s", exec, gitCommand);

        try {
            return getRunner().run(directory, nativeGitTimeoutInMs, command.trim()).trim();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String runGitCommand(File directory, long nativeGitTimeoutInMs, String gitCommand) throws NativeCommandException {
        final String env = System.getenv("GIT_PATH");
        String exec = env == null ? "git" : env;
        final String command = String.format("%s %s", exec, gitCommand);

        try {
            return getRunner().run(directory, nativeGitTimeoutInMs, command.trim()).trim();
        } catch (NativeCommandException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ProcessRunner getRunner() {
        if (runner == null) {
            runner = new JavaProcessRunner();
        }
        return runner;
    }

    public interface ProcessRunner {
        /** Run a command and return the entire output as a String - naive, we know. */
        String run(File directory, long nativeGitTimeoutInMs, String command) throws IOException;

        /** Run a command and return false if it contains at least one output line*/
        boolean runEmpty(File directory, long nativeGitTimeoutInMs, String command) throws IOException;
    }

    public static class NativeCommandException extends IOException {
        private static final long serialVersionUID = 3511033422542257748L;
        private final int exitCode;
        private final String command;
        private final File directory;
        private final String stdout;
        private final String stderr;

        public NativeCommandException(
                int exitCode,
                String command,
                File directory,
                String stdout,
                String stderr) {
            this.exitCode = exitCode;
            this.command = command;
            this.directory = directory;
            this.stdout = stdout;
            this.stderr = stderr;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String getCommand() {
            return command;
        }

        public File getDirectory() {
            return directory;
        }

        public String getStdout() {
            return stdout;
        }

        public String getStderr() {
            return stderr;
        }

        @Override
        public String getMessage() {
            return format("Git command exited with invalid status [%d]: directory: `%s`, command: `%s`, stdout: `%s`, stderr: `%s`", exitCode, directory, command, stdout, stderr);
        }
    }

    protected static class JavaProcessRunner implements ProcessRunner {
        @Override
        public String run(File directory, long nativeGitTimeoutInMs, String command) throws IOException {
            String output = "";
            try {
                final StringBuilder commandResult = new StringBuilder();

                final Function<String, Boolean> stdoutConsumer = line -> {
                    if (line != null) {
                        commandResult.append(line).append("\n");
                    }
                    // return true to indicate we want to read more content
                    return true;
                };
                runProcess(directory, nativeGitTimeoutInMs, command, stdoutConsumer);

                output = commandResult.toString();
            } catch (final InterruptedException ex) {
                throw new IOException(ex);
            }
            return output;
        }

        @Override
        public boolean runEmpty(File directory, long nativeGitTimeoutInMs, String command) throws IOException {
            final AtomicBoolean empty = new AtomicBoolean(true);

            try {
                final Function<String, Boolean> stdoutConsumer = line -> {
                    if (line != null) {
                        empty.set(false);
                    }
                    // return false to indicate we don't need to read more content
                    return false;
                };
                runProcess(directory, nativeGitTimeoutInMs, command, stdoutConsumer);
            } catch (final InterruptedException ex) {
                throw new IOException(ex);
            }
            return empty.get(); // was non-empty
        }

        private void runProcess(
                File directory,
                long nativeGitTimeoutInMs,
                String command,
                final Function<String, Boolean> stdoutConsumer) throws InterruptedException, IOException {

            final ProcessBuilder builder = new ProcessBuilder(command.split("\\s"));
            final Process proc = builder.directory(directory).start();

            final ExecutorService executorService = Executors.newFixedThreadPool(2);
            final StringBuilder errMsg = new StringBuilder();

            final Future<Optional<RuntimeException>> stdoutFuture = executorService.submit(
                    new CallableBufferedStreamReader(proc.getInputStream(), stdoutConsumer));
            final Future<Optional<RuntimeException>> stderrFuture = executorService.submit(
                    new CallableBufferedStreamReader(proc.getErrorStream(),
                            line -> {
                                errMsg.append(line);
                                // return true to indicate we want to read more content
                                return true;
                            }));

            if (!proc.waitFor(nativeGitTimeoutInMs, TimeUnit.MILLISECONDS)) {
                proc.destroy();
                executorService.shutdownNow();
                throw new RuntimeException(String.format("GIT-Command '%s' did not finish in %d milliseconds", command, nativeGitTimeoutInMs));
            }

            try {
                stdoutFuture.get()
                        .ifPresent(e -> {
                            throw e;
                        });
                stderrFuture.get()
                        .ifPresent(e -> {
                            throw e;
                        });
            } catch (final ExecutionException e) {
                throw new RuntimeException(String.format("Executing GIT-Command '%s' threw an '%s' exception.", command, e.getMessage()), e);
            }

            executorService.shutdown();
            if (proc.exitValue() != 0) {
                throw new NativeCommandException(proc.exitValue(), command, directory, "", errMsg.toString());
            }
        }

        private static class CallableBufferedStreamReader implements Callable<Optional<RuntimeException>> {
            private final InputStream is;
            private final Function<String, Boolean> streamConsumer;

            CallableBufferedStreamReader(final InputStream is, final Function<String, Boolean> streamConsumer) {
                this.is = is;
                this.streamConsumer = streamConsumer;
            }

            @Override
            public Optional<RuntimeException> call() {
                RuntimeException thrownException = null;
                try (final BufferedReader br = new BufferedReader(
                        new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    for (String line = br.readLine();
                         line != null;
                         line = br.readLine()) {
                        if (!streamConsumer.apply(line)) {
                            break;
                        }
                    }
                } catch (final IOException e) {
                    thrownException = new RuntimeException(String.format("Executing GIT-Command threw an '%s' exception.", e.getMessage()), e);
                }

                return Optional.ofNullable(thrownException);
            }
        }
    }

}
