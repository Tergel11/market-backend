package market.commerce.service.system;

import lombok.RequiredArgsConstructor;
import market.commerce.constants.ConstantValues;
import market.commerce.model.Sequence;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Year;

/**
 * Generates order numbers from an atomic Mongo counter, so two concurrent
 * checkouts can never produce the same number.
 *
 * @author Tergel
 */
@Service
@RequiredArgsConstructor
public class SequenceService {

    private final MongoTemplate mongoTemplate;

    public String nextOrderNumber() {
        String prefix = ConstantValues.ORDER_NUMBER_PREFIX
                + String.valueOf(Year.now().getValue()).substring(2);
        return prefix + String.format("%08d", next(prefix));
    }

    /**
     * findAndModify with upsert — atomic on the server, no read-then-write race.
     */
    public long next(String prefix) {
        Sequence sequence = mongoTemplate.findAndModify(
                Query.query(Criteria.where(Sequence.Fields.prefix).is(prefix)),
                new Update().inc(Sequence.Fields.value, 1),
                FindAndModifyOptions.options().returnNew(true).upsert(true),
                Sequence.class);

        return sequence == null || sequence.getValue() == null ? 1L : sequence.getValue();
    }
}
