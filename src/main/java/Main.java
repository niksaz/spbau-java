import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by niksaz on 04/05/2017.
 */
public class Main {
    public static void main(@NotNull String[] args) throws ClassNotFoundException, NoSuchMethodException {
        Class<?> c = Class.forName("Example");
        Object o = null;

        try {
            Constructor<?> constructor = c.getConstructor();
            o = constructor.newInstance();
        } catch (NoSuchMethodException e) {
            System.out.println("There is no public constructor with zero arguments");
            return;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        //List<Method>

        List<Method> beforeMethods = new ArrayList<>();
        List<Method> testMethods = new ArrayList<>();
        List<Method> afterMethods = new ArrayList<>();
        Method[] methods = c.getMethods();
        for (Method method : methods) {
            if (method.getAnnotation(XTest.class) == null) {
                continue;
            }
            XTest test = method.getAnnotation(XTest.class);
            System.out.println(test.expected());
            System.out.println(test.ignore());
            if (Modifier.isStatic(method.getModifiers())) {
                // TODO: create Exception
                System.out.println("Methods annotated with XTest should not be static: " + method.getName());
                return;
            }
            if (method.getParameterCount() != 0) {
                System.out.println("Methods annotated with XTest should be zero-arg: " + method.getName());
                return;
            }
            testMethods.add(method);
        }
        // TODO: format number
        System.out.println(testMethods.size() + " tests are found.");
        for (Method method : testMethods) {
            System.out.println("Running test -- " + method.getName());
            try {
                method.invoke(o);
                System.out.println("!!!Successfully");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                System.out.println(e.getCause());
            }
        }
    }
}
