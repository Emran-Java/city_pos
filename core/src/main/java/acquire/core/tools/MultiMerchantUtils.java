package acquire.core.tools;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import acquire.base.utils.LoggerUtils;
import acquire.core.bean.PubBean;
import acquire.core.constant.TransType;
import acquire.database.model.Merchant;
import acquire.database.repository.MerchantRepository;

/**
 * @author Janson
 * @date 2024/3/11 11:25
 */
public class MultiMerchantUtils {
    public static Merchant getMerchant(PubBean pubBean) {
        MerchantRepository merchantRepository = new MerchantRepository();
        Merchant merchant;
        if (!TextUtils.isEmpty(pubBean.getMid()) && !TextUtils.isEmpty(pubBean.getTid())) {
            merchant = merchantRepository.findByMidTid(pubBean.getMid(), pubBean.getTid());
        } else {
            merchant = merchantRepository.findByType(pubBean.getCardScheme());
        }

        if (pubBean.getTransType().equalsIgnoreCase(TransType.TRANS_INSTALLMENT)) {
            List<Merchant> merchants = merchantRepository.findEmiMerchant();
            if (!merchants.isEmpty()) {
                merchant = merchants.get(0);
                LoggerUtils.d("newCall Import params->*** EMI Merchant ***: " + merchant.toString());
            } else {
                LoggerUtils.d("newCall Import params->*** No EMI Merchant ***");
            }
        }

        if (merchant == null) {
            //default merchant
            List<Merchant> merchants = merchantRepository.findAll();
            if (!merchants.isEmpty())
                merchant = merchants.get(0);
            else {
                LoggerUtils.d("Import params->*** No Merchant ***");
            }
        } else {
        }

        return merchant;
    }
} 
