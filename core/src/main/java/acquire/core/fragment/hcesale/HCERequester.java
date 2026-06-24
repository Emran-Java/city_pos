package acquire.core.fragment.hcesale;

import acquire.core.trans.pack.json.npi_sale.HCESaleResp;

/**
 * A request interface for {@link HCEFragment}
 *
 * @author Wendy
 * @date 2023/08/29
 */
public interface HCERequester {

    HCESaleResp createOrder();

    HCESaleResp queryResult();

}
