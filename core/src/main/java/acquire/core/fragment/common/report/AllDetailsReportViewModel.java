package acquire.core.fragment.common.report;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import acquire.base.utils.LoggerUtils;
import acquire.core.BuildConfig;
import acquire.core.constant.ReportConstant;
import acquire.core.model.FeatureSubMenuModel;
import acquire.core.model.SchemeGroup;
import acquire.core.report_data_factiry.CardDataFactory;
import acquire.database.model.Merchant;
import acquire.database.model.Record;
import acquire.database.repository.MerchantRepository;
import acquire.database.repository.RecordRepository;

public class AllDetailsReportViewModel extends ViewModel {

    private final MutableLiveData<AllDetailsReportUiState> uiState = new MutableLiveData<>();

    public LiveData<AllDetailsReportUiState> getUiState() {
        return uiState;
    }

    public void loadReport(FeatureSubMenuModel menuModel, String titleBarText) {

        Executors.newSingleThreadExecutor().execute(() -> {

            boolean isEmiEnable = true;//<TODO>, it should set from setting

            AllDetailsReportUiState state = new AllDetailsReportUiState();

            state.isPayFlexEnable = isEmiEnable;

            MerchantRepository merchantRepository = new MerchantRepository();
            Merchant merchant = merchantRepository.findByType("default");
            state.merchant = merchant;

            RecordRepository repo = new RecordRepository();
            List<Record> records = new ArrayList<>();
            List<Record> emiRecords = new ArrayList<>();

            String reportCode = menuModel.getCode();
            state.isPayFlexReport = false;


            if (ReportConstant.REPORT_ITEM_CODE_DETAILS_REPORT.equalsIgnoreCase(reportCode)) {
                state.topTitle = "DETAILS REPORT(SALE)";
                state.bottomTitle = "END OF DETAILS REPORT(SALE)";
                records = repo.findSchemeWiseOnOffUs();
                //records = repo.findAll();
            } else if (ReportConstant.REPORT_ITEM_CODE_VOID_REPORT.equalsIgnoreCase(reportCode)) {
                state.topTitle = "DETAILS REPORT(VOID SALE)";
                state.bottomTitle = "END OF VOID-SALE REPORT";
                records = repo.findAllVoidReport();
                // state.schemeGroups = CardDataFactory.getSchemeAllVoidGroups();
            } else if (ReportConstant.REPORT_ITEM_CODE_CARD_REPORT.equalsIgnoreCase(reportCode)) {
                state.topTitle = "DETAILS REPORT(SALE & VOID)";
                state.bottomTitle = "END OF CARD REPORT";
                state.showTranType = false;
                records = repo.findSchemeWiseOnOffUs();
            } else if (ReportConstant.REPORT_ITEM_CODE_PRE_AUTH_REPORT.equalsIgnoreCase(reportCode)) {
                state.topTitle = "PRE-AUTH REPORT";
                state.bottomTitle = "END OF PRE-AUTH REPORT";
                records = repo.findAllPreAuth();
            } else if (ReportConstant.REPORT_ITEM_CODE_RRE_AUTH_VOID_REPORT.equalsIgnoreCase(reportCode)) {
                state.topTitle = "VOID PRE-AUTH REPORT";
                state.bottomTitle = "END OF VOID PRE-AUTH REPORT";
                records = repo.findAllVoidPreAuth();
            } else if (ReportConstant.REPORT_ITEM_CODE_INSTALLMENT_REPORT.equalsIgnoreCase(reportCode)) {
                state.topTitle = ReportConstant.REPORT_TITLE_TOP_PAY_FLEX;
                state.bottomTitle = ReportConstant.REPORT_TITLE_BOTTOM_PAY_FLEX;
                state.isPayFlexReport = true;
                records = repo.findAllPayFlex();
            }

            state.toolbarTitle = titleBarText == null ? "" : titleBarText;

            state.records.clear();
            state.schemeGroups.clear();
            state.records.addAll(records);
            state.schemeGroups.addAll(CardDataFactory.getSchemeGroups(records));

            if (isEmiEnable) {
                //PayFlex
                Merchant emiMerchant = merchantRepository.findByType("EMI");
                state.emiMerchant = emiMerchant;

                if (ReportConstant.REPORT_ITEM_CODE_DETAILS_REPORT.equalsIgnoreCase(reportCode)
                        || ReportConstant.REPORT_ITEM_CODE_INSTALLMENT_REPORT.equalsIgnoreCase(reportCode)
                ) {
                    emiRecords = repo.findEmiSchemeWiseOnOffUs();
                    state.emiRecords.clear();
                    state.emiSchemeGroups.clear();
                    state.emiRecords.addAll(emiRecords);
                    state.emiSchemeGroups.addAll(CardDataFactory.getSchemeGroups(emiRecords));
                }

            }

            uiState.postValue(state);

            LoggerUtils.i("Loaded records: " + (records != null ? records.size() : 0));
        });
    }
}
