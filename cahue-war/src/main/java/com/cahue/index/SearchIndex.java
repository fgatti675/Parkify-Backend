package com.cahue.index;


import com.cahue.model.transfer.QueryResult;
import com.google.appengine.api.search.*;

import javax.inject.Singleton;
import java.util.*;

/**
 * Date: 12.09.14
 *
 * @author francesco
 */
@Singleton
public class SearchIndex implements SpotsIndex {

    private final static String SPOTS_INDEX = "spots";

    private final static String INDEX_TIME_FIELD = "time";
    private final static String INDEX_EXPIRY_TIME_FIELD = "expiryTime";
    private final static String INDEX_LOCATION_FIELD = "location";

    private final static int MAX_BATCH_DELETE = 200;

    private IndexSpec indexSpec = IndexSpec.newBuilder().setName(SPOTS_INDEX).build();

    private Index createSpotsIndex() {
        return SearchServiceFactory.getSearchService().getIndex(indexSpec);
    }

    /**
     * Delete the entries with the following ids
     *
     * @param docIds
     */
    public void delete(List<String> docIds) {
        Index index = createSpotsIndex();
        while (docIds.size() > MAX_BATCH_DELETE) {
            List<String> idsSubList = docIds.subList(0, MAX_BATCH_DELETE);
            index.delete(idsSubList);
            docIds = docIds.subList(MAX_BATCH_DELETE, docIds.size());
        }

        index.delete(docIds);
    }


    private void deleteIndexDocuments(Index index, Collection<Document> response) {
        List<String> docIds = new ArrayList<>();
        for (Document doc : response) {
            docIds.add(doc.getId());
        }
        index.delete(docIds);
    }

    @Override
    public QueryResult queryNearest(Double latitude, Double longitude, int nearest) {

        QueryResult result = new QueryResult();

        /**
         * Query index first
         */
        String queryString = String.format("distance(%s, geopoint(%f, %f)) < %s",
                INDEX_LOCATION_FIELD,
                latitude,
                longitude,
                2000);

        QueryOptions options = QueryOptions.newBuilder()
                //                .setLimit(MAX_RESULTS)
                .build();

        Index index = createSpotsIndex();

        Query query = Query.newBuilder().setOptions(options).build(queryString);
        Results<ScoredDocument> documents = index.search(query);

        Set<Long> ids = new HashSet<>();
        for (ScoredDocument document : documents) {
            ids.add(Long.parseLong(document.getId()));
        }

        // TODO: this is just an empty result now
        return result;
    }

    @Override
    public QueryResult queryArea(Double southwestLatitude, Double southwestLongitude, Double northeastLatitude, Double northeastLongitude) {
        return null;
    }

    @Override
    public void put(ParkingSpotIndexEntry spot) {
        // Save in Index
        GeoPoint geoPoint = new GeoPoint(spot.getLatitude(), spot.getLongitude());
        Document doc = Document.newBuilder()
                .setId(spot.getId().toString())
                .addField(Field.newBuilder().setName(INDEX_LOCATION_FIELD).setGeoPoint(geoPoint))
                .addField(Field.newBuilder().setName(INDEX_TIME_FIELD).setNumber(spot.getTime().getTime()))
                .addField(Field.newBuilder().setName(INDEX_EXPIRY_TIME_FIELD).setNumber(spot.getExpiryTime().getTime()))
                .build();

        Index index = createSpotsIndex();
        try {
            index.put(doc);
        } catch (PutException e) {
            if (StatusCode.TRANSIENT_ERROR.equals(e.getOperationResult().getCode())) {
                // retry putting the document
            }
        }
    }

    @Override
    public int expireStale() {
        Index index = createSpotsIndex();
        int deleteCount = 0;

        String queryString = String.format("%s < %d",
                INDEX_EXPIRY_TIME_FIELD,
                new Date().getTime());

        QueryOptions options = QueryOptions.newBuilder()
                .setLimit(MAX_BATCH_DELETE)
                .build();
        Query query = Query.newBuilder().setOptions(options).build(queryString);

        while (true) {
            Results documents = index.search(query);
            if (documents.getResults().isEmpty()) break;
            deleteIndexDocuments(index, documents.getResults());
            deleteCount = documents.getResults().size();
        }
        return deleteCount;
    }

    @Override
    public void clear() {
        Index index = createSpotsIndex();

        // looping because getRange by default returns up to 100 documents at a time
        while (true) {
            // Return a set of doc_ids.
            GetRequest request = GetRequest.newBuilder().setReturningIdsOnly(true).build();
            GetResponse<Document> response = index.getRange(request);
            if (response.getResults().isEmpty()) {
                break;
            }
            deleteIndexDocuments(index, response.getResults());
        }
    }
}
