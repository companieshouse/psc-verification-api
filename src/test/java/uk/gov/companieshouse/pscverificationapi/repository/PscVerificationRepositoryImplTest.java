package uk.gov.companieshouse.pscverificationapi.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;

@ExtendWith(MockitoExtension.class)
class PscVerificationRepositoryImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private PscVerificationRepositoryImpl repository;

    @Test
    void findByNotificationIdReturnsMatchingRecords() {
        String notificationId = "testId";
        PscVerification verification = new PscVerification();
        when(mongoTemplate.find(any(), eq(PscVerification.class))).thenReturn(List.of(verification));

        List<PscVerification> results = repository.findByNotificationId(notificationId);

        assertEquals(1, results.size());
        assertSame(verification, results.getFirst());
        verify(mongoTemplate).find(
            Query.query(Criteria.where("data.psc_notification_id").is(notificationId)),
            PscVerification.class
        );
    }
}
