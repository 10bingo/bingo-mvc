package com.bingo.mvc.annoation;

import java.lang.annotation.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: bingo
 * Date: 2019-07-12
 * Time: 8:35
 */

@Documented
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BinRequestMapping {

    String value() default "";
}
