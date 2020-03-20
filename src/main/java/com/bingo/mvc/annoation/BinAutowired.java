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
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BinAutowired {

    String value() default "";
}
