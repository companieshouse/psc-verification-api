package uk.gov.companieshouse.pscverificationapi.service.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Array;
import java.util.EnumSet;
import org.mockito.MockedStatic;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;

public class TestBaseService {

    static final String PASSTHROUGH_HEADER = "passthrough";
    static final String TRANS_ID = "23445657412";
    static final String COMPANY_NUMBER = "12345678";
    static final String FILING_ID = "6332aa6ed28ad2333c3a520a";
    static PscType mockedValue;
    static MockedStatic<PscType> myMockedEnum;
    static Class<PscType> enumPscType = PscType.class;

    //Use to mock out the PscType enum class
    @SuppressWarnings("unchecked")
    static <T extends Enum<T>> T[] addNewEnumValue() {
        final EnumSet<T> enumSet = EnumSet.allOf((Class<T>) enumPscType);
        final T[] newValues = (T[]) Array.newInstance(enumPscType, enumSet.size() + 1);
        int i = 0;
        for (final T value : enumSet) {
            newValues[i] = value;
            i++;
        }

        final T newEnumValue = mock((Class<T>) enumPscType);
        newValues[newValues.length - 1] = newEnumValue;

        when(newEnumValue.ordinal()).thenReturn(newValues.length - 1);

        return newValues;
    }
}
