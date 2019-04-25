package elasticsearch.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 更新es所添加的注解
 * 
 * @author Lenovo
 *
 */
@Documented
@Inherited
@Target(ElementType.METHOD )
@Retention(RetentionPolicy.RUNTIME)
public @interface EsUpdate {

}
