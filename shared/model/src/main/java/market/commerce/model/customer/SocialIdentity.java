package market.commerce.model.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import market.commerce.model.enums.SocialProvider;

import java.io.Serializable;
import java.time.Instant;

/**
 * A sign-in method linked to a customer.
 *
 * <p>Firebase already merges Google, Apple and phone into one uid, so this is a
 * record of what the customer has linked (for account settings and for deciding
 * whether to offer password login) rather than the lookup key.
 *
 * @author Tergel
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialIdentity implements Serializable {

    private SocialProvider provider;

    /** Email or phone number the provider reported, for display only. */
    private String identifier;

    private Instant linkedAt;
}
