package org.icloud.pipeline;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkResponse;

@Slf4j
public class MyActionListener implements ActionListener<BulkResponse> {

    @Override
    public void onResponse(BulkResponse bulkResponse) {
        if (bulkResponse.hasFailures()) {
            log.error(bulkResponse.buildFailureMessage());
        } else {
            log.info("bulk save success");
        }
    }

    @Override
    public void onFailure(Exception e) {
        log.error(e.getMessage(), e);
    }
}
