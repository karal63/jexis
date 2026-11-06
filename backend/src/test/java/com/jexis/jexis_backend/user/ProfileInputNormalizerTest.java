package com.jexis.jexis_backend.user;
import com.jexis.jexis_backend.user.domain.*;
import com.jexis.jexis_backend.user.infrastructure.UserWriteTransactions;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
class ProfileInputNormalizerTest {
 @Test void canonicalizesEquivalentNumbersAndAbsence() {
  for (String value : new String[]{"+48 512 345 678", "+48 (512) 345-678", "+48512345678"}) assertThat(ProfileInputNormalizer.phone(value)).isEqualTo("+48512345678");
  assertThat(ProfileInputNormalizer.phone(null)).isNull();
  assertThat(ProfileInputNormalizer.phone("  ")).isNull();
 }
 @Test void rejectsInvalidNumbersWithoutGuessingACountry() {
  for (String value : new String[]{"512345678", "---", "+48", "+48 512 345 678 ext 2", "+" + "1".repeat(64)}) assertThatThrownBy(() -> ProfileInputNormalizer.phone(value)).isInstanceOf(ProfileValidationException.class);
 }
 @Test void trimsAndValidatesNames() {
  assertThat(ProfileInputNormalizer.name(" Alex ", "firstName")).isEqualTo("Alex");
  for (String value : new String[]{"  ", "x".repeat(101)}) assertThatThrownBy(() -> ProfileInputNormalizer.name(value, "firstName")).isInstanceOf(ProfileValidationException.class);
 }
 @Test void translatesOnlyThePhoneConstraintIncludingCommitWrappers() {
  var sql = new java.sql.SQLException("duplicate", "23505");
  var phone = new org.hibernate.exception.ConstraintViolationException("duplicate", sql, UserWriteTransactions.PHONE_CONSTRAINT);
  assertThat(UserWriteTransactions.translate(new org.springframework.transaction.TransactionSystemException("commit", phone))).isInstanceOf(com.jexis.jexis_backend.user.domain.exceptions.PhoneNumberExistsException.class);
  var unrelated = new org.hibernate.exception.ConstraintViolationException("duplicate", sql, "users_email_key");
  assertThat(UserWriteTransactions.translate(unrelated)).isSameAs(unrelated);
 }
}
