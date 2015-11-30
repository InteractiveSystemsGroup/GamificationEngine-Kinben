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
 * expression: (\d+)(,\s*\d+)*
 * <p></p>
 * Accepts any digit or a list of comma separated digits, also zeros.
 * {@code null} elements are considered valid.
 */
@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = ValidListOfDigits.ListOfDigitsValidator.class)
@Documented
public @interface ValidListOfDigits {

	public static class ListOfDigitsValidator implements ConstraintValidator<ValidListOfDigits, CharSequence> {

		static final Pattern pattern = Pattern.compile("(\\d+)(,\\s*\\d+)*");

		@Override
		public void initialize(ValidListOfDigits constraintAnnotation) {
		}

		@Override
		public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
			return value == null || pattern.matcher(value).matches();
		}
	}

	String message() default "{Invalid entry, must contain only comma separated numbers, implies matching (\\d+)(,\\s*\\d+)*}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
