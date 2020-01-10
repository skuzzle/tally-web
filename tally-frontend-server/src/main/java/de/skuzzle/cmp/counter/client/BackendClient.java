package de.skuzzle.cmp.counter.client;

public interface BackendClient {

    boolean isHealthy();

    RestTallyMetaInfoResponse getMetaInfo();

    RestTallySheetsReponse listTallySheets();

    RestTallyResponse createNewTallySheet(String name);

    RestTallyResponse getTallySheet(String publicKey);

    void increment(String adminKey, RestTallyIncrement increment);

    void deleteTallySheet(String adminKey);

    void deleteIncrement(String adminKey, String incrementId);

    void updateIncrement(String adminKey, RestTallyIncrement increment);

    void assignToCurrentUser(String adminKey);

    void changeName(String adminKey, String newTitle);

}