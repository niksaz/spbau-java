package ru.spbau.sazanovich.nikita.xunit.runner;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.sazanovich.nikita.xunit.*;
import ru.spbau.sazanovich.nikita.xunit.XTest.None;

import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Class which allows to run test classes and log results to the given {@link PrintStream}.
 */
class Executor {

    @NotNull
    private final PrintStream logStream;

    /**
     * Constructs an executor with the given output stream.
     *
     * @param logStream a stream where to write logs
     */
    Executor(@NotNull PrintStream logStream) {
        this.logStream = logStream;
    }

    /**
     * Executes tests which are located in the class with a given name.
     * Execution is as follows:
     * 1) methods with {@link XBeforeClass} annotation
     * 2) methods with {@link XTest} annotation;
     *    each one is preceded with all {@link XBefore} and is followed with all {@link XAfter} methods
     * 3) methods with {@link XAfterClass} annotation
     *
     * @param testClassName class name with tests to be executed
     * @throws IllegalTestClassException if given class is not valid test class
     */
    void execute(@NotNull String testClassName) throws IllegalTestClassException {
        logStream.println("==================================================");
        logStream.println("Running tests of " + testClassName + " class");

        Class<?> testClass = getTestClass(testClassName);

        List<Method> beforeClassMethods = extractMethodsWithAnnotation(testClass, XBeforeClass.class);
        ensureXClassMethods(beforeClassMethods);
        List<Method> afterClassMethods = extractMethodsWithAnnotation(testClass, XAfterClass.class);
        ensureXClassMethods(afterClassMethods);

        List<Method> beforeMethods = extractMethodsWithAnnotation(testClass, XBefore.class);
        ensureXMethods(beforeMethods);
        List<Method> afterMethods = extractMethodsWithAnnotation(testClass, XAfter.class);
        ensureXMethods(afterMethods);
        List<Method> testMethods = extractMethodsWithAnnotation(testClass, XTest.class);
        ensureXMethods(testMethods);

        Object testClassInstance = getTestClassInstance(testClass);
        boolean beforeClassSuccessful = runNonTestMethods(beforeClassMethods, testClass);

        if (!beforeClassSuccessful) {
            logStream.println("Tests are ignored because @XBeforeClass methods failed");
        } else {
            runTestMethods(beforeMethods, testMethods, afterMethods, testClassInstance);
        }

        runNonTestMethods(afterClassMethods, testClass);
        logStream.println();
    }

    private void runTestMethods(@NotNull List<Method> beforeMethods, @NotNull List<Method> testMethods,
                                @NotNull List<Method> afterMethods, @NotNull Object instance) {
        for (Method method : testMethods) {
            logStream.println();
            logStream.println("Test: " + method.getName());
            XTest testAnnotation = method.getAnnotation(XTest.class);
            String ignore = testAnnotation.ignore();
            if (ignore.length() > 0) {
                logStream.println("Ignored because of custom cause \"" + ignore + "\"");
                continue;
            }

            boolean beforeSuccessful = runNonTestMethods(beforeMethods, instance);
            if (!beforeSuccessful) {
                logStream.println("Ignored because @XBefore methods failed");
                continue;
            }

            long timeStarted = System.currentTimeMillis();
            boolean finished = true;
            Throwable caughtException = null;
            try {
                method.invoke(instance);
            } catch (IllegalAccessException e) {
                e.printStackTrace(logStream);
                finished = false;
            } catch (InvocationTargetException e) {
                caughtException = e.getCause();
            } finally {
                if (finished) {
                    checkWhetherTestPassed(method.getAnnotation(XTest.class).expected(), caughtException);
                    long timeSpent = System.currentTimeMillis() - timeStarted;
                    logStream.println("Time spent is " + timeSpent + " ms");
                }
            }

            runNonTestMethods(afterMethods, instance);
        }
    }

    private void checkWhetherTestPassed(@NotNull Class<? extends Throwable> expectedClass,
                                        @Nullable    Throwable caughtException) {
        if (caughtException == null) {
            if (expectedClass.equals(None.class)) {
                showSuccessful();
            } else {
                showFailedBecauseExpected(expectedClass.getName());
            }
        } else {
            if (expectedClass.isAssignableFrom(caughtException.getClass())) {
                showSuccessful();
            } else {
                if (expectedClass.equals(None.class)) {
                    showFailedBecauseOf(caughtException);
                } else {
                    showFailedBecauseExpectedButGot(expectedClass.getName(), caughtException);
                }
            }
        }
    }

    void showSuccessful() {
        logStream.println("Successful");
    }

    void showFailedBecauseExpected(@NotNull String name) {
        logStream.println("Failed because expected " + name);
    }

    void showFailedBecauseOf(@NotNull Throwable throwable) {
        logStream.println("Failed because of");
        throwable.printStackTrace(logStream);
    }

    void showFailedBecauseExpectedButGot(@NotNull String name, @NotNull Throwable throwable) {
        logStream.println("Failed because expected " + name + " but got");
        throwable.printStackTrace(logStream);
    }

    private boolean runNonTestMethods(@NotNull List<Method> methods, @NotNull Object instance) {
        for (Method method : methods) {
            try {
                method.invoke(instance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace(logStream);
                return false;
            }
        }
        return true;
    }

    @NotNull
    private Class<?> getTestClass(@NotNull String className) throws IllegalTestClassException {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalTestClassException("Could not find " + className + " class", e);
        }
    }

    @NotNull
    private Object getTestClassInstance(@NotNull Class<?> testClass) throws IllegalTestClassException {
        Constructor<?> constructor;
        try {
            constructor = testClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalTestClassException(testClass.getName() + " should have public parameterless constructor", e);
        }
        try {
            return constructor.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalTestClassException(testClass.getName() + " should not be abstract", e);
        } catch (IllegalAccessException e) {
            throw new IllegalTestClassException("Could not access " + constructor, e);
        } catch (InvocationTargetException e) {
            throw new IllegalTestClassException("Constructor has thrown an exception: " + e.getMessage(), e);
        }
    }

    private void ensureXMethods(@NotNull List<Method> methods) throws IllegalTestClassException {
        Optional<Method> staticMethod =
                methods.stream().filter(method -> Modifier.isStatic(method.getModifiers())).findAny();
        if (staticMethod.isPresent()) {
            throw new IllegalTestClassException(staticMethod.get().getName() + " should not be static");
        }
        ensureParameterless(methods);
    }

    private void ensureXClassMethods(@NotNull List<Method> methods) throws IllegalTestClassException {
        Optional<Method> nonStaticMethod =
                methods.stream().filter(method -> !Modifier.isStatic(method.getModifiers())).findAny();
        if (nonStaticMethod.isPresent()) {
            throw new IllegalTestClassException(nonStaticMethod.get().getName() + " should be static");
        }
        ensureParameterless(methods);
    }

    private void ensureParameterless(@NotNull List<Method> methods) throws IllegalTestClassException {
        Optional<Method> nonParameterlessMethod =
                methods.stream().filter(method -> method.getParameterCount() > 0).findAny();
        if (nonParameterlessMethod.isPresent()) {
            throw new IllegalTestClassException(nonParameterlessMethod.get().getName() + " should be parameterless");
        }
    }

    @NotNull
    private static <T extends Annotation> List<Method> extractMethodsWithAnnotation(@NotNull Class<?> cl,
                                                                                    @NotNull Class<T> annotation) {
        return Arrays
                .stream(cl.getMethods())
                .filter(method -> method.getAnnotation(annotation) != null)
                .collect(Collectors.toList());
    }
}
