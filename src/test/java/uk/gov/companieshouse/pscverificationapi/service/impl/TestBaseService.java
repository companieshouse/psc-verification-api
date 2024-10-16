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
    static <PscType extends Enum<PscType>> PscType[] addNewEnumValue() {
        final EnumSet<PscType> enumSet = EnumSet.allOf((Class<PscType>) enumPscType);
        final PscType[] newValues = (PscType[]) Array.newInstance(enumPscType, enumSet.size() + 1);
        int i = 0;
        for (final PscType value : enumSet) {
            newValues[i] = value;
            i++;
        }

        final PscType newEnumValue = mock((Class<PscType>) enumPscType);
        newValues[newValues.length - 1] = newEnumValue;

        when(newEnumValue.ordinal()).thenReturn(newValues.length - 1);

        return newValues;
    }
}
