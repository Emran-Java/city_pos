package acquire.core.bean;

import acquire.base.activity.BaseActivity;
import acquire.core.trans.BaseStep;
import acquire.database.model.Record;

/**
 * The parameters of {@link BaseStep}
 *
 * @author Janson
 * @date 2019/9/18 9:00
 */
public class StepBean {
    private final BaseActivity activity;
    private final PubBean pubBean;
    private Record record;
    private Record origRecord;
    public StepBean(BaseActivity activity, PubBean pubBean) {
        this.activity = activity;
        this.pubBean = pubBean;
    }

    public BaseActivity getActivity() {
        return activity;
    }


    public PubBean getPubBean() {
        return pubBean;
    }


    public Record getRecord() {
        return record;
    }

    public Record getOrigRecord() {
        return origRecord;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

    public void setOrigRecord(Record origRecord) {
        this.origRecord = origRecord;
    }
}
