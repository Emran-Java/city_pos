package acquire.core.fragment.common.report;

import java.util.ArrayList;
import java.util.List;

import acquire.core.model.SchemeGroup;
import acquire.database.model.Merchant;
import acquire.database.model.Record;

public class AllDetailsReportUiState {
    public Merchant merchant;
    public List<Record> records = new ArrayList<>();
    public List<SchemeGroup> schemeGroups = new ArrayList<>();
    //public List<SchemeGroup> grandTotalGroups;
    public String topTitle;
    public String bottomTitle;
    public String toolbarTitle;
    public boolean showTranType = true, isPayFlexEnable;

    //for PayFlex
    public Merchant emiMerchant;
    public List<Record> emiRecords = new ArrayList<>();
    public List<SchemeGroup> emiSchemeGroups = new ArrayList<>();
    public boolean isPayFlexReport;
}