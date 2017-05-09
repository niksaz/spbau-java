import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which allows to mark methods as test methods.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface XTest {

    String ignore() default "";

    Class<? extends Throwable> expected() default None.class;

    /**
     * Class representing no exception.
     */
    class None extends Throwable {

        private None() {
        }
    }
}
