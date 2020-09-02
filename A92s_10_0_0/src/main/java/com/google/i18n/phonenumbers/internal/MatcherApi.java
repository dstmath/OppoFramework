package com.google.i18n.phonenumbers.internal;

import com.google.i18n.phonenumbers.Phonemetadata;

public interface MatcherApi {
    boolean matchNationalNumber(CharSequence charSequence, Phonemetadata.PhoneNumberDesc phoneNumberDesc, boolean z);
}
