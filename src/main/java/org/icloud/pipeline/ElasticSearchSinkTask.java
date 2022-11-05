package org.icloud.pipeline;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.icloud.pipeline.config.ElasticSearchSinkConnectorConfig;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static org.icloud.pipeline.config.ElasticSearchSinkConnectorConfig.*;

@Slf4j
public class ElasticSearchSinkTask extends SinkTask {

    private ElasticSearchSinkConnectorConfig config;
    private RestHighLevelClient elasticClient;

    @Override
    public String version() {
        return "1.0";
    }


    @Override
    public void start(Map<String, String> props) {
        try {
            config = new ElasticSearchSinkConnectorConfig(props);
        } catch (ConfigException e) {
            throw new ConnectException(e.getMessage(), e);
        }

        elasticClient = new RestHighLevelClient(
                RestClient.builder(new HttpHost(
                        config.getString(ES_CLUSTER_HOST),
                        config.getInt(ES_CLUSTER_PORT)
                ))
        );
    }

    @Override
    public void put(Collection<SinkRecord> records) {
        if (records.size() == 0) return;
        var bulkRequest = new BulkRequest();
        records.stream()
                .peek(record -> log.info("record: {}", record.value()))
                .map(record -> new Gson().fromJson(record.value().toString(), Map.class))
                .map(map -> new IndexRequest(config.getString(ES_INDEX)).source(map, XContentType.JSON))
                .forEach(bulkRequest::add);
        elasticClient.bulkAsync(bulkRequest, RequestOptions.DEFAULT, new MyActionListener());
    }

    @Override
    public void flush(Map<TopicPartition, OffsetAndMetadata> currentOffsets) {
        log.info("flush");
    }

    @Override
    public void stop() {
        try {
            elasticClient.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
