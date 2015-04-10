package info.interactivesystems.gamificationengine.api.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import info.interactivesystems.gamificationengine.dao.OrganisationDAO;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Inject;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

/**
 * The annotated {@code CharSequence} will be checked for existance in data
 * base.
 * 
 * Note: {@code null} elements are considered as invalid.
 */
@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = ValidApiKey.ApiKeyValidator.class)
@Documented
public @interface ValidApiKey {

	public static class ApiKeyValidator implements ConstraintValidator<ValidApiKey, CharSequence> {

		@Inject
		OrganisationDAO organisationDao;

		@Override
		public void initialize(ValidApiKey constraintAnnotation) {
		}

		@Override
		public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
			return organisationDao.checkApiKey(value);
		}
	}

	String message() default "{API key is not valid}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
