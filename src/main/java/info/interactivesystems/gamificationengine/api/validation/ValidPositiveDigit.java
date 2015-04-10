package info.interactivesystems.gamificationengine.api.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.regex.Pattern;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

/**
 * The annotated {@code CharSequence} must match the following regular
 * expression: \d+
 * <p/>
 * Accepts any digit, also a zero. {@code null} elements are considered valid.
 */
@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = ValidPositiveDigit.PositiveDigitValidator.class)
@Documented
public @interface ValidPositiveDigit {

	public static class PositiveDigitValidator implements ConstraintValidator<ValidPositiveDigit, CharSequence> {

		static final Pattern pattern = Pattern.compile("\\d+");

		@Override
		public void initialize(ValidPositiveDigit constraintAnnotation) {
		}

		@Override
		public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
			return value == null || pattern.matcher(value).matches();
		}
	}

	String message() default "{Invalid number, must match \\d+}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
