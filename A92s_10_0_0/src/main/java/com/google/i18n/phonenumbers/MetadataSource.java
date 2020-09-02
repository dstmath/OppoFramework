package com.google.i18n.phonenumbers;

import com.google.i18n.phonenumbers.Phonemetadata;

interface MetadataSource {
    Phonemetadata.PhoneMetadata getMetadataForNonGeographicalRegion(int i);

    Phonemetadata.PhoneMetadata getMetadataForRegion(String str);
}
