package acquire.sdk.nonbankcard.contactless;


import com.newland.nsdk.rfic.card.NTAG424DNACard;

import acquire.sdk.nonbankcard.BasicContactlessReader;


/**
 * A NTAG 424DNA contactless card reader.
 * <p>e.g.</p>
 * <pre>
 * </pre>
 *
 * @author Janson
 * @date 2023/10/10 15:51
 * @since 3.8
 */
public class Ntag424Reader extends BasicContactlessReader<NTAG424DNACard> {

    @Override
    protected Class<NTAG424DNACard> getRfCardClass() {
        return NTAG424DNACard.class;
    }

}
