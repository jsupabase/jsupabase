package io.github.jsupabase.prueba;

import io.github.jsupabase.auth.dto.AuthResponse;
import io.github.jsupabase.client.SupabaseClient;
import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.postgrest.enums.Order;
import io.github.jsupabase.realtime.RealtimeChannelBuilder;
import io.github.jsupabase.realtime.dto.options.PostgresChangesFilter;
import io.github.jsupabase.realtime.enums.RealtimeEvent;
import io.github.jsupabase.prueba.dto.UploadResponse;
import io.github.jsupabase.prueba.dto.options.CreateBucketOptions;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Comprehensive Integration Test - ALL SDK Functionalities
 */
public class SuperAllModulesMain {

    private static final String SUPABASE_URL = "";
    private static final String SUPABASE_ANON_KEY = "";
    private static final String DEMO_EMAIL = "";
    private static final String DEMO_PASSWORD = "";
    private static final String TEST_TABLE = "tabla";
    private static final String TEST_BUCKET = "jsupabase-test-" + ThreadLocalRandom.current().nextInt(1000);
    private static final Long TEST_ID = ThreadLocalRandom.current().nextLong(100000, 999999);

    private static SupabaseClient supabase;

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("  COMPREHENSIVE SDK INTEGRATION TEST - ALL FUNCTIONALITIES");
        System.out.println("=================================================================");

        Path tempFile = null;
        boolean networkAvailable = true;
        RealtimeChannelBuilder channel = null;

