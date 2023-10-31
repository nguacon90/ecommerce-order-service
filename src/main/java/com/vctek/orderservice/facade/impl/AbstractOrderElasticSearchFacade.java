package com.vctek.orderservice.facade.impl;

import com.vctek.orderservice.dto.request.OrderSearchRequest;
import com.vctek.orderservice.elasticsearch.query.NestedSearchQuery;
import com.vctek.orderservice.util.DateUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.Date;
import java.util.List;

public class AbstractOrderElasticSearchFacade {
    public static final String MINIMUM_SHOULD_MATCH = "1";
    private static final String WILD_CARD_PATTERN = "*%s*";

    protected void populateSearchDate(BoolQueryBuilder boolQueryBuilder, Date fromDate, Date toDate, String field) {
        if (fromDate != null && toDate != null) {
            boolQueryBuilder.must(QueryBuilders.rangeQuery(field).gte(DateUtil.getDateWithoutTime(fromDate).getTime())
                    .lte(DateUtil.getEndDay(toDate).getTime()));
            return;
        }

        if (fromDate != null) {
            boolQueryBuilder.must(QueryBuilders.rangeQuery(field).gte(DateUtil.getDateWithoutTime(fromDate).getTime()));
            return;
        }

        if (toDate != null) {
            boolQueryBuilder.must(QueryBuilders.rangeQuery(field).lte(DateUtil.getEndDay(toDate).getTime()));
        }
    }

    protected void populateSearchRange(BoolQueryBuilder boolQueryBuilder, Object from, Object to, String field) {
        if (from != null && to != null) {
            boolQueryBuilder.must(QueryBuilders.rangeQuery(field).gte(from).lte(to));
            return;
        }

        if (from != null) {
            boolQueryBuilder.must(QueryBuilders.rangeQuery(field).gte(from));
            return;
        }

        if (to != null) {
            boolQueryBuilder.must(QueryBuilders.rangeQuery(field).lte(to));
        }
    }

    protected void populateSearchNestedObject(BoolQueryBuilder boolQueryBuilder, String searchTerm,
                                              String nestedPath, String idField, String subIdField, String[] fields) {
        BoolQueryBuilder nestedQuery = new BoolQueryBuilder();
        try {
            long id = Long.parseLong(searchTerm);
            nestedQuery.should(QueryBuilders.matchQuery(idField, id));
            nestedQuery.should(QueryBuilders.matchQuery(subIdField, id));
            buildShouldQueries(searchTerm, fields, nestedQuery).minimumShouldMatch(MINIMUM_SHOULD_MATCH);
            boolQueryBuilder.must(QueryBuilders.nestedQuery(nestedPath, nestedQuery, ScoreMode.Avg));
        } catch (NumberFormatException e) {
            buildShouldQueries(searchTerm, fields, nestedQuery).minimumShouldMatch(MINIMUM_SHOULD_MATCH);
            boolQueryBuilder.must(QueryBuilders.nestedQuery(nestedPath, nestedQuery, ScoreMode.Avg));
        }
    }

    protected void populateSearchNestedObject(BoolQueryBuilder boolQueryBuilder, String productSearch,
                                              List<NestedSearchQuery> nestedSearchQuery) {

        BoolQueryBuilder nestedQuery = new BoolQueryBuilder();
        for (NestedSearchQuery query : nestedSearchQuery) {
            try {
                long productId = Long.parseLong(productSearch);
                nestedQuery.should(QueryBuilders.matchQuery(query.getIdQuery(), productId));
                buildShouldQueries(productSearch, query.getOtherFields(), nestedQuery)
                        .minimumShouldMatch(MINIMUM_SHOULD_MATCH);
                boolQueryBuilder.should(QueryBuilders.nestedQuery(query.getNestedPath(), nestedQuery, ScoreMode.Avg));
            } catch (NumberFormatException e) {
                buildShouldQueries(productSearch, query.getOtherFields(), nestedQuery)
                        .minimumShouldMatch(MINIMUM_SHOULD_MATCH);
                boolQueryBuilder.should(QueryBuilders.nestedQuery(query.getNestedPath(), nestedQuery, ScoreMode.Avg));
            }
        }
        boolQueryBuilder.minimumShouldMatch(MINIMUM_SHOULD_MATCH);
    }


    protected BoolQueryBuilder buildShouldQueries(String productSearch, String[] fields, BoolQueryBuilder boolQueryBuilder) {
        if (ArrayUtils.isNotEmpty(fields)) {
            for (String field : fields) {
                boolQueryBuilder.should(QueryBuilders.matchQuery(field, QueryParser.escape(productSearch)));
            }
        }
        return boolQueryBuilder;
    }

    protected NestedQueryBuilder buildMatchNestedQuery(String field, String nestedPath, Object value) {
        BoolQueryBuilder nestedQuery = new BoolQueryBuilder();
        nestedQuery.must(QueryBuilders.matchQuery(field, value));
        return QueryBuilders.nestedQuery(nestedPath, nestedQuery, ScoreMode.Avg);
    }

    protected Long getProductId(OrderSearchRequest request) {
        try {
            return Long.parseLong(request.getProduct());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected void populateSearchLikeNestedObject(BoolQueryBuilder boolQueryBuilder, String searchTerm,
                                              String nestedPath, String idField, String stringfield) {
        BoolQueryBuilder nestedQuery = new BoolQueryBuilder();
        try {
            long id = Long.parseLong(searchTerm);
            nestedQuery.must(QueryBuilders.matchQuery(idField, id));
        } catch (NumberFormatException e) {
            nestedQuery.must(QueryBuilders.wildcardQuery(stringfield, String.format(WILD_CARD_PATTERN, QueryParser.escape(searchTerm).toLowerCase())));

        }
        boolQueryBuilder.must(QueryBuilders.nestedQuery(nestedPath, nestedQuery, ScoreMode.Avg));
    }
}
