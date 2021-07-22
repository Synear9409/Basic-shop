package com.synear.service.imp;

import com.synear.service.ItemsESService;
import com.synear.utils.PagedGridResult;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ItemsESServiceImpl implements ItemsESService {

    @Autowired
    private RestHighLevelClient client;

    @Override
    public PagedGridResult searchItems(String keywords, String sort, Integer page, Integer pageSize) {

        String itemNameField = "itemName";
        try {
            SearchRequest searchRequest = new SearchRequest();
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            // 分页
            sourceBuilder.from((page-1) * pageSize);
            sourceBuilder.size(pageSize);


            // 排序搜索
            if (sort.equals("c")) {
                sourceBuilder.sort("sellCounts", SortOrder.DESC);
            } else if (sort.equals("p")) {
                sourceBuilder.sort("price", SortOrder.ASC);
            } else {
                sourceBuilder.sort("itemName.keyword", SortOrder.ASC);
            }

            // 高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("itemName");
            highlightBuilder.preTags("<font style='color:red'>");
            highlightBuilder.postTags("</font>");
            sourceBuilder.highlighter(highlightBuilder);


            // 模糊匹配
            MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery(itemNameField, keywords);
//            TermQueryBuilder queryBuilder = QueryBuilders.termQuery(itemNameField, keywords);
            sourceBuilder.query(queryBuilder);
            sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

            ArrayList<Map<String, Object>> list = new ArrayList<>();
            // 执行搜索
            searchRequest.source(sourceBuilder);
            SearchResponse searchResults = client.search(searchRequest, RequestOptions.DEFAULT);
            for (SearchHit hit : searchResults.getHits().getHits()) {
                // 获取高亮的内容
                HighlightField itemName = hit.getHighlightFields().get(itemNameField);
                // 替换旧数据中该字段的内容
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();

                if (itemName != null) {
                    String HL_itemName = itemName.getFragments()[0].toString();
//                    System.out.println(HL_itemName);
                    sourceAsMap.put(itemNameField, HL_itemName);
                }
                list.add(sourceAsMap);
            }

            // 构建分页对象
            PagedGridResult gridResult = new PagedGridResult();
            long total = searchResults.getHits().getTotalHits().value;
            gridResult.setPage(page  >= total ? (int) total : page);
            gridResult.setRows(list);
            gridResult.setRecords(total);
            gridResult.setTotal(total == 0 ? 0
                    : (int) (total % pageSize == 0 ? total / pageSize : (total / pageSize) + 1));

            return gridResult;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 6.x 版本的写法  7.x 已经没有esTemplate了
    /*@Override
    public PagedGridResult searchItems(String keywords, String sort, Integer page, Integer pageSize) {
        String preTag = "<font color='red'>";
        String postTag = "</font>";

        Pageable pageable = PageRequest.of(page, pageSize);

        String itemNameField = "itemName";


        SearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery(itemNameField, keywords))
                .withHighlightFields(new HighlightBuilder.Field(itemNameField)
//                        .preTags(preTag)
//                        .postTags(postTag)
                )
                .withPageable(pageable)
                .build();

        AggregatedPage<Items> pageItems = esTemplate.queryForPage(query, Items.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {

                List<Items> itemListHighlight = new ArrayList<>();
                SearchHits hits = searchResponse.getHits();
                for (SearchHit h : hits) {
                    HighlightField highlightField = h.getHighlightFields().get(itemNameField);
                    String itemName = highlightField.getFragments()[0].toString();

                    String itemId = (String) h.getSourceAsMap().get("itemId");
                    String imgUrl = (String) h.getSourceAsMap().get("imgUrl");
                    Integer price = (Integer) h.getSourceAsMap().get("price");
                    Integer sellCounts = (Integer) h.getSourceAsMap().get("sellCounts");

                    Items item = new Items();
                    item.setItemName(itemName);
                    item.setItemId(itemId);
                    item.setImgUrl(imgUrl);
                    item.setPrice(price);
                    item.setSellCounts(sellCounts);

                    itemListHighlight.add(item);
                }
                return new AggregatedPageImpl<>((List<T>) itemListHighlight, pageable, searchResponse.getHits().totalHits);
            }
        });

        PagedGridResult gridResult = new PagedGridResult();
        gridResult.setRows(pageItems.getContent());
        gridResult.setRecords(pageItems.getTotalElements());
        gridResult.setTotal(pageItems.getTotalPages());
        gridResult.setPage(page + 1);

        return gridResult;
    }*/
}
