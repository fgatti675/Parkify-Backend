package com.cahue.index;

import com.google.appengine.api.search.*;

import javax.inject.Singleton;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Date: 12.09.14
 *
 * @author francesco
 */
@Singleton
public class IndexManager {

    private final static String SPOTS_INDEX = "spots";

    private final static String INDEX_TIME_FIELD = "time";
    private final static String INDEX_LOCATION_FIELD = "location";

    private final static int MAX_RESULTS = 100;
    private final static int MAX_BATCH_DELETE = 200;

    private IndexSpec indexSpec = IndexSpec.newBuilder().setName(SPOTS_INDEX).build();


    private Index createSpotsIndex() {
        return SearchServiceFactory.getSearchService().getIndex(indexSpec);
    }


    /**
     * Query the index with the specified parameters and get set of ids
     *
     * @param latitude
     * @param longitude
     * @param range
     * @return
     */
    public Set<Long> query(Double latitude, Double longitude, Long range) {
        /**
         * Query index first
         */
        String queryString = String.format("distance(%s, geopoint(%f, %f)) < %s",
                INDEX_LOCATION_FIELD,
                latitude,
                longitude,
                range);

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
        return ids;
    }

    /**
     * Put a new entry in the idnex
     *
     * @param id
     * @param latitude
     * @param longitude
     * @param time
     */
    public void put(Long id, Double latitude, Double longitude, Date time) {

        // Save in Index
        GeoPoint geoPoint = new GeoPoint(latitude, longitude);
        Document doc = Document.newBuilder()
                .setId(id.toString())
                .addField(Field.newBuilder().setName(INDEX_LOCATION_FIELD).setGeoPoint(geoPoint))
                .addField(Field.newBuilder().setName(INDEX_TIME_FIELD).setDate(time))
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

    /**
     * Delete all the entries before 00:00 of the day indicated as a parameter
     *
     * @param date
     */
    public int deleteBefore(Date date) {

        Index index = createSpotsIndex();
        int deleteCount = 0;

        String queryString = String.format("%s < %s",
                INDEX_TIME_FIELD,
                new SimpleDateFormat("yyyy-mm-dd").format(date));

        QueryOptions options = QueryOptions.newBuilder()
                .setLimit(MAX_RESULTS)
                .build();
        Query query = Query.newBuilder().setOptions(options).build(queryString);
        while (true) {
            Results documents = index.search(query);
            if (documents.getResults().isEmpty()) break;
            deleteIndexDocuments(index, documents.getResults());
            deleteCount += documents.getResults().size();
        }
          return  deleteCount;
    }

    public void reset() {

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

    private void deleteIndexDocuments(Index index, Collection<Document> response) {
        List<String> docIds = new ArrayList<>();
        for (Document doc : response) {
            docIds.add(doc.getId());
        }
        index.delete(docIds);
    }
}