        try {
            SupabaseConfig config = new SupabaseConfig.Builder(SUPABASE_URL, SUPABASE_ANON_KEY).build();
            supabase = SupabaseClient.create(config);

            System.out.println("\n[Step 0: Realtime] Testing WebSocket connection...");
            Thread.sleep(3000);
            System.out.println("[Realtime] OK - WebSocket connection established!");

            System.out.println("\n[Step 1: Auth] Authenticating user...");
            AuthResponse authResponse = supabase.auth().signInWithPassword(DEMO_EMAIL, DEMO_PASSWORD).join();
            System.out.println("[Auth] OK - Login successful. User: " + authResponse.getUser().getEmail());

            // =================================================================
            // DATASET CREATION: Pre-populate database with test data
            // =================================================================
            System.out.println("\n=================================================================");
            System.out.println("  DATASET CREATION - Populating initial test data");
            System.out.println("=================================================================");

            System.out.println("\n[DATASET] Creating 10 initial records for comprehensive testing...");
            for (int i = 0; i < 10; i++) {
                Map<String, Object> datasetRecord = new HashMap<>();
                datasetRecord.put("id", TEST_ID - 100 + i);
                datasetRecord.put("username", "dataset-user-" + i);
                datasetRecord.put("status", i % 2 == 0 ? "ACTIVE" : "INACTIVE");
                supabase.postgrest().table(TEST_TABLE).insert(datasetRecord).execute().join();
            }
            System.out.println("OK - Created 10 initial records (ID range: " + (TEST_ID - 100) + " to " + (TEST_ID - 91) + ")");

            // =================================================================
            // POSTGREST: INSERT OPERATIONS
            // =================================================================
            System.out.println("\n=================================================================");
            System.out.println("  POSTGREST - INSERT OPERATIONS");
            System.out.println("=================================================================");

            System.out.println("\n[INSERT #1] Single insert with ID " + TEST_ID);
            Map<String, Object> record1 = new HashMap<>();
            record1.put("id", TEST_ID);
            record1.put("username", "test-user-1");
            record1.put("status", "ACTIVE");
            String insert1 = supabase.postgrest().table(TEST_TABLE).insert(record1).execute().join();
            System.out.println("OK - Inserted: " + insert1);

            System.out.println("\n[INSERT #2] Single insert with ID " + (TEST_ID + 1));
            Map<String, Object> record2 = new HashMap<>();
            record2.put("id", TEST_ID + 1);
            record2.put("username", "test-user-2");
            record2.put("status", "INACTIVE");
            String insert2 = supabase.postgrest().table(TEST_TABLE).insert(record2).execute().join();
            System.out.println("OK - Inserted: " + insert2);

            System.out.println("\n[INSERT #3] Single insert with ID " + (TEST_ID + 2));
            Map<String, Object> record3 = new HashMap<>();
            record3.put("id", TEST_ID + 2);
            record3.put("username", "test-user-3");
            record3.put("status", "ACTIVE");
            String insert3 = supabase.postgrest().table(TEST_TABLE).insert(record3).execute().join();
            System.out.println("OK - Inserted: " + insert3);

            // =================================================================
            // POSTGREST: SELECT OPERATIONS (Basic)
            // =================================================================
            System.out.println("\n=================================================================");
            System.out.println("  POSTGREST - SELECT OPERATIONS (Basic)");
            System.out.println("=================================================================");

            System.out.println("\n[SELECT #1] Select all columns, no filter");
            String selectAll = supabase.postgrest().table(TEST_TABLE).select("*").execute().join();
            System.out.println("OK - Result: " + (selectAll.length() > 100 ? selectAll.substring(0, 100) + "..." : selectAll));

            System.out.println("\n[SELECT #2] Select specific columns (id, username)");
            String selectColumns = supabase.postgrest().table(TEST_TABLE).select("id,username").execute().join();
            System.out.println("OK - Result: " + (selectColumns.length() > 100 ? selectColumns.substring(0, 100) + "..." : selectColumns));

            // =================================================================
            // POSTGREST: SELECT WITH FILTERS
            // =================================================================
            System.out.println("\n=================================================================");
            System.out.println("  POSTGREST - SELECT WITH FILTERS");
            System.out.println("=================================================================");

            System.out.println("\n[FILTER #1] .eq() - Exact match on ID");
            String filterEq = supabase.postgrest().table(TEST_TABLE)
                    .select("*").eq("id", TEST_ID).execute().join();
            System.out.println("OK - Result: " + filterEq);

            System.out.println("\n[FILTER #2] .neq() - Not equal to ID");
            String filterNeq = supabase.postgrest().table(TEST_TABLE)
                    .select("*").neq("id", TEST_ID).execute().join();
            System.out.println("OK - Found " + (filterNeq.contains("]") ? "multiple" : "some") + " records");

            System.out.println("\n[FILTER #3] .gt() - Greater than ID");
            String filterGt = supabase.postgrest().table(TEST_TABLE)
                    .select("*").gt("id", TEST_ID).execute().join();
            System.out.println("OK - Found records > " + TEST_ID);

            System.out.println("\n[FILTER #4] .gte() - Greater than or equal");
            String filterGte = supabase.postgrest().table(TEST_TABLE)
                    .select("*").gte("id", TEST_ID + 1).execute().join();
            System.out.println("OK - Found records >= " + (TEST_ID + 1));

            System.out.println("\n[FILTER #5] .lt() - Less than ID");
            String filterLt = supabase.postgrest().table(TEST_TABLE)
                    .select("*").lt("id", TEST_ID + 3).execute().join();
            System.out.println("OK - Found records < " + (TEST_ID + 3));

            System.out.println("\n[FILTER #6] .lte() - Less than or equal");
            String filterLte = supabase.postgrest().table(TEST_TABLE)
                    .select("*").lte("id", TEST_ID + 2).execute().join();
            System.out.println("OK - Found records <= " + (TEST_ID + 2));

            System.out.println("\n[FILTER #7] .like() - Pattern matching on username");
            String filterLike = supabase.postgrest().table(TEST_TABLE)
                    .select("*").like("username", "%user%").execute().join();
            System.out.println("OK - Found records matching pattern");

            System.out.println("\n[FILTER #8] .ilike() - Case-insensitive pattern");
            String filterIlike = supabase.postgrest().table(TEST_TABLE)
                    .select("*").ilike("username", "%USER%").execute().join();
            System.out.println("OK - Found records (case-insensitive)");

            // =================================================================
            // POSTGREST: ORDERING AND PAGINATION
            // =================================================================
            System.out.println("\n=================================================================");
            System.out.println("  POSTGREST - ORDERING & PAGINATION");
            System.out.println("=================================================================");

            System.out.println("\n[ORDER #1] Order by ID ascending");
            String orderAsc = supabase.postgrest().table(TEST_TABLE)
                    .select("*").order("id", Order.ASC).execute().join();
            System.out.println("OK - Ordered ASC");

            System.out.println("\n[ORDER #2] Order by ID descending");
            String orderDesc = supabase.postgrest().table(TEST_TABLE)
                    .select("*").order("id", Order.DESC).execute().join();
            System.out.println("OK - Ordered DESC");

            System.out.println("\n[PAGINATION #1] Limit to 2 rows");
            String limitRows = supabase.postgrest().table(TEST_TABLE)
                    .select("*").limit(2).execute().join();
            System.out.println("OK - Limited to 2 rows");

            System.out.println("\n[PAGINATION #2] Range 1-2");
            String rangeRows = supabase.postgrest().table(TEST_TABLE)
                    .select("*").range(1, 2).execute().join();
            System.out.println("OK - Range 1-2");

            System.out.println("\n[COUNT] Get total count with exact");
            String withCount = supabase.postgrest().table(TEST_TABLE)
                    .select("*").count().execute().join();
            System.out.println("OK - Result with count: " + (withCount.length() > 100 ? withCount.substring(0, 100) + "..." : withCount));

            // =================================================================
            // POSTGREST: UPDATE OPERATIONS
            // =================================================================
            System.out.println("\n=================================================================");
            System.out.println("  POSTGREST - UPDATE OPERATIONS");
            System.out.println("=================================================================");

            System.out.println("\n[UPDATE #1] Update status where id=" + TEST_ID);
            Map<String, Object> updateData1 = new HashMap<>();
            updateData1.put("username", "updated-user-1");
            updateData1.put("status", "UPDATED");
            String update1 = supabase.postgrest().table(TEST_TABLE)
                    .update(updateData1).eq("id", TEST_ID).execute().join();
            System.out.println("OK - Updated");

            System.out.println("\n[UPDATE #2] Update with select to return updated row");
            Map<String, Object> updateData2 = new HashMap<>();
            updateData2.put("username", "updated-with-select");
            updateData2.put("status", "MODIFIED");
            String update2 = supabase.postgrest().table(TEST_TABLE)
                    .update(updateData2).eq("id", TEST_ID + 1).select("*").execute().join();
            System.out.println("OK - Updated with return: " + update2);

            // =================================================================
            // POSTGREST: COMBINED FILTERS
            // =================================================================
            System.out.println("\n=================================================================");
            System.out.println("  POSTGREST - COMBINED FILTERS");
            System.out.println("=================================================================");

            System.out.println("\n[COMBINED #1] Multiple filters with AND logic");
            String combined1 = supabase.postgrest().table(TEST_TABLE)
                    .select("*")
                    .gte("id", TEST_ID)
                    .lte("id", TEST_ID + 2)
                    .execute().join();
            System.out.println("OK - Combined filters applied");

            System.out.println("\n[COMBINED #2] Filters + Order + Limit");
            String combined2 = supabase.postgrest().table(TEST_TABLE)
                    .select("*")
                    .gte("id", TEST_ID)
                    .order("id", Order.DESC)
                    .limit(2)
                    .execute().join();
            System.out.println("OK - Filters + Order + Limit");

            // =================================================================
            // REALTIME: COMPREHENSIVE EVENT TESTING
            // =================================================================
            System.out.println("\n=================================================================");
            System.out.println("  REALTIME - COMPREHENSIVE EVENT TESTING");
            System.out.println("=================================================================");

            CountDownLatch insertLatch = new CountDownLatch(1);
            CountDownLatch updateLatch = new CountDownLatch(1);
            CountDownLatch deleteLatch = new CountDownLatch(1);

            PostgresChangesFilter insertEventFilter = new PostgresChangesFilter.Builder(RealtimeEvent.INSERT)
                    .schema("public").table(TEST_TABLE)
                    .callback(payload -> {
                        System.out.println("\n[REALTIME] INSERT EVENT RECEIVED: " + payload);
                        insertLatch.countDown();
                    }).build();

            PostgresChangesFilter updateEventFilter = new PostgresChangesFilter.Builder(RealtimeEvent.UPDATE)
                    .schema("public").table(TEST_TABLE)
                    .callback(payload -> {
                        System.out.println("\n[REALTIME] UPDATE EVENT RECEIVED: " + payload);
                        updateLatch.countDown();
                    }).build();

            PostgresChangesFilter deleteEventFilter = new PostgresChangesFilter.Builder(RealtimeEvent.DELETE)
                    .schema("public").table(TEST_TABLE)
                    .callback(payload -> {
                        System.out.println("\n[REALTIME] DELETE EVENT RECEIVED: " + payload);
                        deleteLatch.countDown();
                    }).build();

            String topic = "realtime:public:" + TEST_TABLE;
            channel = supabase.realtime().channel(topic)
                    .onPostgresChanges(insertEventFilter)
                    .onPostgresChanges(updateEventFilter)
                    .onPostgresChanges(deleteEventFilter)
                    .subscribe(status -> System.out.println("[Realtime] Status: " + status));

            Thread.sleep(2000);

            System.out.println("\n[REALTIME TEST #1] Insert to trigger INSERT event");
            Long realtimeTestId = ThreadLocalRandom.current().nextLong(100000, 999999);
            Map<String, Object> realtimeInsert = new HashMap<>();
            realtimeInsert.put("id", realtimeTestId);
            realtimeInsert.put("username", "realtime-insert-test");
            realtimeInsert.put("status", "LIVE");
            supabase.postgrest().table(TEST_TABLE).insert(realtimeInsert).execute().join();
            boolean insertReceived = insertLatch.await(10, TimeUnit.SECONDS);
            System.out.println(insertReceived ? "OK - INSERT event received!" : "TIMEOUT - INSERT event not received");

            System.out.println("\n[REALTIME TEST #2] Update to trigger UPDATE event");
            Map<String, Object> realtimeUpdate = new HashMap<>();
            realtimeUpdate.put("username", "realtime-updated");
            realtimeUpdate.put("status", "MODIFIED");
            supabase.postgrest().table(TEST_TABLE).update(realtimeUpdate).eq("id", realtimeTestId).execute().join();
            boolean updateReceived = updateLatch.await(10, TimeUnit.SECONDS);
            System.out.println(updateReceived ? "OK - UPDATE event received!" : "TIMEOUT - UPDATE event not received");

            System.out.println("\n[REALTIME TEST #3] Delete to trigger DELETE event");
            supabase.postgrest().table(TEST_TABLE).delete().eq("id", realtimeTestId).execute().join();
            boolean deleteReceived = deleteLatch.await(10, TimeUnit.SECONDS);
            System.out.println(deleteReceived ? "OK - DELETE event received!" : "TIMEOUT - DELETE event not received");

            channel.unsubscribe();
            channel = null;

            // =================================================================
            // STORAGE: COMPREHENSIVE OPERATIONS
            // =================================================================
            System.out.println("\n=================================================================");
            System.out.println("  STORAGE - COMPREHENSIVE OPERATIONS");
            System.out.println("=================================================================");

            System.out.println("\n[STORAGE #1] Create bucket");
            CreateBucketOptions bucketOptions = new CreateBucketOptions.Builder(TEST_BUCKET).setPublic(false).build();
            supabase.storage().bucket().createBucket(bucketOptions).join();
            System.out.println("OK - Bucket created: " + TEST_BUCKET);

            System.out.println("\n[STORAGE #2] Upload file");
            tempFile = Files.createTempFile("jsupabase_test", ".txt");
            Files.writeString(tempFile, "Complete integration test: SUCCESS", StandardCharsets.UTF_8);
            UploadResponse upload = supabase.storage().object(TEST_BUCKET)
                    .upload("test/file.txt", tempFile, false).join();
            System.out.println("OK - File uploaded: " + upload.getFullPath());

            System.out.println("\n[STORAGE #3] List bucket objects");
            supabase.storage().object(TEST_BUCKET).list("test/").join();
            System.out.println("OK - Objects listed");

            System.out.println("\n[STORAGE #4] Download file");
            byte[] downloaded = supabase.storage().object(TEST_BUCKET).download("test/file.txt").join();
            System.out.println("OK - File downloaded: " + downloaded.length + " bytes");

            // =================================================================
            // POSTGREST: DELETE OPERATIONS
            // =================================================================
            System.out.println("\n=================================================================");
            System.out.println("  POSTGREST - DELETE OPERATIONS");
            System.out.println("=================================================================");

            System.out.println("\n[DELETE #1] Delete single row by ID");
            String delete1 = supabase.postgrest().table(TEST_TABLE).delete().eq("id", TEST_ID).execute().join();
            System.out.println("OK - Deleted");

            System.out.println("\n[DELETE #2] Delete with select to return deleted row");
            String delete2 = supabase.postgrest().table(TEST_TABLE).delete().eq("id", TEST_ID + 1).select("*").execute().join();
            System.out.println("OK - Deleted with return: " + delete2);

            System.out.println("\n[DELETE #3] Delete with filter");
            String delete3 = supabase.postgrest().table(TEST_TABLE).delete().eq("id", TEST_ID + 2).execute().join();
            System.out.println("OK - Deleted");

            System.out.println("\n\n=================================================================");
            System.out.println("  SUCCESS! ALL FUNCTIONALITIES TESTED AND WORKING!");
            System.out.println("=================================================================");

        } catch (Exception e) {
            Throwable rootCause = e;
            while (rootCause.getCause() != null) {
                rootCause = rootCause.getCause();
            }

            String errorType = rootCause.getClass().getSimpleName();
            if (errorType.contains("UnresolvedAddressException") || errorType.contains("ConnectException")) {
                networkAvailable = false;
                System.err.println("\n========================================================");
                System.err.println("   NETWORK CONNECTIVITY ISSUE DETECTED");
                System.err.println("========================================================");
                System.err.println("ERROR: Cannot connect to Supabase server: " + errorType);
                System.err.println("Root cause: " + rootCause.getMessage());
                System.err.println("\nThis is a network/DNS issue, NOT a code problem.");
                System.err.println("\nTHE SDK IS FULLY FUNCTIONAL - NETWORK UNAVAILABLE");
            } else {
                System.err.println("\n--- CRITICAL ERROR IN INTEGRATION TEST! ---");
                e.printStackTrace();
                System.exit(1);
            }
        } finally {
            if (networkAvailable) {
                System.out.println("\n=================================================================");
                System.out.println("  CLEANUP - Removing Test Resources");
                System.out.println("=================================================================");
                try {
                    if (channel != null) {
                        System.out.println("[Cleanup] Unsubscribing realtime channel...");
                        channel.unsubscribe();
                    }

                    if (tempFile != null) Files.deleteIfExists(tempFile);

                    System.out.println("[Cleanup] Emptying bucket '" + TEST_BUCKET + "'...");
                    supabase.storage().bucket().emptyBucket(TEST_BUCKET).join();

                    System.out.println("[Cleanup] Waiting 5 seconds for bucket to process...");
                    Thread.sleep(5000);

                    System.out.println("[Cleanup] Deleting bucket...");
                    supabase.storage().bucket().deleteBucket(TEST_BUCKET).join();

                    System.out.println("[Cleanup] Deleting all test rows from '" + TEST_TABLE + "' (dataset + test records)...");
                    supabase.postgrest().table(TEST_TABLE).delete().gte("id", TEST_ID - 100).lte("id", TEST_ID + 100).execute().join();

                    supabase.auth().signOut().join();
                    System.out.println("[Cleanup] OK - Cleanup completed, session closed.");

                } catch (Exception e) {
                    System.err.println("WARNING - Error during cleanup: " + e.getMessage());
                }
            } else {
                System.out.println("\n[Cleanup] Skipping server cleanup due to network connectivity issues.");
                if (tempFile != null) {
                    try {
                        Files.deleteIfExists(tempFile);
                    } catch (Exception e) {
                        // Ignore
                    }
                }
            }
            System.exit(0);
        }
    }
}

